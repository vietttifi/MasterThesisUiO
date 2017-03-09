package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.util.ArrayList;

/**
 * Created by viettt on 21/02/2017.
 */

public class RecordFragment {
    private long r_id;
    private int index;
    private long timestamp;

    private ArrayList<Sample> samples_In_The_Same_Fragment;

    public RecordFragment(){
        samples_In_The_Same_Fragment = new ArrayList<>();
    }

    public long getR_id() {
        return r_id;
    }

    public void setR_id(long r_id) {
        this.r_id = r_id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Sample> getSamples_In_The_Same_Fragment() {
        return samples_In_The_Same_Fragment;
    }

}
