package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PatientAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ClientThread;

/**
 * Created by viettt on 20/12/2016.
 */

public class SensorSource {
    public MainActivity mainActivity;
    public static final String ACTIVESTATUS = "Active", UNACTIVESTATUS = "Disconnected", OFFLINESOURCE = "Offline source";
    public static int MAX_DURATION_EACH_DATA_RECORD = 3; //In second

    private String sensor_source_ID;
    private String recordKeyForDBSystem; //for database b/c sensor_source_ID + dateTime = A RECORD
    private String source_name;
    private String source_type;
    private long startDateTime;
    private byte[] reserved;
    private int data_record_duration;

    private DataRecord bufferDataRecord;
    private long dataRecordNr = 0;
    private HashMap<String,Channel> channelsOfThisSource;

    //Maybe register to database immediately?
    public Patient patient;
    public Clinic clinic;

    private ClientThread client_thread;
    public int logo_in_drawable;
    public String source_status;
    private boolean recFlag;

    public SensorSource(String sensor_source_ID, String source_name, String source_type){
        this.sensor_source_ID = sensor_source_ID;
        this.source_name = source_name;
        this.source_type = source_type;
        this.startDateTime = System.currentTimeMillis();
        this.recFlag = false;
        channelsOfThisSource = new HashMap<>();

        if(source_type.toLowerCase().contains("bitalino")) logo_in_drawable = R.drawable.bitalino_logo;
        else if(source_type.toLowerCase().contains("nox-t3")) logo_in_drawable = R.drawable.bitalino_logo;
        else if(source_type.toLowerCase().contains("edf")) logo_in_drawable = R.drawable.edf_logo;

        source_status = ACTIVESTATUS;
    }

    public void initBufferDataRecord(){
        bufferDataRecord = new DataRecord(dataRecordNr++,sensor_source_ID,patient.getPatient_ID(),clinic.getClinic_ID(),System.currentTimeMillis());
        bufferDataRecord.initSampleChannel(channelsOfThisSource);
    }


    public void setReferenceThread(ClientThread client_thread){
        this.client_thread = client_thread;
    }

    //If new datarecord, return true
    public boolean addSample_true_if_createNew(String id_channel, long createDate, float value){
        if(!bufferDataRecord.addSample(id_channel,value)){
            //NEED TO SAVE TO DATABASE HERE...
            //..........
            if(recFlag && source_status.equals(ACTIVESTATUS)){
                System.out.println(" SENSOR SOURCE -----> "+recordKeyForDBSystem+ " FULL DATARECORD NR:"+ dataRecordNr+" IS SENT TO DATABASE");
                //Save data record and its sample into database
            }else{
                System.out.println(" SENSOR SOURCE -----> "+recordKeyForDBSystem+ " FULL DATARECORD NR:"+ dataRecordNr+" IS DROPPED");
            }
            recordKeyForDBSystem = sensor_source_ID+System.currentTimeMillis();
            bufferDataRecord = new DataRecord(dataRecordNr++,recordKeyForDBSystem,patient.getPatient_ID(),clinic.getClinic_ID(),createDate);
            bufferDataRecord.initSampleChannel(channelsOfThisSource);
            bufferDataRecord.addSample(id_channel,value);
            return true;
        }
        return false;
    }

    public ClientThread getClient_thread() {
        return client_thread;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setReserved(byte[] reserved) {
        this.reserved = reserved;
    }

    public void setData_record_duration(int data_record_duration) {
        this.data_record_duration = data_record_duration;
    }

    public String getSensor_source_ID() {
        return sensor_source_ID;
    }

    public String getSource_name() {
        return source_name;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public int getData_record_duration() {
        return data_record_duration;
    }

    public DataRecord getbufferDataRecord() {
        return bufferDataRecord;
    }

    public HashMap<String, Channel> getChannelsOfThisSource() {
        return channelsOfThisSource;
    }

    public boolean getRecFlag(){
        return this.recFlag;
    }

    public synchronized boolean setRecFlag(boolean recFlag){
        if(recFlag){
            if(source_status.equals(UNACTIVESTATUS)) return false;
            System.out.println("Create data_record from nr 0 until stop, and begin to REC....");
            dataRecordNr = 0;
            recordKeyForDBSystem = sensor_source_ID+System.currentTimeMillis();
            bufferDataRecord = new DataRecord(dataRecordNr++,recordKeyForDBSystem,patient.getPatient_ID(),clinic.getClinic_ID(),System.currentTimeMillis());
            bufferDataRecord.initSampleChannel(channelsOfThisSource);
            //save source READ_CORD, channel, patient and clinic into database
        }else {
            System.out.println("STOP REC, RECORD HAS SAVED TO DATABASE.");
        }
        this.recFlag = recFlag;
        return true;
    }
}
