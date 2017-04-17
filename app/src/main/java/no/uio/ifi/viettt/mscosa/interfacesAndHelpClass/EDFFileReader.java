package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.AnnotationAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAnnotationAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFAnnotation;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeader;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeaderParser;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Annotation;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Physician;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.RecordAnnotation;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

import static no.uio.ifi.viettt.mscosa.MainFragments.SourceFromFileFragment.FILE_IS_LOADED;
import static no.uio.ifi.viettt.mscosa.MainFragments.SourceFromFileFragment.FILE_IS_LOADING;

/**
 * Created by viettt on 11/03/2017.
 */

public class EDFFileReader extends Thread{
    //Assume overhead is 40bytes for each sample.
    //then, long + long + float = 20
    //so, it is about 60 bytes/ sample. 100000 samples is about 600KB
    private final int SAMPLE_BUFF_SIZE_IN_SAMPLE_NUMBER = 50000;
    private MainActivity mMainActivity;
    private File_Sensor_Source file_source;
    private List<LogReadFile> logReadFiles;
    private Handler progressHandler;

    private SensorSource sensorSource;
    private Patient patient;
    private Physician physician;
    private Clinic clinic;
    private Channel[] channels;
    private HashMap<String,Record> records = new HashMap<>();
    private SimpleDateFormat sdf;
    private ArrayList<Annotation> annotationsList = new ArrayList<>();

    private long sqlUsageTime = 0, totalInsertion =0;

    public EDFFileReader(String name , File_Sensor_Source file_source, List<LogReadFile> logReadFiles, MainActivity mMainActivity, Handler progressHandler){
        super(name) ;
        this.file_source = file_source ;
        this.logReadFiles = logReadFiles;
        this.mMainActivity = mMainActivity;
        this.progressHandler = progressHandler;
        sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom
    }

