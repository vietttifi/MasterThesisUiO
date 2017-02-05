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

import no.uio.ifi.viettt.mscosa.MainFragments.ServerFragment;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 05/01/2017.
 */

public class ClientThread extends Thread{
    ClientThread selv;
    public Socket clientsSocket;
    Context context;
    ServerFragment serverFragment;
    BeNotifiedComingSample beNotifiedComingSample;

    public boolean stop = false;
    public SensorSource handlingSource;
    Handler serverUpdateUI;

    public ClientThread(Socket clientsSocket, Context context, ServerFragment serverFragment, Handler serverUpdateUI){
        this.clientsSocket = clientsSocket;
        this.context = context;
        this.serverFragment = serverFragment;
        this.serverUpdateUI = serverUpdateUI;
        selv = this;
    }

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

            if(!clientsSocket.isClosed()){
                PrintWriter out = new PrintWriter(clientsSocket.getOutputStream(),true);
                out.println("SEE YOU NEXT TIME");
                out.flush();
                out.close();
            }

        }catch (IOException e){
            //e.printStackTrace();
        }

        serverUpdateUI.post(new Runnable() {
            @Override
            public void run() {
                if(context != null)
                    Toast.makeText(context,clientIP+" disconnected.",Toast.LENGTH_SHORT).show();
            }
        });

        try {
            if (handlingSource != null){
                handlingSource.source_status = SensorSource.UNACTIVESTATUS;
                handlingSource.setReferenceThread(null);
                serverFragment.close_a_socket(clientsSocket);
                serverFragment.invalidateSourceList();
            }
            if(!clientsSocket.isClosed()) clientsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    void registerNewSensorSource(JSONObject jsonObj) throws JSONException{
        String id_source = jsonObj.getString("id");
        if(serverFragment.activeDisconnectedSource(id_source,clientsSocket)){
            return;
        }

        //Create information for new coming source
        String name_source = jsonObj.getString("name");
        String type_source = "bitalino";
        if(jsonObj.has("type_source")) type_source = jsonObj.getString("type_source");
        SensorSource new_source = new SensorSource(id_source, name_source, "bitalino");
        handlingSource = new_source;
        new_source.setReferenceThread(selv);
        new_source.setStartDateTime(System.currentTimeMillis());
        new_source.setReserved(null);
        new_source.setData_record_duration(SensorSource.MAX_DURATION_EACH_DATA_RECORD);

        new_source.patient = new Patient();
        new_source.clinic = new Clinic();
        new_source.mainActivity = serverFragment.mMainActivity;

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
            double physical_min = -1000;
            double physical_max = 1000;
            if(channel.has("physical_min")) physical_min = Double.parseDouble(channel.getString("physical_min").trim());
            if(channel.has("physical_max")) physical_max = Double.parseDouble(channel.getString("physical_max").trim());
            int digital_min = -1000;
            int digital_max = 1000;
            if(channel.has("digital_min")) digital_min = Integer.parseInt(channel.getString("digital_min").trim());
            if(channel.has("digital_max")) digital_max = Integer.parseInt(channel.getString("digital_max").trim());
            String pre_filtering = "";
            if(channel.has("prefiltering")) pre_filtering = channel.getString("prefiltering").trim();
            String description = "";
            if(channel.has("description")) description = channel.getString("description");

            Channel s_new = new Channel(id,id_source,(int)(SensorSource.MAX_DURATION_EACH_DATA_RECORD*Float.parseFloat("10")));
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

            new_source.getChannelsOfThisSource().put(s_new.getChannel_ID(),s_new);
        }

        new_source.initBufferDataRecord();
        serverFragment.registerNewConnectionUI(new_source);

    }

    void updateSample(JSONObject jsonObj) throws JSONException{
        String id_source = jsonObj.getString("id");

        SensorSource sensorSource = serverFragment.clientConnectedList.get(id_source);
        if(sensorSource == null) return;
        long created_time = System.currentTimeMillis();

        // Getting JSON Array node
        JSONArray datas = jsonObj.getJSONArray("data");
        for (int i = 0; i < datas.length(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String id_channel = data.getString("id");
            float value = Float.parseFloat(data.getString("value").trim());

            boolean isNewDataRecord = sensorSource.addSample_true_if_createNew(id_channel,created_time,value);

            if(beNotifiedComingSample != null)
                beNotifiedComingSample.addNewSample(id_channel,value,created_time);
        }
    }

    public void setBeNotifiedComingSampleObject(BeNotifiedComingSample beNotifiedComingSample){
        this.beNotifiedComingSample = beNotifiedComingSample;
    }

    public void removeBeNotifiedComingSampleObject(){
        this.beNotifiedComingSample = null;
    }

}
