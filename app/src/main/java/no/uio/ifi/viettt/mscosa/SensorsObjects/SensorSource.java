package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ClientThread;

/**
 * Created by viettt on 20/12/2016.
 */

public class SensorSource {
    //ATTRIBUTES FOR DATABASE
    private String source_id; //for database b/c source_type + dateTime = A RECORD
    private String source_name;
    private String source_type;
    private long startDateTime;
    private byte[] reserved;
    private int data_record_duration = 3; //default value is 30s in case the users do not give it

    //ASSISTANT ATTRIBUTES
    public MainActivity mainActivity;
    public static final String ACTIVESTATUS = "Active", UNACTIVESTATUS = "Disconnected", OFFLINESOURCE = "Offline source";

    private DataRecord bufferDataRecord;
    private long dataRecordNr = 0;

    //Maybe register to database immediately?
    public Patient patient;
    public Clinic clinic;

    private ClientThread client_thread;
    public int logo_in_drawable;
    public String source_status;
    private boolean recFlag;

    public SensorSource(String source_name, String source_type){
        this.source_name = source_name;
        this.source_type = source_type;
        this.startDateTime = System.currentTimeMillis();
        this.recFlag = false;

        if(source_type.toLowerCase().contains("bitalino")) logo_in_drawable = R.drawable.bitalino_logo;
        else if(source_type.toLowerCase().contains("edf")) logo_in_drawable = R.drawable.edf_logo;

        source_status = OFFLINESOURCE;
    }

    public SensorSource(String source_name, String source_type, ClientThread clientThread){
        this.source_name = source_name;
        this.source_type = source_type;
        this.startDateTime = System.currentTimeMillis();
        this.recFlag = false;
        this.client_thread = clientThread;

        if(source_type.toLowerCase().contains("bitalino")) logo_in_drawable = R.drawable.bitalino_logo;
        else if(source_type.toLowerCase().contains("edf")) logo_in_drawable = R.drawable.edf_logo;

        source_status = ACTIVESTATUS;
    }

    public void setRecFlag(boolean recFlag, String patient_id, String clinic_id, List<String> channelIDs){
        if(client_thread != null){
            client_thread.setRec(recFlag,patient_id, clinic_id, channelIDs);
            this.recFlag = recFlag;
        }
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
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

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public void setReserved(byte[] reserved) {
        this.reserved = reserved;
    }

    public int getData_record_duration() {
        return data_record_duration;
    }

    public void setData_record_duration(int data_record_duration) {
        this.data_record_duration = data_record_duration;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public ClientThread getClient_thread() {
        return client_thread;
    }

    public String getSource_status() {
        return source_status;
    }

    public void setSource_status(String source_status) {
        this.source_status = source_status;
    }

    public boolean isRecFlag() {
        return recFlag;
    }

    public void closeConnection(){
        client_thread.closeConnection();
    }

}
