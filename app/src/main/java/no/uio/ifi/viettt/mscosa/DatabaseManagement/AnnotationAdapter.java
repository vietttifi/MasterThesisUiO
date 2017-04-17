package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Annotation;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;

/**
 * Created by viettt on 21/02/2017.
 */

public class AnnotationAdapter {

    public static final String TAG = "Annotation";

    private SQLiteDatabase mDatabase;
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumns = {OSADBHelper.ANNOTATION_ID, OSADBHelper.ANNOTATION_ONSET, OSADBHelper.ANNOTATION_DURATION,
            OSADBHelper.ANNOTATION_TIMEKEEPING, OSADBHelper.ANNOTATION_TEXT};

    public AnnotationAdapter(Context context){
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

    Annotation cursorToAnnotation(Cursor cursor) {
        Annotation annotation = new Annotation();
        annotation.setAnn_id(cursor.getLong(0));
        annotation.setOnset(cursor.getDouble(1));
        annotation.setDuration(cursor.getDouble(2));
        annotation.setTimeKeeping(cursor.getDouble(3));
        annotation.setAnn(cursor.getString(4));

        return annotation;
    }

    public long saveAnnotationToDB(Annotation annotation){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.ANNOTATION_ONSET,annotation.getOnset());
        values.put(OSADBHelper.ANNOTATION_DURATION,annotation.getDuration());
        values.put(OSADBHelper.ANNOTATION_TIMEKEEPING, annotation.getTimeKeeping());
        values.put(OSADBHelper.ANNOTATION_TEXT, annotation.getAnn());

        return mDatabase.insert(OSADBHelper.TABLE_ANNOTATION, null, values);
    }

    public ArrayList<Annotation> getAnnotationsForRecordList(Record[] records) {
        String queries = "";
        for(Record r : records) queries += OSADBHelper.RECORD_ANNOTATION_R_ID+" = "+ r.getR_id() + "  OR ";
        queries = queries.substring(0,queries.length()-4);
        queries = "SELECT DISTINCT(b."+OSADBHelper.ANNOTATION_ID+"), "+ OSADBHelper.ANNOTATION_ONSET+","
                + OSADBHelper.ANNOTATION_DURATION +","+
                OSADBHelper.ANNOTATION_TIMEKEEPING+","+ OSADBHelper.ANNOTATION_TEXT+
                " FROM "+OSADBHelper.TABLE_RECORD_ANNOTATION+" as a JOIN "+OSADBHelper.TABLE_ANNOTATION+" as b ON a."+
                OSADBHelper.RECORD_ANNOTATION_ID +" = b."+OSADBHelper.ANNOTATION_ID+
                " WHERE "+queries+" ORDER BY "+OSADBHelper.ANNOTATION_ONSET+","+OSADBHelper.ANNOTATION_TIMEKEEPING;
        ArrayList<Annotation> results = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery(queries,null,null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                results.add(cursorToAnnotation(cursor));
                cursor.moveToNext();
            }
        } else results = null;
        cursor.close();
        return results;
    }
}
