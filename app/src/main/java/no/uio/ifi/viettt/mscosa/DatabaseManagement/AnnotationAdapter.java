package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Annotation;

/**
 * Created by viettt on 21/02/2017.
 */

public class AnnotationAdapter {

    public static final String TAG = "Annotation";

    private SQLiteDatabase mDatabase;
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumns = {OSADBHelper.ANNOTATION_RECORD_ID, OSADBHelper.ANNOTATION_ONSET, OSADBHelper.ANNOTATION_DURATION,
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
        annotation.setRecord_id(cursor.getLong(0));
        annotation.setOnset(cursor.getDouble(1));
        annotation.setDuration(cursor.getDouble(2));
        annotation.setTimeKeeping(cursor.getDouble(3));
        annotation.setAnn(cursor.getString(4));

        return annotation;
    }

    public void saveAnnotationToDB(Annotation annotation){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.ANNOTATION_RECORD_ID,annotation.getRecord_id());
        values.put(OSADBHelper.ANNOTATION_ONSET,annotation.getOnset());
        values.put(OSADBHelper.ANNOTATION_DURATION,annotation.getDuration());
        values.put(OSADBHelper.ANNOTATION_TIMEKEEPING, annotation.getTimeKeeping());
        values.put(OSADBHelper.ANNOTATION_TEXT, annotation.getAnn());

        mDatabase.insert(OSADBHelper.TABLE_RECORD_ANNOTATION, null, values);
    }

    public void saveListFragmentsToDB(List<Annotation> listAnns){
        mDatabase.beginTransaction();
        try{
            for(Annotation anns : listAnns){
                saveAnnotationToDB(anns);
            }
            mDatabase.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mDatabase.endTransaction();
        }
    }

    public List<Annotation> getAnnotationsForARecord(long r_id) {
        ArrayList<Annotation> results = new ArrayList<>();
        Cursor cursor = mDatabase.query(OSADBHelper.TABLE_RECORD_ANNOTATION, mAllColumns,
                OSADBHelper.ANNOTATION_RECORD_ID + " = ? ", new String[] {String.valueOf(r_id)}, null, null, null);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                results.add(cursorToAnnotation(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return results;
    }
}
