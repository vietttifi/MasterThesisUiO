package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 18/12/2016.
 */

public class SensorSourceAdapter{
    public static final String TAG = "SensorSourceAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.SENSOR_SOURCE_ID, OSADBHelper.SENSOR_SOURCE_NAME, OSADBHelper.SENSOR_SOURCE_TYPE};

    public SensorSourceAdapter(Context context){
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
        SensorSource sensorSource = new SensorSource();
        sensorSource.setS_id(cursor.getString(0));
        sensorSource.setS_name(cursor.getString(1));
        sensorSource.setS_type(cursor.getString(2));
        return sensorSource;
    }

    public void saveSensorSourceToDB(SensorSource source){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.SENSOR_SOURCE_ID,source.getS_id());
        values.put(OSADBHelper.SENSOR_SOURCE_NAME,source.getS_name());
        values.put(OSADBHelper.SENSOR_SOURCE_TYPE,source.getS_type());
        mDatabase.insertWithOnConflict(OSADBHelper.TABLE_SENSOR_SOURCE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public SensorSource getSensorSourceById(String source_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_SENSOR_SOURCE, mAllColumns,
                OSADBHelper.SENSOR_SOURCE_ID + " = ?", new String[] {source_ID}, null, null, null);

        SensorSource newSensorSource = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newSensorSource = cursorToSensorSource(cursor);
        }
        cursor.close();
        return newSensorSource;
    }

    public void deleteSource(String source_ID) {
        // delete all ALL CHANNEL AND RECORD belong to this SOURCE ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_SENSOR_SOURCE, OSADBHelper.SENSOR_SOURCE_ID + " = " + source_ID, null);
    }
}