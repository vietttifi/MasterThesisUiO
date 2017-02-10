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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.DataRecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PatientAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleSetAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeaderParser;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.OpenSourceFileChooserFromDelaroy.FileUtils;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeader;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFSignal;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SampleSet;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

import static android.app.Activity.RESULT_OK;

/**
 * Created by viettt on 18/12/2016.
 */

public class SourceFromFileFragment extends Fragment {
/*
    MainActivity mMainActivity;
    PlotViewFragment mPlotViewFragment;

    //For progressBar management
    static final int ADD_FILE_SOURCE = 0xCAFE1, FILE_IS_LOADING = 0xCAFE2, FILE_IS_LOADED = 0xCAFE3, START_STOP_LOADING = 0xCAFE4;
    static final long REFRESH_INTERVAL = 100;
    private static final int REQUEST_CODE = 6384;
    final Object lock = new Object();

    int source_ID = 0;
    View v;

    ListView listview;
    Button btn_addNewFile, btn_chooseFile;
    TextView lblFilePath;

    SourceListAdapter adapter;
    LayoutInflater m_inflater;

    LinkedHashMap<Integer, File_Sensor_Source> loading_File_data;
    LinkedHashMap<Integer, File_Sensor_Source> in_coming_File_data = new LinkedHashMap<>();
    List<Integer> done_list = new ArrayList<>();

    protected boolean isScrolling = false ;
    long lastUpdate = 0;

    Handler progressHandler;
    //The handler for manage GUI
    private Handler toastHandler = new Handler();

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
                file_info.title = filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.'));
                file_info.process = 0;
                file_info.id = source_ID;
                file_info.index = source_ID;
                file_info.filePath = filePath;
                synchronized (lock) {
                    in_coming_File_data.put(source_ID,file_info);
                }
                sendMessageToHandler(ADD_FILE_SOURCE,source_ID);
            }
        });

        m_inflater = LayoutInflater.from(getContext());
        return v;
    }

    public void setPointerToCurUI(PlotViewFragment mPlotViewFragment, MainActivity mMainActivity){
        this.mMainActivity = mMainActivity;
        this.mPlotViewFragment = mPlotViewFragment;
    }

    void sendMessageToHandler(int what,int arg1){
        Message message = progressHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    void updateListView(){
        synchronized (lock) {
            //Transfer to current_connected_list
            //Then just remove from the
            for (Integer done_id : done_list){
                File_Sensor_Source s = loading_File_data.remove(done_id);
                // FINISH WITH READ EDF SOURCE
                // ...........
                final String title = s.title;
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

        public SensorSourceAdapter sensorSourceAdapter;
        public ChannelAdapter channelAdapter;
        public ClinicAdapter clinicAdapter;
        public DataRecordAdapter dataRecordAdapter;
        public PatientAdapter patientAdapter;
        public SampleSetAdapter sampleSetAdapter;

        SourceFileLoaderThread(String name ,File_Sensor_Source in){
            super(name) ;
            this.in = in ;
        }

        @Override
        public void run(){
            final String filePath = loading_File_data.get(in.index).filePath;
            sensorSourceAdapter = new SensorSourceAdapter(getContext());
            SensorSource lookUpOldSource = sensorSourceAdapter.getSensorSourceById("EDF"+filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.')));
            if(lookUpOldSource != null){
                toastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"File source has been read before!",Toast.LENGTH_SHORT).show();
                    }
                });
                sendMessageToHandler(FILE_IS_LOADED, in.index);
                sensorSourceAdapter.close();
                return;
            }
            channelAdapter = new ChannelAdapter(getContext());
            clinicAdapter = new ClinicAdapter(getContext());
            dataRecordAdapter = new DataRecordAdapter(getContext());
            patientAdapter = new PatientAdapter(getContext());
            sampleSetAdapter = new SampleSetAdapter(getContext());
            try {

                File_Sensor_Source file_source = loading_File_data.get(in.index);

                boolean finished = false;
                EDFHeader header = null;
                InputStream is = new BufferedInputStream(new FileInputStream(new File(filePath)));

                //Parse Header to create new sensor source
                header = EDFHeaderParser.parseHeader(is);
                String name_source = filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.'));
                String id_source = "EDF" +name_source;

                file_source.sensor_source = new SensorSource(id_source, name_source, "edf");
                file_source.sensor_source.source_status = SensorSource.OFFLINESOURCE;

                String dateTime = header.getStartDate().replace('.','/')+ " "+ header.getStartTime().replace('.',':');
                SimpleDateFormat f = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                long created_time_eks = System.currentTimeMillis();
                try {
                    Date d = f.parse(dateTime);
                    created_time_eks = d.getTime();
                    System.out.println(dateTime+ " "+created_time_eks);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                file_source.sensor_source.setStartDateTime(created_time_eks);

                // Store source to database
                sensorSourceAdapter.createSensorSource(id_source,name_source,"edf",file_source.sensor_source.getStartDateTime(),file_source.sensor_source.getReserved(),
                        file_source.sensor_source.getData_record_duration());
                // ..... ==== .....

                //parse Patient info according to EDF format
                String patientfromEDF[] = (header.getPatientInfo().trim()).split(" ");
                //String patient_code_in_clinic, String gender,String last_name, String first_name,String date_of_birth, String address, String phone_nr, String email
                //System.out.println("PATIENT FROM EDF -> "+ patientfromEDF[0]+" "+patientfromEDF[1]+" "+patientfromEDF[3]);
                Patient patient = patientAdapter.createPatient(patientfromEDF[0],patientfromEDF[1],patientfromEDF[3],"",patientfromEDF[2],"","","");
                //String clinic_CODE, String technician_ID, String address, String phone_nr,String email
                String clinicfromEDF[] = header.getClinicInfo().trim().split(" ");
                Clinic clinic = clinicAdapter.createClinic(clinicfromEDF[2],clinicfromEDF[3],"","","");
                //A code specifying the used equipment.
                file_source.sensor_source.setSource_type("edf "+clinicfromEDF[3]);

                //parse Clinic info according to EDF format
                System.out.println("EDF: HAS SAVED SOURCE ----> "+file_source.sensor_source.getSource_name()
                        + ", patient "+patient.getPatient_ID()+" clinic "+clinic.getClinic_ID()+" "+clinic.getClinic_CODE()+" --- "+result.header.getStartDate().trim()+
                " --- "+result.header.getStartTime().trim());

                //looping through channels
                String channels[] = header.getChannelLabels();
                String transducer[] = header.getTransducerTypes();
                String dimensions[] = header.getDimensions();
                Double physicalMin[] = header.getMinInUnits();
                Double physicalMax[] = header.getMaxInUnits();
                Integer digitalMin[] = header.getDigitalMin();
                Integer digitalMax[] = header.getDigitalMax();
                String prefiltering[] = header.getPrefilterings();
                final Integer nrOfsample[] = header.getNumberOfSamples();
                byte reserved[][] = header.getReserveds();

                //PATTERN FOR GET THE FREQUENCE AND DIMENSION
                Pattern patternFreq = Pattern.compile("LP:\\d+Hz");

                for (int i = 0; i < channels.length; i++) {
                    //FREQUENCE
                    float frequence = 0;
                    Matcher matcherFreq = patternFreq.matcher(prefiltering[i]);
                    if (matcherFreq.find())
                    {
                        frequence = Float.parseFloat(matcherFreq.group(0).substring(matcherFreq.group(0).indexOf(':')+1,matcherFreq.group(0).indexOf('H')));
                    }else {
                        frequence = 10f; //10Hz as default value
                    }

                    // Store channel to database
                    // ....
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
                    Channel channel = channelAdapter.createChannel(channel_id,id_source,channel_max_sample_per_dataRecord,channel_name,
                            channel_transducer_type,channel_dimension,channel_physical_min,channel_physical_max,channel_digital_min,channel_digital_max,
                            channel_prefiltering,channel_reserved,channel_description);

                    System.out.println("EDF: HAS SAVED CHANNEL ----> "+channel.getChannel_name());
                }

                //parse samples for each channel
                while (!finished) {
                    EDFSignal signal = new EDFSignal();

                    signal.setUnitsInDigit(new Double[header.getNumberOfChannels()]);
                    for (int i = 0; i < signal.getUnitsInDigit().length; i++)
                        signal.getUnitsInDigit()[i] = (double)(header.getMaxInUnits()[i] - header.getMinInUnits()[i])
                                / (double) (header.getDigitalMax()[i] - header.getDigitalMin()[i]);

                    int samplesPerRecord = 0;
                    for (int nos : header.getNumberOfSamples())
                    {
                        samplesPerRecord += nos;
                    }

                    ReadableByteChannel ch = Channels.newChannel(is);
                    ByteBuffer bytebuf = ByteBuffer.allocate(samplesPerRecord * 2);
                    bytebuf.order(ByteOrder.LITTLE_ENDIAN);

                    int oldProgress = file_source.process;
                    double cnt = 0;
                    double maxCnt = samplesPerRecord*header.getNumberOfRecords();

                    for (int i = 0; i < header.getNumberOfRecords(); i++) {
                        bytebuf.rewind();
                        ch.read(bytebuf);
                        bytebuf.rewind();

                        //System.out.println("EDF: data record nr ----> "+i+" "+samplesPerRecord*2);
                        //long data_record_id, String source_id, String patient_id, String clinic_id, long createDate, String experiments, String descriptions
                        DataRecord dr = dataRecordAdapter.createRecord(i,id_source,patient.getPatient_ID(),clinic.getClinic_ID(),created_time_eks,"","");

                        for (int j = 0; j < header.getNumberOfChannels(); j++){
                            if(!file_source.running ) {
                                synchronized (file_source.lock) {
                                    try {
                                        file_source.lock.wait() ;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            byte[] samples = new byte[header.getNumberOfSamples()[j]*2];
                            bytebuf.get(samples);
                            //String channel_id, String record_id, int maxSample, float coefficient, byte[] sample_data
                            Channel c = channelAdapter.getChannelById(j+"",id_source);
                            SampleSet s = sampleSetAdapter.createSample(j+"",i+"",c.getMaxSampleEachDataRecord(),c.getCoefficient(),samples);

                            //System.out.println("EDF: HAS SAVED SAMPLES FOR DATA_RECORD "+dr.getData_record_ID()+", channel ----> "+c.getChannel_ID()+c.getChannel_name()+" nrbytes "+s.getNr_of_sample()*2);

                            file_source.process = (int)((cnt/maxCnt)*100);
                            cnt += header.getNumberOfSamples()[j];
                            //update how far loading process is going on
                            if(file_source.process != 0 && oldProgress<file_source.process){
                                sendMessageToHandler(FILE_IS_LOADING,in.index);
                            }
                            oldProgress = file_source.process;

                        }

                    }

                    finished = true;
                }
                sendMessageToHandler(FILE_IS_LOADED, in.index);


            }catch (Exception e){
                e.printStackTrace();
                sendMessageToHandler(FILE_IS_LOADED, in.index);
            }

            sensorSourceAdapter.close();
            channelAdapter.close();
            clinicAdapter.close();
            dataRecordAdapter.close();
            patientAdapter.close();
            sampleSetAdapter.close();
        }

    }

    class File_Sensor_Source{
        int index;
        int id;

        boolean running = true;

        String filePath;
        String title;
        int process;
        public SensorSource sensor_source;

        final Object lock = new Object();

        public File_Sensor_Source(){
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
            return ((File_Sensor_Source)this.getItem(position)).id ;
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

            holder.listener.setId(data.index);
            holder.button.setOnClickListener(holder.listener);

            holder.text.setText(data.title);
            holder.bar.setProgress(data.process);

            if (data.running) {
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


}

class ProgressBarHandler extends Handler{

    private SourceFromFileFragment sourceFromFileFragment;

    ProgressBarHandler(SourceFromFileFragment sourceFromFileFragment){
        this.sourceFromFileFragment = sourceFromFileFragment;
    }

    public void handleMessage(Message msg) {
        switch (msg.what){
            case SourceFromFileFragment.ADD_FILE_SOURCE:
                sourceFromFileFragment.updateListView();
                sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                break;
            case SourceFromFileFragment.FILE_IS_LOADING:
                boolean needUpdate = (System.currentTimeMillis() - sourceFromFileFragment.lastUpdate) > SourceFromFileFragment.REFRESH_INTERVAL;
                if ( !sourceFromFileFragment.isScrolling && needUpdate) {
                    sourceFromFileFragment.updateListView();
                    sourceFromFileFragment.lastUpdate = System.currentTimeMillis();
                }
                break;
            case SourceFromFileFragment.START_STOP_LOADING:{
                if(!sourceFromFileFragment.loading_File_data.get(msg.arg1).running ) {
                    synchronized (sourceFromFileFragment.loading_File_data.get(msg.arg1).lock) {
                        sourceFromFileFragment.loading_File_data.get(msg.arg1).lock.notifyAll();
                    }
                }
                sourceFromFileFragment.loading_File_data.get(msg.arg1).running = !sourceFromFileFragment.loading_File_data.get(msg.arg1).running;
                if ( !sourceFromFileFragment.isScrolling ) {
                    sourceFromFileFragment.updateListView();
                    sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                }
                break;
            }
            case SourceFromFileFragment.FILE_IS_LOADED:
                synchronized (sourceFromFileFragment.lock) {
                    sourceFromFileFragment.done_list.add(msg.arg1);
                }
                sourceFromFileFragment.updateListView();
                sourceFromFileFragment.lastUpdate += SourceFromFileFragment.REFRESH_INTERVAL;
                break;

            default:
                break;
        }

    }

    */
}
