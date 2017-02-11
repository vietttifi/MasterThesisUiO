package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.MainFragments.PlotViewFragment;
import no.uio.ifi.viettt.mscosa.MainFragments.ServerFragment;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SampleSet;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 05/01/2017.
 */

public class ClientThread extends Thread{
    //MAIN ATTRIBUTE OF A CLIENT THREAD
    private List<Channel> channelList;
    private boolean isRec = false;
    private boolean stop = false;
    private Socket clientsSocket;
    private SensorSource sensorSource;
    private DataRecord dataRecord;
    long data_record_ID = 0;

    private UpdatePlotThread treadUpdatePlot;
    private UpdateDBThread threadUpdateDB;


    //HELP ATTRIBUTE
    private Context context;
    private Handler serverUpdateUI;
    private float maxFrequence = 1;

    List<String> channelIDsToBeStoreToDB;

    public ClientThread(Socket clientsSocket, Context context, Handler serverUpdateUI){
        this.clientsSocket = clientsSocket;
        this.context = context;
        this.serverUpdateUI = serverUpdateUI;
    }

    public void registerSensorSource(SensorSource sensorSource){
        this.sensorSource = sensorSource;
    }

    public void closeConnection(){
        try {
            if(threadUpdateDB != null) threadUpdateDB.stopThread();
            if(treadUpdatePlot != null) treadUpdatePlot.stopThread();
            clientsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        final String clientIP = clientsSocket.getRemoteSocketAddress().toString();
        try{
            String jsonStringFromBITalino = null;
            BufferedReader bf = new BufferedReader(new InputStreamReader(clientsSocket.getInputStream()));
            while ((jsonStringFromBITalino = bf.readLine()) != null && !jsonStringFromBITalino.equals("END") && !stop){
                try {
                    JSONObject jsonObj = new JSONObject(jsonStringFromBITalino);
                    String type = jsonObj.getString("type");

                    if(type.equals("meta")){
                        //SO IT IS META DATA
                        registerNewSensorSource(jsonObj);
                    }else{
                        //SAMPLES
                        updateSample(jsonObj);
                    }

                } catch (final JSONException e) {
                    serverUpdateUI.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Json parsing error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }

        }catch (IOException e){
            //e.printStackTrace();
        }

        try {
            if(!clientsSocket.isClosed()){
                PrintWriter out = new PrintWriter(clientsSocket.getOutputStream(),true);
                out.println("SEE YOU NEXT TIME");
                out.flush();
                out.close();
            }
            clientsSocket.close();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e);
        }

        sensorSource.setSource_status(SensorSource.UNACTIVESTATUS);

        serverUpdateUI.post(new Runnable() {
            @Override
            public void run() {
                if(context != null)
                    Toast.makeText(context,clientIP+" disconnected.",Toast.LENGTH_SHORT).show();
            }
        });

    }


    void registerNewSensorSource(JSONObject jsonObj) throws JSONException{
        channelList = new ArrayList<>();
        String id_source = jsonObj.getString("id");
        //Create information for new coming source
        String name_source = jsonObj.getString("name");
        String type_source = "bitalino";
        if(jsonObj.has("type_source")) type_source = jsonObj.getString("type_source");

        id_source = name_source + id_source;
        sensorSource.setSource_name(name_source);
        sensorSource.setSource_type(type_source);
        sensorSource.setSource_id(id_source);
        sensorSource.setStartDateTime(System.currentTimeMillis());

        // Getting JSON Array node
        JSONArray channels = jsonObj.getJSONArray("channels");
        // looping through channels
        for (int i = 0; i < channels.length(); i++) {
            JSONObject channel = channels.getJSONObject(i);

            String id = channel.getString("id");
            String channel_name = channel.getString("type");
            String transducer = "";
            if(channel.has("transducer_type")) transducer = channel.getString("transducer_type");
            String dimension = "";
            if(channel.has("dimension") || channel.has("metric")){
                if(channel.has("metric")) dimension = channel.getString("metric");
                else dimension = channel.getString("dimension");
            }
            double physical_min = 0;
            double physical_max = 0;
            if(channel.has("physical_min")) physical_min = Double.parseDouble(channel.getString("physical_min").trim());
            if(channel.has("physical_max")) physical_max = Double.parseDouble(channel.getString("physical_max").trim());
            int digital_min = 0;
            int digital_max = 0;
            if(channel.has("digital_min")) digital_min = Integer.parseInt(channel.getString("digital_min").trim());
            if(channel.has("digital_max")) digital_max = Integer.parseInt(channel.getString("digital_max").trim());
            String pre_filtering = "";
            if(channel.has("prefiltering")) pre_filtering = channel.getString("prefiltering").trim();
            String description = "";
            if(channel.has("description")) description = channel.getString("description");

            Channel s_new = new Channel(id,id_source);
            s_new.setChannel_name(channel_name);
            s_new.setTransducer_type(transducer);
            s_new.setPhysical_dimension(dimension);
            s_new.setPhysical_min(physical_min);
            s_new.setPhysical_max(physical_max);
            s_new.setDigital_min(digital_min);
            s_new.setDigital_max(digital_max);
            s_new.setPrefiltering(pre_filtering);
            s_new.setDescription(description);

            s_new.frequence = Float.parseFloat("10");
            channelList.add(s_new);
        }

    }

    void updateSample(JSONObject jsonObj) throws JSONException{

        String id_source = jsonObj.getString("id");

        // Getting JSON Array node
        //jsonObj.getString("time")
        ABITalinoData abiTalinoData = new ABITalinoData(System.currentTimeMillis());
        JSONArray datas = jsonObj.getJSONArray("data");
        for (int i = 0; i < datas.length(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String id_channel = data.getString("id");
            String value = data.getString("value").trim();
            abiTalinoData.addDataSample(id_channel,value);
        }

        if(treadUpdatePlot != null) treadUpdatePlot.updateSamples(abiTalinoData);

        if(isRec){
            if(dataRecord.isNOTDataRecordFull()){ //buffer the sample
                for(SampleSet s : dataRecord.getSampleSetList()){
                    s.addABITalinoSample(abiTalinoData);
                }
                dataRecord.countUpSample();
                System.out.println("----> COUNT UP");
            } else { //save to DB when buff is full
                /*Give DataRecord for UpDateDB thread*/
                System.out.println("FULL A BUFFER -> STORE");
                if(threadUpdateDB != null) threadUpdateDB.updateDataRecord(dataRecord);
                data_record_ID++;
                //if 1000hz, each second we have 1000 samples
                int maxSample = (int)Math.ceil(sensorSource.getData_record_duration()*maxFrequence);
                dataRecord = new DataRecord(data_record_ID, sensorSource.getSource_id(),
                        dataRecord.getPatient_ID(),dataRecord.getClinic_ID(),
                        System.currentTimeMillis(), maxSample);
                //INIT SAMPLESET WITH RESPECT TO SELECTED CHANNELS
                dataRecord.initSampleSet(channelIDsToBeStoreToDB);
            }
        }

        //else drop this data
    }

    public void setRec(boolean rec, String patient_ID, String clinic_ID, List<String> channelIDs) {
        //CLINIC AND PATIENT MUST BE STORED BEFORE CALLING THIS FUNCTION
        if(rec){//BEGIN REC
            channelIDsToBeStoreToDB = channelIDs;
            /*store sensor source, channel list to DB*/
            storeSensorSourceAndChannelList();
            /*begin to buff and store data record for this sensor source*/
            data_record_ID = 0;
            //if 1000hz, each second we have 1000 samples
            int maxSample = (int)Math.ceil(sensorSource.getData_record_duration()*maxFrequence);

            dataRecord = new DataRecord(data_record_ID, sensorSource.getSource_id(),
                    patient_ID,clinic_ID,System.currentTimeMillis(),maxSample);
            threadUpdateDB = new UpdateDBThread(context);
            //INIT SAMPLESET WITH RESPECT TO SELECTED CHANNELS
            dataRecord.initSampleSet(channelIDs);

            new Thread(threadUpdateDB).start();
        } else{
            if(threadUpdateDB != null) threadUpdateDB.stopThread();
            threadUpdateDB = null;
        }
        isRec = rec;
    }

    public void visualisePlotView(PlotViewFragment plotViewFragment){
        treadUpdatePlot = new UpdatePlotThread(plotViewFragment);
        new Thread(treadUpdatePlot).start();
    }

    private void storeSensorSourceAndChannelList(){
        SensorSourceAdapter sourceAdapter = new SensorSourceAdapter(this.context);
        sourceAdapter.saveSensorSourceToDB(sensorSource.getSource_id(),sensorSource.getSource_name(),sensorSource.getSource_type(),
                sensorSource.getStartDateTime(),sensorSource.getReserved(),sensorSource.getData_record_duration());
        sourceAdapter.close();

        ChannelAdapter channelAdapter = new ChannelAdapter(this.context);
        for(Channel c : channelList){
            channelAdapter.saveChannelToDB(c.getChannel_ID(),c.getSource_ID(),c.getChannel_name(),c.getTransducer_type(),
                    c.getPhysical_dimension(),c.getPhysical_min(),c.getPhysical_max(),c.getDigital_min(),
                    c.getDigital_max(),c.getPrefiltering(),c.getReserved(),c.getDescription());
            //calculate max frequence of all channels
            if(c.frequence > maxFrequence) maxFrequence = c.frequence;
        }
        channelAdapter.close();
    }

    public List<Channel> getChannelList() {
        return channelList;
    }

    public void stopThreadPlotUpdate(){
        if(treadUpdatePlot != null){
            treadUpdatePlot.stopThread();
        }
        treadUpdatePlot = null;
    }
}
