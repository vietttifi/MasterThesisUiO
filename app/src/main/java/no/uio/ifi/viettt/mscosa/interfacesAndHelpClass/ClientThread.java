package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.MainFragments.ServerFragment;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.Services.ForegroundDBService;

/**
 * Created by viettt on 05/01/2017.
 */

public class ClientThread extends Thread{
    //MAIN ATTRIBUTE OF A CLIENT THREAD
    private String thread_ID;
    private boolean isStoring;
    private boolean isPlotting;
    private boolean isDisconnected;
    private boolean readyToUse = false;
    private String status;

    private SensorSource sensorSource;
    private HashMap<String,Channel> channels = new HashMap<>();
    private HashMap<String,Record> records = new HashMap<>(); //key is channel nr, for that it is easy to retrieve record when save samples
    private Socket clientsSocket;
    private UpdateDBThread updateDBThread;

    private long timeStamp;

    private ArrayList<Sample> bufferSamples;

    //HELP ATTRIBUTE
    private Context context;
    private Handler serverUpdateUI;
    private ServerFragment serverFragment;
    private BeNotifiedComingSample plotViewFragment;
    private float maxFrequence = 1;

    private boolean firstTimeStamp;
    //BUFFER SIZE IN SECOND FOR REAL TIME SOURCE
    private int frag_duration;

    private int cnt = 0;

    public ClientThread(Socket clientsSocket, Context context, Handler serverUpdateUI, ServerFragment serverFragment){
        this.clientsSocket = clientsSocket;
        this.context = context;
        this.serverUpdateUI = serverUpdateUI;
        this.serverFragment = serverFragment;
        this.status = "Connected";
        this.thread_ID = ""+this.getId();
    }

    public void closeConnection(){
        try {
            clientsSocket.close();
            isDisconnected = true;
            setStoring(false);
            setPlotting(false,null);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("DISCONNECTED");
        }
    }

