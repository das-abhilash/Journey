package in.zollet.abhilash.reached.UI;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Calendar;

import in.zollet.abhilash.reached.Location.Constants;
import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceProvider;
import in.zollet.abhilash.reached.geofence.AllAboutGeofence;
import in.zollet.abhilash.reached.geofence.UserGeofence;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnHomeFragmentInteractionListener,PlacesFragment.OnPlacesFragmentInteractionListener,
        AddGeoFenceFragment.AddGeofenceFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int LOCATIONGROUP_PERMISSIONS_REQUEST = 100;
    int selectedItem = -1;
    TextView nav_user;
    private boolean mTwoPane;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    NavigationView navigationView;
    FloatingActionButton fab;
    LocationManager locationManager;
    private static final int PLACE_PICKER_REQUEST = 100;
    private boolean placePicker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstShowcase", false)
                && PreferenceManager.getDefaultSharedPreferences(this).getString("show_text", "").trim().isEmpty()){
            boolean firstrun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstrun", true);

            if (firstrun){

                addNameDialog();

            }
        }



        AllAboutGeofence.getInstance().init(this);

        {
            mTwoPane = false;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment(), "HomeFragment")
                    .commit();

        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        final PlacePicker.IntentBuilder PlaceBuilder = new PlacePicker.IntentBuilder();
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*AddGeoFenceFragment dialogFragment = new AddGeoFenceFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("name","bla bla");
                    dialogFragment.setArguments(bundle);
                    dialogFragment.setListener(MainActivity.this);
                    dialogFragment.show(getFragmentManager(), "AddGeofenceFragment");*/
                    try {
                        if (!placePicker) {
                            startActivityForResult(PlaceBuilder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                            placePicker = true;
                        }
                    } catch (GooglePlayServicesRepairableException e) {
                        GooglePlayServicesUtil
                                .getErrorDialog(e.getConnectionStatusCode(), MainActivity.this, 0);
                    } catch (GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getApplicationContext(), "Google Play Services is not available.",
                                Toast.LENGTH_LONG)
                                .show();
                    }

                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_subHeader = (TextView)hView.findViewById(R.id.subHeader);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            nav_subHeader.setText("Good Morning.");
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            nav_subHeader.setText("Good Afternoon.");
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            nav_subHeader.setText("Good Evening.");
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            nav_subHeader.setText("Good Night. Sweet Dreams");
        }
        nav_user = (TextView)hView.findViewById(R.id.header);
        nav_user.setText("Hi " + PreferenceManager.getDefaultSharedPreferences(this).getString("show_text", ""));

        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        selectedItem = R.id.nav_home;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            placePicker = false;
            if (resultCode == RESULT_OK) {
                /*selectPlace.setVisibility(View.GONE);
                longitudeEditText.setVisibility(View.VISIBLE);
                latitudeEditText.setVisibility(View.VISIBLE);*/


                Place selectedPlace = PlacePicker.getPlace(data, this);
                UserGeofence geofence = new UserGeofence((String) selectedPlace.getName(), String.valueOf(selectedPlace.getLatLng().latitude),
                        String.valueOf(selectedPlace.getLatLng().longitude), (String) selectedPlace.getAddress());
                AddGeoFenceFragment dialogFragment = new AddGeoFenceFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(AddGeoFenceFragment.ARG_USERGEOFENCE_ADD, geofence);
                dialogFragment.setArguments(bundle);
                dialogFragment.setListener(MainActivity.this);
                dialogFragment.show(getFragmentManager(), "AddGeofenceFragment");


                /*// Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                longitudeEditText.setText(String.valueOf(selectedPlace.getLatLng().longitude));
                //longitudeEditText.setText(String.valueOf(selectedPlace.getLatLng()));
                *//*longitudeEditText.setText(*//**//*String.valueOf*//**//*(Location.convert
                        (selectedPlace.getLatLng().longitude, Location.FORMAT_DEGREES)));*//*
                latitudeEditText.setText(String.valueOf(selectedPlace.getLatLng().latitude));
                if (name_geofence.getText().toString().trim().isEmpty()) {
                    name_geofence.setText(selectedPlace.getName());
                }*/

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private AllAboutGeofence.AllAboutGeofenceListener allAboutGeofenceListener = new AllAboutGeofence.AllAboutGeofenceListener() {
        @Override
        public void onGeofencesAdded() {

            Cursor cursor = getContentResolver().query(GeoFenceProvider.GeoFence.CONTENT_URI,null,null,null,null);
            int  ui = cursor.getCount();

            getSupportFragmentManager()
        .popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new PlacesFragment(),"PlacesFragment")
            .commit();

        }

        @Override
        public void onGeofencesDeleted() {

        }

        @Override
        public void onError() {
            showErrorToast();
        }
    };

    @Override
    public void onDialogPositiveClick(UserGeofence geofence) {
        selectedItem = R.id.nav_places;
        navigationView.getMenu().findItem(R.id.nav_places).setChecked(true);
        AllAboutGeofence.getInstance().addGeofence(geofence, allAboutGeofenceListener);
    }

    private void showErrorToast() {
        Toast.makeText(this, "There was an error. Please try again.", Toast.LENGTH_SHORT).show();

    }

    private void refresh() {
        //geoFenceAdapter.notifyDataSetChanged();
    }

    public void onHiddenFirstShowcase() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new PlacesFragment())
                .commit();
    }

    public void addNameDialog(){

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.edit_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.input);
        userInput.requestFocus();
        alertDialogBuilder
                //.setCancelable(false)
                .setTitle("Your Name")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                if (!userInput.getText().toString().trim().isEmpty()){

                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                            .edit()
                                            .putBoolean("firstrun", false)
                                            .putString("show_text", userInput.getText().toString().trim())
                                            .commit();
                                    Snackbar snackbar = Snackbar.make(fab, "Your name has been saved. You can change it later in Settings.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lime));
                                    snackbar.show();
                                }


                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Name");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.edit_dialog, (ViewGroup) findViewById(android.R.id.content), false);

// Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);//new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String hg = input.getText().toString();
                Toast.makeText(getApplicationContext(),hg,Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            //addNameDialog();
            return false;
        } else*/ if (id == R.id.action_direction) {
            return false;
        } else if (id == R.id.action_refresh){

            return false;
        }

        return false/*super.onOptionsItemSelected(item)*/;
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
       // item.setChecked(true);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home && selectedItem != R.id.nav_home ) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    getSupportFragmentManager()
                            .popBackStack();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, new HomeFragment(), "HomeFragment")
                            //.disallowAddToBackStack()
                            .commit();

                    selectedItem = R.id.nav_home;
                }

            }, 300);

            /*getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment(), "HomeFragment")
                    //.setCustomAnimations()
                    .setCustomAnimations(
                            R.anim.card_flip_right_in,
                            R.anim.card_flip_right_out,
                            R.anim.card_flip_left_in,
                            R.anim.card_flip_left_out)
                    .disallowAddToBackStack()
                    .commit();
            selectedItem = R.id.nav_home;*/

        } else if (id == R.id.nav_places && selectedItem != R.id.nav_places) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    getSupportFragmentManager()
                            .popBackStack();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new PlacesFragment(), "PlacesFragment")
                            .commit();

                    selectedItem = R.id.nav_places;
                }

            }, 300);
            /*getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new PlacesFragment(),"PlacesFragment")
                    .commit();
            selectedItem = R.id.nav_places;*/

        } /*else if (id == R.id.nav_timeline && selectedItem != R.id.nav_timeline) {
            selectedItem = R.id.nav_timeline;

        }*/ else if (id == R.id.nav_settings ) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    selectedItem = R.id.nav_settings;
                }

            }, 300);


        } else if (id == R.id.nav_feedback ) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent feedbackIntent = new Intent(getApplicationContext(), FeedbackActivity.class);
                    startActivity(feedbackIntent);
                    selectedItem = R.id.nav_feedback;
                }

            }, 300);

        } else if (id == R.id.nav_about ) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent feedbackIntent = new Intent(getApplicationContext(), FAQActivity.class);
                    startActivity(feedbackIntent);
                    selectedItem = R.id.nav_about;
                }

            }, 300);

        }  else if (id == R.id.nav_share ) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent feedbackIntent = new Intent(getApplicationContext(), ShareActivity.class);
                    startActivity(feedbackIntent);
                    selectedItem = R.id.nav_share;
                }

            }, 300);

        }
        else if (id == R.id.nav_rate) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                    selectedItem = R.id.nav_rate;

                }

            }, 300);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDestinationInteraction(boolean placesPage) {
        selectedItem = R.id.nav_places;
        navigationView.getMenu().findItem(R.id.nav_places).setChecked(true);

    }

    @Override
    public void onDestinationSelected (boolean homePage) {
        selectedItem = R.id.nav_home;
        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

    }


    @Override
    public void onPlacesFragmentInteractionDelete(UserGeofence geofence) {

        AllAboutGeofence.getInstance().removeGeofences(geofence, allAboutGeofenceListener);
    }

    @Override
    public void onPlacesFragmentInteractionEdit(UserGeofence geofence) {

        //AllAboutGeofence.getInstance().addGeofence(geofence, allAboutGeofenceListener,true);
        AddGeoFenceFragment addGeoFenceFragment = new AddGeoFenceFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddGeoFenceFragment.ARG_USERGEOFENCE_ADD, geofence);
        bundle.putBoolean(AddGeoFenceFragment.ARG_UPDATE, true);
        addGeoFenceFragment.setArguments(bundle);

        addGeoFenceFragment.setListener(MainActivity.this);
        addGeoFenceFragment.show(getFragmentManager(), "AddGeofenceFragment");

        getSupportFragmentManager()
                .popBackStack();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new AddGeoFenceFragment(),"AddGeoFenceFragment")
                //.addToBackStack(null)
                .commit();
    }

    @Override
    public void onPlacesFragmentInteractionSelect(UserGeofence geofence) {

        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(HomeFragment.ARG_USERGEOFENCE_HOME, geofence);
        homeFragment.setArguments(bundle);

                //ndle);

        getSupportFragmentManager()
                .popBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, homeFragment, "HomeFragment")
                .commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("show_text")){
             nav_user.setText("Hi " +PreferenceManager.getDefaultSharedPreferences(this).getString("show_text", ""));

        }
        if(key.equals("firstShowcase")) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getString("show_text", "").trim().isEmpty()) {
                boolean firstrun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstrun", true);

                if (firstrun) {

                    addNameDialog();

                }
            }
        }


    }

    public boolean isGPSEnabled(){

        boolean permisson = true;
       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            permisson = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return locationManager.isProviderEnabled(GPS_PROVIDER) && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void askForPermisson(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATIONGROUP_PERMISSIONS_REQUEST);
        }
    }

    public void grantDeniedPermisson(){
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Create layout inflator object to inflate toast.xml file
                LayoutInflater inflater = getLayoutInflater();

                // Call toast.xml file for toast layout
                View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);

                final Toast toast = new Toast(getApplicationContext());

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
}
