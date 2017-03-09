package no.uio.ifi.viettt.mscosa.MainFragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeaderParser;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.OpenSourceFileChooserFromDelaroy.FileUtils;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeader;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.File_Sensor_Source;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.LogReadFile;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ProgressBarHandler;

import static android.app.Activity.RESULT_OK;

/**
 * Created by viettt on 18/12/2016.
 */

public class SourceFromFileFragment extends Fragment {

    /*

    MainActivity mMainActivity;

    //For progressBar management
    static public final int ADD_FILE_SOURCE = 0xCAFE1, FILE_IS_LOADING = 0xCAFE2, FILE_IS_LOADED = 0xCAFE3, START_STOP_LOADING = 0xCAFE4;
    static public final long REFRESH_INTERVAL = 100;
    public boolean isScrolling = false ;
    public long lastUpdate = 0;
    public LinkedHashMap<Integer, File_Sensor_Source> loading_File_data;
    LinkedHashMap<Integer, File_Sensor_Source> in_coming_File_data = new LinkedHashMap<>();
    public List<Integer> done_list = new ArrayList<>();

    private static final int REQUEST_CODE = 6384;
    public final Object lock = new Object();
    int source_ID = 0;

    //For GUI
    View v;

    ListView listview;
    Button btn_addNewFile, btn_chooseFile;
    TextView lblFilePath;

    SourceListAdapter adapter;
    LayoutInflater m_inflater;

    //helper object
    Handler progressHandler;
    List<LogReadFile> logReadFiles = new ArrayList<>();

    public SourceFromFileFragment(){
        loading_File_data  = new LinkedHashMap<>();
        progressHandler = new ProgressBarHandler(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.source_from_file_fragment_layout, container, false);

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);//PERMISSION_REQUEST_CODE
        }

        listview = (ListView) v.findViewById(R.id.listFile_sources) ;
        adapter = new SourceListAdapter();
        listview.setAdapter(adapter);
        listview.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        isScrolling = false;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        isScrolling = true;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        isScrolling = true;
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        }) ;


        btn_addNewFile  = (Button)v.findViewById(R.id.btn_addNewFile);
        btn_chooseFile = (Button)v.findViewById(R.id.btn_chooseFile);
        lblFilePath = (TextView) v.findViewById(R.id.lblFilePath);
        btn_chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the GET_CONTENT intent from the utility class
                Intent target = FileUtils.createGetContentIntent();
                // Create the chooser Intent
                Intent intent = Intent.createChooser(
                        target, getString(R.string.chooser_title));
                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
            }
        });

        btn_addNewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filePath = lblFilePath.getText().toString();
                btn_addNewFile.setEnabled(false);
                File_Sensor_Source file_info = new File_Sensor_Source();
                file_info.setTitle(filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.')));
                file_info.setProgress(0);
                file_info.setId(source_ID);
                file_info.setIndex(source_ID);
                file_info.setFilePath(filePath);
                synchronized (lock) {
                    in_coming_File_data.put(source_ID,file_info);
                }
                sendMessageToHandler(ADD_FILE_SOURCE,source_ID);
            }
        });

        m_inflater = LayoutInflater.from(getContext());
        return v;
    }

    public void setPointerToCurUI(MainActivity mMainActivity){
        this.mMainActivity = mMainActivity;
    }

    void sendMessageToHandler(int what,int arg1){
        Message message = progressHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    public void updateListView(){
        synchronized (lock) {
            //Transfer to current_connected_list
            //Then just remove from the
            for (Integer done_id : done_list){
                File_Sensor_Source s = loading_File_data.remove(done_id);
                // FINISH WITH READ EDF SOURCE
                // ...........
                final String title = s.getTitle();
                if(getActivity() != null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),title+" has successful saved to database",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            //load those who coming to the current loading
            loading_File_data.putAll(in_coming_File_data);
            //and let it go :)
            for (File_Sensor_Source file_info : in_coming_File_data.values()){
                new SourceFileLoaderThread("FILE#"+source_ID,file_info).start();
                source_ID++;
            }

            //empty those who have done
            //and those who have just come
            done_list.clear();
            in_coming_File_data.clear();
        }
        adapter.notifyDataSetChanged();
    }

    //---------------------- READ FILE SOURCE THREAD ---------------

    class SourceFileLoaderThread extends Thread{
        File_Sensor_Source in;
        SourceFileLoaderThread(String name ,File_Sensor_Source in){
            super(name) ;
            this.in = in ;
        }

        private SensorSource createAndStoreSensorSource(EDFHeader header, File_Sensor_Source file_source){
            String filePath = loading_File_data.get(in.getIndex()).getFilePath();
            String name_source = filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.'));

            file_source.setSensor_source(new SensorSource(name_source, "edf"));
            file_source.getSensor_source().source_status = SensorSource.OFFLINESOURCE;

            String dateTime = header.getStartDate().replace('.','/')+ " "+ header.getStartTime().replace('.',':');
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            long created_date_time = System.currentTimeMillis();
            try {
                Date d = f.parse(dateTime);
                created_date_time = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            file_source.getSensor_source().setReserved(header.getReservedFormat().getBytes());
            file_source.getSensor_source().setSource_id(name_source);
            file_source.getSensor_source().setStartDateTime(created_date_time);
            file_source.getSensor_source().setData_record_duration(header.getDurationOfRecords());

            // Store source to database
            SensorSourceAdapter sensorSourceAdapter = new SensorSourceAdapter(mMainActivity);
            try {
                sensorSourceAdapter.saveSensorSourceToDB(name_source,name_source,"edf",created_date_time,file_source.getSensor_source().getReserved(),
                        file_source.getSensor_source().getData_record_duration());

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            sensorSourceAdapter.close();
            return file_source.getSensor_source();

        }

        private Clinic createAndStoreClinic(EDFHeader header){
            ClinicAdapter clinicAdapter = new ClinicAdapter(mMainActivity);
            Clinic clinic = new Clinic();
            try{
                //parse Patient info according to EDF format
                String clinicfromEDF[] = header.getClinicInfo().trim().split(" ");
                clinic.setClinic_CODE(clinicfromEDF[2]);
                clinic.setTechnician_ID(clinicfromEDF[3]);
                clinic.setUsed_equipment(clinicfromEDF[4]);

                long c_id = clinicAdapter.storeNewClinic(clinicfromEDF[2],clinicfromEDF[3],clinicfromEDF[4],"","");
                clinic.setClinic_ID(c_id+"");

                if(c_id == -1) clinic = clinicAdapter.getClinicByIds(clinicfromEDF[2],clinicfromEDF[3],clinicfromEDF[4],"","");
            }catch (Exception e){
                e.printStackTrace();
            }
            clinicAdapter.close();

            return clinic;
        }

        private Patient createAndStorePatient(EDFHeader header){
            PersonAdapter personAdapter = new PersonAdapter(mMainActivity);
            Patient patient = new Patient();
            try{

                String patientfromEDF[] = (header.getPatientInfo().trim()).split(" ");
                patient.setPatient_ID(patientfromEDF[0]);
                patient.setPatient_code_in_clinic(patientfromEDF[0]);
                patient.setDateOfBirth(patientfromEDF[2]);
                patient.setGender(patientfromEDF[1]);
                patient.setFirstName(patientfromEDF[3]);

                long p_id = personAdapter.storeNewPatient(patientfromEDF[0],patientfromEDF[1],patientfromEDF[3],patientfromEDF[3],patientfromEDF[2],"","","");
                patient.setPatient_ID(p_id+"");
                if(p_id == -1) patient = personAdapter.getPatientByIds(patientfromEDF[0],patientfromEDF[1],patientfromEDF[3],patientfromEDF[3],patientfromEDF[2],"","","");
            }catch (Exception e){
                e.printStackTrace();
            }

            personAdapter.close();

            return patient;
        }

        private Channel[] createAndStoreChannels(EDFHeader header, String source_ID){
            Channel channelObjects[] = new Channel[header.getNumberOfChannels()];

            //looping through channels
            String channels[] = header.getChannelLabels();
            String transducer[] = header.getTransducerTypes();
            String dimensions[] = header.getDimensions();
            Double physicalMin[] = header.getMinInUnits();
            Double physicalMax[] = header.getMaxInUnits();
            Integer digitalMin[] = header.getDigitalMin();
            Integer digitalMax[] = header.getDigitalMax();
            String prefiltering[] = header.getPrefilterings();
            Integer nrOfsample[] = header.getNumberOfSamples();
            byte reserved[][] = header.getReserveds();

            //PATTERN FOR GET THE FREQUENCE AND DIMENSION
            ChannelAdapter channelAdapter = new ChannelAdapter(mMainActivity, true);
            try{
                for (int i = 0; i < channels.length; i++) {
                    String channel_id = i+"";
                    int channel_max_sample_per_dataRecord = nrOfsample[i];
                    String channel_name = channels[i].trim();
                    String channel_transducer_type = transducer[i].trim();
                    String channel_dimension = dimensions[i].trim();
                    double channel_physical_min = physicalMin[i];
                    double channel_physical_max = physicalMax[i];
                    int channel_digital_min = digitalMin[i];
                    int channel_digital_max = digitalMax[i];
                    String channel_prefiltering = prefiltering[i].trim();
                    byte[] channel_reserved = reserved[i];
                    String channel_description = "";

                    channelObjects[i] = new Channel(channel_id,source_ID);
                    channelObjects[i].setNumberSampleEDF(channel_max_sample_per_dataRecord);
                    channelObjects[i].setChannel_name(channel_name);
                    channelObjects[i].setTransducer_type(channel_transducer_type);
                    channelObjects[i].setPhysical_dimension(channel_dimension);
                    channelObjects[i].setPhysical_min(channel_physical_min);
                    channelObjects[i].setPhysical_max(channel_physical_max);
                    channelObjects[i].setDigital_min(channel_digital_min);
                    channelObjects[i].setDigital_max(channel_digital_max);
                    channelObjects[i].setPrefiltering(channel_prefiltering);
                    channelObjects[i].setReserved(channel_reserved);
                    channelObjects[i].setDescription(channel_description);

                    channelAdapter.saveChannelToDB(channel_id,source_ID,channel_name,channel_transducer_type,channel_dimension,
                            channel_physical_min,channel_physical_max,channel_digital_min,channel_digital_max,channel_prefiltering,
                            channel_reserved,channel_description);

                }
            }catch (Exception e){
                e.printStackTrace();
            }

            channelAdapter.close();

            return channelObjects;
        }

        @Override
        public void run(){
            final String filePath = loading_File_data.get(in.getIndex()).getFilePath();
            RecordAdapter recordAdapter = new RecordAdapter(mMainActivity);
            SampleAdapter sampleAdapter = new SampleAdapter(mMainActivity);
            try {
                File_Sensor_Source file_source = loading_File_data.get(in.getIndex());
                EDFHeader header = null;
                InputStream is = new BufferedInputStream(new FileInputStream(new File(filePath)));

                //Parse Header to create new sensor source
                header = EDFHeaderParser.parseHeader(is);
                if (header == null) return;
                is.close();

                SensorSource sensorSource = null;
                Clinic clinic = null;
                Patient patient = null;
                Channel[] channels = null;

                RandomAccessFile randomAccessFile = new RandomAccessFile(filePath,"r");

                //Check in logList if re-read,
                LogReadFile currentLog = null;
                for(LogReadFile lrf : logReadFiles){
                    if(filePath.equals(lrf.getPath())){
                        sensorSource = lrf.getSensorSource();
                        clinic = lrf.getClinic();
                        patient = lrf.getPatient();
                        channels = lrf.getChannels();

                        //seek to the previous read position
                        randomAccessFile.seek(lrf.getNr_of_bytes_have_read());
                        currentLog = lrf;
                        break;
                    }
                }
                //yes, seek the previous position and continue to read and parse samples
                //Else, create new LOG
                if(currentLog == null){
                    //Create and store SensorSource
                    sensorSource = createAndStoreSensorSource(header,file_source);
                    //clinic and patient
                    clinic = createAndStoreClinic(header);
                    patient = createAndStorePatient(header);
                    //All of the channels
                    channels = createAndStoreChannels(header,sensorSource.getSource_id());
                    //Create LOG object and put the above info into listLog.
                    currentLog = new LogReadFile(filePath,sensorSource,channels,patient,clinic,sensorSource.getNumberOfbytesHeaderEDF());
                    logReadFiles.add(currentLog);
                    randomAccessFile.seek(header.getBytesInHeader());
                }

                //parse samples for each channel
                currentLog.setNr_of_bytes_have_read(randomAccessFile.getFilePointer());
                int prevProgressBar = 0;


                //For each DataRecord
                while (currentLog.getNr_of_bytes_have_read() < randomAccessFile.length()) {
                    Record record = new Record(
                            currentLog.getRecordNR(),
                            sensorSource.getSource_id(),
                            patient.getPatient_ID(),
                            clinic.getClinic_ID(),
                            System.currentTimeMillis());

                    long totalBytesReadaRecord = 0;
                    //for each channel c : channels
                    List<Sample> sampleSetsForThisRecord = new ArrayList<>();
                    int maxSampleForDataRecord = 0;
                    for(int i = 0; i < channels.length; i++){
                        //get the number of samples of this one
                        int nr_sample_this_channel = channels[i].getNumberSampleEDF();

                        //create a float array with this sample inside sampleset
                        Sample sampleForThisChannel = new Sample(sensorSource.getSource_id(),
                                channels[i].getChannel_ID(), record.getData_record_ID()+"",
                                patient.getPatient_ID(),clinic.getClinic_ID(),nr_sample_this_channel);
                        byte[] buff = new byte[nr_sample_this_channel*2];
                        randomAccessFile.readFully(buff);




                        sampleForThisChannel.setSamples(buff);

                        sampleSetsForThisRecord.add(sampleForThisChannel);

                        totalBytesReadaRecord += nr_sample_this_channel*2;

                        if(nr_sample_this_channel > maxSampleForDataRecord) maxSampleForDataRecord = nr_sample_this_channel;
                    }
                    record.setSampleSetList(sampleSetsForThisRecord);
                    record.setMax_sample(maxSampleForDataRecord);

                    //============== HOW MANY DATA RECORD CAN WE BUFF BEFORE SEND TO DB ?===============
                    recordAdapter.saveRecordToDB(record.getData_record_ID(), record.getSource_ID(),
                    record.getPatient_ID(), record.getClinic_ID(), record.getCreated_date(),
                    record.getExperiments(), record.getDescriptions(), record.getMax_sample());

                    //Transaction will be used here
                    sampleAdapter.saveSampleToDB(sampleSetsForThisRecord);

                    //============================= END STORING ======================================

                    //LOG NUMBER OF BYTE READ INCREASE
                    currentLog.increaseBytesRead(totalBytesReadaRecord);

                    //update progress bar
                    file_source.setProgress((int)(((float)currentLog.getNr_of_bytes_have_read()/(float) randomAccessFile.length())*100));
                    if(file_source.getProgress() != 0 && prevProgressBar < file_source.getProgress()){
                        //System.out.println(currentLog.getNr_of_bytes_have_read()+" / "+randomAccessFile.length());
                        sendMessageToHandler(FILE_IS_LOADING,in.getIndex());
                    }
                    prevProgressBar = file_source.getProgress();

                    //IF USER CLICK STOP
                    if(!file_source.isRunning() ) {
                        synchronized (file_source.lock) {
                            sendMessageToHandler(FILE_IS_LOADED, in.getIndex());
                            return;
                        }
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }

            recordAdapter.close();
            sampleAdapter.close();
            sendMessageToHandler(FILE_IS_LOADED, in.getIndex());
        }

    }

    //-------------------- HELP CLASS FOR GUI -----------------------------

    class ViewHolder{
        TextView text;
        ProgressBar bar;
        Button button;
        ItemClickListener listener;

        ViewHolder(){
            listener = new ItemClickListener();
        }

    }

    class ItemClickListener implements View.OnClickListener {
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            sendMessageToHandler(START_STOP_LOADING,id);
        }

    }

    class SourceListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return loading_File_data.keySet().size();
        }

        @Override
        public Object getItem(int position) {
            Object[] d = loading_File_data.keySet().toArray();
            int c = (Integer)d[position];
            return loading_File_data.get(c);
        }

        @Override
        public long getItemId(int position) {
            return ((File_Sensor_Source)this.getItem(position)).getId() ;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder holder;
            if (v==null) {
                v = m_inflater.inflate(R.layout.source_from_file_listview_item, null);
                holder = new ViewHolder();

                holder.text = (TextView) v.findViewById(R.id.lbl_newFile_filename);
                holder.bar = (ProgressBar) v.findViewById(R.id.progressBar_loading);
                holder.button = (Button) v.findViewById(R.id.btn_start_stop);

                v.setTag(holder);
            }else{
                holder = (ViewHolder) v.getTag();
            }
            final File_Sensor_Source data = (File_Sensor_Source)getItem(position);

            holder.listener.setId(data.getIndex());
            holder.button.setOnClickListener(holder.listener);

            holder.text.setText(data.getTitle());
            holder.bar.setProgress(data.getProgress());

            if (data.isRunning()) {
                holder.button.setText("STOP");
            }else{
                holder.button.setText("START");
            }

            return v;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(getContext(), uri);
                            lblFilePath.setText(path);
                            btn_addNewFile.setEnabled(true);
                            Toast.makeText(getActivity(),
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("FileS", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

*/
}
