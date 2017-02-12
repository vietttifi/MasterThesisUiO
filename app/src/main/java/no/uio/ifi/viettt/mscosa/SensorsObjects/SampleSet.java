package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
    private int maxSample = 0;

    //HELP ATTRIBUTES FOR OTHER PURPOSE
    private int nr_of_sample;
    private float sample_data[];

    private byte[] samples;


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

    public byte[] floatSamplesToBytes(){
        ByteBuffer bb = ByteBuffer.allocate(maxSample*2);
        for(int i = 0; i < sample_data.length; i++){
            bb.putShort((short)sample_data[i]);
        }
        bb.rewind();
        samples = new byte[maxSample*2];
        bb.get(samples);
        return samples;
    }

    public float[] samplesByteToFloat(boolean isConvert, float yMin, float accuracy, boolean sign){
        sample_data = new float[maxSample];
        ByteBuffer bb = ByteBuffer.wrap(samples);

        if(isConvert){
            for(int i = 0; i < maxSample; i++){
                if(sign) sample_data[i] = (-1)*(float) (yMin*Math.exp((-1)*accuracy*bb.getShort()));
                else sample_data[i] = (float) (yMin*Math.exp(accuracy*bb.getShort()));
            }
        }else{
            for(int i = 0; i < maxSample; i++){
                sample_data[i] = bb.getShort();
            }
        }
        return sample_data;
    }

    public void stringDataToFloatArray(String samples){
        String ar[] = null;
        if(samples.indexOf(0) == '['){
            ar = samples.substring(1,samples.length()-1).split(", ");
        }else{
            ar = samples.split(", ");
        }

        sample_data = new float[ar.length];
        for(int i = 0; i < ar.length; i++){
            try{
                sample_data[i] = Float.parseFloat(ar[i]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    //convert float to 2 bytes int
    public byte[] getSamples(){
        if(samples == null) floatSamplesToBytes();
        return samples;
    }

    public String getSource_id() {
        return source_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public String getRecord_id() {
        return record_id;
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

    public void setSamples(byte[] samples) {
        this.samples = samples;
    }


}
