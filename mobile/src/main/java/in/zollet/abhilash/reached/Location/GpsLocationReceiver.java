package in.zollet.abhilash.reached.Location;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Created by Abhilash on 8/26/2016.
 */
public class GpsLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                context.stopService(new Intent(context, LocationService.class));
                context.getSharedPreferences("reachedGeoFence", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("isServiceRunning", -1)
                        .remove("ServiceObject")
                        .apply();
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
                Toast.makeText(context, "GPS need to be turned on to Alert you. Turn the GPS and try again",
                        Toast.LENGTH_SHORT).show();

            }
        }

    }
}
