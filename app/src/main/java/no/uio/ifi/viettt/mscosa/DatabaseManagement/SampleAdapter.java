package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;


/**
 * Created by viettt on 04/01/2017.
 */

public class SampleAdapter {
    public static final String TAG = "SampleAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.SAMPLE_CHANNEL_ID,
            OSADBHelper.SAMPLE_DATA_RECORD_ID, OSADBHelper.SAMPLE_MAX_SAMPLE, OSADBHelper.SAMPLE_COEFFICIENT,
            OSADBHelper.SAMPLE_DATA};

    public SampleAdapter(Context context){
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

    Sample cursorToSample(Cursor cursor) {
        Sample sample = new Sample(cursor.getString(0),cursor.getString(1),cursor.getInt(2),cursor.getFloat(3));
        sample.setSample_for_database(cursor.getBlob(4));
        sample.digi_short_to_physical_float();
        return sample;
    }

    public Sample createSample(String channel_id, String record_id, int maxSample, float coefficient, byte[] sample_data){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.SAMPLE_CHANNEL_ID,channel_id);
        values.put(OSADBHelper.SAMPLE_DATA_RECORD_ID,record_id);
        values.put(OSADBHelper.SAMPLE_MAX_SAMPLE,maxSample);
        values.put(OSADBHelper.SAMPLE_COEFFICIENT,coefficient);
        values.put(OSADBHelper.SAMPLE_DATA,sample_data);

        long insertId = mDatabase.insert(OSADBHelper.TABLE_SAMPLE, null, values);

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SAMPLE, mAllColumns,
                OSADBHelper.SAMPLE_CHANNEL_ID + " = " + channel_id + " AND "+OSADBHelper.SAMPLE_DATA_RECORD_ID+ " = " + record_id, null, null,
                null, null);
        cursor.moveToFirst();

        Sample newSample = cursorToSample(cursor);
        cursor.close();
        return newSample;
    }

    public Sample getSampleById(String channel_id, String record_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SAMPLE, mAllColumns,
                OSADBHelper.SAMPLE_CHANNEL_ID + " = " + channel_id + " AND "+OSADBHelper.SAMPLE_DATA_RECORD_ID+ " = " + record_id, null, null,
                null, null);

        if(cursor == null) return null;

        Sample newSample = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            newSample = cursorToSample(cursor);

        }
        cursor.close();
        return newSample;
    }

    public List<Sample> getallPatients(){
        List<Sample> listSample = new ArrayList<Sample>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SAMPLE, mAllColumns,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Sample dataRecord = cursorToSample(cursor);
                listSample.add(dataRecord);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listSample;
    }

    public void deleteRecord(Sample dataRecord) {
        String channel_id = dataRecord.getChannel_id();
        String record_id = dataRecord.getRecord_id();
        mDatabase.delete(OSADBHelper.TABLE_SAMPLE, OSADBHelper.SAMPLE_CHANNEL_ID + " = " + channel_id + " AND "+OSADBHelper.SAMPLE_DATA_RECORD_ID+ " = " + record_id, null);
    }
}
