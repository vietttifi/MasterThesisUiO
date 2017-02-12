package no.uio.ifi.viettt.mscosa.DatabaseManagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by viettt on 18/12/2016.
 */

public class OSADBHelper extends SQLiteOpenHelper {

    public static final String TAG = "OSADBHelper";

    //SensorSource table and its columns -------- SENSOR_SOURCE ---------
    public static final String TABLE_SENSOR_SOURCE = "sensor_source";
    public static final String SENSOR_SOURCE_ID = "source_id";
    public static final String SENSOR_SOURCE_NAME = "source_name";
    public static final String SENSOR_SOURCE_TYPE = "type";
    public static final String SENSOR_SOURCE_START_DATE = "start_date";
    public static final String SENSOR_SOURCE_RESERVED = "reserved";
    public static final String SENSOR_SOURCE_DATA_RECORD_DURATION = "data_record_duration";

    //Channel table and its columns ---------- CHANNEL ------------
    public static final String TABLE_CHANNEL = "channel";
    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_SENSOR_SOURCE_ID = "source_id";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String CHANNEL_TRANSDUCER_TYPE = "transducer_type";
    public static final String CHANNEL_DIMENSION = "dimension";
    public static final String CHANNEL_PHYSICAL_MIN = "physical_min";
    public static final String CHANNEL_PHYSICAL_MAX = "physical_max";
    public static final String CHANNEL_DIGITAL_MIN = "digital_min";
    public static final String CHANNEL_DIGITAL_MAX = "digital_max";
    public static final String CHANNEL_PREFILTERING = "prefiltering";
    public static final String CHANNEL_RESERVED = "reserved";
    public static final String CHANNEL_DESCRIPTION = "description";

    //Data_record table and its columns    --------------- DATA_RECORD ------------
    public static final String TABLE_DATA_RECORD = "data_record";
    public static final String DATA_RECORD_ID = "dr_id";
    public static final String DATA_RECORD_SOURCE_ID = "source_id";
    public static final String DATA_RECORD_PATIENT_ID = "patient_id";
    public static final String DATA_RECORD_CLINIC_ID = "clinic_id";
    public static final String DATA_RECORD_CREATEDATE = "createDate";
    public static final String DATA_RECORD_EXPERIMENTS = "experiments";
    public static final String DATA_RECORD_DESCRIPTIONS = "descriptions";
    public static final String DATA_RECORD_MAX_SAMPLE = "max_sample";

    //Sample table and its columns ----------- SAMPLE SET---------
    public static final String TABLE_SAMPLE_SET = "sampleset";
    public static final String SAMPLE_SOURCE_ID = "source_id";
    public static final String SAMPLE_CHANNEL_ID = "channel_id";
    public static final String SAMPLE_DATA_RECORD_ID = "dr_id";
    public static final String SAMPLE_PATIENT_ID = "patient_id";
    public static final String SAMPLE_CLINIC_ID = "clinic_id";
    public static final String SAMPLE_DATA = "sample_data";

    //Patient table and its columns
    public static final String TABLE_PATIENT = "patient";
    public static final String PATIENT_ID = "patient_id";
    public static final String PATIENT_CODE_IN_CLINIC = "p_code_in_clinic";
    public static final String PATIENT_GENDER = "gender";
    public static final String PATIENT_LASTNAME = "last_name";
    public static final String PATIENT_FIRSTNAME = "first_name";
    public static final String PATIENT_DATEOFBIRTH = "date_of_birth";
    public static final String PATIENT_ADDRESS = "address";
    public static final String PATIENT_PHONE_NR = "phone_nr";
    public static final String PATIENT_EMAIL = "email";

    //Clinic table and its columns
    public static final String TABLE_CLINIC = "clinic";
    public static final String CLINIC_ID = "clinic_id";
    public static final String CLINIC_CODE = "clinic_code";
    public static final String CLINIC_TECHNICIAN_ID = "technician_ID";
    public static final String CLINIC_ADDRESS = "address";
    public static final String CLINIC_PHONE_NR = "phone_nr";
    public static final String CLINIC_EMAIL = "email";

    //DATABASE NAME
    private static final String DATABASE_NAME = "osamsc.db";
    private static final int DATABASE_VERSION = 1;

    // SQL statement of the -----SENSOR_SOURCE ------ table creation
    private static final String SQL_CREATE_TABLE_SENSOR_SOURCE =
            "CREATE TABLE " + TABLE_SENSOR_SOURCE + "("
            + SENSOR_SOURCE_ID + " TEXT PRIMARY KEY, "
            + SENSOR_SOURCE_NAME + " TEXT NOT NULL, "
            + SENSOR_SOURCE_TYPE + " TEXT, "
            + SENSOR_SOURCE_START_DATE + " INTEGER, "
            + SENSOR_SOURCE_RESERVED + " BLOB, "
            + SENSOR_SOURCE_DATA_RECORD_DURATION + " REAL "
            +");";

