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
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumns = {OSADBHelper.SAMPLE_RECORD_ID, OSADBHelper.SAMPLE_TIMESTAMP, OSADBHelper.SAMPLE_VALUE};

    public SampleAdapter(Context context){
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

    Sample cursorToSample(Cursor cursor) {
        return new Sample(cursor.getLong(0), cursor.getLong(1),cursor.getFloat(2));
    }

    public void saveSampleToDB(List<Sample> listSample){
        mDatabase.beginTransaction();
        try{
            for(Sample s : listSample){
                ContentValues values = new ContentValues();
                values.put(OSADBHelper.SAMPLE_RECORD_ID,s.getR_id());
                values.put(OSADBHelper.SAMPLE_TIMESTAMP,s.getTimestamp());
                values.put(OSADBHelper.SAMPLE_VALUE,s.getSample_data());

                mDatabase.insert(OSADBHelper.TABLE_SAMPLE, null, values);
            }
            mDatabase.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            mDatabase.endTransaction();
        }

    }

    public short[] getShortValues(long r_id ,int from, int to){
        short[] values = new short[to-from];
        int COUNT = to - from;
        String queryString = "SELECT "+ OSADBHelper.SAMPLE_TIMESTAMP+","+ OSADBHelper.SAMPLE_VALUE+" " +
                "FROM "+OSADBHelper.TABLE_SAMPLE+ " WHERE "+OSADBHelper.SAMPLE_RECORD_ID +" = "+r_id +" ORDER BY "+OSADBHelper.SAMPLE_TIMESTAMP
                + " LIMIT "+COUNT+" OFFSET "+from;
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            int i = 0;
            while (!cursor.isAfterLast()) {
                values[i++] = cursor.getShort(1);
                cursor.moveToNext();
            }
        }
        else return null;
        cursor.close();
        return values;
    }

    public Sample getSampleById(long r_id, long timestamp) {
        String condition = OSADBHelper.SAMPLE_RECORD_ID + " = ? AND "
                +OSADBHelper.SAMPLE_TIMESTAMP +" = ?  ";
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SAMPLE, mAllColumns,condition,
                new String[] {String.valueOf(r_id), String.valueOf(timestamp)}, null, null, null);

        Sample sample = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            sample = cursorToSample(cursor);
        }
        cursor.close();
        return sample;
    }

}
