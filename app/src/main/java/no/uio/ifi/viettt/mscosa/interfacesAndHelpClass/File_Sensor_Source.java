package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 11/02/2017.
 */

public class File_Sensor_Source {
    private int index;
    private int id;

    private boolean running = true;
    private String filePath;
    private String title;
    private int progress;

    private SensorSource sensor_source;

    public final Object lock = new Object();

    public File_Sensor_Source(){
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public SensorSource getSensor_source() {
        return sensor_source;
    }

    public void setSensor_source(SensorSource sensor_source) {
        this.sensor_source = sensor_source;
    }
}
