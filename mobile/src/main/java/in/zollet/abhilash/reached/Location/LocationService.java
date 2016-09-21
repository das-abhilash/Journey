package in.zollet.abhilash.reached.Location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.gson.Gson;

import in.zollet.abhilash.reached.API.Distance;
import in.zollet.abhilash.reached.API.LocationAPI;
import in.zollet.abhilash.reached.API.LocationData;
import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.UI.MainActivity;
import in.zollet.abhilash.reached.geofence.UserGeofence;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.location.LocationManager.GPS_PROVIDER;

/**
 * Created by Abhilash on 8/1/2016.
 */
public class LocationService extends Service
        implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String API_KEY = "AIzaSyB2taazCHzkDHIVLH96KGR9eq1yxbvYfDc";
    public static String text;
    Context mContext;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static int UPDATE_INTERVAL = 240000; // 4 min
    private static int FATEST_INTERVAL = 10000; // 10 sec
    private static int DISPLACEMENT = 600; // 0.6 km

     public static Uri defaultRingtoneUri;
     public static Ringtone defaultRingtone;
    public static Vibrator vibrator;


    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private LocationManager locationManager;

    public LocationService() {
       super();
    }

    public LocationService(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

       // defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
       // defaultRingtone = RingtoneManager.getRingtone(this, defaultRingtoneUri);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String strRingtonePreference = preference.getString("notifications_new_message_ringtone", String.valueOf(RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)));
        defaultRingtoneUri = Uri.parse(strRingtonePreference);
        defaultRingtone = RingtoneManager.getRingtone(this, defaultRingtoneUri);
        vibrator = (Vibrator) LocationService.this.getSystemService(Context.VIBRATOR_SERVICE);
        createLocationRequest();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
        if(!mGoogleApiClient.isConnected())
        mGoogleApiClient.connect();
        sharedpreferences = this.getSharedPreferences(Constants.SHARED_PREFERENCE_REACHED, MODE_PRIVATE);
        editor = sharedpreferences.edit();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        defaultRingtoneUri = null;
        defaultRingtone = null;
        vibrator = null;
        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mContext = this;

        if (mGoogleApiClient.isConnected()) {
            getDistance(getLocation());
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDistance(Location location) {
        final int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);
        String geofenceJson = sharedpreferences.getString("ServiceObject", null);
        Gson gson = new Gson();
        final UserGeofence geofence = gson.fromJson(geofenceJson, UserGeofence.class);

        if (location != null && isServiceRunning != -1 && geofence != null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            String origin = location.getLatitude()+","+location.getLongitude();

            String destination = geofence.getLatitude()+ "," + geofence.getLongitude();
            String key = API_KEY;
            String units = prefs.getString("unit_system", "metric");
            String departureTime = "now";
            String mode = prefs.getString("travel_mode", "driving");
            String language = prefs.getString("language", "en");
            String trafficModel = prefs.getString("traffic_model", "best_guess");
            String transitMode = prefs.getString("transit_mode", "bus");

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            OkHttpClient client = builder.build();
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            LocationAPI locationAPI = restAdapter.create(LocationAPI.class);
            Call<LocationData> call = locationAPI.getLocation(origin, destination, key, units,departureTime,mode,language,trafficModel,transitMode);

            {

                call.enqueue(new retrofit2.Callback<LocationData>() {

                    @Override
                    public void onResponse(Call<LocationData> call, Response<LocationData> response) {

                        if (response.body() != null) {
                            LocationData locate = response.body();
                            if(locate.getStatus().equals("OK")) {
                                String distance = locate.getRoutes().get(0).getLegs().get(0).getDistance().getText();
                                int distanceValue = locate.getRoutes().get(0).getLegs().get(0).getDistance().getValue();

                                String duration = locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic() != null ?
                                        locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic().getText()
                                        : locate.getRoutes().get(0).getLegs().get(0).getDuration().getText();
                                int durationValue = locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic() != null ?
                                        locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic().getValue()
                                        : locate.getRoutes().get(0).getLegs().get(0).getDuration().getValue();

                                editor.putString("distance.key", distance);
                                editor.putString("duration.key", duration);
                                editor.commit();
                                String safd = geofence.getRadius();
int sd = 0;
                                if (distanceValue <= (Float.parseFloat(geofence.getRadius())* 1000.0f) || (durationValue < 420)) {

                                    boolean vibrate = PreferenceManager.getDefaultSharedPreferences(LocationService.this).getBoolean("notifications_new_message_vibrate", true);
                                    if(vibrate) {

                                        long pattern[] = {50,100,100,250,150,350};
                                        vibrator.vibrate(pattern,3);
                                    }
                                    if(PreferenceManager.getDefaultSharedPreferences(LocationService.this).getBoolean("notifications_new_message", true))
                                    showHeadsUpNotificattion(geofence.getName());
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<LocationData> call, Throwable t) {

                    }
                });

            }
        }
    }
    private void showHeadsUpNotificattion(String name) {

        Intent dismissIntent = new Intent(this, NotificationReceiver.class);
        dismissIntent.putExtra("action","dismiss");
        // dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent./*getService*/getBroadcast(this, 0, dismissIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

//set intents and pending intents to call service on click of "snooze" action button of notification
        Intent snoozeIntent = new Intent(this, NotificationReceiver.class);//(this, MyService.class);
        snoozeIntent.putExtra("action","snooze");
        // snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent piSnooze = PendingIntent./*getService*/getBroadcast(this, 1/*0*/, snoozeIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);

        //Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("max_volume", true)) {
            AudioManager myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int maxVolumeForDevice = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            myAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeForDevice, AudioManager.FLAG_ALLOW_RINGER_MODES);
        }

        if(LocationService.defaultRingtone != null)
        defaultRingtone.play();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 4/*0*/, notificationIntent, /*0*/PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentTitle("Reached")
                        //.setSound(defaultRingtoneUri)
                        .setAutoCancel(true)
                        .setContentText("You're about to reach at: " + name)
                        .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //must give priority to High, Max which will considered as heads-up notification
                        .addAction(R.drawable.ic_cancel_black_24dp,
                                "Dismiss"/*getString(R.string.dismiss)*/, piDismiss)
                        .addAction(R.drawable.ic_snooze_black_24dp,
                                "Snooze"/*getString(R.string.snooze)*/, piSnooze)
                        .setDeleteIntent(piDismiss)
                        .setContentIntent(notificationPendingIntent);

//set intents and pending intents to call service on click of "dismiss" action button of notification


// Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//to post your notification to the notification bar with a id. If a notification with same id already exists, it will get replaced with updated information.
        notificationManager.notify(10, builder.build());

    }

    private Location getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {



            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this,"Location Permission Required",Toast.LENGTH_LONG).show();
            stopService();


            return null; // TODO check this return statement
        }
       Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            /*lt1 = lc.getLatitude();
            ln1 = lc.getLongitude();
*/
        }

        return  location;


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        startLocationUpdates();
        getDistance(getLocation());

    }
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this,"Location Permission Required",Toast.LENGTH_LONG).show();
            stopService();


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private void stopService(){
        editor.putInt("isServiceRunning", -1);
        editor.remove("ServiceObject");
        editor.apply();
        PackageManager pm = this.getPackageManager();
        ComponentName locationreciever =
                new ComponentName(this,
                        GpsLocationReceiver.class);
        pm.setComponentEnabledSetting(
                locationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        ComponentName notificationreciever =
                new ComponentName(this,
                        NotificationReceiver.class);
        pm.setComponentEnabledSetting(
                notificationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        this.stopSelf();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

        int fhg = 9;
    }

    @Override
    public void onLocationChanged(Location location) {

        getDistance(location);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }
}
