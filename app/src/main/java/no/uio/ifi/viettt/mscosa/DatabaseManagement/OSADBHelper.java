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

    //======================  TABLE DEFINE PART  ===========================
    //SensorSource table and its columns -------- SENSOR_SOURCE ---------
    public static final String TABLE_SENSOR_SOURCE = "SOURCE";
    public static final String SENSOR_SOURCE_ID = "s_id";
    public static final String SENSOR_SOURCE_NAME = "s_name";
    public static final String SENSOR_SOURCE_TYPE = "s_type";

    //Channel table and its columns ---------- CHANNEL ------------
    public static final String TABLE_CHANNEL = "CHANNEL";
    public static final String CHANNEL_S_ID = "s_id";
    public static final String CHANNEL_NR = "ch_nr";
    public static final String CHANNEL_NAME = "ch_name";
    public static final String CHANNEL_TRANSDUCER_TYPE = "transducer";
    public static final String CHANNEL_DIMENSION = "metric";
    public static final String CHANNEL_PHYSICAL_MIN = "phy_min";
    public static final String CHANNEL_PHYSICAL_MAX = "phy_max";
    public static final String CHANNEL_DIGITAL_MIN = "dig_min";
    public static final String CHANNEL_DIGITAL_MAX = "dig_max";
    public static final String CHANNEL_PREFILTERING = "prefiltering";
    public static final String CHANNEL_EDF_RESERVED = "s_edf_reserved";


    //Record table and its columns    --------------- RECORD ------------
    public static final String TABLE_RECORD = "RECORD";
    public static final String RECORD_ID = "r_id";
    public static final String RECORD_S_ID = "s_id";
    public static final String RECORD_CH_NR = "ch_nr";
    public static final String RECORD_PHYSICIAN_ID = "p_collect";
    public static final String RECORD_PATIENT_ID = "p_owner";
    public static final String RECORD_TIMESTAMP = "timestamp";
    public static final String RECORD_DESCRIPTIONS = "descriptions";
    public static final String RECORD_FREQUENCY = "frequency";
    public static final String RECORD_USED_EQUIPMENT = "used_equip";
    public static final String RECORD_EDF_RESERVED = "edf_reserved";

    //Record fragment table and its columns ----------- ANNOTATION ---------
    public static final String TABLE_RECORD_ANNOTATION = "ANNOTATION";
    public static final String ANNOTATION_RECORD_ID = "r_id";
    public static final String ANNOTATION_ONSET = "onset";
    public static final String ANNOTATION_DURATION = "duration";
    public static final String ANNOTATION_TIMEKEEPING = "timekeeping";
    public static final String ANNOTATION_TEXT = "ann";

    //Sample table and its columns ----------- SAMPLE ---------
    public static final String TABLE_SAMPLE = "SAMPLE";
    public static final String SAMPLE_RECORD_ID = "r_id";
    public static final String SAMPLE_TIMESTAMP = "timestamp";
    public static final String SAMPLE_VALUE = "sample_value";

    //Person table and its columns
    public static final String TABLE_PERSON = "PERSON";
    public static final String PERSON_ID = "p_id";
    public static final String PERSON_NAME = "name";
    public static final String PERSON_CITY = "city";
    public static final String PERSON_PHONE = "phone";
    public static final String PERSON_EMAIL = "email";
    public static final String PERSON_GENDER = "gender";
    public static final String PERSON_DAY_OF_BIRTH = "dayOfBirth";
    public static final String PERSON_AGE = "age";

    //PHYSICIAN table and its columns
    public static final String TABLE_PHYSICIAN = "PHYSICIAN";
    public static final String PHY_PERSON_ID = "p_id";
    public static final String PHY_CLINIC_ID = "clinic_code";
    public static final String PHY_EMPLOYEE_NR = "employee_nr";
    public static final String PHY_TITLE = "title";

    //PATIENT table and its columns
    public static final String TABLE_PATIENT = "PATIENT";
    public static final String PATIENT_PER_ID = "p_id";
    public static final String PATIENT_CLINIC_P = "clinic_code";
    public static final String PATIENT_PATIENT_NR = "patientnr";
    public static final String PATIENT_HEIGHT = "height";
    public static final String PATIENT_WEIGHT = "weight";
    public static final String PATIENT_BMI = "BMI";
    public static final String PATIENT_HEALTH_ISSUES = "otherHealthIssues";

    //Clinic table and its columns
    public static final String TABLE_CLINIC = "CLINIC";
    public static final String CLINIC_ID = "cl_id";
    public static final String CLINIC_NAME = "name";
    public static final String CLINIC_ADDRESS = "address";
    public static final String CLINIC_PHONE_NR = "phone_nr";
    public static final String CLINIC_EMAIL = "email";

    //==================================================================


    // ================ DATABASE NAME ==================================
    public static final String DATABASE_NAME = "osamsc.db";
    private static final int DATABASE_VERSION = 1;
    //==================================================================


    //=================== CREATE TABLE CODE ============================
    // SQL statement of the -----SENSOR_SOURCE ------ table creation
    private static final String SQL_CREATE_TABLE_SENSOR_SOURCE =
            "CREATE TABLE " + TABLE_SENSOR_SOURCE + "("
                    + SENSOR_SOURCE_ID + " TEXT PRIMARY KEY, "
                    + SENSOR_SOURCE_NAME + " TEXT NOT NULL, "
                    + SENSOR_SOURCE_TYPE + " TEXT NOT NULL "
                    +");";

    // SQL statement of the -------  CHANNEL ------ table creation
    private static final String SQL_CREATE_TABLE_CHANNEL =
            "CREATE TABLE " + TABLE_CHANNEL + "("
                    + CHANNEL_S_ID + " TEXT NOT NULL, "
                    + CHANNEL_NR + " INTEGER NOT NULL, "
                    + CHANNEL_NAME + " TEXT NOT NULL, "
                    + CHANNEL_TRANSDUCER_TYPE + " TEXT, "
                    + CHANNEL_DIMENSION + " TEXT, "
                    + CHANNEL_PHYSICAL_MIN + " REAL, "
                    + CHANNEL_PHYSICAL_MAX + " REAL, "
                    + CHANNEL_DIGITAL_MIN + " INTEGER, "
                    + CHANNEL_DIGITAL_MAX + " INTEGER, "
                    + CHANNEL_PREFILTERING + " TEXT, "
                    + CHANNEL_EDF_RESERVED + " BLOB, "
                    + " PRIMARY KEY ("+ CHANNEL_NR +","+CHANNEL_S_ID+"), "
                    + " FOREIGN KEY( "+ CHANNEL_S_ID +" ) REFERENCES "+ TABLE_SENSOR_SOURCE +"("+ SENSOR_SOURCE_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ---------- RECORD ------- table creation
    private static final String SQL_CREATE_TABLE_RECORD =
            "CREATE TABLE " + TABLE_RECORD + "("
                    + RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RECORD_S_ID + " TEXT NOT NULL, "
                    + RECORD_CH_NR + " INTEGER NOT NULL, "
                    + RECORD_PHYSICIAN_ID + " TEXT NOT NULL, "
                    + RECORD_PATIENT_ID + " TEXT NOT NULL, "
                    + RECORD_TIMESTAMP + " INTEGER NOT NULL, "
                    + RECORD_DESCRIPTIONS + " TEXT, "
                    + RECORD_FREQUENCY + " REAL, "
                    + RECORD_USED_EQUIPMENT + " TEXT, "
                    + RECORD_EDF_RESERVED + " BLOB, "
                    + " UNIQUE ("+ RECORD_S_ID +","+ RECORD_CH_NR +","+RECORD_TIMESTAMP+","+RECORD_PHYSICIAN_ID+","+RECORD_PATIENT_ID+"), "
                    + " FOREIGN KEY( "+ RECORD_S_ID +","+ RECORD_CH_NR +" ) REFERENCES "+ TABLE_CHANNEL +"( "+ CHANNEL_S_ID + ", "+ CHANNEL_NR +") ON DELETE CASCADE, "
                    + " FOREIGN KEY( "+ RECORD_PHYSICIAN_ID +" ) REFERENCES "+ TABLE_PHYSICIAN +"("+ PHY_PERSON_ID +") ON DELETE CASCADE, "
                    + " FOREIGN KEY( "+ RECORD_PATIENT_ID +" ) REFERENCES "+ TABLE_PATIENT +"("+ PATIENT_PER_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ---------- FRAGMENT ------- table creation
    private static final String SQL_CREATE_TABLE_ANNOTATION =
            "CREATE TABLE " + TABLE_RECORD_ANNOTATION + "("
                    + ANNOTATION_RECORD_ID + " INTEGER NOT NULL, "
                    + ANNOTATION_ONSET + " INTEGER NOT NULL, "
                    + ANNOTATION_DURATION + " REAL, "
                    + ANNOTATION_TIMEKEEPING + " INTEGER, "
                    + ANNOTATION_TEXT + " TEXT, "
                    + " PRIMARY KEY ("+ ANNOTATION_RECORD_ID +","+ANNOTATION_ONSET+","+ANNOTATION_DURATION+","+ANNOTATION_TIMEKEEPING+","+ANNOTATION_TEXT+"), "
                    + " FOREIGN KEY( "+ ANNOTATION_RECORD_ID +" ) REFERENCES "+ TABLE_RECORD +"("+ RECORD_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ------- SAMPLE ------ table creation
    private static final String SQL_CREATE_TABLE_SAMPLE =
            "CREATE TABLE " + TABLE_SAMPLE + "("
                    + SAMPLE_RECORD_ID + " INTEGER NOT NULL, "
                    + SAMPLE_TIMESTAMP + " INTEGER NOT NULL, "
                    + SAMPLE_VALUE + " REAL NOT NULL, "
                    + " PRIMARY KEY ("+ SAMPLE_RECORD_ID +","+ SAMPLE_TIMESTAMP +"), "
                    + " FOREIGN KEY( "+SAMPLE_RECORD_ID+") REFERENCES "+ TABLE_RECORD +"("+RECORD_ID+") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ---------- PERSON ------- table creation
    private static final String SQL_CREATE_TABLE_PERSON =
            "CREATE TABLE " + TABLE_PERSON + "("
                    + PERSON_ID + " TEXT PRIMARY KEY, "
                    + PERSON_NAME + " TEXT, "
                    + PERSON_CITY + " TEXT, "
                    + PERSON_PHONE + " TEXT, "
                    + PERSON_EMAIL + " TEXT, "
                    + PERSON_GENDER + " TEXT, "
                    + PERSON_DAY_OF_BIRTH + " TEXT, "
                    + PERSON_AGE + " INTEGER"
                    +");";

    // SQL statement of the ------- CLINIC ------ table creation
    private static final String SQL_CREATE_TABLE_CLINIC =
            "CREATE TABLE " + TABLE_CLINIC + "("
                    + CLINIC_ID + " TEXT PRIMARY KEY, "
                    + CLINIC_NAME + " TEXT, "
                    + CLINIC_ADDRESS + " TEXT, "
                    + CLINIC_PHONE_NR + " TEXT, "
                    + CLINIC_EMAIL + " TEXT "
                    +");";

    // SQL statement of the ------- PHYSICIAN ------ table creation
    private static final String SQL_CREATE_TABLE_PHYSICIAN =
            "CREATE TABLE " + TABLE_PHYSICIAN + "("
                    + PHY_PERSON_ID + " TEXT NOT NULL, "
                    + PHY_CLINIC_ID + " TEXT NOT NULL, "
                    + PHY_EMPLOYEE_NR + " TEXT, "
                    + PHY_TITLE + " TEXT, "
                    + " PRIMARY KEY ("+ PHY_PERSON_ID +","+ PHY_CLINIC_ID +"), "
                    + " UNIQUE ("+ PHY_CLINIC_ID +","+PHY_EMPLOYEE_NR+"), "
                    + " FOREIGN KEY("+ PHY_PERSON_ID+") REFERENCES "+ TABLE_PERSON +"("+ PERSON_ID +") ON DELETE CASCADE, "
                    + " FOREIGN KEY("+ PHY_CLINIC_ID+") REFERENCES "+ TABLE_CLINIC +"("+ CLINIC_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ------- PATIENT ------ table creation
    private static final String SQL_CREATE_TABLE_PATIENT =
            "CREATE TABLE " + TABLE_PATIENT + "("
                    + PATIENT_PER_ID + " TEXT NOT NULL, "
                    + PATIENT_CLINIC_P + " TEXT NOT NULL, "
                    + PATIENT_PATIENT_NR + " TEXT, "
                    + PATIENT_HEIGHT + " TEXT, "
                    + PATIENT_WEIGHT + " TEXT, "
                    + PATIENT_BMI + " TEXT, "
                    + PATIENT_HEALTH_ISSUES + " TEXT, "
                    + " PRIMARY KEY ("+ PATIENT_PER_ID +","+ PATIENT_CLINIC_P +"), "
                    + " UNIQUE ("+ PATIENT_CLINIC_P +","+PATIENT_PATIENT_NR+"), "
                    + " FOREIGN KEY("+ PATIENT_PER_ID+") REFERENCES "+ TABLE_PERSON +"("+ PERSON_ID +") ON DELETE CASCADE,"
                    + " FOREIGN KEY("+ PATIENT_CLINIC_P+") REFERENCES "+ TABLE_CLINIC +"("+ CLINIC_ID +") ON DELETE CASCADE "
                    +");";
    //================================================================================

    //============================= TRIGGERS =========================================
    // SQL TRIGGER WHEN DELETE SENSOR_SOURCE
    private static final String SQL_TRIGGER_DELETE_SENSOR_SOURCE =
            "CREATE TRIGGER Delete_Sensor_Source_trigger "
                    + " AFTER DELETE ON " +TABLE_SENSOR_SOURCE
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_CHANNEL
                    + " WHERE "
                    + CHANNEL_S_ID + " = OLD."+SENSOR_SOURCE_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE CHANNEL
    private static final String SQL_TRIGGER_DELETE_CHANNEL =
            "CREATE TRIGGER Delete_Channel_trigger "
                    + " AFTER DELETE ON " +TABLE_CHANNEL
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_RECORD
                    + " WHERE "
                    + RECORD_S_ID + " = OLD."+CHANNEL_S_ID+ " AND "
                    + RECORD_CH_NR + " = OLD."+CHANNEL_NR+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE DATA_RECORD
    private static final String SQL_TRIGGER_DELETE_RECORD =
            "CREATE TRIGGER Delete_data_record_trigger "
                    + " AFTER DELETE ON " +TABLE_RECORD
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_SAMPLE
                    + " WHERE "
                    + SAMPLE_RECORD_ID + " = OLD."+RECORD_ID+"; "
                    + " DELETE FROM "+ TABLE_RECORD_ANNOTATION
                    + " WHERE "
                    + ANNOTATION_RECORD_ID + " = OLD."+RECORD_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE PATIENT
    private static final String SQL_TRIGGER_DELETE_PATIENT =
            "CREATE TRIGGER Delete_Patient_trigger "
                    + " AFTER DELETE ON " +TABLE_PATIENT
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_RECORD
                    + " WHERE "
                    + RECORD_PATIENT_ID + " = OLD."+PATIENT_PER_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE PHYSICIAN
    private static final String SQL_TRIGGER_DELETE_PHYSICIAN =
            "CREATE TRIGGER Delete_Physician_trigger "
                    + " AFTER DELETE ON " +TABLE_PHYSICIAN
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_RECORD
                    + " WHERE "
                    + RECORD_PHYSICIAN_ID + " = OLD."+PHY_PERSON_ID+"; "
                    + " END";

    // SQL TRIGGER WHEN DELETE PERSON
    private static final String SQL_TRIGGER_DELETE_PERSON =
            "CREATE TRIGGER Delete_Person_trigger "
                    + " AFTER DELETE ON " +TABLE_PERSON
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_PATIENT
                    + " WHERE "
                    + PATIENT_PER_ID + " = OLD."+PERSON_ID+"; "
                    + " DELETE FROM "+ TABLE_PHYSICIAN
                    + " WHERE "
                    + PHY_PERSON_ID + " = OLD."+PERSON_ID+"; "
                    + " END";
    // SQL TRIGGER WHEN DELETE CLINIC
    private static final String SQL_TRIGGER_DELETE_CLINIC =
            "CREATE TRIGGER Delete_Clinic_trigger "
                    + " AFTER DELETE ON " +TABLE_CLINIC
                    + " FOR EACH ROW "
                    + " BEGIN "
                    + " DELETE FROM "+ TABLE_PATIENT
                    + " WHERE "
                    + PATIENT_CLINIC_P + " = OLD."+CLINIC_ID+"; "
                    + " DELETE FROM "+ TABLE_PHYSICIAN
                    + " WHERE "
                    + PHY_CLINIC_ID + " = OLD."+CLINIC_ID+"; "
                    + " END";

    //================================================================================
    public OSADBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CHANNEL);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_RECORD);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_ANNOTATION);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SAMPLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PERSON);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CLINIC);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PHYSICIAN);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PATIENT);

        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_CLINIC);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_PERSON);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_PHYSICIAN);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_PATIENT);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_RECORD);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_CHANNEL);
        sqLiteDatabase.execSQL(SQL_TRIGGER_DELETE_SENSOR_SOURCE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(TAG,"DANGER AREA, upgrade from "+i+" to "+i1);

        //DELETE ALL TABLES
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CHANNEL);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_RECORD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+SQL_CREATE_TABLE_ANNOTATION);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SAMPLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PERSON);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CLINIC);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PHYSICIAN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PATIENT);

        //AND RECREATE THEM
        onCreate(sqLiteDatabase);
    }



}