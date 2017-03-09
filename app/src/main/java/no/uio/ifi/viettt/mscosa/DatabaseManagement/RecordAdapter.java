package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;

/**
 * Created by viettt on 04/01/2017.
 */

public class RecordAdapter {
    public static final String TAG = "DataRecordAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.RECORD_ID, OSADBHelper.RECORD_S_ID, OSADBHelper.RECORD_PHYSICIAN_ID, OSADBHelper.RECORD_PATIENT_ID, OSADBHelper.RECORD_TIMESTAMP,
            OSADBHelper.RECORD_DESCRIPTIONS, OSADBHelper.RECORD_FRAGMENT_DURATION, OSADBHelper.RECORD_FREQUENCY, OSADBHelper.RECORD_PREFILTERING,
            OSADBHelper.RECORD_USED_EQUIPMENT, OSADBHelper.RECORD_EDF_RESERVED};

    public RecordAdapter(Context context){
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

    public Record cursorToRecord(Cursor cursor) {
        Record record = new Record();
        record.setR_id(cursor.getLong(0));
        record.setS_id(cursor.getString(1));
        record.setPhysician_id(cursor.getString(2));
        record.setPatient_id(cursor.getString(3));
        record.setTimestamp(cursor.getLong(4));
        record.setDescriptions(cursor.getString(5));
        record.setFrag_duration(cursor.getLong(6));
        record.setFrequency(cursor.getFloat(7));
        record.setPrefiltering(cursor.getString(8));
        record.setUsed_equip(cursor.getString(9));
        record.setEdf_reserved(cursor.getBlob(10));

        return record;
    }

    public long saveRecordToDB(Record record){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.RECORD_S_ID,record.getS_id());
        values.put(OSADBHelper.RECORD_PHYSICIAN_ID,record.getPhysician_id());
        values.put(OSADBHelper.RECORD_PATIENT_ID,record.getPatient_id());
        values.put(OSADBHelper.RECORD_TIMESTAMP,record.getTimestamp());
        values.put(OSADBHelper.RECORD_DESCRIPTIONS,record.getDescriptions());
        values.put(OSADBHelper.RECORD_FRAGMENT_DURATION,record.getFrag_duration());
        values.put(OSADBHelper.RECORD_FREQUENCY,record.getFrequency());
        values.put(OSADBHelper.RECORD_PREFILTERING,record.getPrefiltering());
        values.put(OSADBHelper.RECORD_USED_EQUIPMENT,record.getUsed_equip());
        values.put(OSADBHelper.RECORD_EDF_RESERVED,record.getEdf_reserved());

        return mDatabase.insert(OSADBHelper.TABLE_RECORD, null, values);
    }

    public Record getRecordById(int r_id) {
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

    public Record getRecordById(String source_id, String physician_id, String patient_id, long timestamp) {
        String condition = OSADBHelper.RECORD_S_ID + " = ? AND "+OSADBHelper.RECORD_PHYSICIAN_ID + " = ? AND "+ OSADBHelper.RECORD_PATIENT_ID + " = ? AND "+
                OSADBHelper.RECORD_TIMESTAMP + " = ? ";
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD, mAllColumns, condition,
                new String[] {source_id, physician_id, patient_id, String.valueOf(timestamp)}, null, null, null);

        Record newRecord = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newRecord = cursorToRecord(cursor);
        }
        cursor.close();
        return newRecord;
    }

    public void deleteRecord(String r_id) {
        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_RECORD, OSADBHelper.RECORD_ID + " = " + r_id, null);
    }
}
