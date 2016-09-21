package in.zollet.abhilash.reached.UI;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceColumns;
import in.zollet.abhilash.reached.data.GeoFenceProvider;
import in.zollet.abhilash.reached.geofence.UserGeofence;


public class AddGeoFenceFragment extends DialogFragment {

    private static final int PLACE_PICKER_REQUEST = 100;
    public static final String ARG_USERGEOFENCE_ADD = "selectedUserGeoFenceAdd";
    public static final String ARG_UPDATE = "selectedUserGeoFenceUpdate";
    private static final int LOCATIONGROUP_PERMISSIONS_REQUEST = 100;
    private LocationManager locationManager;
    private Activity mActivity;
    private boolean placePicker = false;

    public AddGeoFenceFragment() {
        // Required empty public constructor
    }

    TextInputLayout input_layout_name_geofence, input_layout_latitude, input_layout_longitude, input_layout_radius;
    EditText name_geofence, latitudeEditText, longitudeEditText, radiusEditText;
    Place selectedPlace;
    Button selectPlace;
    UserGeofence geofence = null;


    AddGeofenceFragmentListener listener;

    public void setListener(AddGeofenceFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        View view = inflater.inflate(R.layout.fragment_add_geo_fence, null);

        name_geofence = (EditText) view.findViewById(R.id.name_geofence);
        latitudeEditText = (EditText) view.findViewById(R.id.latitudeEditText);
        latitudeEditText.setKeyListener(null);
        longitudeEditText = (EditText) view.findViewById(R.id.longitudeEditText);
        longitudeEditText.setKeyListener(null);
        radiusEditText = (EditText) view.findViewById(R.id.radius);
        //selectPlace = (Button) view.findViewById(R.id.selectPlace);
        input_layout_radius = (TextInputLayout) view.findViewById(R.id.input_layout_radius);
        input_layout_name_geofence = (TextInputLayout) view.findViewById(R.id.input_layout_name_geofence);
        input_layout_latitude = (TextInputLayout) view.findViewById(R.id.input_layout_latitude);
        input_layout_longitude = (TextInputLayout) view.findViewById(R.id.input_layout_longitude);

        longitudeEditText.addTextChangedListener(new GeoFenceTextWatcher(longitudeEditText));
        latitudeEditText.addTextChangedListener(new GeoFenceTextWatcher(latitudeEditText));
        radiusEditText.addTextChangedListener(new GeoFenceTextWatcher(radiusEditText));

        Boolean isUpdate = false;
        if (getArguments() != null) {
            geofence = getArguments().getParcelable(ARG_USERGEOFENCE_ADD);
            name_geofence.setText(geofence.getName());
            latitudeEditText.setText(formatLatitude(String.valueOf(geofence.getLatitude())) + "  " + formatLongitude(String.valueOf(geofence.getLongitude())));
            longitudeEditText.setText(String.valueOf(geofence.getAddress()));
            latitudeEditText.setFocusableInTouchMode(true);
            latitudeEditText.setFocusable(true);
            longitudeEditText.setFocusableInTouchMode(true);
            longitudeEditText.setFocusable(true);

            //radiusEditText.setText(String.valueOf(geofence.getRadius()));
        }

        final PlacePicker.IntentBuilder PlaceBuilder = new PlacePicker.IntentBuilder();

        /*latitudeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!placePicker) {
                        startActivityForResult(PlaceBuilder.build(getActivity()), PLACE_PICKER_REQUEST);
                        placePicker = true;
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getActivity(), "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        longitudeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!placePicker) {
                        startActivityForResult(PlaceBuilder.build(getActivity()), PLACE_PICKER_REQUEST);
                        placePicker = true;
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getActivity(), "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });*/
        /*selectPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(PlaceBuilder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view);
        final Boolean finalIsUpdate = isUpdate;

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (/*validateLatLong() && */validateRadius()) {

                            if (((MainActivity) mActivity/*getActivity()*/).isGPSEnabled()) {

                                String name = name_geofence.getText().toString();
                               /* String latitude = latitudeEditText.getText().toString();
                                String longitude = longitudeEditText.getText().toString();*/
                                String radius = String.valueOf(radiusEditText.getText());
                                if (name.trim().isEmpty())
                                    name = String.valueOf(selectedPlace.getName());
                                UserGeofence addGeofence = new UserGeofence(name, String.valueOf(geofence.getLatitude()),
                                        String.valueOf(geofence.getLongitude()), radius,String.valueOf(geofence.getAddress()));

                                if (isDuplicateData()) {
                                    if (listener != null) {
                                        listener.onDialogPositiveClick(addGeofence);
                                        dialog.dismiss();
                                    }
                                } else {


                                    //input_layout_name_geofence.setError(getString(R.string.err_msg_duplicate));
                                    dialog.dismiss();
                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "This Territory already exists", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                                    snackbar.show();
                                }
                            } else {
                                //showValidationErrorToast();
                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Grant Location Permission and try again", Snackbar.LENGTH_LONG)
                                                    .setAction("Grant", new View.OnClickListener() {

                                                        // // TODO: 8/26/2016
                                                        @Override
                                                        public void onClick(View view) {
                                                            ((MainActivity) getActivity()).askForPermisson();
                                                        }
                                                    });
                                            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.lightCyan));
                                            View sbView = snackbar.getView();
                                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                                            snackbar.show();
                                        } else {
                                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Location Permission is required. Go to Location Permission and Enable for the app", Snackbar.LENGTH_LONG);
                                            View sbView = snackbar.getView();
                                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                                            snackbar.show();
                                        }
                                    }
                                } else {

                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Turn on the GPS and try again.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                                    snackbar.show();
                                }
                                dialog.dismiss();
                            }
                        }
                    }

                });

            }
        });
      /*  builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if ()
                {


                    String name = name_geofence.getText().toString();
                    String latitude = latitudeEditText.getText().toString();
                    String longitude = longitudeEditText.getText().toString();
                    String radius = String.valueOf(radiusEditText.getText());
                    UserGeofence geofence = new UserGeofence(name, latitude, longitude, radius);

                    if (listener != null) {
                        listener.onDialogPositiveClick(geofence);
                    }

                    dismiss();
                }
            }
        });*/


        return dialog;//builder.create();
    }

    public boolean isDuplicateData() {

        Cursor cursor = getActivity().getContentResolver().query(GeoFenceProvider.GeoFence.CONTENT_URI,
                null, GeoFenceColumns.GEONAME + "=? AND " + GeoFenceColumns.LATITUDE + "=? AND " + GeoFenceColumns.LONGITUDE + "=? ",
                new String[]{name_geofence.getText().toString(), String.valueOf(geofence.getLatitude()), String.valueOf(geofence.getLongitude())}, null);
        return !(cursor != null && cursor.getCount() != 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            getActivity();
            placePicker = false;
            if (resultCode == Activity.RESULT_OK) {
                /*selectPlace.setVisibility(View.GONE);
                longitudeEditText.setVisibility(View.VISIBLE);
                latitudeEditText.setVisibility(View.VISIBLE);*/


                selectedPlace = PlacePicker.getPlace(data, getActivity());
                // Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                longitudeEditText.setText(String.valueOf(selectedPlace.getLatLng().longitude));
                //longitudeEditText.setText(String.valueOf(selectedPlace.getLatLng()));
                /*longitudeEditText.setText(*//*String.valueOf*//*(Location.convert
                        (selectedPlace.getLatLng().longitude, Location.FORMAT_DEGREES)));*/
                latitudeEditText.setText(String.valueOf(selectedPlace.getLatLng().latitude));
                if (name_geofence.getText().toString().trim().isEmpty()) {
                    name_geofence.setText(selectedPlace.getName());
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public interface AddGeofenceFragmentListener {
        void onDialogPositiveClick(UserGeofence geofence);

    }

    private boolean validateSubject() {
        if (name_geofence.getText().toString().trim().isEmpty()) {
            //inputLayoutSubject.setErrorEnabled(true);
            input_layout_name_geofence.setError(getString(R.string.err_msg_subject));
            // requestFocus(subject);
            name_geofence.requestFocus();
            return false;
        } else {
            input_layout_name_geofence.setError(null);
            // inputLayoutSubject.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateRadius() {
        // inputLayoutBody.setErrorEnabled(true);
        if (radiusEditText.getText().toString().trim().isEmpty()) {
            //  inputLayoutBody.setErrorEnabled(true);
            input_layout_radius.setError(getString(R.string.err_msg_radius));
            // getView().requestFocus(body);
            radiusEditText.requestFocus();
            return false;
        } else if (0.01 <= (Double.parseDouble(radiusEditText.getText().toString())) && (Double.parseDouble(radiusEditText.getText().toString())) >= 20.0) {

            input_layout_radius.setError(getString(R.string.err_msg_range_radius));
            // getView().requestFocus(body);
            radiusEditText.requestFocus();
            return false;
        } else {
            input_layout_radius.setError(null);
        }
        /*if {

        } else    {
                input_layout_radius.setError(null);
                // inputLayoutBody.setErrorEnabled(false);

            }*/

        return true;
    }

    private boolean validateLatLong() {
        if (latitudeEditText.getText().toString().trim().isEmpty() || longitudeEditText.getText().toString().trim().isEmpty()) {
            //inputLayoutSubject.setErrorEnabled(true);
            //input_layout_name_geofence.setError(getString(R.string.err_msg_subject));
            // requestFocus(subject);
            // name_geofence.requestFocus();
            longitudeEditText.setError("required");
            latitudeEditText.setError("required");
            return false;
        } else {
            longitudeEditText.setError(null);
            latitudeEditText.setError(null);
            //input_layout_name_geofence.setError(null);
            // inputLayoutSubject.setErrorEnabled(false);
        }

        return true;
    }
    private String formatLatitude(String latitude) {
        String secondFormatLatitude = Location.convert(Double.parseDouble(latitude), Location.FORMAT_SECONDS);
        String[] LatitudeArray = secondFormatLatitude.split(":");
        String formattedLatitude = Math.abs(Double.parseDouble(LatitudeArray[0]))+"\u00B0" + LatitudeArray[1]+ "'"+String.format("%.2f",Double.parseDouble(LatitudeArray[2]))+"\"";
        if(Double.parseDouble(latitude)>=0)
            return formattedLatitude + "N";
        else
            return formattedLatitude + "S";
    }
    private String formatLongitude(String longitude) {
        String secondFormatLongitude = Location.convert(Double.parseDouble(longitude), Location.FORMAT_SECONDS);
        String[] LongitudeArray = secondFormatLongitude.split(":");
        String formattedLatitude = Math.abs(Double.parseDouble(LongitudeArray[0]))+"\u00B0" + LongitudeArray[1]+ "'"+String.format("%.2f",Double.parseDouble(LongitudeArray[2]))+"\"";
        if(Double.parseDouble(longitude)>=0)
            return formattedLatitude + "E";
        else
            return formattedLatitude + "W";
    }

    private class GeoFenceTextWatcher implements TextWatcher {

        private View view;

        private GeoFenceTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.radius:
                    validateRadius();
                    break;
                case R.id.latitudeEditText:
                    validateLatLong();
                    break;
                case R.id.longitudeEditText:
                    validateLatLong();
                    break;
                default:
                    validateRadius();
                    validateLatLong();
            }
        }
    }
}
