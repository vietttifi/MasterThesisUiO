package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 21/02/2017.
 */

public class Physician extends Person{
    private String clinic_code;
    private String title;

    public String getClinic_code() {
        return clinic_code;
    }

    public void setClinic_code(String clinic_code) {
        this.clinic_code = clinic_code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