    @Override
    public void run(){
        final String filePath = file_source.getFilePath();
        try {

            File path = new File(Environment.getExternalStorageDirectory().getPath()+"/Download/");
            if(!path.exists()) path.mkdir();
            path.setReadable(true);
            path.setWritable(true);
            String fileName = path.getPath()+"/"+"UsageTimeA01Seconds.txt";

            EDFHeader header = null;
            InputStream is = new BufferedInputStream(new FileInputStream(new File(filePath)));
            //Parse Header to create new sensor source
            header = EDFHeaderParser.parseHeader(is);
            is.close();
            if (header == null) {
                sendMessageToHandler(FILE_IS_LOADED, file_source.getIndex());
                return;
            }
            createAndStoreSensorSource(header);
            createAndSavePatientPhysicianClinic(header);
            createAndSaveRecord(header);
            createAndStoreChannels(header);

            saveRecordFragmentAndSample(header);

            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            pw.println("total used time: "+sqlUsageTime+" ms. Total insertion: "+totalInsertion);
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        sendMessageToHandler(FILE_IS_LOADED, file_source.getIndex());
    }

    private void saveRecordFragmentAndSample(EDFHeader edfHeader) throws Exception{
        RandomAccessFile randomAccessFile = new RandomAccessFile(file_source.getFilePath(),"r");
        FileChannel fileChannel =randomAccessFile.getChannel();
        fileChannel.position(edfHeader.getBytesInHeader());

        long totalBytesRead = edfHeader.getBytesInHeader();
        final long fileLength = randomAccessFile.length();

        int cnt = 0;

        int prevProgressBar = 0;
        ArrayList<Sample> samplesBuffer = new ArrayList<>();
        //For each DataRecord
        int datarecordCnt = 0;
        long bufftimer = System.currentTimeMillis();
        System.out.println("BUFF FOR DB "+cnt);
        while (totalBytesRead < fileLength) {
            for(int i = 0; i < channels.length; i++){
                Channel currentChannel = channels[i];
                //each sample is 2 bytes
                ByteBuffer buffer = ByteBuffer.allocate(currentChannel.getMaxSamplesPerDataRecord()*2);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                totalBytesRead += fileChannel.read(buffer);
                buffer.flip();

                //=============== BUFFERING UP TO MAX BUFFER =====================
                if(currentChannel.getCh_name().toLowerCase().equals("edf annotations")){
                    //annotation parsing
                    createAndPutAnnotationToBuffer(samplesBuffer,buffer,currentChannel,datarecordCnt);
                }else{
                    //just normal samples
                    createAndPutSamplesToBuffer(samplesBuffer,buffer,currentChannel,datarecordCnt);
                }
                //===================================================================
            }

            //============== HOW MANY DATA RECORD CAN WE BUFF BEFORE SEND TO DB - EXPLAIN ABOVE ===============
            //store record fragment (a data record) when BUFF is full.
            if(samplesBuffer.size() >= SAMPLE_BUFF_SIZE_IN_SAMPLE_NUMBER){
                System.out.println("DONE BUFF "+(System.currentTimeMillis() - bufftimer)+" milliseconds");

                System.out.println("---------> CONNECT TO DB "+cnt++);
                long timmer = System.currentTimeMillis();
                //PUSH SAMPLES TO DB by using transactions
                totalInsertion += samplesBuffer.size();
                SampleAdapter sampleAdapter = new SampleAdapter(mMainActivity);
                sampleAdapter.saveSampleToDB(samplesBuffer);
                sampleAdapter.close();
                sqlUsageTime += (System.currentTimeMillis() - timmer);
                System.out.println("---------> DONE DB JOB "+(System.currentTimeMillis() - timmer)+" milliseconds");
                samplesBuffer = new ArrayList<>();
                bufftimer = System.currentTimeMillis();
                System.out.println("BUFF FOR DB "+cnt);
            }
            //========================== END STORING =========================================


            //update progress bar
            file_source.setProgress((int)(((float)totalBytesRead/(float)fileLength)*100));
            if(file_source.getProgress() != 0 && prevProgressBar < file_source.getProgress()){
                //System.out.println(currentLog.getNr_of_bytes_have_read()+" / "+randomAccessFile.length());
                sendMessageToHandler(FILE_IS_LOADING,file_source.getIndex());
            }
            prevProgressBar = file_source.getProgress();

            //IF USER CLICK STOP
            if(!file_source.isRunning() ) {
                synchronized (file_source.lock) {
                    randomAccessFile.close();
                    return;
                }
            }
        }
        randomAccessFile.close();

        //If there is still data in the buffers
        //PUSH SAMPLES TO DB if any by using transactions
        if(samplesBuffer.size() > 0){
            SampleAdapter sampleAdapter = new SampleAdapter(mMainActivity);
            long timmer = System.currentTimeMillis();
            sampleAdapter.saveSampleToDB(samplesBuffer);
            sampleAdapter.close();
            sqlUsageTime += (System.currentTimeMillis() - timmer);
        }

        //Store annotations to DB if any
        if(!annotationsList.isEmpty()){
            RecordAnnotationAdapter recordAnnotationAdapter = new RecordAnnotationAdapter(mMainActivity);
            for(Annotation a : annotationsList){
                for(Record r: records.values()){
                    RecordAnnotation recordAnnotation = new RecordAnnotation();
                    recordAnnotation.setAnn_id(a.getAnn_id());
                    recordAnnotation.setRecord_id(r.getR_id());
                    long timmer = System.currentTimeMillis();
                    recordAnnotationAdapter.saveAnnotationToDB(recordAnnotation);
                    sqlUsageTime += (System.currentTimeMillis() - timmer);
                    totalInsertion++;
                }
            }
            recordAnnotationAdapter.close();
        }

    }

    private void createAndPutSamplesToBuffer(ArrayList<Sample> samplesBuffer, ByteBuffer buffer, Channel currentChannel, int fragmentInx){
        //fragmentInx;
        long timeStamp = currentChannel.getTimeStampReading();
        long period = (long)currentChannel.getPeriod();
        if(currentChannel.getPrefiltering() != null && currentChannel.getPrefiltering().length() >=6
                && currentChannel.getPrefiltering().substring(0,6).toLowerCase().equals("sign*ln")){
            //DAC convert
            for(int i = 0; i < currentChannel.getMaxSamplesPerDataRecord(); i++){
                short value = buffer.getShort();
                Sample s = new Sample(records.get(currentChannel.getCh_nr()).getR_id(),timeStamp,value);
                samplesBuffer.add(s);
                timeStamp = timeStamp+period;
            }
        }else {
            //no need to convert
            for(int i = 0; i < currentChannel.getMaxSamplesPerDataRecord(); i++){
                short value = buffer.getShort();
                Sample s = new Sample(records.get(currentChannel.getCh_nr()).getR_id(),timeStamp,value);
                samplesBuffer.add(s);
                timeStamp = timeStamp+period;
            }
        }
        currentChannel.setTimeStampReading(timeStamp);
    }

    private void createAndPutAnnotationToBuffer(ArrayList<Sample> samplesBuffer, ByteBuffer buffer, Channel currentChannel, int fragmentInx){
        //System.out.println("Annotations");
        //byte[] tmpp = buffer.array();
        //for(int k = 0; k < tmpp.length; k++){
          //  if(tmpp[k] == 20) System.out.print("20");
        // else if (tmpp[k] == 21) System.out.print("21");
            //else if(tmpp[k] == 0) System.out.print("0");
          //  else if(Character.isLetterOrDigit((char)tmpp[k])) System.out.print((char)tmpp[k]);
            //else System.out.print((int)tmpp[k]);
        //}
        int buffLength = buffer.array().length;
        ArrayList<EDFAnnotation> annotations = new ArrayList<>();
        for (int i = 0; i < buffLength - 1;){
            //new TALs
            EDFAnnotation ann = new EDFAnnotation();
            byte[] charInBuff = new byte[buffLength];
            int j;
            for(j = i; j < buffLength - 1; j++){
                //A time stamp with Duration

                if(buffer.get(j) == 21){
                    if(j>i){
                        byte onSetValueString[] = new byte[j-i];
                        buffer.position(i);
                        buffer.get(onSetValueString);
                        double onSetValue = Double.parseDouble(new String(onSetValueString));
                        //skip 21
                        j++;

                        int durationInx = j;
                        while (buffer.get(durationInx) != 20 && durationInx < buffLength) durationInx++;
                        if(durationInx>j){
                            byte durationValueString[] = new byte[durationInx - j];
                            buffer.position(j);
                            buffer.get(durationValueString);
                            double durationValue = Double.parseDouble(new String(durationValueString));
                            ann.setDuration(durationValue);
                            j+=durationInx;
                        }
                        ann.setOnSet(onSetValue);
                    }

                } else if(buffer.get(j) == 20){ // time stamp without Duration
                    //if timekeeping
                    if(buffer.get(j+1) == 20){
                        ann.setTimekeeping(true);
                        j++;
                    }else{
                        byte onSetValueString[] = new byte[j-i];
                        buffer.position(i);
                        buffer.get(onSetValueString);
                        double onSetValue = Double.parseDouble(new String(onSetValueString));
                        ann.setOnSet(onSetValue);
                        //skip 20
                        j++;

                        //collecting annotation string
                        while(buffer.get(j) != 0){
                            int annInx = j;
                            byte annBytes[];
                            while(buffer.get(annInx) != 20 && annInx < buffLength) annInx++;
                            if(annInx>j){
                                annBytes = new byte[annInx-j];
                                buffer.position(j);
                                buffer.get(annBytes);
                                ann.addAnnotation(new String(annBytes));
                                j+= annInx;
                            }

                        }

                    }
                }else if(buffer.get(j) == 0){
                    //Has collected a TALs
                    j++;
                    break;
                }
            }

            i = j;
            if(ann.getAnnotationsList().size()!=0) {
                ann.setTimestampRecord(records.get(currentChannel.getCh_nr()).getTimestamp()+1000*((long)ann.getOnSet()));
                annotations.add(ann);
            }
        }
        AnnotationAdapter annotationAdapter = new AnnotationAdapter(mMainActivity);
        for(EDFAnnotation edfAnnotation: annotations){
            for(String ann : edfAnnotation.getAnnotationsList()){
                Annotation annotation = new Annotation();
                annotation.setOnset(edfAnnotation.getOnSet());
                annotation.setDuration(edfAnnotation.getDuration());
                annotation.setTimeKeeping(edfAnnotation.getTimestampRecord());
                annotation.setAnn(ann);
                annotation.setAnn_id(annotationAdapter.saveAnnotationToDB(annotation));
                annotationsList.add(annotation);
            }
        }
        annotationAdapter.close();
    }

    private void createAndStoreSensorSource(EDFHeader header){
        String filePath = file_source.getFilePath();
        String fileName = filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.'));

        sensorSource = new SensorSource();
        sensorSource.setS_id(fileName);
        sensorSource.setS_name(fileName);
        String stype = "EDF";
        if(header.getReservedFormat().contains("EDF+C")) stype = "EDF+C";
        else if(header.getReservedFormat().contains("EDF+D")) stype = "EDF+D";
        sensorSource.setS_type(stype);

        // Store source to database
        SensorSourceAdapter sensorSourceAdapter = new SensorSourceAdapter(mMainActivity);
        long startTimer = System.currentTimeMillis();
        sensorSourceAdapter.saveSensorSourceToDB(sensorSource);
        sqlUsageTime += (System.currentTimeMillis() - startTimer);
        totalInsertion++;
        sensorSourceAdapter.close();
    }

    private void createAndSavePatientPhysicianClinic(EDFHeader header){
        patient = new Patient();
        physician = new Physician();
        clinic = new Clinic();

        //Fill clinic info
        clinic.setCl_id(sensorSource.getS_type());

        //Fill patient info
        String patient_line[] = header.getPatientInfo().split(" ");
        if(patient_line[0].toLowerCase().equals("x")){
            patient.setP_id(sensorSource.getS_name());
        }else{
            patient.setP_id(patient_line[0]);
        }
        patient.setClinic_code(clinic.getCl_id());
        patient.setPatient_id_in_clinic(patient.getP_id());

        if(patient_line.length > 1 && !patient_line[1].toLowerCase().equals("x")) {
            patient.setGender(patient_line[1]);
        }

        if(patient_line.length > 2 && !patient_line[2].toLowerCase().equals("x")) {
            patient.setDayOfBirth(patient_line[2]);
        }

        if(patient_line.length > 3 && !patient_line[3].toLowerCase().equals("x")) {
            patient.setName(patient_line[3]);
        }

        //Fill physician info
        String phy_line[] = header.getClinicInfo().split(" ");
        System.out.println(header.getClinicInfo());
        if(phy_line.length > 3 && phy_line[3].toLowerCase().equals("x")){
            physician.setP_id(sensorSource.getS_name());
        }else if (phy_line.length > 3){
            physician.setP_id(phy_line[3]+phy_line[1]);
        } else{
            physician.setP_id(phy_line[1]);
        }
        physician.setClinic_code(clinic.getCl_id());
        physician.setEmployee_id(physician.getP_id());
        physician.setTitle(phy_line[2]);


        PersonAdapter personAdapter = new PersonAdapter(mMainActivity);
        long startTimer = System.currentTimeMillis();
        personAdapter.storeNewPerson(patient);
        personAdapter.storeNewPerson(physician);
        personAdapter.close();
        ClinicAdapter clinicAdapter = new ClinicAdapter(mMainActivity);
        clinicAdapter.storeNewClinic(clinic);
        sqlUsageTime += (System.currentTimeMillis() - startTimer);
        totalInsertion+=3;
        clinicAdapter.close();
    }

    private void createAndSaveRecord(EDFHeader header){
        RecordAdapter recordAdapter = new RecordAdapter(mMainActivity);
        for(int i = 0; i < header.getNumberOfChannels(); i++){
            Record record = new Record();
            record.setS_id(sensorSource.getS_id());
            record.setCh_nr(i);
            record.setPatient_id(patient.getP_id());
            record.setPhysician_id(physician.getP_id());
            if(header.getClinicInfo().split(" ").length > 4) record.setUsed_equip(header.getClinicInfo().split(" ")[4]);

            String startDate[] = header.getStartDate().split("\\.");
            String startTime[] = header.getStartTime().split("\\.");
            int year = Integer.parseInt(startDate[2]);
            if(year >= 85 && year <= 99) year = 1900+year;
            else year = 2000+year;
            long timeStamp = 0;

            String timeStampUnix = String.valueOf(year)+"-"+startDate[1]+"-"+startDate[0]+" "+startTime[0]+":"+startTime[1]+":"+startTime[2];
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = format.parse(timeStampUnix);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date != null) timeStamp = date.getTime();

            record.setTimestamp(timeStamp);//ns
            record.setFrequency(header.getNumberOfSamples()[i]/(float)header.getDurationOfRecords());
            long startTimer = System.currentTimeMillis();
            record.setR_id(recordAdapter.saveRecordToDB(record));
            sqlUsageTime += (System.currentTimeMillis() - startTimer);
            totalInsertion++;
            records.put(String.valueOf(i),record);
        }
        recordAdapter.close();

    }

