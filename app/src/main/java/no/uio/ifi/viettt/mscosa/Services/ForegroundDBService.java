package no.uio.ifi.viettt.mscosa.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;

/**
 * Created by viettt on 16/04/2017.
 */

public class ForegroundDBService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Toast.makeText(this,"Start Service",Toast.LENGTH_SHORT).show();
            Log.i("DB SERVICE", "Received Start Foreground Intent ");


            Intent notificationIntent = new Intent();
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("MSc viettt")
                    .setTicker("OSA DB collector")
                    .setContentText("BITalino")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        }
        else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Toast.makeText(this,"Stop Service",Toast.LENGTH_SHORT).show();
            Log.i("DB SERVICE", "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("DB SERVICE", "In onDestroy");
    }

    private final IBinder mBinder = new DBService();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DBService extends Binder{
        public ForegroundDBService  getService(){
            return ForegroundDBService.this;
        }
    }

    public void manageIsStoring(long id, ArrayList<Sample> aSamplesBuff) {
        Log.i("DB SERVICE", id+" request saving");
        SampleAdapter sampleAdapter = new SampleAdapter(getApplicationContext());
        sampleAdapter.saveSampleToDB(aSamplesBuff);
        sampleAdapter.close();
    }

}
