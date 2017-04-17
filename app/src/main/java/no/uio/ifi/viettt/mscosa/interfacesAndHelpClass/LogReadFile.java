package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 11/02/2017.
 */

public class LogReadFile {
    private long recordNR;
    private String path;
    private SensorSource sensorSource;
    private Channel[] channels;
    private Patient patient;
    private Clinic clinic;
    private long nr_of_bytes_have_read;

    public LogReadFile(String path, SensorSource sensorSource, Channel[] channels, Patient patient, Clinic clinic, int nr_of_bytes_have_read){
        this.path = path;
        this.sensorSource = sensorSource;
        this.channels = channels;
        this.patient = patient;
        this.clinic = clinic;
        this.nr_of_bytes_have_read = nr_of_bytes_have_read;
    }

    public String getPath() {
        return path;
    }

    public SensorSource getSensorSource() {
        return sensorSource;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public Patient getPatient() {
        return patient;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public long getNr_of_bytes_have_read() {
        return nr_of_bytes_have_read;
    }

    public void setNr_of_bytes_have_read(long nr_of_bytes_have_read) {
        this.nr_of_bytes_have_read = nr_of_bytes_have_read;
    }

    public void increaseBytesRead(long bytesHasRead){
        this.nr_of_bytes_have_read += bytesHasRead;
    }

    public long getRecordNR() {
        long tmp = recordNR;
        recordNR++;
        return tmp;
    }

    public void setRecordNR(long recordNR) {
        this.recordNR = recordNR;
    }
}
