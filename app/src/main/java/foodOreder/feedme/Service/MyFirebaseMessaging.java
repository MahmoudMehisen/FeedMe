package foodOreder.feedme.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Helper.NotificationHelper;
import foodOreder.feedme.MainActivity;
import foodOreder.feedme.OrderStatus;
import foodOreder.feedme.R;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationAPI26(remoteMessage);
            else
                sendNotification(remoteMessage);
        }
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if(Common.currentUser != null) {


            Intent intent = new Intent(this, OrderStatus.class);
            intent.putExtra("userPhone", Common.currentUser.getPhone());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getFeedMeChannelNotification(title, message , pendingIntent, defaultSoundUri);

            helper.getManger().notify(new Random().nextInt(), builder.build());

        }
        else {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getFeedMeChannelNotification(title, message , defaultSoundUri);
            helper.getManger().notify(new Random().nextInt(), builder.build());
        }

    }

    private void sendNotification(RemoteMessage remoteMessage) {


        Map<String,String>data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");


        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());

    }
}
