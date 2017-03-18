package no.uio.ifi.viettt.mscosa.SensorsObjects;

/**
 * Created by viettt on 14/03/2017.
 */

public class Annotation {
    private long record_id;
    private double onset;
    private double duration;
    private double timeKeeping;
    private String ann;

    public long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(long record_id) {
        this.record_id = record_id;
    }

    public double getOnset() {
        return onset;
    }

    public void setOnset(double onset) {
        this.onset = onset;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getTimeKeeping() {
        return timeKeeping;
    }

    public void setTimeKeeping(double timeKeeping) {
        this.timeKeeping = timeKeeping;
    }

    public String getAnn() {
        return ann;
    }

    public void setAnn(String ann) {
        this.ann = ann;
    }
}
