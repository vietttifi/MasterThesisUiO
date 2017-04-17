package no.uio.ifi.viettt.mscosa.SensorsObjects;


/**
 * Created by viettt on 04/01/2017.
 */

public class Sample {
    private long r_id;
    private long timestamp;
    private float sample_data;

    public Sample(long r_id, long timestamp, float sample_data) {
        this.r_id = r_id;
        this.timestamp = timestamp;
        this.sample_data = sample_data;
    }

    public long getR_id() {
        return r_id;
    }

    public void setR_id(long r_id) {
        this.r_id = r_id;
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
}
