package szte.beadando.balatonilatnivalok.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import szte.beadando.balatonilatnivalok.R;

public class NotificationSender {
    private static final String CHANNEL_ID = "sights_of_balaton_notification_channel";
    private final int NOTIFICATON_ID = 0;

    private NotificationManager notificationManager;
    private Context context;

    public NotificationSender(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION_CODES.O > Build.VERSION.SDK_INT) return;

        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "Sights of Balaton notification channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Notification from Sights of Balaton application");
        this.notificationManager.createNotificationChannel(notificationChannel);
    }

    public void sendNotificationMessage(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Sights of Balaton")
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon);

        this.notificationManager.notify(NOTIFICATON_ID, builder.build());
    }
}
