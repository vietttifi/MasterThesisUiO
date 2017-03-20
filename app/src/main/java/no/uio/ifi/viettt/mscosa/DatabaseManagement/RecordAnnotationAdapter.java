package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import no.uio.ifi.viettt.mscosa.SensorsObjects.RecordAnnotation;

/**
 * Created by viettt on 21/02/2017.
 */

public class RecordAnnotationAdapter {

    public static final String TAG = "RecordAnnotation";

    private SQLiteDatabase mDatabase;
    private OSADataBaseManager mDbManagerInstance;
    private String[] mAllColumns = {OSADBHelper.RECORD_ANNOTATION_R_ID, OSADBHelper.RECORD_ANNOTATION_ID};

    public RecordAnnotationAdapter(Context context){
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

    RecordAnnotation cursorToAnnotation(Cursor cursor) {
        RecordAnnotation annotation = new RecordAnnotation();
        annotation.setRecord_id(cursor.getLong(0));
        annotation.setAnn_id(cursor.getLong(1));
        return annotation;
    }

    public void saveAnnotationToDB(RecordAnnotation annotation){
        ContentValues values = new ContentValues();
        values.put(OSADBHelper.RECORD_ANNOTATION_R_ID,annotation.getRecord_id());
        values.put(OSADBHelper.RECORD_ANNOTATION_ID,annotation.getAnn_id());

        mDatabase.insert(OSADBHelper.TABLE_RECORD_ANNOTATION, null, values);
    }

}