    @Override
    public void run(){
        final String clientIP = clientsSocket.getRemoteSocketAddress().toString();
        try{
            String jsonStringFromBITalino = null;
            BufferedReader bf = new BufferedReader(new InputStreamReader(clientsSocket.getInputStream()));
            while ((jsonStringFromBITalino = bf.readLine()) != null && !jsonStringFromBITalino.equals("END") && !isDisconnected){
                //System.out.println("have gotten "+cnt++);
                try {
                    JSONObject jsonObj = new JSONObject(jsonStringFromBITalino);
                    String type = jsonObj.getString("type");

                    if(type.equals("meta")){
                        //SO IT IS META DATA
                        String source_ID = jsonObj.getString("id");
                        boolean isRemoveNew = serverFragment.checkAndRemoveSourceNewSource(source_ID, this);
                        if(!isRemoveNew) {
                            this.thread_ID = source_ID;
                            registerNewSensorSource(jsonObj);
                        }
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
            System.out.println("DISCONNECTED");
        }

        try {
            if(!clientsSocket.isClosed()){
                PrintWriter out = new PrintWriter(clientsSocket.getOutputStream(),true);
                out.println("SEE YOU NEXT TIME");
                out.flush();
                out.close();
                clientsSocket.close();
            }


        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e);
        }

        isDisconnected = true;
        setStoring(false);
        setPlotting(false,null);
        status = ((isStoring)?"Storing ":"")+((isPlotting)?" Plotting ":"")+ " Disconnected ";
        serverFragment.invalidateSourceList();

        serverUpdateUI.post(new Runnable() {
            @Override
            public void run() {
                if(context != null)
                    Toast.makeText(context,clientIP+" disconnected.",Toast.LENGTH_SHORT).show();
            }
        });

    }


    void registerNewSensorSource(JSONObject jsonObj) throws JSONException{
        //PHYSICIAN, CLINIC AND PATIENT ARE CREATED BEFORE RECORDING
        //THEY DO NOT BELONG TO THIS PLACE

        String id_source = jsonObj.getString("id");
        String name_source = jsonObj.getString("name");
        String type_source = "bitalino";
        if(jsonObj.has("type_source")) type_source = jsonObj.getString("type_source");

        //SENSOR_SOURCE OBJECT
        sensorSource = new SensorSource();
        sensorSource.setS_id(id_source);
        sensorSource.setS_name(name_source);
        sensorSource.setS_type(type_source);

        //  CHANNELS  Getting JSON Array node
        JSONArray channelsJSON = jsonObj.getJSONArray("channels");
        //  looping through CHANNELS
        for (int i = 0; i < channelsJSON.length(); i++) {
            JSONObject channel = channelsJSON.getJSONObject(i);
            System.out.println(channel.toString());
            Channel channelNew = new Channel();
            channelNew.setS_id(sensorSource.getS_id());
            channelNew.setCh_nr(channel.getString("id"));
            channelNew.setCh_name(channel.getString("type"));
            if(channel.has("transducer_type")) channelNew.setTransducer(channel.getString("transducer_type"));
            if(channel.has("metric")) channelNew.setDimension(channel.getString("metric"));
            if(channel.has("physical_max"))
                channelNew.setPhy_max((channel.getString("physical_max") != null) ? Float.parseFloat(channel.getString("physical_max")): Float.MAX_VALUE);
            if(channel.has("physical_min"))
                channelNew.setPhy_min((channel.getString("physical_min") != null) ? Float.parseFloat(channel.getString("physical_min")): Float.MIN_VALUE);
            if(channel.has("digital_max"))
                channelNew.setDig_max((channel.getString("digital_max") != null) ? Integer.parseInt(channel.getString("digital_max")): Integer.MAX_VALUE);
            if(channel.has("digital_min"))
                channelNew.setDig_min((channel.getString("digital_min") != null) ? Integer.parseInt(channel.getString("digital_min")): Integer.MIN_VALUE);
            if(channel.has("description")) channelNew.setDescription(channel.getString("description"));
            if(channel.has("frequency"))
                channelNew.setFrequency((channel.getString("frequency") != null) ? Float.parseFloat(channel.getString("frequency")): 1);
            channels.put(channelNew.getCh_nr(),channelNew);
        }

        readyToUse = true;
    }

    void updateSample(JSONObject jsonObj) throws JSONException{
        if(!isPlotting && !isStoring) return;
        long timeStamp = jsonObj.getLong("time");
        //  CHANNELS DATA  Getting JSON Array node
        JSONArray channelsData = jsonObj.getJSONArray("data");
        BitalinoDataSample[] samples = new BitalinoDataSample[channelsData.length()];
        for(int i = 0; i < channelsData.length(); i++){
            JSONObject channelData = channelsData.getJSONObject(i);
            String channel_nr = channelData.getString("id");
            float channel_data = Float.parseFloat(channelData.getString("value"));
            samples[i] = new BitalinoDataSample(timeStamp,channel_nr,channel_data);
        }
        //SEND TO DATABASE BUFFER OR PLOTTING
        if(isStoring) manageIsStoring(samples);
        if(isPlotting) manageIsPlotting(samples);
    }

    private void manageIsPlotting(BitalinoDataSample samples[]){
        this.plotViewFragment.addNewSample(samples);
    }

    private void manageIsStoring(BitalinoDataSample samples[]) {
        //FIRST TIME
        if(firstTimeStamp) {
            timeStamp = samples[0].getCreatedDate();
            //update DB for RECORDs
            RecordAdapter recordAdapter = new RecordAdapter(context);
            for(Record r : records.values()){
                System.out.println(r.getR_id());
                r.setTimestamp(timeStamp);
                r.setFrequency(channels.get(String.valueOf(r.getCh_nr())).getFrequency());
                recordAdapter.updateRecordTimestamp(r.getR_id(),timeStamp, r.getFrequency());
            }
            recordAdapter.close();
            firstTimeStamp = false;
        }

        //if we need to save empty fragment n = (samples timestamp - fragment timestamp)/duration >=1
        long n = (samples[0].getCreatedDate() - timeStamp)/(long)frag_duration;
        if(n >= 1){
            //send storing request to DB with current fragment as parameter
            updateDBThread.requestDataBaseSaving(bufferSamples);
            timeStamp = samples[0].getCreatedDate();
            //create new current fragment
            bufferSamples = new ArrayList<>();
            File f = context.getDatabasePath(OSADBHelper.DATABASE_NAME);
            long dbSize = f.length();
            //System.out.println("DB size: "+dbSize/(1024.0*1024.0)+" MB.");
            //if((dbSize/(1024.0*1024.0)) > 700.0) this.closeConnection();
        }
        //(samples timestamp - fragment timestamp)/duration < 1
        //add all samples to current fragment buffer
        for(BitalinoDataSample sample : samples){
            if(channels.get(sample.getChannel_nr()).isSelectedToSaveSample() && bufferSamples != null)
                bufferSamples.add(new Sample(records.get(sample.getChannel_nr()).getR_id(),sample.getCreatedDate(),sample.getSample_data()));
        }
    }

    public void setStoring(boolean storing) {
        System.out.println("STORING " + storing);
        if(storing){
            status = "storing";
            serverFragment.invalidateSourceList();
            updateDBThread = new UpdateDBThread(context);
            updateDBThread.start();
            bufferSamples = new ArrayList<>();
            firstTimeStamp = true;
        } else {
            isStoring = false;
            if(bufferSamples != null && !bufferSamples.isEmpty()) {
                updateDBThread.requestDataBaseSaving(bufferSamples);
            }
            bufferSamples = null;
            if(updateDBThread != null) updateDBThread.setStop(true);
            File f = context.getDatabasePath(OSADBHelper.DATABASE_NAME);
            long dbSize = f.length();
            if(updateDBThread != null)
                System.out.println("Thread ID: "+thread_ID+". DB size: "+dbSize/(1024.0*1024.0)+" MB. Total used time"+updateDBThread.getUsedTimeForSQL());

        }

        isStoring = storing;

    }

    public void setPlotting(boolean plotting, BeNotifiedComingSample plotViewFragment) {
        isPlotting = plotting;
        if(plotting){
            this.plotViewFragment = plotViewFragment;
            status = "Plotting";
            serverFragment.invalidateSourceList();
        }
    }

    public boolean isPlotting() {
        return isPlotting;
    }

    public String getThread_ID() {
        return thread_ID;
    }

    public void setThread_ID(String thread_ID) {
        this.thread_ID = thread_ID;
    }

    public boolean isStoring() {
        return isStoring;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void setDisconnected(boolean disconnected) {
        isDisconnected = disconnected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SensorSource getSensorSource() {
        return sensorSource;
    }

    public void setSensorSource(SensorSource sensorSource) {
        this.sensorSource = sensorSource;
    }

    public HashMap<String, Channel> getChannels() {
        return channels;
    }

    public void setChannels(HashMap<String, Channel> channels) {
        this.channels = channels;
    }

    public Socket getClientsSocket() {
        return clientsSocket;
    }

    public void setClientsSocket(Socket clientsSocket) {
        this.clientsSocket = clientsSocket;
    }

    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        this.readyToUse = readyToUse;
    }

    public HashMap<String, Record> getRecords() {
        return records;
    }

    public void setRecords(HashMap< String, Record> records) {
        this.records = records;
    }

    public int getFrag_duration() {
        return frag_duration;
    }

    public void setFrag_duration(int frag_duration) {
        this.frag_duration = frag_duration;
    }
}