    private void createAndStoreChannels(EDFHeader header){
        channels = new Channel[header.getNumberOfChannels()];
        //looping through channels
        String channelsLabels[] = header.getChannelLabels();
        String transducer[] = header.getTransducerTypes();
        String dimensions[] = header.getDimensions();
        Double physicalMin[] = header.getMinInUnits();
        Double physicalMax[] = header.getMaxInUnits();
        Integer digitalMin[] = header.getDigitalMin();
        Integer digitalMax[] = header.getDigitalMax();
        String prefiltering[] = header.getPrefilterings();
        Integer nrOfsample[] = header.getNumberOfSamples();
        byte reserved[][] = header.getReserveds();

        //PATTERN FOR GET THE FREQUENCY AND DIMENSION
        ChannelAdapter channelAdapter = new ChannelAdapter(mMainActivity);
        for (int i = 0; i < channels.length; i++) {
            String channel_nr = String.valueOf(i);
            String channel_name = channelsLabels[i].trim();
            String channel_transducer_type = transducer[i].trim();
            String channel_dimension = dimensions[i].trim();
            double channel_physical_min = physicalMin[i];
            double channel_physical_max = physicalMax[i];
            int channel_digital_min = digitalMin[i];
            int channel_digital_max = digitalMax[i];
            String channel_prefiltering = prefiltering[i].trim();
            int channel_max_sample_per_dataRecord = nrOfsample[i];
            byte[] channel_reserved = reserved[i];

            channels[i] = new Channel();
            channels[i].setS_id(sensorSource.getS_id());
            channels[i].setCh_nr(channel_nr);
            channels[i].setCh_name(channel_name);
            channels[i].setTransducer(channel_transducer_type);
            channels[i].setDimension(channel_dimension);
            channels[i].setPhy_min(channel_physical_min);
            channels[i].setPhy_max(channel_physical_max);
            channels[i].setDig_min(channel_digital_min);
            channels[i].setDig_max(channel_digital_max);
            channels[i].setPrefiltering(channel_prefiltering);
            channels[i].setMaxSamplesPerDataRecord(channel_max_sample_per_dataRecord);
            channels[i].setEdf_reserved(channel_reserved);
            channels[i].setFrequency(channel_max_sample_per_dataRecord/(float)header.getDurationOfRecords());
            channels[i].setPeriod(1000*((float)1.0 /channels[i].getFrequency()));//ms
            channels[i].setTimeStampReading(records.get(String.valueOf(i)).getTimestamp());

            if(!channels[i].getCh_name().toLowerCase().equals("edf annotations")){
                long startTimer = System.currentTimeMillis();
                channelAdapter.saveChannelToDB(channels[i]);
                sqlUsageTime += (System.currentTimeMillis() - startTimer);
                totalInsertion++;
            }

        }
        channelAdapter.close();

    }

    private void sendMessageToHandler(int what,int arg1){
        Message message = progressHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

}
