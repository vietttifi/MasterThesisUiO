package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 18/12/2016.
 */

public class SensorSourceAdapter{
    public static final String TAG = "SensorSourceAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.SENSOR_SOURCE_ID, OSADBHelper.SENSOR_SOURCE_NAME, OSADBHelper.SENSOR_SOURCE_TYPE,
    OSADBHelper.SENSOR_SOURCE_START_DATE, OSADBHelper.SENSOR_SOURCE_RESERVED, OSADBHelper.SENSOR_SOURCE_DATA_RECORD_DURATION};

    public SensorSourceAdapter(Context context){
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

    SensorSource cursorToSensorSource(Cursor cursor) {
        SensorSource sensorSource = new SensorSource(cursor.getString(1),cursor.getString(2));
        sensorSource.setSource_id(cursor.getString(0));
        sensorSource.setStartDateTime(cursor.getInt(3));
        sensorSource.setReserved(cursor.getBlob(4));
        sensorSource.setData_record_duration(cursor.getInt(5));

        return sensorSource;
    }

    public void saveSensorSourceToDB(String sensor_source_ID, String source_name, String source_type,
                                   long startDateTime, byte[] reserved, double data_record_duration){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.SENSOR_SOURCE_ID,sensor_source_ID);
        values.put(OSADBHelper.SENSOR_SOURCE_NAME,source_name);
        values.put(OSADBHelper.SENSOR_SOURCE_TYPE,source_type);
        values.put(OSADBHelper.SENSOR_SOURCE_START_DATE,startDateTime);
        if(reserved != null) values.put(OSADBHelper.SENSOR_SOURCE_RESERVED,reserved);
        values.put(OSADBHelper.SENSOR_SOURCE_DATA_RECORD_DURATION,data_record_duration);

        mDatabase.insert(OSADBHelper.TABLE_SENSOR_SOURCE, null, values);
    }

    public SensorSource getSensorSourceById(String source_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SENSOR_SOURCE, mAllColumns,
                OSADBHelper.SENSOR_SOURCE_ID + " = ?", new String[] {source_ID}, null, null, null);

        SensorSource newSensorSource = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newSensorSource= cursorToSensorSource(cursor);
        }
        cursor.close();
        return newSensorSource;
    }

    public List<SensorSource> getallSensorSource(){
        List<SensorSource> listSensorSources = new ArrayList<SensorSource>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SENSOR_SOURCE, mAllColumns,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                SensorSource sensorSource = cursorToSensorSource(cursor);
                listSensorSources.add(sensorSource);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listSensorSources;
    }

    public void deleteSource(SensorSource sensorSource) {
        String id = sensorSource.getSource_id();

        // delete all ALL CHANNEL AND RECORD belong to this SOURCE ------ TRIGGER will be called.

        mDatabase.delete(OSADBHelper.TABLE_SENSOR_SOURCE, OSADBHelper.SENSOR_SOURCE_ID + " = " + id, null);
    }

    public List<SensorSource> searchSource(String searchText) {
        List<SensorSource> listSensorSources = new ArrayList<SensorSource>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SENSOR_SOURCE, mAllColumns,
                OSADBHelper.SENSOR_SOURCE_ID + " like ", new String[] {searchText}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                SensorSource sensorSource = cursorToSensorSource(cursor);
                listSensorSources.add(sensorSource);
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();
        }
        return listSensorSources;
    }
}