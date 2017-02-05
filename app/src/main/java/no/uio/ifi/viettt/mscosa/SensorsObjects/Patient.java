package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 20/12/2016.
 */

public class Patient {
    private String patient_ID;
    private String patient_code_in_clinic;
    private String gender;
    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private String address;
    private String phoneNr;
    private String email;

    public Patient(){

    }

    public void setPatient_ID(String patient_ID) {
        this.patient_ID = patient_ID;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPatient_ID() {
        return patient_ID;
    }

    public String getGender() {
        return gender;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public String getEmail() {
        return email;
    }

    public String getPatient_code_in_clinic() {
        return patient_code_in_clinic;
    }

    public void setPatient_code_in_clinic(String patient_code_in_clinic) {
        this.patient_code_in_clinic = patient_code_in_clinic;
    }
}
