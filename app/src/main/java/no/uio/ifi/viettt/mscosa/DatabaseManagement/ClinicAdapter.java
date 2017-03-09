package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;

/**
 * Created by viettt on 04/01/2017.
 */

public class ClinicAdapter {
    public static final String TAG = "ClinicAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.CLINIC_ID, OSADBHelper.CLINIC_NAME, OSADBHelper.CLINIC_ADDRESS, OSADBHelper.CLINIC_PHONE_NR, OSADBHelper.CLINIC_EMAIL};

    public ClinicAdapter(Context context){
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
        clinic.setCl_id(cursor.getString(0));
        clinic.setName(cursor.getString(1));
        clinic.setAddress(cursor.getString(2));
        clinic.setPhone_nr(cursor.getString(3));
        clinic.setEmail(cursor.getString(4));

        return clinic;
    }

    public void storeNewClinic(Clinic clinic){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.CLINIC_ID,clinic.getCl_id());
        values.put(OSADBHelper.CLINIC_NAME,clinic.getName());
        values.put(OSADBHelper.CLINIC_ADDRESS,clinic.getAddress());
        values.put(OSADBHelper.CLINIC_PHONE_NR,clinic.getPhone_nr());
        values.put(OSADBHelper.CLINIC_EMAIL,clinic.getEmail());

        mDatabase.insertWithOnConflict(OSADBHelper.TABLE_CLINIC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Clinic getClinicByIds(String cl_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CLINIC, mAllColumns,
                OSADBHelper.CLINIC_ID + " = ? ", new String[] {cl_id}, null, null, null);

        Clinic newClinic = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newClinic= cursorToClinic(cursor);

        }
        cursor.close();
        return newClinic;
    }

    public void deleteClinic(String cl_id) {
        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_CLINIC, OSADBHelper.CLINIC_ID + " = " + cl_id, null);
    }

    public ArrayList<String> getAllClinicIDs(){
        ArrayList<String> ids = new ArrayList<>();
        String rawQuery = "SELECT " + OSADBHelper.CLINIC_ID +
                " FROM " + OSADBHelper.TABLE_CLINIC;
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
