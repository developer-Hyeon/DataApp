package sinabro.today.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import me.leolin.shortcutbadger.ShortcutBadger;
import sinabro.today.R;
import sinabro.today.main.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private int pushFlag;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {

            pushFlag = 0;
            String title = remoteMessage.getData().get("title");
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
            int flag = settings.getInt(""+title, 0);
            int logout = settings.getInt("logout",0);
            if(logout == 1)
                return;
            if(flag == 1)
                return;

            String str = remoteMessage.getData().get("click_action");
            String text = remoteMessage.getData().get("text");

            assert str != null;
            String click_action = str.substring(0,1);
            String url = str.substring(1);
            if(click_action.equals("p")){
                int propose = settings.getInt("propose", 0);
                if(propose == 1){
                    Log.d("namgung", "대화요청 푸시알림을 받지 않습니다.");
                    return;
                }else{
                    Log.d("namgung", "대화요청 푸시알림을 받습니다.");
                }


            } else if(click_action.equals("m")) {
                pushFlag = 1;
                int message = settings.getInt("message", 0);
                if(message == 1){
                    Log.d("namgung", "메세지 푸시알림을 받지 않습니다.");
                    return;
                }else{
                    Log.d("namgung", "메세지 푸시알림을 받습니다.");
                }
            } else if(click_action.equals("r")) {
                int chatroom = settings.getInt("chatroom", 0);
                if(chatroom == 1){
                    Log.d("namgung", "대화방 생성 푸시알림을 받지 않습니다.");
                    return;
                }else{
                    Log.d("namgung", "대화방 생성 푸시알림을 받습니다.");
                }
            } // 메세지 알림 설정에따라 메세지를 보냄

            int badgeMessage, badgeLove;
            SharedPreferences badgeSetting = getApplicationContext().getSharedPreferences("sinabro",0);
            badgeMessage = badgeSetting.getInt("badgeMessage",0);
            badgeLove =  badgeSetting.getInt("badgeLove",0);
            Log.d("namgung","배지카운트 "+(badgeMessage+badgeLove) );
            ShortcutBadger.applyCount(getApplicationContext(), (badgeMessage+badgeLove));
            sendNotification(title, text, click_action, url);

        }
    }

    private void sendNotification(String title, String text, String click_action,String url) {

        // 푸시알림을 만들어주는 메소드
        String channelId = getString(R.string.namgung_channel_id);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = getString(R.string.namgung_channel_name);

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            //notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

        } // 첫 가입시 푸시를 위한 채널생성

        //전달된 액티비티에 따라 분기하여 해당 액티비티를 오픈하도록 한다.
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        if (click_action.equals("p")) {
            intent.putExtra("move",2);
        }
        else if(click_action.equals("m") || click_action.equals("r")){
            intent.putExtra("move",1);
        }else{
            intent.putExtra("move",0);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, createID() /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        if(url.equals("")){ // 원본사진이 없는 유저가 보낸 푸시
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.applogo)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

            if(notificationManager != null)
                if(pushFlag == 0){
                    notificationManager.notify(createID() /* ID of notification */, notificationBuilder.build()); // 푸시가 쌓인다.
                    Log.d("namgung","푸시가 쌓인다.");
                }else{
                    notificationManager.notify(5654 /* ID of notification */, notificationBuilder.build()); // 푸시가 쌓이지 않는다.
                    Log.d("namgung","푸시가 쌓이지 않는다.");
                }


        }else{
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.applogo)
                            .setContentTitle(title)
                            .setLargeIcon(getBitmapFromURL(url))
                            .setContentText(text)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);


            if(notificationManager != null){
                if(pushFlag == 0)
                    notificationManager.notify(createID() /* ID of notification */, notificationBuilder.build()); // 푸시가 쌓인다.
                else
                    notificationManager.notify(5654 /* ID of notification */, notificationBuilder.build()); // 푸시가 쌓이지 않는다.
            }
        }


    } // sendNotification


    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.KOREA).format(now));

        return id;
    }

}
