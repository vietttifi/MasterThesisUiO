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
    private Context mContext;
    private String[] mAllColumns = {OSADBHelper.CHANNEL_ID, OSADBHelper.CHANNEL_SENSOR_SOURCE_ID,
            OSADBHelper.CHANNEL_NAME, OSADBHelper.CHANNEL_TRANSDUCER_TYPE, OSADBHelper.CHANNEL_DIMENSION,
            OSADBHelper.CHANNEL_PHYSICAL_MIN, OSADBHelper.CHANNEL_PHYSICAL_MAX,
            OSADBHelper.CHANNEL_DIGITAL_MIN,OSADBHelper.CHANNEL_DIGITAL_MAX,
            OSADBHelper.CHANNEL_PREFILTERING, OSADBHelper.CHANNEL_RESERVED, OSADBHelper.CHANNEL_DESCRIPTION};

    public ChannelAdapter(Context context){
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

    Channel cursorToChannel(Cursor cursor) {
        Channel channel = new Channel(cursor.getString(0), cursor.getString(1));
        channel.setChannel_name(cursor.getString(2));
        channel.setTransducer_type(cursor.getString(3));
        channel.setPhysical_dimension(cursor.getString(4));
        channel.setPhysical_min(cursor.getDouble(5));
        channel.setPhysical_max(cursor.getDouble(6));
        channel.setDigital_min(cursor.getInt(7));
        channel.setDigital_max(cursor.getInt(8));
        channel.setPrefiltering(cursor.getString(9));
        channel.setReserved(cursor.getBlob(10));
        channel.setDescription(cursor.getString(11));

        return channel;
    }

    public void saveChannelToDB(String channel_id, String sensor_source_id,
                                 String channel_name, String channel_transducer_type, String channel_dimension,
                                 double channel_physical_min, double channel_physical_max,
                                 int channel_digital_min, int channel_digital_max,
                                 String channel_prefiltering, byte[] channel_reserved,
                                 String channel_description){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.CHANNEL_ID,channel_id);
        values.put(OSADBHelper.CHANNEL_SENSOR_SOURCE_ID,sensor_source_id);
        values.put(OSADBHelper.CHANNEL_NAME,channel_name);
        values.put(OSADBHelper.CHANNEL_TRANSDUCER_TYPE,channel_transducer_type);
        values.put(OSADBHelper.CHANNEL_DIMENSION,channel_dimension);

        values.put(OSADBHelper.CHANNEL_PHYSICAL_MIN,channel_physical_min);
        values.put(OSADBHelper.CHANNEL_PHYSICAL_MAX,channel_physical_max);
        values.put(OSADBHelper.CHANNEL_DIGITAL_MIN,channel_digital_min);
        values.put(OSADBHelper.CHANNEL_DIGITAL_MAX,channel_digital_max);

        values.put(OSADBHelper.CHANNEL_PREFILTERING,channel_prefiltering);
        values.put(OSADBHelper.CHANNEL_RESERVED,channel_reserved);
        values.put(OSADBHelper.CHANNEL_DESCRIPTION,channel_description);

        mDatabase.insert(OSADBHelper.TABLE_CHANNEL, null, values);
    }

    public Channel getChannelById(String channel_ID, String source_id) {
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CHANNEL, mAllColumns,
                OSADBHelper.CHANNEL_ID + " = ? and " + OSADBHelper.CHANNEL_SENSOR_SOURCE_ID + " = ? ",
                new String[] {channel_ID, source_id}, null, null, null);

        Channel newChannel = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            newChannel = cursorToChannel(cursor);
        }
        cursor.close();
        return newChannel;
    }

    public List<Channel> getallChannels(){
        List<Channel> listChannel = new ArrayList<Channel>();

        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_CHANNEL, mAllColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Channel channel = cursorToChannel(cursor);
            listChannel.add(channel);
            cursor.moveToNext();
        }

        //close the cursor
        cursor.close();
        return listChannel;
    }

    public void deleteRecord(Channel channel) {
        String id = channel.getChannel_ID();
        // delete all ALL RECORD belong to this CLINIC ------ TRIGGER will be called.
        mDatabase.delete(OSADBHelper.TABLE_CHANNEL, OSADBHelper.CHANNEL_ID + " = " + id, null);
    }
}
