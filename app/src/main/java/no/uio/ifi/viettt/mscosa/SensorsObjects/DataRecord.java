package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viettt on 20/12/2016.
 */

public class DataRecord {
    //ATTRIBUTES FOR DATABASE
    private long data_record_ID;
    private String source_ID;
    private String patient_ID;
    private String clinic_ID;
    private long created_date;
    private String experiments;
    private String descriptions;
    private int max_sample;
    private int cnt_sample;

    //ASSISTANT ATTRIBUTES
    private List<SampleSet> sampleSetList;

    public DataRecord(long data_record_ID, String source_ID, String patient_ID, String clinic_ID, long created_date){
        this.data_record_ID = data_record_ID;
        this.source_ID = source_ID;
        this.patient_ID = patient_ID;
        this.clinic_ID = clinic_ID;
        this.created_date = created_date;
        sampleSetList = new ArrayList<>();
    }

    public DataRecord(long data_record_ID, String source_ID, String patient_ID,
                      String clinic_ID, long created_date, int max_sample){
        this.data_record_ID = data_record_ID;
        this.source_ID = source_ID;
        this.patient_ID = patient_ID;
        this.clinic_ID = clinic_ID;
        this.created_date = created_date;
        this.max_sample = max_sample;
        sampleSetList = new ArrayList<>();
    }

    public void initSampleSet(List<String> channelIDs){
        for(String s : channelIDs){
            SampleSet ss = new SampleSet(source_ID,s,data_record_ID+"",patient_ID,clinic_ID,max_sample);
            getSampleSetList().add(ss);
        }
    }

    public long getData_record_ID() {
        return data_record_ID;
    }

    public void setData_record_ID(long data_record_ID) {
        this.data_record_ID = data_record_ID;
    }

    public String getSource_ID() {
        return source_ID;
    }

    public void setSource_ID(String source_ID) {
        this.source_ID = source_ID;
    }

    public String getPatient_ID() {
        return patient_ID;
    }

    public void setPatient_ID(String patient_ID) {
        this.patient_ID = patient_ID;
    }

    public String getClinic_ID() {
        return clinic_ID;
    }

    public void setClinic_ID(String clinic_ID) {
        this.clinic_ID = clinic_ID;
    }

    public long getCreated_date() {
        return created_date;
    }

    public void setCreated_date(long created_date) {
        this.created_date = created_date;
    }

    public String getExperiments() {
        return experiments;
    }

    public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public int getMax_sample() {
        return max_sample;
    }

    public void setMax_sample(int max_sample) {
        this.max_sample = max_sample;
    }

    public boolean isNOTDataRecordFull(){
        return cnt_sample < max_sample;
    }

    public void countUpSample(){
        cnt_sample++;
    }

    public List<SampleSet> getSampleSetList() {
        return sampleSetList;
    }

    public void setSampleSetList(List<SampleSet> sampleSetList) {
        this.sampleSetList = sampleSetList;
    }
}
