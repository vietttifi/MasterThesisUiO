package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by viettt on 04/01/2017.
 */

public class Sample {
    private String channel_id;
    private String record_id;
    private int nr_of_sample;
    private float sample_data[];
    public final int MAXSAMPLE;
    private final float coefficient;

    private byte[] sample_for_database;

    public Sample(String channel_id, String record_id, int maxSample, float coefficient){
        this.MAXSAMPLE = maxSample;
        this.channel_id = channel_id;
        this.record_id = record_id;
        this.nr_of_sample = 0;
        this.coefficient = coefficient;
        sample_data = new float[maxSample];
    }

    /**
     * This method is used to add a sample to a data_record
     * This will return false if this data record is full for that the caller can create new sample
     */
    public boolean addSample(float sample){
        if(MAXSAMPLE == nr_of_sample) return false;

        //System.out.println(" SAMPLE ---> inx:"+nr_of_sample+" value: "+sample+" channel-record_ID: "+channel_id+"-"+record_id);

        sample_data[nr_of_sample++] = sample;
        return true;
    }

    /**
     * This method will convert array of float_sample into array_of_byte that EDF compatible.
     * coefficient number is calculated by (physicalmax-physicalmin)/(digitalmax-digitalmin)
     * Therefore, digi = physical/coefficient, and physical = digi*coefficient
     */
    public byte[] sample_data_to_digit(){
        sample_for_database = new byte[nr_of_sample*2];
        ByteBuffer bb = ByteBuffer.allocate(nr_of_sample * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i < nr_of_sample; i++){
            short sample_in_digi = (short) (sample_data[i]/coefficient);
            bb.putShort(sample_in_digi);
        }
        bb.get(sample_for_database,0,sample_for_database.length);
        return sample_for_database;
    }

    /**
     * This method will convert array_of_byte that EDF compatible to float physical sample.
     *
     */
    public float[] digi_short_to_physical_float(){
        ByteBuffer bb = ByteBuffer.wrap(sample_for_database);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < sample_for_database.length/2; i++) {
            sample_data[i] = bb.getShort()*coefficient;
        }
        nr_of_sample = sample_for_database.length/2;
        return sample_data;
    }

    public boolean isFull(){
        return nr_of_sample >= MAXSAMPLE;
    }

    public void setSample_for_database(byte[] sample_for_database) {
        this.sample_for_database = sample_for_database;
    }

    public int sizeOfArraySample(){
        return nr_of_sample;
    }

    public float[] getArrayPhysicalSample(){
        return sample_data;
    }

    public byte[] getSample_for_database() {
        return sample_for_database;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public String getRecord_id() {
        return record_id;
    }

    public int getNr_of_sample() {
        return nr_of_sample;
    }
}
