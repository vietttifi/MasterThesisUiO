package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class Clinic {
    private String clinic_ID;
    private String clinic_CODE;
    private String technician_ID;
    private String used_equipment;
    private String clinic_address;
    private String phone_nr;
    private String email;

    public Clinic(){

    }

    public String getClinic_ID() {
        return clinic_ID;
    }

    public void setClinic_ID(String clinic_ID) {
        this.clinic_ID = clinic_ID;
    }

    public String getClinic_CODE() {
        return clinic_CODE;
    }

    public void setClinic_CODE(String clinic_CODE) {
        this.clinic_CODE = clinic_CODE;
    }

    public String getTechnician_ID() {
        return technician_ID;
    }

    public void setTechnician_ID(String technician_ID) {
        this.technician_ID = technician_ID;
    }

    public String getClinic_address() {
        return clinic_address;
    }

    public void setClinic_address(String clinic_address) {
        this.clinic_address = clinic_address;
    }

    public String getPhone_nr() {
        return phone_nr;
    }

    public void setPhone_nr(String phone_nr) {
        this.phone_nr = phone_nr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsed_equipment() {
        return used_equipment;
    }

    public void setUsed_equipment(String used_equipment) {
        this.used_equipment = used_equipment;
    }
}
