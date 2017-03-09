package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class Channel {
    //=================== DATABASE ATTRIBUTE =========
    private long ch_id;
    private String s_id;
    private String ch_nr;
    private String ch_name;
    private String transducer;
    private String dimension;
    private float phy_min;
    private float phy_max;
    private int dig_min;
    private int dig_max;
    private byte[] edf_reserved;
    //=================================================


    //================= REAL-TIME-HELP ATTRIBUTE ======
    private boolean isSelectedToSaveSample;
    private int lastXRealtime = 0;
    private String description;

    public Channel(){
    }

    public String getS_id() {
        return s_id;
    }

    public void setS_id(String s_id) {
        this.s_id = s_id;
    }

    public long getCh_id() {
        return ch_id;
    }

    public void setCh_id(long ch_id) {
        this.ch_id = ch_id;
    }

    public String getCh_name() {
        return ch_name;
    }

    public void setCh_name(String ch_name) {
        this.ch_name = ch_name;
    }

    public String getTransducer() {
        return transducer;
    }

    public void setTransducer(String transducer) {
        this.transducer = transducer;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public float getPhy_min() {
        return phy_min;
    }

    public void setPhy_min(float phy_min) {
        this.phy_min = phy_min;
    }

    public float getPhy_max() {
        return phy_max;
    }

    public void setPhy_max(float phy_max) {
        this.phy_max = phy_max;
    }

    public int getDig_min() {
        return dig_min;
    }

    public void setDig_min(int dig_min) {
        this.dig_min = dig_min;
    }

    public int getDig_max() {
        return dig_max;
    }

    public void setDig_max(int dig_max) {
        this.dig_max = dig_max;
    }

    public String getCh_nr() {
        return ch_nr;
    }

    public void setCh_nr(String ch_nr) {
        this.ch_nr = ch_nr;
    }

    public boolean isSelectedToSaveSample() {
        return isSelectedToSaveSample;
    }

    public void setSelectedToSaveSample(boolean selectedToSaveSample) {
        isSelectedToSaveSample = selectedToSaveSample;
    }

    public byte[] getEdf_reserved() {

        return edf_reserved;
    }

    public void setEdf_reserved(byte[] edf_reserved) {
        this.edf_reserved = edf_reserved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLastXRealtime() {
        return lastXRealtime;
    }

    public void setLastXRealtime(int lastXRealtime) {
        this.lastXRealtime = lastXRealtime;
    }
}
