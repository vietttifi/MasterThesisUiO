package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import no.uio.ifi.viettt.mscosa.SensorsObjects.RecordFragment;

/**
 * Created by viettt on 21/02/2017.
 */

public class RecordFragmentAdapter {

    public static final String TAG = "RecordFragmentAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.FRAGMENT_RECORD_ID, OSADBHelper.FRAGMENT_INDEX, OSADBHelper.FRAGMENT_TIMESTAMP};

    public RecordFragmentAdapter(Context context){
        mDbHelper = new OSADBHelper(context);
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close(){
        mDbHelper.close();
    }

    public void updateRecordFragmentTimestamp(long r_id, int inx, long timestamp){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.FRAGMENT_TIMESTAMP,timestamp);
        mDatabase.update(OSADBHelper.TABLE_RECORD_FRAGMENT,values,
                OSADBHelper.FRAGMENT_RECORD_ID + " = " + r_id
                        +" AND "+OSADBHelper.FRAGMENT_INDEX + " = "+inx,null);
    }

    RecordFragment cursorToRecordFragment(Cursor cursor) {
        RecordFragment recordFragment = new RecordFragment();
        recordFragment.setR_id(cursor.getLong(0));
        recordFragment.setIndex(cursor.getInt(1));
        recordFragment.setTimestamp(cursor.getLong(2));

        return recordFragment;
    }

    public void saveRecordFragmentToDB(RecordFragment recordFragment){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.FRAGMENT_RECORD_ID,recordFragment.getR_id());
        values.put(OSADBHelper.FRAGMENT_INDEX,recordFragment.getIndex());
        values.put(OSADBHelper.FRAGMENT_TIMESTAMP,recordFragment.getTimestamp());

        mDatabase.insert(OSADBHelper.TABLE_RECORD_FRAGMENT, null, values);
    }

    public RecordFragment getRecordFragmentById(long r_id, int fragment_index) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD_FRAGMENT, mAllColumns,
                OSADBHelper.FRAGMENT_RECORD_ID + " = ? AND "+OSADBHelper.FRAGMENT_INDEX +" = ? ", new String[] {String.valueOf(r_id),String.valueOf(fragment_index)}, null, null, null);

        RecordFragment recordFragment = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            recordFragment = cursorToRecordFragment(cursor);
        }
        cursor.close();
        return recordFragment;
    }

    public void deleteFragment(long r_id, int fragment_index) {
        // delete all ALL CHANNEL AND RECORD belong to this SOURCE ------ TRIGGER will be called.
        String condition = OSADBHelper.FRAGMENT_RECORD_ID + " = " + r_id + " AND " +OSADBHelper.FRAGMENT_INDEX + " = " + String.valueOf(fragment_index);
        mDatabase.delete(OSADBHelper.TABLE_RECORD_FRAGMENT, condition , null);
    }
}
