package tech.bogomolov.incomingsmsgateway;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Timer;
import java.util.TimerTask;

public class RunningService extends Service {
    private String serverUrl = "";
    private String password = "";
    private String username = "";

    private static final String CHANNEL_2_ID = "";
    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode;

    /**
     * interface for clients that bind
     */
    IBinder mBinder;

    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind;

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
    }

    /**
     * The service is starting, due to a call to startService()
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand( Intent intent, int flags, int startId) {
        NotificationChannel channel2 = new NotificationChannel(
                CHANNEL_2_ID,
                "Channel 2",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel2.setDescription("This is channel 2");
        this.username = intent.getStringExtra("username");
        this.password = intent.getStringExtra("password");
        this.serverUrl = intent.getStringExtra("serverUrl");

        System.out.println("<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(username);
        System.out.println(username);
        System.out.println(password);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel2);

        Notification myNotification = new Notification.Builder(this,CHANNEL_2_ID)
                .setContentTitle("ZedSMS ")
                .setContentText("ZedSMS is Active")
                .build();

        // Start the foreground service immediately.
        startForeground((int) System.currentTimeMillis(),myNotification);
        AuthenticatUser authenticateUser = new AuthenticatUser(this.username, this.password, this.serverUrl);
        authenticateUser.execute();
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                AuthenticatUser authenticateUser = new AuthenticatUser(username, password, serverUrl);
                authenticateUser.execute();
            }
        }, 0, 30 * 60 * 1000);
        return START_STICKY;
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }

}
