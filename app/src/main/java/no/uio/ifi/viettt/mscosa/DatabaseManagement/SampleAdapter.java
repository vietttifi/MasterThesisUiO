package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;


/**
 * Created by viettt on 04/01/2017.
 */

public class SampleAdapter {
    public static final String TAG = "SampleAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.SAMPLE_RECORD_ID, OSADBHelper.SAMPLE_RECORD_FRAGMENT_INDEX, OSADBHelper.SAMPLE_TIMESTAMP,
            OSADBHelper.SAMPLE_CHANNEL_ID, OSADBHelper.SAMPLE_VALUE_FLOAT, OSADBHelper.SAMPLE_VALUE_ANNO};

    public SampleAdapter(Context context){
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

    public Sample cursorToSample(Cursor cursor) {
        return new Sample(cursor.getLong(0),cursor.getInt(1),cursor.getLong(2),cursor.getInt(3),cursor.getFloat(4), cursor.getString(5));
    }

    public void saveSampleToDB(List<Sample> listSample){
        mDatabase.beginTransaction();
        for(Sample s : listSample){
            ContentValues values = new ContentValues();
            values.put(OSADBHelper.SAMPLE_RECORD_ID,s.getR_id());
            values.put(OSADBHelper.SAMPLE_RECORD_FRAGMENT_INDEX,s.getFragment_index());
            values.put(OSADBHelper.SAMPLE_TIMESTAMP,s.getTimestamp());
            values.put(OSADBHelper.SAMPLE_CHANNEL_ID,s.getCh_id());
            values.put(OSADBHelper.SAMPLE_VALUE_FLOAT,s.getSample_data());
            values.put(OSADBHelper.SAMPLE_VALUE_ANNO,s.getSample_anno());

            mDatabase.insert(OSADBHelper.TABLE_SAMPLE, null, values);
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    public Sample getSampleById(long r_id, int fragment_index, long timestamp, int ch_id) {
        String condition = OSADBHelper.SAMPLE_RECORD_ID + " = ? AND "
                +OSADBHelper.SAMPLE_RECORD_FRAGMENT_INDEX +" = ? AND "
                +OSADBHelper.SAMPLE_TIMESTAMP +" = ? AND "
                +OSADBHelper.SAMPLE_CHANNEL_ID +" = ?";
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SAMPLE, mAllColumns,condition,
                new String[] {String.valueOf(r_id),String.valueOf(fragment_index), String.valueOf(timestamp), String.valueOf(ch_id)}, null, null, null);

        Sample sample = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            sample = cursorToSample(cursor);
        }
        cursor.close();
        return sample;
    }

}