    // SQL statement of the -------  CHANNEL ------ table creation
    private static final String SQL_CREATE_TABLE_CHANNEL =
            "CREATE TABLE " + TABLE_CHANNEL + "("
                    + CHANNEL_ID + " TEXT NOT NULL, "
                    + CHANNEL_SENSOR_SOURCE_ID + " TEXT NOT NULL, "
                    + CHANNEL_NAME + " TEXT NOT NULL, "
                    + CHANNEL_TRANSDUCER_TYPE + " TEXT NOT NULL, "
                    + CHANNEL_DIMENSION + " TEXT, "
                    + CHANNEL_PHYSICAL_MIN + " REAL, "
                    + CHANNEL_PHYSICAL_MAX + " REAL, "
                    + CHANNEL_DIGITAL_MIN + " INTEGER, "
                    + CHANNEL_DIGITAL_MAX + " INTEGER, "
                    + CHANNEL_PREFILTERING + " TEXT, "
                    + CHANNEL_RESERVED + " BLOB, "
                    + CHANNEL_DESCRIPTION + " TEXT, "
                    + " PRIMARY KEY ("+ CHANNEL_ID +","+CHANNEL_SENSOR_SOURCE_ID+"), "
                    + " FOREIGN KEY( "+ CHANNEL_SENSOR_SOURCE_ID +" ) REFERENCES "+ TABLE_SENSOR_SOURCE +"("+ SENSOR_SOURCE_ID +")"
                    +");";

    // SQL statement of the ---------- DATA_RECORD ------- table creation
    private static final String SQL_CREATE_TABLE_DATA_RECORD =
            "CREATE TABLE " + TABLE_DATA_RECORD + "("
                    + DATA_RECORD_ID + " TEXT, "
                    + DATA_RECORD_SOURCE_ID + " TEXT NOT NULL, "
                    + DATA_RECORD_PATIENT_ID + " TEXT NOT NULL, "
                    + DATA_RECORD_CLINIC_ID + " TEXT NOT NULL, "
                    + DATA_RECORD_CREATEDATE + " INTEGER NOT NULL, "
                    + DATA_RECORD_EXPERIMENTS + " TEXT, "
                    + DATA_RECORD_DESCRIPTIONS + " TEXT, "
                    + DATA_RECORD_MAX_SAMPLE + " INTEGER NOT NULL, "
                    + " PRIMARY KEY ("+ DATA_RECORD_ID +","+DATA_RECORD_SOURCE_ID+","+DATA_RECORD_PATIENT_ID+","+DATA_RECORD_CLINIC_ID+"), "
                    + " FOREIGN KEY( "+ DATA_RECORD_SOURCE_ID +" ) REFERENCES "+ TABLE_SENSOR_SOURCE +"("+ SENSOR_SOURCE_ID +"), "
                    + " FOREIGN KEY( "+ DATA_RECORD_PATIENT_ID +" ) REFERENCES "+ TABLE_PATIENT +"("+ PATIENT_ID +"), "
                    + " FOREIGN KEY( "+ DATA_RECORD_CLINIC_ID +" ) REFERENCES "+ TABLE_CLINIC +"("+ CLINIC_ID +")"
                    +");";

    // SQL statement of the ------- SAMPLE SET------ table creation
    private static final String SQL_CREATE_TABLE_SAMPLE_SET =
            "CREATE TABLE " + TABLE_SAMPLE_SET + "("
                    + SAMPLE_SOURCE_ID + " TEXT NOT NULL, "
                    + SAMPLE_CHANNEL_ID + " TEXT NOT NULL, "
                    + SAMPLE_DATA_RECORD_ID + " TEXT NOT NULL, "
                    + SAMPLE_PATIENT_ID + " TEXT NOT NULL, "
                    + SAMPLE_CLINIC_ID + " TEXT NOT NULL, "
                    + SAMPLE_DATA + " BLOB , "
                    + " PRIMARY KEY ("+ SAMPLE_SOURCE_ID +","+ SAMPLE_CHANNEL_ID +","
                                +SAMPLE_DATA_RECORD_ID+","+SAMPLE_PATIENT_ID+","+SAMPLE_CLINIC_ID+"), "
                    + " FOREIGN KEY( "+ SAMPLE_SOURCE_ID +","+ SAMPLE_CHANNEL_ID +" ) REFERENCES "+ TABLE_CHANNEL +"("+ CHANNEL_ID +","+CHANNEL_SENSOR_SOURCE_ID +"),"
                    + " FOREIGN KEY( "+ SAMPLE_DATA_RECORD_ID+","+ SAMPLE_SOURCE_ID+","+SAMPLE_PATIENT_ID+","+SAMPLE_CLINIC_ID
                                +" ) REFERENCES "+ TABLE_DATA_RECORD +"("+ DATA_RECORD_ID +","+DATA_RECORD_SOURCE_ID+","+DATA_RECORD_PATIENT_ID+","+DATA_RECORD_CLINIC_ID +")"
                    +");";

