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
    private String[] mAllColumns = {OSADBHelper.DATA_RECORD_ID, OSADBHelper.DATA_RECORD_SOURCE_ID, OSADBHelper.DATA_RECORD_PATIENT_ID,
            OSADBHelper.DATA_RECORD_CLINIC_ID, OSADBHelper.DATA_RECORD_CREATEDATE,
            OSADBHelper.DATA_RECORD_EXPERIMENTS, OSADBHelper.DATA_RECORD_DESCRIPTIONS, OSADBHelper.DATA_RECORD_MAX_SAMPLE};

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

    public static DataRecord cursorToRecord(Cursor cursor) {
        DataRecord dataRecord = new DataRecord(cursor.getLong(0),cursor.getString(1),cursor.getString(2),
                cursor.getString(3),cursor.getLong(4));
        dataRecord.setExperiments(cursor.getString(5));
        dataRecord.setDescriptions(cursor.getString(6));
        dataRecord.setMax_sample(cursor.getInt(7));
        return dataRecord;
    }

    public void saveRecordToDB(long data_record_id, String source_id,
                                   String patient_id, String clinic_id, long createDate,
                                   String experiments, String descriptions, int max_sample){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.DATA_RECORD_ID,data_record_id);
        values.put(OSADBHelper.DATA_RECORD_SOURCE_ID,source_id);
        values.put(OSADBHelper.DATA_RECORD_PATIENT_ID,patient_id);
        values.put(OSADBHelper.DATA_RECORD_CLINIC_ID,clinic_id);
        values.put(OSADBHelper.DATA_RECORD_CREATEDATE,createDate);
        values.put(OSADBHelper.DATA_RECORD_EXPERIMENTS,experiments);
        values.put(OSADBHelper.DATA_RECORD_DESCRIPTIONS,descriptions);
        values.put(OSADBHelper.DATA_RECORD_MAX_SAMPLE,max_sample);

        mDatabase.insert(OSADBHelper.TABLE_DATA_RECORD, null, values);
    }
}
