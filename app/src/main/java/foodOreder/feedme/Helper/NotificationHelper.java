package foodOreder.feedme.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import foodOreder.feedme.R;

public class NotificationHelper extends ContextWrapper {

    private static final String FEED_ME_CHANEL_ID="foodOreder.feedme";

    private static final String FEED_ME_CHANEL_NAME="Feed Me";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            createChannel();
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel fmChannel = new NotificationChannel(FEED_ME_CHANEL_ID,FEED_ME_CHANEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        fmChannel.enableLights(true);
        fmChannel.enableVibration(true);
        fmChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManger().createNotificationChannel(fmChannel);

    }

    public NotificationManager getManger() {
        if(manager == null)
        {
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getFeedMeChannelNotification(String title, String body, PendingIntent contentIntent, Uri soundUri){
        return new android.app.Notification.Builder(getApplicationContext(),FEED_ME_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getFeedMeChannelNotification(String title, String body, Uri soundUri){
        return new android.app.Notification.Builder(getApplicationContext(),FEED_ME_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}

