package no.uio.ifi.viettt.mscosa.SensorsObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viettt on 20/12/2016.
 */

public class Record {
    private long r_id;
    private String s_id;
    private int ch_nr;
    private String physician_id;
    private String patient_id;
    private long timestamp;
    private String descriptions;
    private float frequency;
    private String used_equip;
    private byte[] edf_reserved;

    public Record(){}

    public long getR_id() {
        return r_id;
    }

    public void setR_id(long r_id) {
        this.r_id = r_id;
    }

    public String getS_id() {
        return s_id;
    }

    public void setS_id(String s_id) {
        this.s_id = s_id;
    }

    public String getPhysician_id() {
        return physician_id;
    }

    public void setPhysician_id(String physician_id) {
        this.physician_id = physician_id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public String getUsed_equip() {
        return used_equip;
    }

    public void setUsed_equip(String used_equip) {
        this.used_equip = used_equip;
    }

    public byte[] getEdf_reserved() {
        return edf_reserved;
    }

    public void setEdf_reserved(byte[] edf_reserved) {
        this.edf_reserved = edf_reserved;
    }

    public int getCh_nr() {
        return ch_nr;
    }

    public void setCh_nr(int ch_nr) {
        this.ch_nr = ch_nr;
    }

}
