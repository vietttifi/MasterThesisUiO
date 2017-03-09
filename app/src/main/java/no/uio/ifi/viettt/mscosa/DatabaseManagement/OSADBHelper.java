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
    public static final String CHANNEL_ID = "ch_id";
    public static final String CHANNEL_S_ID = "s_id";
    public static final String CHANNEL_NR = "ch_nr";
    public static final String CHANNEL_NAME = "ch_name";
    public static final String CHANNEL_TRANSDUCER_TYPE = "transducer";
    public static final String CHANNEL_DIMENSION = "dimension";
    public static final String CHANNEL_PHYSICAL_MIN = "phy_min";
    public static final String CHANNEL_PHYSICAL_MAX = "phy_max";
    public static final String CHANNEL_DIGITAL_MIN = "dig_min";
    public static final String CHANNEL_DIGITAL_MAX = "dig_max";
    public static final String CHANNEL_EDF_RESERVED = "s_edf_reserved";


    //Record table and its columns    --------------- RECORD ------------
    public static final String TABLE_RECORD = "RECORD";
    public static final String RECORD_ID = "r_id";
    public static final String RECORD_S_ID = "s_id";
    public static final String RECORD_PHYSICIAN_ID = "p_collect";
    public static final String RECORD_PATIENT_ID = "p_owner";
    public static final String RECORD_TIMESTAMP = "timestamp";
    public static final String RECORD_DESCRIPTIONS = "descriptions";
    public static final String RECORD_FRAGMENT_DURATION = "frag_duration";
    public static final String RECORD_FREQUENCY = "frequency";
    public static final String RECORD_PREFILTERING = "prefiltering";
    public static final String RECORD_USED_EQUIPMENT = "used_equip";
    public static final String RECORD_EDF_RESERVED = "edf_reserved";

    //Record fragment table and its columns ----------- RECORD FRAGMENT ---------
    public static final String TABLE_RECORD_FRAGMENT = "FRAGMENT";
    public static final String FRAGMENT_RECORD_ID = "r_id";
    public static final String FRAGMENT_INDEX = "index_nr";
    public static final String FRAGMENT_TIMESTAMP = "timestamp";

    //Sample table and its columns ----------- SAMPLE ---------
    public static final String TABLE_SAMPLE = "SAMPLE";
    public static final String SAMPLE_RECORD_ID = "r_id";
    public static final String SAMPLE_CHANNEL_ID = "ch_id";
    public static final String SAMPLE_RECORD_FRAGMENT_INDEX = "index_nr";
    public static final String SAMPLE_TIMESTAMP = "timestamp";
    public static final String SAMPLE_VALUE_FLOAT = "sample_value_float";
    public static final String SAMPLE_VALUE_ANNO = "sample_value_anno";

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
    public static final String PHY_CLINIC_ID = "clinic_code_f";
    public static final String PHY_TITLE = "title";

    //PATIENT table and its columns
    public static final String TABLE_PATIENT = "PATIENT";
    public static final String PATIENT_PER_ID = "p_id";
    public static final String PATIENT_CLINIC_P = "clinic_code_p";
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
    private static final String DATABASE_NAME = "osamsc.db";
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
                    + CHANNEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CHANNEL_S_ID + " TEXT NOT NULL, "
                    + CHANNEL_NR + " INTEGER NOT NULL, "
                    + CHANNEL_NAME + " TEXT NOT NULL, "
                    + CHANNEL_TRANSDUCER_TYPE + " TEXT, "
                    + CHANNEL_DIMENSION + " TEXT, "
                    + CHANNEL_PHYSICAL_MIN + " REAL, "
                    + CHANNEL_PHYSICAL_MAX + " REAL, "
                    + CHANNEL_DIGITAL_MIN + " INTEGER, "
                    + CHANNEL_DIGITAL_MAX + " INTEGER, "
                    + CHANNEL_EDF_RESERVED + " BLOB, "
                    + " UNIQUE ("+ CHANNEL_NR +","+CHANNEL_S_ID+"), "
                    + " FOREIGN KEY( "+ CHANNEL_S_ID +" ) REFERENCES "+ TABLE_SENSOR_SOURCE +"("+ SENSOR_SOURCE_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ---------- RECORD ------- table creation
    private static final String SQL_CREATE_TABLE_RECORD =
            "CREATE TABLE " + TABLE_RECORD + "("
                    + RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RECORD_S_ID + " TEXT NOT NULL, "
                    + RECORD_PHYSICIAN_ID + " TEXT NOT NULL, "
                    + RECORD_PATIENT_ID + " TEXT NOT NULL, "
                    + RECORD_TIMESTAMP + " INTEGER NOT NULL, "
                    + RECORD_DESCRIPTIONS + " TEXT, "
                    + RECORD_FRAGMENT_DURATION + " INTEGER NOT NULL, "
                    + RECORD_FREQUENCY + " REAL, "
                    + RECORD_PREFILTERING + " TEXT, "
                    + RECORD_USED_EQUIPMENT + " TEXT, "
                    + RECORD_EDF_RESERVED + " BLOB, "
                    + " UNIQUE ("+ RECORD_S_ID +","+RECORD_TIMESTAMP+","+RECORD_PHYSICIAN_ID+","+RECORD_PATIENT_ID+"), "
                    + " FOREIGN KEY( "+ RECORD_S_ID +" ) REFERENCES "+ TABLE_SENSOR_SOURCE +"("+ SENSOR_SOURCE_ID +") ON DELETE CASCADE, "
                    + " FOREIGN KEY( "+ RECORD_PHYSICIAN_ID +" ) REFERENCES "+ TABLE_PHYSICIAN +"("+ PHY_PERSON_ID +") ON DELETE CASCADE, "
                    + " FOREIGN KEY( "+ RECORD_PATIENT_ID +" ) REFERENCES "+ TABLE_PATIENT +"("+ PATIENT_PER_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ---------- FRAGMENT ------- table creation
    private static final String SQL_CREATE_TABLE_RECORD_FRAGMENT =
            "CREATE TABLE " + TABLE_RECORD_FRAGMENT + "("
                    + FRAGMENT_RECORD_ID + " INTEGER NOT NULL, "
                    + FRAGMENT_INDEX + " INTEGER NOT NULL, "
                    + FRAGMENT_TIMESTAMP + " INTEGER NOT NULL, "
                    + " PRIMARY KEY ("+ FRAGMENT_RECORD_ID +","+FRAGMENT_INDEX+"), "
                    + " FOREIGN KEY( "+ FRAGMENT_RECORD_ID +" ) REFERENCES "+ TABLE_RECORD +"("+ RECORD_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ------- SAMPLE ------ table creation
    private static final String SQL_CREATE_TABLE_SAMPLE =
            "CREATE TABLE " + TABLE_SAMPLE + "("
                    + SAMPLE_RECORD_ID + " INTEGER NOT NULL, "
                    + SAMPLE_CHANNEL_ID + " INTEGER NOT NULL, "
                    + SAMPLE_RECORD_FRAGMENT_INDEX + " INTEGER NOT NULL, "
                    + SAMPLE_TIMESTAMP + " INTEGER NOT NULL, "
                    + SAMPLE_VALUE_FLOAT + " REAL , "
                    + SAMPLE_VALUE_ANNO + " TEXT , "
                    + " PRIMARY KEY ("+ SAMPLE_RECORD_ID +","+ SAMPLE_RECORD_FRAGMENT_INDEX +","+ SAMPLE_CHANNEL_ID +"," +SAMPLE_TIMESTAMP+"), "
                    + " FOREIGN KEY( "+SAMPLE_CHANNEL_ID+") REFERENCES "+ TABLE_CHANNEL +"("+CHANNEL_ID+") ON DELETE CASCADE, "
                    + " FOREIGN KEY("+ SAMPLE_RECORD_ID+","+ SAMPLE_RECORD_FRAGMENT_INDEX+") "
                    + " REFERENCES "+ TABLE_RECORD_FRAGMENT +"("+ FRAGMENT_RECORD_ID +","+FRAGMENT_INDEX+") ON DELETE CASCADE "
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
                    + PHY_TITLE + " TEXT, "
                    + " PRIMARY KEY ("+ PHY_PERSON_ID +","+ PHY_CLINIC_ID +"), "
                    + " FOREIGN KEY("+ PHY_PERSON_ID+") REFERENCES "+ TABLE_PERSON +"("+ PERSON_ID +") ON DELETE CASCADE, "
                    + " FOREIGN KEY("+ PHY_CLINIC_ID+") REFERENCES "+ TABLE_CLINIC +"("+ CLINIC_ID +") ON DELETE CASCADE "
                    +");";

    // SQL statement of the ------- PATIENT ------ table creation
    private static final String SQL_CREATE_TABLE_PATIENT =
            "CREATE TABLE " + TABLE_PATIENT + "("
                    + PATIENT_PER_ID + " TEXT NOT NULL, "
                    + PATIENT_CLINIC_P + " TEXT NOT NULL, "
                    + PATIENT_HEIGHT + " TEXT, "
                    + PATIENT_WEIGHT + " TEXT, "
                    + PATIENT_BMI + " TEXT, "
                    + PATIENT_HEALTH_ISSUES + " TEXT, "
                    + " PRIMARY KEY ("+ PATIENT_PER_ID +","+ PATIENT_CLINIC_P +"), "
                    + " FOREIGN KEY("+ PATIENT_PER_ID+") REFERENCES "+ TABLE_PERSON +"("+ PERSON_ID +") ON DELETE CASCADE,"
                    + " FOREIGN KEY("+ PATIENT_CLINIC_P+") REFERENCES "+ TABLE_CLINIC +"("+ CLINIC_ID +") ON DELETE CASCADE "
                    +");";
    //================================================================================

    //============================= TRIGGERS =========================================
    //================================================================================
    public OSADBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CHANNEL);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_RECORD);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_RECORD_FRAGMENT);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SAMPLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PERSON);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CLINIC);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PHYSICIAN);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PATIENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(TAG,"DANGER AREA, upgrade from "+i+" to "+i1);

        //DELETE ALL TABLES
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SENSOR_SOURCE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CHANNEL);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_RECORD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_RECORD_FRAGMENT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SAMPLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PERSON);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_CLINIC);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PHYSICIAN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PATIENT);

        //AND RECREATE THEM
        onCreate(sqLiteDatabase);
    }



}