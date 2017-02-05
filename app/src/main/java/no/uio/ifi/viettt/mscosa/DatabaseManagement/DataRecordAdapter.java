package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;

/**
 * Created by viettt on 04/01/2017.
 */

public class DataRecordAdapter {
    public static final String TAG = "DataRecordAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.DATA_RECORD_KEY, OSADBHelper.DATA_RECORD_ID, OSADBHelper.DATA_RECORD_SOURCE_ID, OSADBHelper.DATA_RECORD_PATIENT_ID,
            OSADBHelper.DATA_RECORD_CLINIC_ID, OSADBHelper.DATA_RECORD_CREATEDATE,
            OSADBHelper.DATA_RECORD_EXPERIMENTS, OSADBHelper.DATA_RECORD_DESCRIPTIONS};

    public DataRecordAdapter(Context context){
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

    DataRecord cursorToRecord(Cursor cursor) {
        DataRecord dataRecord = new DataRecord(cursor.getLong(0),cursor.getString(1),cursor.getString(2),
                cursor.getString(3),cursor.getLong(4));
        dataRecord.setExperiments(cursor.getString(5));
        dataRecord.setDescriptions(cursor.getString(6));
        return dataRecord;
    }

    public DataRecord createRecord(long data_record_id, String source_id,
                                   String patient_id, String clinic_id, long createDate,
                                   String experiments, String descriptions){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.DATA_RECORD_ID,data_record_id);
        values.put(OSADBHelper.DATA_RECORD_SOURCE_ID,source_id);
        values.put(OSADBHelper.DATA_RECORD_PATIENT_ID,patient_id);
        values.put(OSADBHelper.DATA_RECORD_CLINIC_ID,clinic_id);
        values.put(OSADBHelper.DATA_RECORD_CREATEDATE,createDate);
        values.put(OSADBHelper.DATA_RECORD_EXPERIMENTS,experiments);
        values.put(OSADBHelper.DATA_RECORD_DESCRIPTIONS,descriptions);

        long insertId = mDatabase.insert(OSADBHelper.TABLE_DATA_RECORD, null, values);

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_DATA_RECORD, mAllColumns,
                OSADBHelper.DATA_RECORD_ID + " = " + data_record_id, null, null,
                null, null);
        cursor.moveToFirst();

        DataRecord newDataRecord = cursorToRecord(cursor);
        cursor.close();
        return newDataRecord;
    }

    public DataRecord getRecordById(String record_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_DATA_RECORD, mAllColumns,
                OSADBHelper.DATA_RECORD_ID + " = ?", new String[] {record_ID}, null, null, null);

        DataRecord newDataRecord = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newDataRecord = cursorToRecord(cursor);

        }
        cursor.close();
        return newDataRecord;
    }

    public List<DataRecord> getallRecords(){
        List<DataRecord> listDataRecord = new ArrayList<DataRecord>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_DATA_RECORD, mAllColumns,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DataRecord dataRecord = cursorToRecord(cursor);
                listDataRecord.add(dataRecord);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listDataRecord;
    }

    public void deleteRecord(DataRecord dataRecord) {
        long id = dataRecord.getData_record_ID();

        // delete all ALL SAMPLE belong to this RECORD ------ TRIGGER will be called.

        mDatabase.delete(OSADBHelper.TABLE_DATA_RECORD, OSADBHelper.DATA_RECORD_ID + " = " + id, null);
    }

    public List<DataRecord> searchRecord(String searchText) {
        List<DataRecord> listDataRecord = new ArrayList<DataRecord>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_DATA_RECORD, mAllColumns,
                OSADBHelper.DATA_RECORD_ID + " like ", new String[] {searchText}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DataRecord dataRecord = cursorToRecord(cursor);
                listDataRecord.add(dataRecord);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listDataRecord;
    }
}
