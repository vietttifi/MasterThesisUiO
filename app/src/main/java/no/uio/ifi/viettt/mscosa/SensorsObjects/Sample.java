package no.uio.ifi.viettt.mscosa.SensorsObjects;


/**
 * Created by viettt on 04/01/2017.
 */

public class Sample {
    private long r_id;
    private int ch_id;
    private int fragment_index;
    private long timestamp;
    private float sample_data;
    private String sample_anno;

    public Sample(long r_id, int fragment_index, long timestamp, int ch_id, float sample_data, String sample_anno) {
        this.r_id = r_id;
        this.fragment_index = fragment_index;
        this.timestamp = timestamp;
        this.sample_data = sample_data;
        this.ch_id = ch_id;
        this.sample_anno = sample_anno;
    }

    public long getR_id() {
        return r_id;
    }

    public void setR_id(long r_id) {
        this.r_id = r_id;
    }

    public int getCh_id() {
        return ch_id;
    }

    public void setCh_id(int ch_id) {
        this.ch_id = ch_id;
    }

    public int getFragment_index() {
        return fragment_index;
    }

    public void setFragment_index(int fragment_index) {
        this.fragment_index = fragment_index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getSample_data() {
        return sample_data;
    }

    public void setSample_data(float sample_data) {
        this.sample_data = sample_data;
    }

    public String getSample_anno() {
        return sample_anno;
    }

    public void setSample_anno(String sample_anno) {
        this.sample_anno = sample_anno;
    }
}
