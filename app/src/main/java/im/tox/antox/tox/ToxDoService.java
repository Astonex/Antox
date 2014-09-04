package im.tox.antox.tox;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import im.tox.antox.R;
import im.tox.jtoxcore.ToxException;

public class ToxDoService extends Service {

    private static final String TAG = "im.tox.antox.tox.ToxDoService";

    private static ToxSingleton toxSingleton = ToxSingleton.getInstance();
    private boolean isRunning = false;
    private Notification mNotification;

    public ToxDoService() {
        super();
    }


    @Override
    public void onCreate() {
        Log.d("ToxDoService", "onCreate");
        if (!toxSingleton.isInited) {
            toxSingleton.initTox(getApplicationContext());
            Log.d("ToxDoService", "Initting ToxSingleton");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int id) {
        Log.d("ToxDoService", "onStart");
        if (this.isRunning) return START_NOT_STICKY;

        this.isRunning = true;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock serviceWL =
                pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Antox Service Wakelock");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_actionbar)
                        .setContentTitle("Antox service is running")
                        .setDefaults(Notification.DEFAULT_ALL);

        mNotification = mBuilder.build();

        Thread doThread = new Thread() {
            @Override
            public void run() {
                /* Praise the sun */
                serviceWL.acquire();
                while (isRunning) {
                    try {
                        Thread.sleep(toxSingleton.jTox.doToxInterval());
                        toxSingleton.jTox.doTox();
                    } catch (ToxException e) {
                        Log.e(TAG, e.getError().toString());
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
                serviceWL.release();
            }
        };
        startForeground(id, mNotification);
        doThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
        stopForeground(true);
        toxSingleton.isInited = false;
        try {
            toxSingleton.jTox.killTox();
        } catch (ToxException e) {
            e.printStackTrace();
        }
        toxSingleton = null;
        Log.d("ToxDoService", "onDestroy() called");
        super.onDestroy();
    }
}
