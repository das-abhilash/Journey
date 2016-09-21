package in.zollet.abhilash.reached.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.UI.MainActivity;
import in.zollet.abhilash.reached.data.GeoFenceColumns;
import in.zollet.abhilash.reached.data.GeoFenceProvider;

public class AllAboutGeofence {

    // region Properties

    private final String TAG = AllAboutGeofence.class.getName();

    //List<Geofence> mGeofenceList = new ArrayList<>();
    private Context context;
    private GoogleApiClient mGoogleApiClient;

    private AllAboutGeofenceListener listener;


   /* private List<NamedGeofence> namedGeofences;

    public List<NamedGeofence> getNamedGeofences() {
        return namedGeofences;
    }

    private List<NamedGeofence> namedGeofencesToRemove;*/

    private Geofence mGeofence;
    private UserGeofence mUserGeofence;
    Context mContext;

    // endregion

    // region Shared Instance

    private static AllAboutGeofence INSTANCE;

    public static AllAboutGeofence getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AllAboutGeofence();
        }
        return INSTANCE;
    }

    // endregion

    // region Public

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addGeofence(UserGeofence mUserGeofence, AllAboutGeofenceListener listener) {


        this.mUserGeofence = mUserGeofence;
        this.mGeofence = mUserGeofence.geofence();
        this.listener = listener;
        // this.isUpdate = isUpdate;

        /*if (isUpdate)
            connectWithCallbacks(connectionRemoveListener);*/
        connectWithCallbacks(connectionAddListener);

    }

    public void removeGeofences(UserGeofence geofence, AllAboutGeofenceListener allAboutGeofenceListener) {

        this.mUserGeofence = geofence;
        this.listener = allAboutGeofenceListener;

        connectWithCallbacks(connectionRemoveListener);

    }


    private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();
    }

    private GoogleApiClient.ConnectionCallbacks connectionAddListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), pendingIntent);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        SaveGeoFence();
                    } else {
                        Toast.makeText(mContext, "Oops, Something went wrong. Try Again Later", Toast.LENGTH_LONG).show();
                        // sendError();
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "Connecting to GoogleApiClient suspended.");
            // sendError();
        }
    };
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "Connecting to GoogleApiClient failed.");
            //sendError();
        }
    };

    private GoogleApiClient.ConnectionCallbacks connectionRemoveListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            List<String> removeIds = new ArrayList<>();
            removeIds.add(mUserGeofence.getId());


            if (removeIds.size() > 0) {
                PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeIds);
                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            removeSavedGeofences();
                        } else {
                            Toast.makeText(mContext, "Oops, Something went wrong. Try Again Later", Toast.LENGTH_LONG).show();
                            // sendError();
                        }
                    }
                });
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "Connecting to GoogleApiClient suspended.");
            // sendError();
        }
    };

    public void SaveGeoFence() {

        if (listener != null) {
            listener.onGeofencesAdded();
        }

        ContentValues values = new ContentValues();

        values.put(GeoFenceColumns.GEONAME, mUserGeofence.getName());
        values.put(GeoFenceColumns.LATITUDE, mUserGeofence.getLatitude());
        values.put(GeoFenceColumns.LONGITUDE, mUserGeofence.getLongitude());
        values.put(GeoFenceColumns.GEO_ADDRESS, mUserGeofence.getAddress());
        values.put(GeoFenceColumns.RADIUS, mUserGeofence.getRadius());
        values.put(GeoFenceColumns.COUNT, 0);
        ArrayList<ContentProviderOperation> geoFence = new ArrayList<ContentProviderOperation>();

            values.put(GeoFenceColumns.ID, mUserGeofence.getId());
            geoFence.add(ContentProviderOperation.newInsert(GeoFenceProvider.GeoFence.CONTENT_URI).withValues(values).build());


        try {
            context.getContentResolver().applyBatch(GeoFenceProvider.AUTHORITY, geoFence);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void removeSavedGeofences() {

        ArrayList<ContentProviderOperation> geoFence = new ArrayList<ContentProviderOperation>();

        boolean i = geoFence.add(ContentProviderOperation.newDelete(GeoFenceProvider.GeoFence.CONTENT_URI)
                .withSelection(GeoFenceColumns.GEONAME + "=?", new String[]{mUserGeofence.getName()}).build());
        try {
            context.getContentResolver().applyBatch(GeoFenceProvider.AUTHORITY, geoFence);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        if (listener != null) {
            listener.onGeofencesAdded();
        }
    }

    public void UpdateGeoFence() {

        if (listener != null) {
            listener.onGeofencesAdded();
        }

        ContentValues values = new ContentValues();

        values.put(GeoFenceColumns.ID, mUserGeofence.getId());
        values.put(GeoFenceColumns.GEONAME, mUserGeofence.getName());
        values.put(GeoFenceColumns.LATITUDE, mUserGeofence.getLatitude());
        values.put(GeoFenceColumns.LONGITUDE, mUserGeofence.getLongitude());
        values.put(GeoFenceColumns.RADIUS, mUserGeofence.getRadius());
        values.put(GeoFenceColumns.COUNT, 0); //
        ArrayList<ContentProviderOperation> geoFence = new ArrayList<ContentProviderOperation>();
        geoFence.add(ContentProviderOperation.newUpdate(GeoFenceProvider.GeoFence.CONTENT_URI)
                .withSelection(GeoFenceColumns.GEONAME + "=? ", new String[]{mUserGeofence.getName()})

                .withValues(values).build());
        try {
            context.getContentResolver().applyBatch(GeoFenceProvider.AUTHORITY, geoFence);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        List<Geofence> mGeofenceList = new ArrayList<>();
        mGeofenceList.add(mGeofence);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    public interface AllAboutGeofenceListener {
        void onGeofencesAdded();

        void onGeofencesDeleted();

        void onError();
    }

}