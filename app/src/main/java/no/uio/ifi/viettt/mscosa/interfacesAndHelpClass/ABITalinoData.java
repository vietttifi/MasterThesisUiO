package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import java.util.HashMap;

/**
 * Created by viettt on 07/02/2017.
 */

public class ABITalinoData {
    private long time;
    private HashMap<String, String> data;

    public ABITalinoData(long time){
        this.time = time;
        data = new HashMap<>();
    }

    public void addDataSample(String channel_id, String value){
        data.put(channel_id,value);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
}
