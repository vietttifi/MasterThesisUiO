package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;

/**
 * Created by viettt on 04/01/2017.
 */

public class ChannelAdapter{
    public static final String TAG = "ChannelAdapter";

    private SQLiteDatabase mDatabase;
    private OSADBHelper mDbHelper;
    private String[] mAllColumns = {OSADBHelper.CHANNEL_ID,OSADBHelper.CHANNEL_S_ID, OSADBHelper.CHANNEL_NR,
            OSADBHelper.CHANNEL_NAME, OSADBHelper.CHANNEL_TRANSDUCER_TYPE, OSADBHelper.CHANNEL_DIMENSION,
            OSADBHelper.CHANNEL_PHYSICAL_MIN, OSADBHelper.CHANNEL_PHYSICAL_MAX,
            OSADBHelper.CHANNEL_DIGITAL_MIN,OSADBHelper.CHANNEL_DIGITAL_MAX, OSADBHelper.CHANNEL_EDF_RESERVED};

    public ChannelAdapter(Context context){
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

    Channel cursorToChannel(Cursor cursor) {
        Channel channel = new Channel();
        channel.setCh_id(cursor.getInt(0));
        channel.setS_id(cursor.getString(1));
        channel.setCh_nr(cursor.getString(2));
        channel.setCh_name(cursor.getString(3));
        channel.setTransducer(cursor.getString(4));
        channel.setDimension(cursor.getString(5));
        channel.setPhy_min(cursor.getFloat(6));
        channel.setPhy_max(cursor.getFloat(7));
        channel.setDig_min(cursor.getInt(8));
        channel.setDig_max(cursor.getInt(9));
        channel.setEdf_reserved(cursor.getBlob(10));
        return channel;
    }

    public long saveChannelToDB(Channel channel){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.CHANNEL_S_ID,channel.getS_id());
        values.put(OSADBHelper.CHANNEL_NR,channel.getCh_nr());
        values.put(OSADBHelper.CHANNEL_NAME,channel.getCh_name());
        values.put(OSADBHelper.CHANNEL_TRANSDUCER_TYPE,channel.getTransducer());
        values.put(OSADBHelper.CHANNEL_DIMENSION,channel.getDimension());

        values.put(OSADBHelper.CHANNEL_PHYSICAL_MIN,channel.getPhy_min());
        values.put(OSADBHelper.CHANNEL_PHYSICAL_MAX,channel.getPhy_max());
        values.put(OSADBHelper.CHANNEL_DIGITAL_MIN,channel.getDig_min());
        values.put(OSADBHelper.CHANNEL_DIGITAL_MAX,channel.getDig_max());
        values.put(OSADBHelper.CHANNEL_EDF_RESERVED,channel.getEdf_reserved());

        return mDatabase.insertWithOnConflict(OSADBHelper.TABLE_CHANNEL, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Channel getChannelById(String channel_nr, String source_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CHANNEL, mAllColumns,
                OSADBHelper.CHANNEL_NR + " = ? and " + OSADBHelper.CHANNEL_S_ID + " = ? ",
                new String[] {channel_nr, source_id}, null, null, null);

        Channel newChannel = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newChannel = cursorToChannel(cursor);
        }
        cursor.close();
        return newChannel;
    }

    public Channel getChannelById(String channel_ID) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CHANNEL, mAllColumns,
                OSADBHelper.CHANNEL_ID + " = ? ",
                new String[] {channel_ID}, null, null, null);

        Channel newChannel = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newChannel = cursorToChannel(cursor);
        }
        cursor.close();
        return newChannel;
    }

    public void deleteRecord(String channel_nr, String source_id) {
        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_CHANNEL, OSADBHelper.CHANNEL_NR + " = " + channel_nr + " AND "+OSADBHelper.CHANNEL_S_ID + " = "+source_id, null);
    }
}
