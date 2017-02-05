package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by viettt on 20/12/2016.
 */

public class DataRecord {
    private long data_record_ID;
    private String source_ID;
    private String patient_ID;
    private String clinic_ID;
    private long created_date;

    private HashMap<String,Sample> samplePerChannel;

    private Sample samples_per_channel[];

    private String experiments;
    private String descriptions;

    public DataRecord(long data_record_ID, String source_ID, String patient_ID, String clinic_ID, long created_date){
        this.data_record_ID = data_record_ID;
        this.source_ID = source_ID;
        this.patient_ID = patient_ID;
        this.clinic_ID = clinic_ID;
        this.created_date = created_date;
        samplePerChannel = new HashMap<>();
    }

    public Sample[] getSamples_per_channel() {
        return samples_per_channel;
    }

    /*
         * This function will add a sample to a given channel, if full, return false
         */
    public boolean addSample(String channel_ID, float sample){
        return samplePerChannel.get(channel_ID).addSample(sample);
    }

    public boolean isFull(String channel_ID){
        return samplePerChannel.get(channel_ID).isFull();
    }

    public void initSampleChannel(HashMap<String, Channel> channels){
        for(Channel c : channels.values()){
            Sample sample = new Sample(c.getChannel_ID(), data_record_ID +"",c.getMaxSampleEachDataRecord(), c.getCoefficient());
            samplePerChannel.put(c.getChannel_ID(), sample);
        }
    }

    public long getData_record_ID() {
        return data_record_ID;
    }

    public String getSource_ID() {
        return source_ID;
    }

    public String getPatient_ID() {
        return patient_ID;
    }

    public String getClinic_ID() {
        return clinic_ID;
    }

    public long getCreated_date() {
        return created_date;
    }

    public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public String getExperiments() {
        return experiments;
    }

    public HashMap<String, Sample> getSamplePerChannel() {
        return samplePerChannel;
    }

}