    // SQL statement of the SensorSource table creation
    private static final String SQL_CREATE_TABLE_PATIENT =
            "CREATE TABLE " + TABLE_PATIENT + "("
                    + PATIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + PATIENT_CODE_IN_CLINIC + " TEXT, "
                    + PATIENT_GENDER + " TEXT, "
                    + PATIENT_LASTNAME + " TEXT, "
                    + PATIENT_FIRSTNAME + " TEXT, "
                    + PATIENT_DATEOFBIRTH + " TEXT, "
                    + PATIENT_ADDRESS + " TEXT, "
                    + PATIENT_PHONE_NR + " TEXT, "
                    + PATIENT_EMAIL + " TEXT"
                    +");";

    // SQL statement of the SensorSource table creation
    private static final String SQL_CREATE_TABLE_CLINIC =
            "CREATE TABLE " + TABLE_CLINIC + "("
                    + CLINIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CLINIC_CODE + " TEXT, "
                    + CLINIC_TECHNICIAN_ID + " TEXT, "
                    + CLINIC_ADDRESS + " TEXT, "
                    + CLINIC_PHONE_NR + " TEXT, "
                    + CLINIC_EMAIL + " TEXT"
                    +");";

    //------------- TRIGGERS -------------------

    // SQL TRIGGER WHEN DELETE PATIENT
    private static final String SQL_TRIGGER_DELETE_PATIENT =
            "CREATE TRIGGER Delete_Patient_trigger "
                    + " AFTER DELETE ON " +TABLE_PATIENT
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_DATA_RECORD
                    + " WHERE "
                    + DATA_RECORD_PATIENT_ID + " = OLD."+PATIENT_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE CLINIC
    private static final String SQL_TRIGGER_DELETE_CLINIC =
            "CREATE TRIGGER Delete_Clinic_trigger "
                    + " AFTER DELETE ON " +TABLE_CLINIC
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_DATA_RECORD
                    + " WHERE "
                    + DATA_RECORD_CLINIC_ID + " = OLD."+CLINIC_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE SENSOR_SOURCE
    private static final String SQL_TRIGGER_DELETE_SENSOR_SOURCE =
            "CREATE TRIGGER Delete_Sensor_Source_trigger "
                    + " AFTER DELETE ON " +TABLE_SENSOR_SOURCE
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_DATA_RECORD
                    + " WHERE "
                    + DATA_RECORD_SOURCE_ID + " = OLD."+SENSOR_SOURCE_ID+"; "
                    + " DELETE FROM "+ TABLE_CHANNEL
                    + " WHERE "
                    + CHANNEL_SENSOR_SOURCE_ID + " = OLD."+SENSOR_SOURCE_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE CHANNEL
    private static final String SQL_TRIGGER_DELETE_CHANNEL =
            "CREATE TRIGGER Delete_Channel_trigger "
                    + " AFTER DELETE ON " +TABLE_CHANNEL
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_SAMPLE_SET
                    + " WHERE "
                    + SAMPLE_CHANNEL_ID + " = OLD."+CHANNEL_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE DATA_RECORD
    private static final String SQL_TRIGGER_DELETE_DATA_RECORD =
            "CREATE TRIGGER Delete_data_record_trigger "
                    + " AFTER DELETE ON " +TABLE_DATA_RECORD
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_SAMPLE_SET
                    + " WHERE "
                    + SAMPLE_DATA_RECORD_ID + " = OLD."+DATA_RECORD_ID+"; "
                    + " END";

    public OSADBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CHANNEL);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_DATA_RECORD);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SAMPLE_SET);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PATIENT);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CLINIC);

        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_PATIENT);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_CLINIC);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_DATA_RECORD);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_CHANNEL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(TAG,"DANGER AREA, upgrade from "+i+" to "+i1);

        //DELETE ALL TABLES
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CHANNEL);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CLINIC);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PATIENT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SAMPLE_SET);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_DATA_RECORD);

        //AND RECREATE THEM
        onCreate(sqLiteDatabase);
    }

}