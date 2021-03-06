package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class Patient extends Person{
    private String clinic_code;
    private float height;
    private float weight;
    private float BMI;
    private String otherHealthIssues;
    private String patient_id_in_clinic;

    public String getOtherHealthIssues() {
        return otherHealthIssues;
    }

    public void setOtherHealthIssues(String otherHealthIssues) {
        this.otherHealthIssues = otherHealthIssues;
    }

    public String getClinic_code() {
        return clinic_code;
    }

    public void setClinic_code(String clinic_code) {
        this.clinic_code = clinic_code;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getBMI() {
        return BMI;
    }

    public void setBMI(float BMI) {
        this.BMI = BMI;
    }

    public String getPatient_id_in_clinic() {
        return patient_id_in_clinic;
    }

    public void setPatient_id_in_clinic(String patient_id_in_clinic) {
        this.patient_id_in_clinic = patient_id_in_clinic;
    }
}
