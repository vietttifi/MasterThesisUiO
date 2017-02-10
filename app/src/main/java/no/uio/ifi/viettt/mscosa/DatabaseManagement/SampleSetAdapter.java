package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.SampleSet;


/**
 * Created by viettt on 04/01/2017.
 */

public class SampleSetAdapter {
    public static final String TAG = "SampleAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.SAMPLE_SOURCE_ID,
            OSADBHelper.SAMPLE_CHANNEL_ID, OSADBHelper.SAMPLE_DATA_RECORD_ID, OSADBHelper.SAMPLE_PATIENT_ID,
            OSADBHelper.SAMPLE_CLINIC_ID, OSADBHelper.SAMPLE_DATA};

    public SampleSetAdapter(Context context){
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

    public static SampleSet cursorToSample(Cursor cursor) {
        SampleSet sampleSet = new SampleSet(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3), cursor.getString(4));
        sampleSet.setSamples(cursor.getBlob(5));
        return sampleSet;
    }

    public void saveSampleToDB(List<SampleSet> listSampleSet){

        mDatabase.beginTransaction();

        for(SampleSet s : listSampleSet){
            ContentValues values = new ContentValues();
            values.put(OSADBHelper.SAMPLE_SOURCE_ID,s.getSource_id());
            values.put(OSADBHelper.SAMPLE_CHANNEL_ID,s.getChannel_id());
            values.put(OSADBHelper.SAMPLE_DATA_RECORD_ID,s.getRecord_id());
            values.put(OSADBHelper.SAMPLE_PATIENT_ID,s.getPatient_id());
            values.put(OSADBHelper.SAMPLE_CLINIC_ID,s.getClinic_id());
            values.put(OSADBHelper.SAMPLE_DATA,s.getSamples());

            mDatabase.insert(OSADBHelper.TABLE_SAMPLE_SET, null, values);
        }

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

}
