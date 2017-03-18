package no.uio.ifi.viettt.mscosa.EDFManagement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class EDFAnnotation{
    private long timestampRecord;
    private double onSet = 0;
    private double duration = 0;
    private boolean timekeeping;
    private final List<String> annotationsList = new ArrayList<>();

    public EDFAnnotation(){
    }

    public double getOnSet() {
        return onSet;
    }

    public void setOnSet(double onSet) {
        this.onSet = onSet;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public List<String> getAnnotationsList() {
        return annotationsList;
    }

    public void addAnnotation(String ann){
        annotationsList.add(ann);
    }

    public void printInfo(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom

        if(!timekeeping) System.out.println("Onset: "+onSet+" means "+ (sdf.format(timestampRecord+1000*onSet)) +", duration: "+duration);
        else System.out.println("TIME KEEPING Onset: "+onSet+" means "+ (sdf.format(timestampRecord+1000*onSet)) +", duration: "+duration);
        for (String s : annotationsList) {
            System.out.print(s+ ", ");
        }
        System.out.println();
    }

    public String annotationToString(){
        String ret = duration+";;";
        for (String s : annotationsList) {
            ret += s+";;";
        }
        return ret;
    }

    public void setTimekeeping(boolean timekeeping) {
        this.timekeeping = timekeeping;
    }

    public long getTimestampRecord() {
        return timestampRecord;
    }

    public void setTimestampRecord(long timestampRecord) {
        this.timestampRecord = timestampRecord;
    }
}
