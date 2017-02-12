package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;

/**
 * Created by viettt on 04/01/2017.
 */

public class ClinicAdapter {
    public static final String TAG = "ClinicAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.CLINIC_ID, OSADBHelper.CLINIC_CODE, OSADBHelper.CLINIC_TECHNICIAN_ID, OSADBHelper.CLINIC_ADDRESS,
            OSADBHelper.CLINIC_PHONE_NR, OSADBHelper.CLINIC_EMAIL};

    public ClinicAdapter(Context context){
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

    Clinic cursorToClinic(Cursor cursor) {
        Clinic clinic = new Clinic();
        clinic.setClinic_ID(cursor.getString(0));
        clinic.setClinic_CODE(cursor.getString(1));
        clinic.setTechnician_ID(cursor.getString(2));
        clinic.setClinic_address(cursor.getString(3));
        clinic.setPhone_nr(cursor.getString(4));
        clinic.setEmail(cursor.getString(5));
        return clinic;
    }

    public long storeNewClinic(String clinic_CODE, String technician_ID, String address,
                                 String phone_nr,String email){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.CLINIC_CODE,clinic_CODE);
        values.put(OSADBHelper.CLINIC_TECHNICIAN_ID,technician_ID);
        values.put(OSADBHelper.CLINIC_ADDRESS,address);
        values.put(OSADBHelper.CLINIC_PHONE_NR,phone_nr);
        values.put(OSADBHelper.CLINIC_EMAIL,email);

        return mDatabase.insert(OSADBHelper.TABLE_CLINIC, null, values);
    }

    public Clinic getClinicById(String clinic_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CLINIC, mAllColumns,
                OSADBHelper.CLINIC_ID + " = ?", new String[] {clinic_ID}, null, null, null);

        Clinic newClinic = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newClinic= cursorToClinic(cursor);

        }
        cursor.close();
        return newClinic;
    }

    public List<Clinic> getallClinics(){
        List<Clinic> listClinic = new ArrayList<Clinic>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CLINIC, mAllColumns,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Clinic clinic = cursorToClinic(cursor);
                listClinic.add(clinic);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listClinic;
    }

    public void deleteClinic(Clinic clinic) {
        String id = clinic.getClinic_ID();

        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.

        mDatabase.delete(OSADBHelper.TABLE_CLINIC, OSADBHelper.CLINIC_ID + " = " + id, null);
    }

    public List<Clinic> searchClinic(String searchText) {
        List<Clinic> listClinic = new ArrayList<Clinic>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CLINIC, mAllColumns,
                OSADBHelper.CLINIC_ID + " like ", new String[] {searchText}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Clinic clinic = cursorToClinic(cursor);
                listClinic.add(clinic);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listClinic;
    }
}
