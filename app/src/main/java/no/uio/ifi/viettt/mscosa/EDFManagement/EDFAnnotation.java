package no.uio.ifi.viettt.mscosa.EDFManagement;

import java.util.ArrayList;
import java.util.List;

public class EDFAnnotation{
    private double onSet = 0;
    private double duration = 0;
    private final List<String> annotationsList = new ArrayList<>();

    EDFAnnotation(double onSet, double duration, String[] annotations){
        this.onSet = onSet;
        this.duration = duration;

        for (int i = 0; i < annotations.length; i++){
            if (annotations[i] != null && !annotations[i].trim().equals(""))
                annotationsList.add(annotations[i]);
        }
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
}
