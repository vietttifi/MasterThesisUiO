package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Person;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Physician;

/**
 * Created by viettt on 04/01/2017.
 */

public class PersonAdapter {
    public static final String TAG = "PersonAdapter";

    private SQLiteDatabase mDatabase;
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumnsPerson = {OSADBHelper.PERSON_ID, OSADBHelper.PERSON_NAME, OSADBHelper.PERSON_CITY, OSADBHelper.PERSON_PHONE,
            OSADBHelper.PERSON_EMAIL, OSADBHelper.PERSON_GENDER, OSADBHelper.PERSON_DAY_OF_BIRTH,
            OSADBHelper.PERSON_AGE};
    private String[] mAllColumnsPatient = {OSADBHelper.PATIENT_PER_ID, OSADBHelper.PATIENT_CLINIC_P, OSADBHelper.PATIENT_PATIENT_NR, OSADBHelper.PATIENT_HEIGHT, OSADBHelper.PATIENT_WEIGHT,
            OSADBHelper.PATIENT_BMI, OSADBHelper.PATIENT_HEALTH_ISSUES};
    private String[] mAllColumnsPhysician = {OSADBHelper.PHY_PERSON_ID, OSADBHelper.PHY_CLINIC_ID, OSADBHelper.PHY_EMPLOYEE_NR, OSADBHelper.PHY_TITLE};

    public PersonAdapter(Context context){
        OSADataBaseManager.initializeInstance(new OSADBHelper(context));
        try{
            mDbManagerInstance = OSADataBaseManager.getInstance();
            mDatabase = mDbManagerInstance.openDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void close(){
        mDbManagerInstance.closeDatabase();
    }

    void cursorToPerson(Cursor cursor, Person person) {
        person.setP_id(cursor.getString(0));
        person.setName(cursor.getString(1));
        person.setCity(cursor.getString(2));
        person.setPhone(cursor.getString(3));
        person.setEmail(cursor.getString(4));
        person.setGender(cursor.getString(5));
        person.setDayOfBirth(cursor.getString(6));
        person.setAge(cursor.getInt(7));
    }

    void cursorToPatient(Cursor cursor, Patient patient) {
        patient.setP_id(cursor.getString(0));
        patient.setClinic_code(cursor.getString(1));
        patient.setPatient_id_in_clinic(cursor.getString(2));
        patient.setHeight(cursor.getFloat(3));
        patient.setWeight(cursor.getFloat(4));
        patient.setBMI(cursor.getFloat(5));
        patient.setOtherHealthIssues(cursor.getString(6));
    }

    void cursorToPhysician(Cursor cursor, Physician physician) {
        physician.setP_id(cursor.getString(0));
        physician.setClinic_code(cursor.getString(1));
        physician.setEmployee_id(cursor.getString(2));
        physician.setTitle(cursor.getString(3));
    }

    public void storeNewPerson(Person person){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.PERSON_ID,person.getP_id());
        values.put(OSADBHelper.PERSON_NAME,person.getName());
        values.put(OSADBHelper.PERSON_CITY,person.getCity());
        values.put(OSADBHelper.PERSON_PHONE,person.getPhone());
        values.put(OSADBHelper.PERSON_EMAIL,person.getEmail());
        values.put(OSADBHelper.PERSON_GENDER,person.getGender());
        values.put(OSADBHelper.PERSON_DAY_OF_BIRTH,person.getDayOfBirth());
        values.put(OSADBHelper.PERSON_AGE,person.getEmail());
        mDatabase.insertWithOnConflict(OSADBHelper.TABLE_PERSON, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values = new ContentValues();
        if(person instanceof Patient){
            Patient p = (Patient)person;
            values.put(OSADBHelper.PATIENT_PER_ID,p.getP_id());
            values.put(OSADBHelper.PATIENT_CLINIC_P,p.getClinic_code());
            values.put(OSADBHelper.PATIENT_PATIENT_NR,p.getPatient_id_in_clinic());
            values.put(OSADBHelper.PATIENT_HEIGHT,p.getHeight());
            values.put(OSADBHelper.PATIENT_WEIGHT,p.getWeight());
            values.put(OSADBHelper.PATIENT_BMI,p.getBMI());
            values.put(OSADBHelper.PATIENT_HEALTH_ISSUES,p.getOtherHealthIssues());
            mDatabase.insertWithOnConflict(OSADBHelper.TABLE_PATIENT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } else if(person instanceof Physician){
            Physician p = (Physician)person;
            values.put(OSADBHelper.PHY_PERSON_ID,p.getP_id());
            values.put(OSADBHelper.PHY_CLINIC_ID,p.getClinic_code());
            values.put(OSADBHelper.PHY_EMPLOYEE_NR,p.getEmployee_id());
            values.put(OSADBHelper.PHY_TITLE,p.getTitle());
            mDatabase.insertWithOnConflict(OSADBHelper.TABLE_PHYSICIAN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public Person getPersonByIds(String p_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PERSON, mAllColumnsPerson,
                OSADBHelper.PERSON_ID + " = ? ", new String[] {p_id}, null, null, null);

        Person newPerson = new Person();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            cursorToPerson(cursor, newPerson);

        }
        cursor.close();
        return newPerson;
    }

    public Patient getPatientByIds(String p_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PATIENT, mAllColumnsPatient,
                OSADBHelper.PATIENT_PER_ID + " = ? ", new String[] {p_id}, null, null, null);

        Patient newPatient = new Patient();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            cursorToPatient(cursor, newPatient);

        }
        cursor.close();

        cursor = mDatabase.query(OSADBHelper.TABLE_PERSON, mAllColumnsPerson,
                OSADBHelper.PERSON_ID + " = ? ", new String[] {p_id}, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            cursorToPerson(cursor, newPatient);

        }
        return newPatient;
    }

    public Physician getPhysicianIds(String p_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PHYSICIAN, mAllColumnsPhysician,
                OSADBHelper.PHY_PERSON_ID + " = ? ", new String[] {p_id}, null, null, null);

        Physician newPhysician = new Physician();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            cursorToPhysician(cursor, newPhysician);

        }
        cursor.close();

        cursor = mDatabase.query(OSADBHelper.TABLE_PERSON, mAllColumnsPerson,
                OSADBHelper.PERSON_ID + " = ? ", new String[] {p_id}, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            cursorToPerson(cursor, newPhysician);

        }
        return newPhysician;
    }

    public void deletePerson(String p_id) {
        // delete all ALL RECORD belong to this PATIENT ------ TRIGGER will be called.

        mDatabase.delete(OSADBHelper.TABLE_PERSON, OSADBHelper.PERSON_ID + " = " + p_id, null);
    }

    public ArrayList<String> getAllPatientIDs(){
        ArrayList<String> ids = new ArrayList<>();
        String rawQuery = "SELECT DISTINCT " + OSADBHelper.PATIENT_PER_ID +
                " FROM " + OSADBHelper.TABLE_PATIENT;
        Cursor cursor = mDatabase.rawQuery(rawQuery, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                ids.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return ids;
    }

    public ArrayList<String> getAllPhysicianIDs(){
        ArrayList<String> ids = new ArrayList<>();
        String rawQuery = "SELECT DISTINCT " + OSADBHelper.PHY_PERSON_ID +
                " FROM " + OSADBHelper.TABLE_PHYSICIAN;
        Cursor cursor = mDatabase.rawQuery(rawQuery, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                ids.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return ids;
    }

}
