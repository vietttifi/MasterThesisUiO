package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;

/**
 * Created by viettt on 04/01/2017.
 */

public class RecordAdapter {
    public static final String TAG = "DataRecordAdapter";

    private SQLiteDatabase mDatabase;
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumns = {OSADBHelper.RECORD_ID, OSADBHelper.RECORD_TIMESTAMP,
            OSADBHelper.RECORD_S_ID, OSADBHelper.RECORD_CH_NR,
            OSADBHelper.RECORD_PHYSICIAN_ID, OSADBHelper.RECORD_PATIENT_ID,
            OSADBHelper.RECORD_DESCRIPTIONS, OSADBHelper.RECORD_FREQUENCY,
            OSADBHelper.RECORD_USED_EQUIPMENT, OSADBHelper.RECORD_EDF_RESERVED};

    public RecordAdapter(Context context){
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

    public void updateRecordTimestamp(long r_id, long timestamp, float frequency){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.RECORD_TIMESTAMP,timestamp);
        values.put(OSADBHelper.RECORD_FREQUENCY,frequency);
        mDatabase.updateWithOnConflict(OSADBHelper.TABLE_RECORD,values,OSADBHelper.RECORD_ID + " = " + r_id,null,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Record cursorToRecord(Cursor cursor) {
        Record record = new Record();
        record.setR_id(cursor.getLong(0));
        record.setTimestamp(cursor.getLong(1));
        record.setS_id(cursor.getString(2));
        record.setCh_nr(cursor.getInt(3));
        record.setPhysician_id(cursor.getString(4));
        record.setPatient_id(cursor.getString(5));
        record.setDescriptions(cursor.getString(6));
        record.setFrequency(cursor.getFloat(7));
        record.setUsed_equip(cursor.getString(8));
        record.setEdf_reserved(cursor.getBlob(9));

        return record;
    }

    public long saveRecordToDB(Record record){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.RECORD_TIMESTAMP,record.getTimestamp());
        values.put(OSADBHelper.RECORD_S_ID,record.getS_id());
        values.put(OSADBHelper.RECORD_CH_NR,record.getCh_nr());
        values.put(OSADBHelper.RECORD_PHYSICIAN_ID,record.getPhysician_id());
        values.put(OSADBHelper.RECORD_PATIENT_ID,record.getPatient_id());
        values.put(OSADBHelper.RECORD_DESCRIPTIONS,record.getDescriptions());
        values.put(OSADBHelper.RECORD_FREQUENCY,record.getFrequency());
        values.put(OSADBHelper.RECORD_USED_EQUIPMENT,record.getUsed_equip());
        values.put(OSADBHelper.RECORD_EDF_RESERVED,record.getEdf_reserved());

        return mDatabase.insertWithOnConflict(OSADBHelper.TABLE_RECORD, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Record getRecordById(long r_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD, mAllColumns,
                OSADBHelper.RECORD_ID + " = ? ",
                new String[] {String.valueOf(r_id)}, null, null, null);

        Record newRecord = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newRecord = cursorToRecord(cursor);
        }
        cursor.close();
        return newRecord;
    }

    public ArrayList<Record> getListRecordByListIds(ArrayList<String> recordIds){
        ArrayList<Record> records = new ArrayList<>();
        for(String rid : recordIds){
            Record r = getRecordById(Long.parseLong(rid));
            records.add(r);
        }
        return records;
    }

    public ArrayList<Record> getAllRecordForSourse(String s_id, long timestamp){
        ArrayList<Record> records = new ArrayList<>();
        String condition = OSADBHelper.RECORD_S_ID + " = ? AND "
                + OSADBHelper.RECORD_TIMESTAMP + " = ? ";
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD, mAllColumns, condition,
                new String[] {s_id, String.valueOf(timestamp)}, null, null, null);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                records.add(cursorToRecord(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return records;
    }

    public Record getRecordById(String source_id, String physician_id, String patient_id, int channel_nr, long timestamp) {
        String condition = OSADBHelper.RECORD_S_ID + " = ? AND "
                + OSADBHelper.RECORD_PHYSICIAN_ID + " = ? AND "
                + OSADBHelper.RECORD_PATIENT_ID + " = ? AND "
                + OSADBHelper.CHANNEL_NR + " = ? AND "
                + OSADBHelper.RECORD_TIMESTAMP + " = ? ";
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD, mAllColumns, condition,
                new String[] {source_id, physician_id, patient_id, String.valueOf(channel_nr), String.valueOf(timestamp)}, null, null, null);

        Record newRecord = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newRecord = cursorToRecord(cursor);
        }
        cursor.close();
        return newRecord;
    }

    public float getRecordMaxSample(long record_id){
        float maxSample = 0;
        String queryString = "SELECT MAX("+OSADBHelper.SAMPLE_VALUE+") " +
                "FROM "+OSADBHelper.TABLE_SAMPLE+ " WHERE "+OSADBHelper.SAMPLE_RECORD_ID +" = "+record_id;
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            maxSample = cursor.getFloat(0);
        }
        cursor.close();
        return maxSample;
    }

    public float getRecordMinSample(long record_id){
        float minSample = 0;
        String queryString = "SELECT MIN("+OSADBHelper.SAMPLE_VALUE+") " +
                "FROM "+OSADBHelper.TABLE_SAMPLE+ " WHERE "+OSADBHelper.SAMPLE_RECORD_ID +" = "+record_id;
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            minSample = cursor.getFloat(0);
        }
        cursor.close();
        return minSample;
    }

    public void deleteRecord(String r_id) {
        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_RECORD, OSADBHelper.RECORD_ID + " = " + r_id,null);
    }
}
