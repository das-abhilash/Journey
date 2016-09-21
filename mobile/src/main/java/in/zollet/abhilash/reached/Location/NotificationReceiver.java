package in.zollet.abhilash.reached.Location;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;

import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.UI.MainActivity;
import in.zollet.abhilash.reached.geofence.UserGeofence;

public  class NotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 10;
    static int ed = 1;
    public NotificationReceiver() {
    }
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    public static final String LocationPREFERENCES = "reachedGeoFence" ;

    @Override
    public void onReceive(final Context context, Intent intent) {

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        sharedpreferences = context.getSharedPreferences(LocationPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        String action = intent.getStringExtra("action");
        if (action.equals("dismiss")){

            editor.putInt("isServiceRunning", -1);
            editor.remove("ServiceObject");
            editor.apply();

            PackageManager pm = context.getPackageManager();
            ComponentName locationreciever =
                    new ComponentName(context,
                            GpsLocationReceiver.class);
            pm.setComponentEnabledSetting(
                    locationreciever,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            ComponentName notificationreciever =
                    new ComponentName(context,
                            NotificationReceiver.class);
            pm.setComponentEnabledSetting(
                    notificationreciever,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);




            if(LocationService.defaultRingtone != null)
            LocationService.defaultRingtone.stop();
            if(LocationService.vibrator != null)
            LocationService.vibrator.cancel();
        }else if (action.equals("snooze")){
            editor.putInt("isServiceRunning",0);
            editor.apply();
            if(LocationService.defaultRingtone != null)
                LocationService.defaultRingtone.stop();
            if(LocationService.vibrator != null)
                LocationService.vibrator.cancel();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(LocationService.defaultRingtone != null)
                        LocationService.defaultRingtone.stop();
                    if(LocationService.vibrator != null)
                        LocationService.vibrator.cancel();

                    int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);
                    String geofenceJson = sharedpreferences.getString("ServiceObject", null);

                    Gson gson = new Gson();
                    UserGeofence geofence = gson.fromJson(geofenceJson, UserGeofence.class);
                    if(isServiceRunning != -1 && geofence != null  ){
                        boolean vibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_new_message_vibrate", true);
                        if(vibrate) {
                            long pattern[] = {500,100,500,100,500,350};
                            LocationService.vibrator.vibrate(pattern,3);
                        }
                        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_new_message", true))
                            showHeadsUpNotificattion(geofence.getName(),context);
                    }
                }
            }, Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString("snooze", "3"))*60000);
            /*Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);


            Ringtone defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);*/
            //if(LocationService.defaultRingtone != null)
            // LocationService.defaultRingtone.stop();
        }
    }

    private void showHeadsUpNotificattion(String triggeringGeofenceName, Context context) {

        Intent dismissIntent = new Intent(context, NotificationReceiver.class);
        dismissIntent.putExtra("action","dismiss");
        // dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent.getBroadcast(context, 0, dismissIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultRingteUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        Ringtone defaultRingne = RingtoneManager.getRingtone(context, defaultRingteUri);

        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);//(this, MyService.class);
        snoozeIntent.putExtra("action","snooze");
        // snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent piSnooze = PendingIntent./*getService*/getBroadcast(context, 1/*0*/, snoozeIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

        //Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        /*Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);


        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);*/
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("max_volume", true)) {
            AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int maxVolumeForDevice = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            myAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeForDevice, AudioManager.FLAG_ALLOW_RINGER_MODES);
        }

        if(LocationService.defaultRingtone != null)
            LocationService.defaultRingtone.play();


        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 4/*0*/, notificationIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentTitle("Destination Alert")
                                //.setSound(defaultRingtoneUri)
                        .setAutoCancel(true)
                        .setContentText("You're about to reach at : "+ triggeringGeofenceName)
                        .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //must give priority to High, Max which will considered as heads-up notification
                        .addAction(R.drawable.ic_cancel_black_24dp,
                                "Dismiss"/*getString(R.string.dismiss)*/, piDismiss)
                        .addAction(R.drawable.ic_snooze_black_24dp,
                                "Snooze"/*getString(R.string.snooze)*/, piSnooze)
                        .setDeleteIntent(piDismiss)
                        .setContentIntent(notificationPendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());


    }
}
