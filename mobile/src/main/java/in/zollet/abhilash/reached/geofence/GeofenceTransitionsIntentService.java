package in.zollet.abhilash.reached.geofence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import in.zollet.abhilash.reached.Location.LocationService;
import in.zollet.abhilash.reached.Location.NotificationReceiver;
import in.zollet.abhilash.reached.UI.MainActivity;
import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceColumns;
import in.zollet.abhilash.reached.data.GeoFenceProvider;

/**
 * Created by Abhilash on 8/13/2016.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 10;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = String.valueOf(geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ){
            List<String> triggeringGeofenceIds = new ArrayList<>();
            for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                triggeringGeofenceIds.add(geofence.getRequestId());
            }
            getGeofenceTransitionDetails(triggeringGeofenceIds);

        }
    }

    private void getGeofenceTransitionDetails(List<String> triggeringGeofenceIds) {

        for (String triggeringGeofenceId : triggeringGeofenceIds) {
            String triggeringGeofenceName = "";

            Cursor cursor = getContentResolver().query(GeoFenceProvider.GeoFence.CONTENT_URI,null,
                    GeoFenceColumns.ID + "=?"
                    , new String[]{triggeringGeofenceId},null);
            if (cursor != null) {
                cursor.moveToFirst();
                int ui = cursor.getCount();
                triggeringGeofenceName = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));

                SharedPreferences sharedpreferences;
                sharedpreferences = this.getSharedPreferences("reachedGeoFence", Context.MODE_PRIVATE);

                int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);
                String geofenceJson = sharedpreferences.getString("ServiceObject", null);

                Gson gson = new Gson();
                UserGeofence geofence = gson.fromJson(geofenceJson, UserGeofence.class);
                if(isServiceRunning != -1 && geofence != null && geofence.getName().equals(triggeringGeofenceName)  ){
                    boolean vibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications_new_message_vibrate", true);
                    if(vibrate) {

                        long pattern[] = {50,100,100,250,150,350};
                        LocationService.vibrator.vibrate(pattern, 3);
                    }
                    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications_new_message", true))
                    showHeadsUpNotificattion(triggeringGeofenceName);
                }

            }
            //triggeringGeofenceName = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));

            //showHeadsUpNotificattion(triggeringGeofenceName);


        }
    }

    private void showHeadsUpNotificattion(String triggeringGeofenceName) {
        Intent dismissIntent = new Intent(this, NotificationReceiver.class);
        dismissIntent.putExtra("action","dismiss");
        // dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent.getBroadcast(this, 0, dismissIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultRingteUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        Ringtone defaultRingne = RingtoneManager.getRingtone(getApplicationContext(), defaultRingteUri);

        Intent snoozeIntent = new Intent(this, NotificationReceiver.class);//(this, MyService.class);
        snoozeIntent.putExtra("action","snooze");
        // snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent piSnooze = PendingIntent./*getService*/getBroadcast(this, 1/*0*/, snoozeIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("max_volume", true)) {
            AudioManager myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int maxVolumeForDevice = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            myAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeForDevice, AudioManager.FLAG_ALLOW_RINGER_MODES);
        }
        //Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        /*Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);


        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);*/

        if(LocationService.defaultRingtone != null)
        LocationService.defaultRingtone.play();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 4/*0*/, notificationIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
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
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        /*NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.notification)
                .setContentTitle(triggeringGeofenceName)
                .setContentText("You have Reached : " + triggeringGeofenceName )
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You have Reached : " +triggeringGeofenceName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(0, notification);*/
    }
}
