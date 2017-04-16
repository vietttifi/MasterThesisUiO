package no.uio.ifi.viettt.mscosa.Services;

/**
 * Created by viettt on 16/04/2017.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "no.uio.ifi.viettt.mscosa.main";
        public static String STARTFOREGROUND_ACTION = "no.uio.ifi.viettt.alertdialog.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "no.uio.ifi.viettt.alertdialog.action.stopforeground";
        public static String SAVE_SAMPLES_ACTION = "no.uio.ifi.viettt.alertdialog.action.savesamples";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
