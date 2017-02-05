package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;

/**
 * Created by viettt on 04/01/2017.
 */

public class PatientAdapter {
    public static final String TAG = "PatientAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.PATIENT_ID, OSADBHelper.PATIENT_CODE_IN_CLINIC, OSADBHelper.PATIENT_GENDER, OSADBHelper.PATIENT_LASTNAME,
            OSADBHelper.PATIENT_FIRSTNAME, OSADBHelper.PATIENT_DATEOFBIRTH, OSADBHelper.PATIENT_ADDRESS,
    OSADBHelper.PATIENT_PHONE_NR,OSADBHelper.PATIENT_EMAIL};

    public PatientAdapter(Context context){
        this.mContext = context;
        mDbHelper = new OSADBHelper(context);

        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void open() throws SQLException{
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close(){
        mDbHelper.close();
    }

    Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();
        patient.setPatient_ID(cursor.getString(0));
        patient.setPatient_code_in_clinic(cursor.getString(1));
        patient.setGender(cursor.getString(2));
        patient.setLastName(cursor.getString(3));
        patient.setFirstName(cursor.getString(4));
        patient.setDateOfBirth(cursor.getString(5));
        patient.setAddress(cursor.getString(6));
        patient.setPhoneNr(cursor.getString(7));
        patient.setEmail(cursor.getString(8));
        return patient;
    }

    public Patient createPatient(String patient_code_in_clinic, String gender,String last_name,
                               String first_name,String date_of_birth,
                                 String address, String phone_nr, String email){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.PATIENT_CODE_IN_CLINIC,patient_code_in_clinic);
        values.put(OSADBHelper.PATIENT_GENDER,gender);
        values.put(OSADBHelper.PATIENT_LASTNAME,last_name);
        values.put(OSADBHelper.PATIENT_FIRSTNAME,first_name);
        values.put(OSADBHelper.PATIENT_DATEOFBIRTH,date_of_birth);
        values.put(OSADBHelper.PATIENT_ADDRESS,address);
        values.put(OSADBHelper.PATIENT_PHONE_NR,phone_nr);
        values.put(OSADBHelper.PATIENT_EMAIL,email);

        long insertId = mDatabase.insert(OSADBHelper.TABLE_PATIENT, null, values);

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PATIENT, mAllColumns,
                OSADBHelper.PATIENT_ID + " = " + insertId, null, null,
                null, null);
        cursor.moveToFirst();

        Patient newPatient = cursorToPatient(cursor);
        cursor.close();
        return newPatient;
    }

    public Patient getPatientById(String patient_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PATIENT, mAllColumns,
                OSADBHelper.PATIENT_ID + " = ?", new String[] {patient_ID}, null, null, null);

        Patient newPatient = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newPatient= cursorToPatient(cursor);
            cursor.close();
        }
        return newPatient;
    }

    public List<Patient> getallPatients(){
        List<Patient> listPatient = new ArrayList<Patient>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PATIENT, mAllColumns,
                null, null, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Patient patient = cursorToPatient(cursor);
                listPatient.add(patient);
                cursor.moveToNext();
            }


        }
        //close the cursor
        cursor.close();
        return listPatient;
    }

    public void deleteRecord(Patient patient) {
        String id = patient.getPatient_ID();

        // delete all ALL RECORD belong to this PATIENT ------ TRIGGER will be called.

        mDatabase.delete(OSADBHelper.TABLE_PATIENT, OSADBHelper.PATIENT_ID + " = " + id, null);
    }

    public List<Patient> searchRecord(String searchText) {
        List<Patient> listPatient = new ArrayList<Patient>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_PATIENT, mAllColumns,
                OSADBHelper.PATIENT_ID + " like ", new String[] {searchText}, null, null, null);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Patient patient = cursorToPatient(cursor);
                listPatient.add(patient);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listPatient;
    }
}
