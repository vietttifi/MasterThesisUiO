package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 26/12/2016.
 */

public class TimeAxisValueFormatter implements IAxisValueFormatter
{
    private BarLineChartBase<?> chart;
    private SensorSource sensorSource;

    public TimeAxisValueFormatter(BarLineChartBase<?> chart, SensorSource sensorSource) {
        this.chart = chart;
        this.sensorSource = sensorSource;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        float frequence = 1;
        for(Channel s: sensorSource.getClient_thread().getChannelList()){
            frequence = s.frequence; break;
        }

        long original_time = sensorSource.getStartDateTime() + (long)((value*1000)/frequence);
        Calendar dateConverter = Calendar.getInstance();
        dateConverter.setTimeInMillis(original_time);

        int year = dateConverter.get(Calendar.YEAR);
        int month = dateConverter.get(Calendar.MONTH);
        int day_of_month = dateConverter.get(Calendar.DAY_OF_MONTH);

        int hour = dateConverter.get(Calendar.HOUR_OF_DAY);
        int minute = dateConverter.get(Calendar.MINUTE);
        int second = dateConverter.get(Calendar.SECOND);

        int milis = dateConverter.get(Calendar.MILLISECOND);

        if (chart.getVisibleXRange() >= 50 * 30) {
            return (month+1) + "/" + year;
        } else if(chart.getVisibleXRange() > 50 * 20){
            return hour+"h:"+minute+"m";
        }else if (chart.getVisibleXRange() > 50 * 10){
            return minute+"m:"+second+"s";
        }else{
            return second+"s:"+milis+"ms";
        }
    }
}