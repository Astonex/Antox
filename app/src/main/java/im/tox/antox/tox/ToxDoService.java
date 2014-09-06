package im.tox.antox.tox;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import im.tox.antox.R;
import im.tox.antox.activities.MainActivity;
import im.tox.jtoxcore.ToxException;

public class ToxDoService extends Service {

    private static final String TAG = "im.tox.antox.tox.ToxDoService";

    private static ToxSingleton toxSingleton = ToxSingleton.getInstance();
    private PowerManager.WakeLock serviceWL;
    private Context ctx;

    public ToxDoService() {
        super();
    }

    @Override
    public void onCreate() {
        this.ctx = getApplicationContext();
        Log.d(TAG, "onCreate");
        if (!toxSingleton.isInited) {
            toxSingleton.initTox(this.ctx);
            Log.d(TAG, "Initting ToxSingleton");
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.serviceWL =
                pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Antox Service Wakelock");

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(ctx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_actionbar)
                        .setContentTitle("Antox service is running")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        Thread doThread = new Thread() {
            @Override
            public void run() {
                /* Praise the sun */
                while (true) {
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
            }
        };
        serviceWL.acquire();
        startForeground(-1, mBuilder.build());
        doThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int id) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        stopForeground(true);
        toxSingleton.isInited = false;
        try {
            toxSingleton.jTox.killTox();
        } catch (ToxException e) {
            e.printStackTrace();
        }
        toxSingleton = null;
        serviceWL.release();
        super.onDestroy();
    }
}
