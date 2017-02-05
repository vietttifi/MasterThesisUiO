package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 26/12/2016.
 */

public class PlotMarkerView extends MarkerView {

    private TextView lblMarker;
    private SensorSource sensorSource;
    private DateFormat mDataFormat;
    private Date mDate;

    public PlotMarkerView (Context context, int layoutResource, SensorSource sensorSource) {
        super(context, layoutResource);

        lblMarker = (TextView) findViewById(R.id.lblMarker);
        this.sensorSource = sensorSource;
        this.mDataFormat = new SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.ENGLISH);
        this.mDate = new Date();
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        float frequence = 1;
        for(Channel s: sensorSource.getChannelsOfThisSource().values()){
            frequence = s.frequence; break;
        }
        long original_time = sensorSource.getStartDateTime() + (long)((e.getX()*1000)/frequence);
        lblMarker.setText(e.getY() + " at " + formatToMMSS(original_time));
        super.refreshContent(e,highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    private String formatToMMSS(long timestamp){

        try{
            mDate.setTime(timestamp);
            return mDataFormat.format(mDate);
        }
        catch(Exception ex){
            return "mm:ss";
        }
    }
}