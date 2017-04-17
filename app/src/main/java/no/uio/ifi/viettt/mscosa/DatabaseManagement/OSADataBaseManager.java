package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by viettt on 15/03/2017.
 */

public class OSADataBaseManager {

    private int mOpenCounter;
    private static OSADataBaseManager instance;
    private static OSADBHelper mOSADBHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(OSADBHelper helper) {
        if (instance == null) {
            instance = new OSADataBaseManager();
            mOSADBHelper = helper;
        }
    }

    public static synchronized OSADataBaseManager getInstance() throws Exception{
        if (instance == null) {
            throw new Exception(OSADataBaseManager.class.getSimpleName()
                    + " is not initialized, call initializeInstance(..) to initialize instance.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        //If it is the first time
        if(mOpenCounter == 1) {
            mDatabase = mOSADBHelper.getWritableDatabase();
        }
        //else just return the opened instance
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        //We do not want to close the DB while the other use it
        mOpenCounter--;
        if(mOpenCounter == 0) {
            //REAL CLOSE
            mDatabase.close();
        }
    }
}
