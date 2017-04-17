package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

/**
 * Created by viettt on 09/03/2017.
 */

public class BitalinoDataSample {
    private long createdDate;
    private String channel_nr;
    private float sample_data;

    public BitalinoDataSample(long createdDate, String channel_nr, float sample_data) {
        this.createdDate = createdDate;
        this.channel_nr = channel_nr;
        this.sample_data = sample_data;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getChannel_nr() {
        return channel_nr;
    }

    public void setChannel_nr(String channel_nr) {
        this.channel_nr = channel_nr;
    }

    public float getSample_data() {
        return sample_data;
    }

    public void setSample_data(float sample_data) {
        this.sample_data = sample_data;
    }
}
