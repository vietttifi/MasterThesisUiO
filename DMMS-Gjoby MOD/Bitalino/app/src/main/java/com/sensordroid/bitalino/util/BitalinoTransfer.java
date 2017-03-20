package com.sensordroid.bitalino.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by sveinpg on 03.03.16.
 */
public class BitalinoTransfer {
    public final static int TYPE_OFF = 0;
    public final static int TYPE_RAW = 1;
    public final static int TYPE_LUX = 2;
    public final static int TYPE_ACC = 3;
    public final static int TYPE_PZT = 4;
    public final static int TYPE_ECG = 5;
    public final static int TYPE_EEG = 6;
    public final static int TYPE_EDA = 7;
    public final static int TYPE_EMG = 8;
    public final static int TYPE_TMP = 9;

    private final static double VCC = 3.3; // Operating voltage
    private final static int cMin = 208;
    private final static int cMax = 312;

    private final static String[] types = new String[]{"Raw data", "LUX",
            "ACC", "PZT", "ECG", "EEG", "EDA", "EMG", "TMP"};

    private final static String[] metric = new String[]{"Raw data", "Percent", "G-force",
        "Percent", "Millivolt", "Microvolt", "Micro-Siemens", "Millivolt", "Celsius"};

    public static String getMetric(int type){
        return metric[type-1];
    }

    public static String getType(int type){
        return types[type-1];
    }
}
