package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class SensorSource {

    //======= DATABASE COMPATIBLE ATTRIBUTES =======
    private String s_id;
    private String s_name;
    private String s_type;
    //==============================================

    //==========  NOT DB ATTRIBUTES ================


    public SensorSource(){
    }

    public String getS_id() {
        return s_id;
    }

    public void setS_id(String s_id) {
        this.s_id = s_id;
    }

    public String getS_name() {
        return s_name;
    }

    public void setS_name(String s_name) {
        this.s_name = s_name;
    }

    public String getS_type() {
        return s_type;
    }

    public void setS_type(String s_type) {
        this.s_type = s_type;
    }

}
