package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class Channel {
    private String channel_key;
    private String channel_ID;
    private String source_ID;
    private int maxSampleEachDataRecord;
    private String channel_name;
    private String transducer_type;
    private String physical_dimension;
    private double physical_min;
    private double physical_max;
    private int digital_min;
    private int digital_max;
    private String prefiltering;
    private byte[] reserved;

    private String description;

    public float frequence;

    public int nrOfsample;

    public Channel(String channel_ID, String source_ID, int maxSampleEachDataRecord){
        this.channel_ID = channel_ID;
        this. source_ID = source_ID;
        this.maxSampleEachDataRecord = maxSampleEachDataRecord;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public void setTransducer_type(String transducer_type) {
        this.transducer_type = transducer_type;
    }

    public void setPhysical_dimension(String physical_dimension) {
        this.physical_dimension = physical_dimension;
    }

    public float getCoefficient(){
        if(digital_max == digital_min) return 1;
        if((physical_max-physical_min)/(digital_max-digital_min) == 0) return 1;
        else return (float) (physical_max-physical_min)/(digital_max-digital_min);
    }

    public void setPhysical_min(double physical_min) {
        this.physical_min = physical_min;
    }

    public void setPhysical_max(double physical_max) {
        this.physical_max = physical_max;
    }

    public void setDigital_min(int digital_min) {
        this.digital_min = digital_min;
    }

    public void setDigital_max(int digital_max) {
        this.digital_max = digital_max;
    }

    public void setPrefiltering(String prefiltering) {
        this.prefiltering = prefiltering;
    }

    public void setReserved(byte[] reserved) {
        this.reserved = reserved;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannel_ID() {
        return channel_ID;
    }

    public String getSource_ID() {
        return source_ID;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public String getTransducer_type() {
        return transducer_type;
    }

    public String getPhysical_dimension() {
        return physical_dimension;
    }

    public double getPhysical_min() {
        return physical_min;
    }

    public double getPhysical_max() {
        return physical_max;
    }

    public int getDigital_min() {
        return digital_min;
    }

    public int getDigital_max() {
        return digital_max;
    }

    public int getMaxSampleEachDataRecord() {
        return maxSampleEachDataRecord;
    }

    public void setMaxSampleEachDataRecord(int maxSampleEachDataRecord) {
        this.maxSampleEachDataRecord = maxSampleEachDataRecord;
    }

    public String getPrefiltering() {
        return prefiltering;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public String getDescription() {
        return description;
    }

    public String getChannel_key() {
        return channel_key;
    }

    public void setChannel_key(String channel_key) {
        this.channel_key = channel_key;
    }
}
