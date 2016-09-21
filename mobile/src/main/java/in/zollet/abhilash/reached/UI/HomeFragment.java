package in.zollet.abhilash.reached.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

import in.zollet.abhilash.reached.API.LocationAPI;
import in.zollet.abhilash.reached.API.LocationData;
import in.zollet.abhilash.reached.API.WeatherAPI;
import in.zollet.abhilash.reached.API.WeatherData;
import in.zollet.abhilash.reached.Location.Constants;
import in.zollet.abhilash.reached.Location.FetchAddressIntentService;
import in.zollet.abhilash.reached.Location.GpsLocationReceiver;
import in.zollet.abhilash.reached.Location.LocationService;
import in.zollet.abhilash.reached.Location.NotificationReceiver;
import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceColumns;
import in.zollet.abhilash.reached.data.GeoFenceProvider;
import in.zollet.abhilash.reached.geofence.UserGeofence;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.location.LocationManager.GPS_PROVIDER;


public class HomeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    public static final String ARG_USERGEOFENCE_HOME = "selectedUserGeoFenceHome";
    private static final int LOCATIONGROUP_PERMISSIONS_REQUEST = 100;
    private OnHomeFragmentInteractionListener mListener;
    UserGeofence geofence;

    EditText current_location, remaining_distance, remaining_time, destination_location;
    //TextView destination_location;
    TextView temp, description;
    ImageView weatherIcon;
    Button current_location_button, destination_location_button, start_button;
    ProgressBar progressBar_remaining, progressBar_destination_location, progressBar_current_location;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Menu menu;

    boolean remove;
    boolean showCouldntCalcualteDialog = true;

    private static int UPDATE_INTERVAL = 120000; // 2min
    private static int FATEST_INTERVAL = 50000; // 50 sec
    private static int DISPLACEMENT = 500; // 0.5 km

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private MainActivity mActivity;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(/*getActivity()*/mActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //mGoogleApiClient.connect();
        /*getActivity()*/
        mActivity.setTitle("Journey");
        setHasOptionsMenu(true);

        createLocationRequest();

        sharedpreferences = /*getActivity()*/mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_REACHED, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if(mGoogleApiClient.isConnected()){
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }*/
    }

    @Override
    public void onPause() {
        /*getActivity().*/
        mActivity.unregisterReceiver(CurrentAddressReceiver);
        // getActivity().unregisterReceiver(ConnectionReceiver);

        sharedpreferences.unregisterOnSharedPreferenceChangeListener(this);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onResume() {

        int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);

        if (isServiceRunning != -1) {
            String geofenceJson = sharedpreferences.getString("ServiceObject", null);
            Gson gson = new Gson();
            geofence = gson.fromJson(geofenceJson, UserGeofence.class);
            if (geofence != null) {
                updateUIForService(geofence);
                if (isServiceRunning == 0) {
                    showSnoozingAlert();
                }
            }
            start_button.setText(getString(R.string.stop_journey));
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.action_direction);
                item.setVisible(true);
            }

        } else {
            start_button.setText(getString(R.string.start_journey));
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.action_direction);
                item.setVisible(false);
            }
        }

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CURRENT_LOCATION_ADDRESS_DATA_KEY);
        /*getActivity()*/
        mActivity.registerReceiver(CurrentAddressReceiver, filter);

        IntentFilter connectionFilter = new IntentFilter();
        connectionFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // getActivity().registerReceiver(ConnectionReceiver, filter);

        sharedpreferences.registerOnSharedPreferenceChangeListener(this);
        /*if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }*/

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        remove = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


    }

    private void showNoNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(/*getActivity()*/mActivity);
        builder.setMessage("You're not connected to Internet, still you'll be notified when you're about to reach at the destination")
                .setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void showCouldntCalculateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(/*getActivity()*/mActivity);
        String msg;
        if (sharedpreferences.getInt("isServiceRunning", -1) != -1)
            msg = "No route found for this Destination, but You will be alerted when you\'re about to reach at your destination.";
        else
            msg = "No route found for this Destination. Try changing the travel mode in settings otherwise no luck :|";

        if (showCouldntCalcualteDialog) {
            builder.setMessage(msg)
                    .setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
            showCouldntCalcualteDialog = false;
        }
    }

    private void showSnoozingAlert() {

        NotificationManager notificationManager =
                (NotificationManager) /*getActivity()*/mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(/*getActivity()*/mActivity);

        // Setting Dialog Title
        alertDialog.setTitle("Destination Alert");

        // Setting Dialog Message
        alertDialog.setMessage("You're about to reach at your Destination");

        // On pressing Settings button
        alertDialog.setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent snoozeIntent = new Intent(/*getActivity()*/mActivity, NotificationReceiver.class);//(this, MyService.class);
                snoozeIntent.putExtra("action", "snooze");
                /*getActivity()*/
                mActivity.sendBroadcast(snoozeIntent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent dismissIntent = new Intent(/*getActivity()*/mActivity, NotificationReceiver.class);
                dismissIntent.putExtra("action", "dismiss");
                /*getActivity()*/
                mActivity.sendBroadcast(dismissIntent);
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    public void showLocationSettingsAlert() {

        if (ActivityCompat.checkSelfPermission(/*getActivity()*/mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(/*getActivity()*/mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (!notAskedevenOnce()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Location Permission required", Snackbar.LENGTH_LONG)
                            .setAction("Grant", new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                            LOCATIONGROUP_PERMISSIONS_REQUEST);
                                }
                            });
                    snackbar.setActionTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lightCyan));
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                    snackbar.show();
                    if (current_location != null)
                        current_location.setText("No Location Found\nGrant Location Permission");
                } else {
                    Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Location Permission is required.", Snackbar.LENGTH_LONG)
                            .setAction("Grant", new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent();
                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                    i.setData(Uri.parse("package:" + /*getActivity()*/mActivity.getPackageName()));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    /*getActivity()*/
                                    mActivity.startActivity(i);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // Create layout inflator object to inflate toast.xml file
                                            LayoutInflater inflater = getLayoutInflater(null);

                                            // Call toast.xml file for toast layout
                                            View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);

                                            final Toast toast = new Toast(/*getActivity()*/mActivity);

                                            // Set layout to toast
                                            toast.setView(toastRoot);
                                            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                                            toast.setDuration(Toast.LENGTH_SHORT);
                                            toast.show();
                                            new CountDownTimer(2000, 1000) {

                                                public void onTick(long millisUntilFinished) {
                                                    toast.show();
                                                }

                                                public void onFinish() {
                                                    toast.show();
                                                }

                                            }.start();
                                        }
                                    }, 1000);
                                }
                            });
                    snackbar.setActionTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lightCyan));

                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                    snackbar.show();
                    if (current_location != null)
                        current_location.setText("No Location Found\nGrant Location Permission in Permission Settings");

                }
            } else {

            }

        } else {

            Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Turn on GPS in Settings", Snackbar.LENGTH_LONG)
                    .setAction("Settings", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            snackbar.setActionTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lightCyan));
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
            snackbar.show();
            current_location.setText("No Location Found.\nTurn on the GPS");



           /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            // Setting Dialog Title
            alertDialog.setTitle("GPS settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    current_location.setText("No Location Found.Turn on the GPS");

                }
            });

            // Showing Alert Message
            alertDialog.show();*/
        }
    }

    private void startLocationService() {
        Intent intent = new Intent(/*getActivity()*/mActivity, LocationService.class);
        /*getActivity()*/
        mActivity.startService(intent);
        PackageManager pm = /*getActivity()*/mActivity.getPackageManager();

        ComponentName locationreciever =
                new ComponentName(/*getActivity()*/mActivity,
                        GpsLocationReceiver.class);
        pm.setComponentEnabledSetting(
                locationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        ComponentName notificationreciever =
                new ComponentName(/*getActivity()*/mActivity,
                        NotificationReceiver.class);
        pm.setComponentEnabledSetting(
                notificationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void stopLocationService() {
        /*getActivity()*/
        mActivity.stopService(new Intent(/*getActivity()*/mActivity, LocationService.class));
        editor.putInt("isServiceRunning", -1);
        editor.remove("ServiceObject");
        editor.apply();
        PackageManager pm = /*getActivity()*/mActivity.getPackageManager();
        ComponentName locationreciever =
                new ComponentName(/*getActivity()*/mActivity,
                        GpsLocationReceiver.class);
        pm.setComponentEnabledSetting(
                locationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        ComponentName notificationreciever =
                new ComponentName(/*getActivity()*/mActivity,
                        NotificationReceiver.class);
        pm.setComponentEnabledSetting(
                notificationreciever,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

    private void updateUIForService(UserGeofence geofence) {
        destination_location.setText(geofence.getName());
        start_button.setText(getString(R.string.stop_journey));
        remaining_distance.setText(sharedpreferences.getString("distance.key", "--"));
        remaining_time.setText(sharedpreferences.getString("duration.key", "--"));

        //getDistance(getLocation());
    }

    ShowcaseView showcaseView;
    int counter = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setTarget(new ViewTarget(destination_location))
                .hideOnTouchOutside()
                .setContentTitle("Clcik")
                .setContentText("wow oww owow ")
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ((MainActivity) getActivity()).onHiddenFirstShowcase();
                    }

                })
                .build();*/

        showcaseView = new ShowcaseView.Builder(/*getActivity()*/mActivity)
                .setTarget(new ViewTarget(/*getActivity()*/mActivity.findViewById(R.id.fab)))
                .setOnClickListener(this)
                .setStyle(R.style.CustomShowcaseTheme2)
                .setContentTitle("Click on the \"+\" icon")
                .setContentText("You can add a Territory which you can select as a destination later")
                .singleShot(10)
                .build();
        showcaseView.setButtonText("NEXT");

        /*Target viewTarget = new ViewTarget(R.id.start_button, getActivity());
        new ShowcaseView.Builder(getActivity())
                .setTarget(viewTarget)
                .setContentTitle("This is single shot")
                .setContentText("check it properly")
                .singleShot(10)
                .build();*/
        /*new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setTarget(new ViewTarget(current_location))
                .hideOnTouchOutside()
                .setContentTitle("tada!!!")
                .setContentText("wow wow wow")
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                    *//*@Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ((MainActivity) getActivity()).onHiddenFirstShowcase();
                    }*//*

                })
                .build();*/
    }

    @Override
    public void onClick(View v) {
        switch (counter) {
            case 0:
                showcaseView.setShowcase(new ViewTarget(R.id.destination_location,/*getActivity()*/mActivity), true);
                showcaseView.setContentTitle("Set a Destination");
                showcaseView.setContentText("The Destination, after you have set, will be displayed here.");
                break;

            case 1:
                showcaseView.setShowcase(new ViewTarget(R.id.start_button,/*getActivity()*/mActivity), true);
                showcaseView.setContentTitle("Start your Journey");
                showcaseView.setContentText("You will be alerted when you're about to reach at the destination");
                break;

            case 2:
                showcaseView.setTarget(Target.NONE);
                showcaseView.setContentTitle("And don't forget to enjoy your journey :)");
                showcaseView.setContentText("");
                showcaseView.setButtonText("Close");
                //setAlpha(0.4f, textView1, textView2, textView3);
                break;

            case 3:
                showcaseView.hide();
                PreferenceManager.getDefaultSharedPreferences(/*getActivity()*/mActivity)
                        .edit()
                        .putBoolean("firstShowcase", true)
                        .commit();
                ;
                // setAlpha(1.0f, textView1, textView2, textView3);
                break;
        }
        counter++;
    }


    private void setAlpha(float alpha, View... views) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            for (View view : views) {
                view.setAlpha(alpha);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        remove = true;

        current_location = (EditText) rootView.findViewById(R.id.current_location);
        disableEditText(current_location);
        current_location.addTextChangedListener(new FocusEnableTextWatcher(current_location));

        temp = (TextView) rootView.findViewById(R.id.temp);
        description = (TextView) rootView.findViewById(R.id.description);
        weatherIcon = (ImageView) rootView.findViewById(R.id.weather_icon);

        remaining_distance = (EditText) rootView.findViewById(R.id.remaining_distance);
        disableEditText(remaining_distance);
        remaining_distance.addTextChangedListener(new FocusEnableTextWatcher(remaining_distance));

        remaining_time = (EditText) rootView.findViewById(R.id.remaining_time);
        disableEditText(remaining_time);

        remaining_time.addTextChangedListener(new FocusEnableTextWatcher(remaining_time));


        destination_location = (EditText) rootView.findViewById(R.id.destination_location);
        destination_location.setKeyListener(null);
        if (getArguments() != null) {
            geofence = getArguments().getParcelable(ARG_USERGEOFENCE_HOME);
            stopLocationService();
            destination_location.setText(geofence.getName());

        }
        destination_location.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        if (destination_location.getText().toString().isEmpty()) {
                                                         /*getActivity()*/
                                                            mActivity.getSupportFragmentManager()
                                                                    //.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                                    .beginTransaction()
                                                                    .replace(R.id.container, new PlacesFragment(), "PlacesFragment")
                                                                    .addToBackStack(null)
                                                                    .commit();
                                                            if (mListener != null) {
                                                                mListener.onDestinationInteraction(true);
                                                            }
                                                            remove = false;

                                                        } else {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                                            builder.setMessage("Do you want to select a different Destination ?")
                                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {

                                                                            stopLocationService();
                                                                            /*getActivity()*/
                                                                            mActivity.getSupportFragmentManager().beginTransaction()
                                                                                    .replace(R.id.container, new PlacesFragment(), "PlacesFragment")
                                                                                    .addToBackStack(null)
                                                                                    .commit();
                                                                            if (mListener != null) {
                                                                                mListener.onDestinationInteraction(true);
                                                                            }
                                                                            remove = false;

                                                                        }
                                                                    })
                                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    })
                                                                    .create()
                                                                    .show();
                                                        }

                                                    }
                                                }

        );

        //destination_location_button = (Button) rootView.findViewById(R.id.destination_location_button);
        start_button = (Button) rootView.findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);


                                                if (isServiceRunning == -1) {
                                                    if (/*((MainActivity) getActivity())*/mActivity.isGPSEnabled()) {
                                                        if (geofence != null) {

                                                            if (!isNetworkAvailable()) {
                                                                showNoNetworkDialog();
                                                            }
                                                            showCouldntCalcualteDialog = true;
                                                            MenuItem item = menu.findItem(R.id.action_direction);
                                                            item.setVisible(true);
                                                            Gson gson = new Gson();
                                                            String json = gson.toJson(geofence);
                                                            editor.putString("ServiceObject", json);
                                                            editor.putInt("isServiceRunning", 1);
                                                            editor.apply();
                                                            startLocationService();
                                                            getDistance(getLocation());
                                                            increaseCountByOne();

                                                        } else {
                                                            Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Select a Destination", Snackbar.LENGTH_LONG)
                                                                    .setAction("Action", null);
                                                            View sbView = snackbar.getView();
                                                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                                            textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                                                            snackbar.show();

                                                        }
                                                    } else {

                                                        showLocationSettingsAlert();
                                                    }
                                                } else {
                                                    showCouldntCalcualteDialog = true;
                                                    MenuItem item = menu.findItem(R.id.action_direction);
                                                    item.setVisible(false);
                                                    /*editor.putInt("isServiceRunning", -1);
                                                    editor.remove("ServiceObject");
                                                    editor.apply();*/
                                                    Intent dismissIntent = new Intent(/*getActivity()*/mActivity, NotificationReceiver.class);
                                                    dismissIntent.putExtra("action", "dismiss");
                                                        /*getActivity()*/
                                                    mActivity.sendBroadcast(dismissIntent);

                                                    stopLocationService();
                                                }


                                            }

                                        }


        );


        progressBar_remaining = (ProgressBar) rootView.findViewById(R.id.progressBar_remaining);
        progressBar_remaining.setVisibility(View.GONE);
        // progressBar_destination_location = (ProgressBar) rootView.findViewById(R.id.progressBar_destination_location);
        progressBar_current_location = (ProgressBar) rootView.findViewById(R.id.progressBar_current_location);


        // Inflate the layout for this fragment
        return rootView;
    }

    private void increaseCountByOne() {

        Cursor cursor = /*getActivity()*/mActivity.getContentResolver().query
                (GeoFenceProvider.GeoFence.CONTENT_URI, null, GeoFenceColumns.GEONAME + "=? ", new String[]{geofence.getName()}, null);
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = (cursor.getInt(cursor.getColumnIndex(GeoFenceColumns.COUNT)));
        }
        count++;
        ArrayList<ContentProviderOperation> geoFence = new ArrayList<ContentProviderOperation>();
        geoFence.add(ContentProviderOperation.newUpdate(GeoFenceProvider.GeoFence.CONTENT_URI)
                .withSelection(GeoFenceColumns.GEONAME + "=? ", new String[]{geofence.getName()})
                .withValue(GeoFenceColumns.COUNT, (count)).build());
        try {
            /*getActivity()*/
            mActivity.getContentResolver().applyBatch(GeoFenceProvider.AUTHORITY, geoFence);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }


    private void disableEditText(EditText editText) {
        editText.setKeyListener(null);
        editText.setFocusableInTouchMode(false);
        editText.setFocusable(false);
    }

    private Location getLocation() {

        if (ActivityCompat.checkSelfPermission(/*getActivity()*/mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(/*getActivity()*/mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

           /* if (shouldShowRequestPermissionRationale(
                    Manifest.permission_group.LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }*/

            if (notAskedevenOnce()) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATIONGROUP_PERMISSIONS_REQUEST);
                // // TODO: 8/26/2016  add something

            }


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null; // TODO check this return statement
        } else {
            progressBar_current_location.setVisibility(View.VISIBLE);
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (location != null) {
                getCurrentLocationAddress(location);
            }
            getWeather(location);

            // progressBar_current_location.setVisibility(View.GONE);

            return location;
        }
        /*progressBar_current_location.setVisibility(View.VISIBLE);
        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (location != null) {
            getCurrentLocationAddress(location);
        }

        // progressBar_current_location.setVisibility(View.GONE);

        return location;*/
    }

    private boolean notAskedevenOnce() {

        return (PreferenceManager.getDefaultSharedPreferences(/*getActivity()*/mActivity).getBoolean("permission", true));

    }


    private void markAsAsked() {

        PreferenceManager.getDefaultSharedPreferences(/*getActivity()*/mActivity).edit().putBoolean("permission", false).commit();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == LOCATIONGROUP_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                markAsAsked();
                //startLocationUpdates();
                //PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("permission", true).commit();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                markAsAsked();
                current_location.setText("No Location Found\nGrant Location Permission");
                //boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION);
                /*boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION);

                if (showRationale) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("shouldAsk", false).commit();
                    Snackbar.make(getActivity().findViewById(R.id.fab), "Location Permission is required", Snackbar.LENGTH_LONG)
                            .setAction("Grant", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    requestPermissions(new String[]{Manifest.permission_group.LOCATION},
                                            LOCATIONGROUP_PERMISSIONS_REQUEST);
                                }
                            })
                            .show();
                }
                    else {
                    Snackbar.make(getActivity().findViewById(R.id.fab), "Location Permission Denied", Snackbar.LENGTH_LONG)
                            .setAction("Grant", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    requestPermissions(new String[]{Manifest.permission_group.LOCATION},
                                            LOCATIONGROUP_PERMISSIONS_REQUEST);
                                }
                            })
                            .show();
                    }*/
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getCurrentLocationAddress(Location location) {
        Intent intent = new Intent(/*getActivity()*/mActivity, FetchAddressIntentService.class); // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference
        //intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        /*getActivity()*/
        mActivity.startService(intent);
    }

    private void getDistance(Location location) {

        if (geofence != null && location != null) {
            progressBar_remaining.setVisibility(View.VISIBLE);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(/*getActivity()*/mActivity);

            String origin = location.getLatitude() + "," + location.getLongitude(); // change is mADE HERE WITH EXTRA COMMA
            String destination = geofence.getLatitude() + "," + geofence.getLongitude();
            String key = Constants.API_KEY;
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
            Call<LocationData> call = locationAPI.getLocation(origin, destination, key, units, departureTime, mode, language, trafficModel, transitMode);

            {

                call.enqueue(new retrofit2.Callback<LocationData>() {

                    int ds = 0;

                    @Override
                    public void onResponse(Call<LocationData> call, Response<LocationData> response) {

                        if (response.body() != null) {
                            LocationData locate = response.body();

                            switch (locate.getStatus()) {
                                case "OK":
                                    // Toast.makeText(getActivity(), response.message(), Toast.LENGTH_LONG).show();
                                    String dis = locate.getRoutes().get(0).getLegs().get(0).getDistance().getText();

                                    String duration = locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic() != null ?
                                            locate.getRoutes().get(0).getLegs().get(0).getDurationInTraffic().getText()
                                            : locate.getRoutes().get(0).getLegs().get(0).getDuration().getText();
                                    //editor.putString("distance.key",dis);
                                    //editor.putString("duration.key",duration);
                                    //editor.commit();


                                    if (dis != null)
                                        remaining_distance.setText(dis);
                                    else
                                        remaining_distance.setText("----");


                                    // remaining_distance.append(String.valueOf(location.distanceTo(ll)));
                                    if (duration != null)
                                        remaining_time.setText(duration);
                                    else
                                        remaining_time.setText("----");
                                    //  progressBar_remaining.setVisibility(View.GONE);
                                    progressBar_remaining.setVisibility(View.GONE);
                                    break;
                                case "ZERO_RESULTS":
                                    showNullValueRemainingText();
                                    //showCouldntCalcualteDialog = true;
                                    //if(sharedpreferences.getInt("isServiceRunning",-1) != -1)
                                    showCouldntCalculateDialog();

                                    break;
                                default:
                                    showNullValueRemainingText();
                                    Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Something went wrong. Try again later", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                                    snackbar.show();
                                    break;
                            }
                        } else {
                            showNullValueRemainingText();
                            Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Something went wrong. Try again later", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                            snackbar.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationData> call, Throwable t) {

                        showNullValueRemainingText();

                    }
                });

            }
        }
    }

    public void getWeather(Location location) {

        // progressBar_remaining.setVisibility(View.VISIBLE);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(/*getActivity()*/mActivity);

        String Lat = String.valueOf(location.getLatitude());
        String Lon = String.valueOf(location.getLongitude());
        String AppID = Constants.WEATHER_API_KEY;
        String units = prefs.getString("unit_system", "metric");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        final WeatherAPI weatherAPI = restAdapter.create(WeatherAPI.class);
        Call<WeatherData> call = weatherAPI.getWeather(Lat, Lon, AppID, units);

        {

            call.enqueue(new retrofit2.Callback<WeatherData>() {

                @Override
                public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {

                    if (response.body() != null) {
                        WeatherData weatherData = response.body();
                        int id = weatherData.getWeather().get(0).getId();
                        String main = weatherData.getWeather().get(0).getMain();
                        Float temperature = weatherData.getMain().getTemp();

                        description.setText(getWeatherDEscForWeatherCondition(id));
                        String tempr;
                        if (prefs.getString("unit_system", "metric").equals("metric"))
                            tempr = String.format(mActivity.getString(R.string.metric_format_temperature), temperature);
                        else
                            tempr = String.format(mActivity.getString(R.string.imperial_format_temperature), temperature);
                        temp.setText(tempr);
                        weatherIcon.setImageResource(getIconResourceForWeatherCondition(id));

                    }
                }

                @Override
                public void onFailure(Call<WeatherData> call, Throwable t) {

                    String tempr;
                    if (prefs.getString("unit_system", "metric").equals("metric"))
                        tempr = "-- \u00B0 C";
                    else
                        tempr = "-- \u00B0 F";
                    temp.setText(tempr);
                    weatherIcon.setImageResource(getIconResourceForWeatherCondition(-1));
                    description.setText(getWeatherDEscForWeatherCondition(-1));

                }
            });

        }
    }

    public int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if((timeOfDay >= 19 && timeOfDay <= 24) || (timeOfDay >= 0 && timeOfDay <= 4.5) )
                return R.drawable.ic_night_clear;
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if((timeOfDay >= 19 && timeOfDay <= 24) || (timeOfDay >= 0 && timeOfDay <= 4.5) )
                return R.drawable.ic_night_cloud;
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return /*-1*/R.drawable.ic_status;
    }

    public String getWeatherDEscForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return "Storm is coming. It's better to stay Inside";
        } else if (weatherId >= 300 && weatherId <= 321) {
            return "It's raining outside. Hope you have a umbrella";
        } else if (weatherId >= 500 && weatherId <= 504) {
            return "It's raining outside. Hope you have a umbrella";
        } else if (weatherId == 511) {
            return "It's snowing outside. Have a jacket and start your journey";
        } else if (weatherId >= 520 && weatherId <= 531) {
            return "It's raining outside. Hope you have a umbrella";
        } else if (weatherId >= 600 && weatherId <= 622) {
            return "It's snowing outside. Have a jacket and start your journey";
        } else if (weatherId >= 701 && weatherId <= 761) {
            return "It's fog outside.The visibility might be low.";
        } else if (weatherId == 761 || weatherId == 781) {
            return "It's not a good weather to go outside.It's better to stay Inside";
        } else if (weatherId == 800) {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if((timeOfDay >= 19 && timeOfDay <= 24) || (timeOfDay >= 0 && timeOfDay <= 4.5) )
                return "Sky is clear. A wonderful day is waiting for you";
            return "Sky is clear outside. Have a good day.";
        } else if (weatherId == 801) {
            return "It might rain. Have a umbrella and start your journey";
        } else if (weatherId >= 802 && weatherId <= 804) {
            return "It might rain. Have a umbrella and start your journey";
        } else if (weatherId >= 900) {
            return "It might rain. Have a umbrella and start your journey";
        }
        return "Couldn't fetch the weather";
    }


    private void showNullValueRemainingText() {
        remaining_distance.setText("----");
        remaining_time.setText("----");
        progressBar_remaining.setVisibility(View.GONE);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    protected void startLocationUpdates() {

        if ((Activity) getContext() != null && ActivityCompat.checkSelfPermission(/*(Activity) getContext()*/mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(/*(Activity) getContext()*/mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (notAskedevenOnce()) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATIONGROUP_PERMISSIONS_REQUEST);
                // // TODO: 8/26/2016  add something
            }

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

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) /*getActivity()*/mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    BroadcastReceiver CurrentAddressReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null) {
                //String[] AddName = message.split(",");
                //current_location.setText(AddName[0]/*+ " , "+AddName[1]*/);
                current_location.setText(message);
            }
            progressBar_current_location.setVisibility(View.GONE);
        }
    };

    BroadcastReceiver ConnectionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);
            if (isServiceRunning != -1) {
                if (/*((MainActivity)getActivity())*/mActivity.isGPSEnabled()) {
                    Location location = getLocation();
                    getDistance(location);
                } else {
                    showLocationSettingsAlert();
                    showNullValueRemainingText();
                }
            }

        }
    };

    @Override
    public void onConnected(Bundle bundle) {
       /* Location location = getLocation();
        if(sharedpreferences.getInt("isServiceRunning",-1) != -1){
            getDistance(location);
        }*/
        startLocationUpdates();
        Location location = null;
        if (/*((MainActivity)getActivity())*/mActivity.isGPSEnabled()) {
            location = getLocation();
        } else {
            showLocationSettingsAlert();
        }

        if (geofence != null && location != null) {
            getDistance(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        getCurrentLocationAddress(location);
        getDistance(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("isServiceRunning")) {
            if ((sharedpreferences.getInt("isServiceRunning", -1)) == -1) {
                start_button.setText(getString(R.string.start_journey));
                MenuItem item = menu.findItem(R.id.action_direction);
                item.setVisible(false);
            } else {
                start_button.setText(getString(R.string.stop_journey));
                MenuItem item = menu.findItem(R.id.action_direction);
                item.setVisible(true);

            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHomeFragmentInteractionListener {

        void onDestinationInteraction(boolean placesPage);
    }

    public class FocusEnableTextWatcher implements TextWatcher {
        EditText editText;

        public FocusEnableTextWatcher(EditText et) {
            this.editText = et;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!s.toString().isEmpty()) {
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        this.menu = menu;
        int isServiceRunning = sharedpreferences.getInt("isServiceRunning", -1);
        if (isServiceRunning == -1) {
            MenuItem item = menu.findItem(R.id.action_direction);
            item.setVisible(false);
        }

        //inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            if (/*((MainActivity)getActivity())*/mActivity.isGPSEnabled()) {
                Location location = getLocation();

                if (location != null) {
                    if (geofence != null)
                        getDistance(location);

                    else {

                        Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Select a Destination", Snackbar.LENGTH_LONG)
                                .setAction("Action", null);

                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                        snackbar.show();
                        //showNullValueRemainingText();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Oops, Something went wrong", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(/*getActivity()*/mActivity, R.color.lime));
                    snackbar.show();

                }
            } else {

                showLocationSettingsAlert();

            }

            return true;
        } else if (id == R.id.action_direction) {


            if (((MainActivity) getActivity()).isGPSEnabled()) {
                Location location = getLocation();
                if (geofence != null && location != null) {

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?addr=" + location.getLatitude() + "," + location.getLongitude()
                                    + "&daddr=" + geofence.getLatitude() + "," + geofence.getLongitude()));
                    startActivity(intent);
                } else {
                    Snackbar snackbar = Snackbar.make(/*getActivity()*/mActivity.findViewById(R.id.fab), "Oops, Something went wrong", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                    snackbar.show();

                }
            } else {

                showLocationSettingsAlert();
                showNullValueRemainingText();
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasPermission() {

        return (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission_group.LOCATION) == PackageManager.PERMISSION_GRANTED);


    }
}
