package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ABITalinoData;

/**
 * Created by viettt on 04/01/2017.
 */

public class SampleSet {
    //MAIN ATTRIBUTES FOR DATABASE
    private String source_id;
    private String channel_id;
    private String record_id;
    private String patient_id;
    private String clinic_id;
    private byte[] samples;
    private int maxSample = 0;

    //HELP ATTRIBUTES FOR OTHER PURPOSE
    private int nr_of_sample;
    private float sample_data[];


    public SampleSet(String source_id, String channel_id, String record_id, String patient_id, String clinic_id, int maxSample){
        this.source_id = source_id;
        this.channel_id = channel_id;
        this.record_id = record_id;
        this.patient_id = patient_id;
        this.clinic_id = clinic_id;
        sample_data = new float[maxSample];
        this.maxSample = maxSample;
    }

    public void addABITalinoSample(ABITalinoData abiTalinoData){
        if(nr_of_sample < sample_data.length)
            sample_data[nr_of_sample++] = Float.parseFloat(abiTalinoData.getData().get(channel_id));
    }

    /**
     * This method will convert array of float_sample into array_of_byte that EDF compatible.
     * coefficient number is calculated by (physicalmax-physicalmin)/(digitalmax-digitalmin)
     * Therefore, digi = physical/coefficient, and physical = digi*coefficient
     */
    public byte[] sample_data_to_digit(){
        samples = new byte[nr_of_sample*2];
        ByteBuffer bb = ByteBuffer.allocate(nr_of_sample * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i < nr_of_sample; i++){
            short sample_in_digi = (short) (sample_data[i]/1);
            bb.putShort(sample_in_digi);
        }
        bb.rewind();
        bb.get(samples,0,samples.length);
        return samples;
    }

    /**
     * This method will convert array_of_byte that EDF compatible to float physical sample.
     *
     */
    public float[] digi_short_to_physical_float(){

        ByteBuffer bb = ByteBuffer.wrap(samples);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < samples.length/2; i++) {
            sample_data[i] = bb.getShort();
        }
        nr_of_sample = samples.length/2;
        return sample_data;
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getClinic_id() {
        return clinic_id;
    }

    public void setClinic_id(String clinic_id) {
        this.clinic_id = clinic_id;
    }

    public byte[] getSamples() {
        sample_data_to_digit();
        return samples;
    }

    public void setSamples(byte[] samples) {
        this.samples = samples;
    }

    public int getNr_of_sample() {
        return nr_of_sample;
    }

    public void setNr_of_sample(int nr_of_sample) {
        this.nr_of_sample = nr_of_sample;
    }

    public float[] getSample_data() {
        return sample_data;
    }

    public void setSample_data(float[] sample_data) {
        this.sample_data = sample_data;
    }
}
