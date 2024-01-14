package com.amazia_iwallcraft.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.wallpaper.SplashActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


public class OnesignalNotificationHelper implements OneSignal.OSRemoteNotificationReceivedHandler {

    String CHANNEL_ID = "hdwall_ch_1";
    String message, bigpicture, title, cid, cname, url;

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {

        OSNotification notification = notificationReceivedEvent.getNotification();

        if (notification.getActionButtons() != null) {
            for (OSNotification.ActionButton button : notification.getActionButtons()) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "ActionButton: " + button.toString());
            }
        }

        title = notification.getTitle();
        message = notification.getBody();
        bigpicture = notification.getBigPicture();

        try {
            cid = notification.getAdditionalData().getString("post_id");
            cname = notification.getAdditionalData().getString("post_title");
            url = notification.getAdditionalData().getString("external_link");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendNotification(context);

        OSMutableNotification mutableNotification = notification.mutableCopy();
        mutableNotification.setExtender(builder -> builder.setColor(context.getResources().getColor(R.color.colorPrimary)));

        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
        notificationReceivedEvent.complete(null);
    }

    private void sendNotification(Context context) {
        Random random = new Random();
        int noti_id = random.nextInt(100);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent;
        if (cid.equals("") && !url.equals("false") && !url.trim().isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        } else {
            intent = new Intent(context, SplashActivity.class);
            intent.putExtra("cid", cid);
            intent.putExtra("cname", cname);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, noti_id, intent, PendingIntent.FLAG_IMMUTABLE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Push Notification";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setAutoCancel(true)
                .setSound(uri)
                .setLights(Color.RED, 800, 800)
                .setContentText(message)
                .setChannelId(CHANNEL_ID);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder, context));
        try {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (title.trim().isEmpty()) {
            mBuilder.setContentTitle(context.getString(R.string.app_name));
            mBuilder.setTicker(context.getString(R.string.app_name));
        } else {
            mBuilder.setContentTitle(title);
            mBuilder.setTicker(title);
        }

        if (bigpicture != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(message));
        } else {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(noti_id, mBuilder.build());

    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder, Context context) {
        notificationBuilder.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.primary));
        return R.drawable.ic_notification;
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if (src.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }
}