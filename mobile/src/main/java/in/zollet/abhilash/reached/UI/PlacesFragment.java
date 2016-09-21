package in.zollet.abhilash.reached.UI;


import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceColumns;
import in.zollet.abhilash.reached.data.GeoFenceProvider;
import in.zollet.abhilash.reached.geofence.GeoFenceAdapter;
import in.zollet.abhilash.reached.geofence.UserGeofence;


public class PlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int LOCATIONGROUP_PERMISSIONS_REQUEST = 100;
    private OnPlacesFragmentInteractionListener mListener;
    private static final int CURSOR_LOADER_ID = 6;
    RecyclerView recyclerView;
    GeoFenceAdapter geoFenceAdapter;
    TextView emptyText;
    ImageView emptyImage;
    Cursor cursor;
    private boolean remove;
    private boolean showcase = false;
    ProgressBar progressBar;

    public PlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_places, container, false);
        remove = true;

        getActivity().setTitle("Your Places");
        emptyImage = (ImageView) view.findViewById(R.id.recycler_view_empty_icon);
        emptyText = (TextView) view.findViewById(R.id.recycler_view_empty_text);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar = (ProgressBar) getActivity().findViewById(R.id.placesProgressList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //geoFenceAdapter = new GeoFenceAdapter(null,emptyText,emptyImage);
        /*geoFenceAdapter.setOnItemClickListener(new GeoFenceAdapter.OnItemClickListener() {

            @Override
            public void onClickDelete(View itemView, int position) {

                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                UserGeofence geofence = new UserGeofence(name,latitude,longitude,radius);

                if (mListener != null) {
                    mListener.onPlacesFragmentInteractionDelete(geofence);
                }

                Toast.makeText(getActivity(), "clicked on Delete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickEdit(View itemView, int position) {
                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                UserGeofence geofence = new UserGeofence(name,latitude,longitude,radius);

                if (mListener != null) {
                    mListener.onPlacesFragmentInteractionEdit(geofence);
                }

                Toast.makeText(getActivity(), "clicked on Edit", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickCard(View itemView, int position) {

                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                UserGeofence geofence = new UserGeofence(name,latitude,longitude,radius);


                if (mListener != null) {
                    mListener.onPlacesFragmentInteractionSelect(geofence);
                }
                Toast.makeText(getActivity(), "clicked on Card", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(geoFenceAdapter);*/

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        setHasOptionsMenu(true);
        super.onAttach(context);
        if (context instanceof OnPlacesFragmentInteractionListener) {
            mListener = (OnPlacesFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        remove = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*if(remove){
            Fragment fragment = (Fragment) getFragmentManager().findFragmentByTag("PlacesFragment");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(fragment).commit();
        }*/

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        return  new CursorLoader(getActivity(), GeoFenceProvider.GeoFence.CONTENT_URI, null,null, null,GeoFenceColumns.COUNT + " DESC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        geoFenceAdapter.swapCursor(data);
        progressBar.setVisibility(View.GONE);
        cursor = data;
        if (data.getCount()!= 0){
            Target viewTarget = new ViewTarget(R.id.showcase, getActivity());
            new ShowcaseView.Builder(getActivity())
                    .setStyle(R.style.CustomShowcaseTheme3)
                    .setTarget(viewTarget)
                    .setContentTitle("Click on this Territory")
                    .setContentText("This Territory will be selected as your Destination.")
                    .singleShot(50)
                    .build();
        }
            showcase = true;
        geoFenceAdapter = new GeoFenceAdapter(data,emptyText,emptyImage);

        geoFenceAdapter.setOnItemClickListener(new GeoFenceAdapter.OnItemClickListener() {

            @Override
            public void onClickDelete(View itemView, int position) {
                Activity activity = (MainActivity)getActivity();

                if ( ((MainActivity)getActivity()).isGPSEnabled()) {
                    cursor.moveToPosition(position);
                    String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                    String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                    String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                    String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                    String address = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEO_ADDRESS));
                    UserGeofence geofence = new UserGeofence(name, latitude, longitude, radius,address);

                    if (mListener != null) {
                        mListener.onPlacesFragmentInteractionDelete(geofence);
                    }
                } else {

                    if(ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        if(shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Grant Location Permission and try again", Snackbar.LENGTH_LONG)
                                .setAction("Grant", new View.OnClickListener() {

                                    // // TODO: 8/26/2016
                                    @Override
                                    public void onClick(View view) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                                    LOCATIONGROUP_PERMISSIONS_REQUEST);
                                        }
                                    }
                                });
                        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.lightCyan));
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                        snackbar.show();

                    } else {
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Location Permission is required.", Snackbar.LENGTH_LONG)
                                    .setAction("Grant", new View.OnClickListener() {

                                                @Override
                                                public void onClick(View view) {
                                                    Intent i = new Intent();
                                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                                    i.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                    getActivity().startActivity(i);
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        public void run() {
                                                            // Create layout inflator object to inflate toast.xml file
                                                            LayoutInflater inflater = getLayoutInflater(null);

                                                            // Call toast.xml file for toast layout
                                                            View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);

                                                            final Toast toast = new Toast(getActivity());

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
                            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.lightCyan));
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                            snackbar.show();
                        }
                    } else {

                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.fab), "Turn on the GPS and try again.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.lime));
                        snackbar.show();
                    }
                }
            }

            @Override
            public void onClickEdit(View itemView, int position) {
              /*  cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                UserGeofence geofence = new UserGeofence(name,latitude,longitude,radius);

                if (mListener != null) {
                    mListener.onPlacesFragmentInteractionEdit(geofence);
                }*/
            }

            @Override
            public void onClickCard(View itemView, int position) {

                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEONAME));
                String latitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.LONGITUDE));
                String radius = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.RADIUS));
                String address = cursor.getString(cursor.getColumnIndex(GeoFenceColumns.GEO_ADDRESS));
                UserGeofence geofence = new UserGeofence(name, latitude, longitude, radius,address);


                if (mListener != null) {
                    mListener.onPlacesFragmentInteractionSelect(geofence);
                    mListener.onDestinationSelected(true);
                }
            }
        });
        recyclerView.setAdapter(geoFenceAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static void RefreshgeoFenceList() {
       // geoFenceAdapter.notifyDataSetChanged();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPlacesFragmentInteractionListener {

        void onPlacesFragmentInteractionDelete(UserGeofence geofence);
        void onPlacesFragmentInteractionEdit(UserGeofence geofence);
        void onPlacesFragmentInteractionSelect(UserGeofence geofence);
        void onDestinationSelected(boolean homePage);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem item = menu.findItem(R.id.action_direction);
        item.setVisible(false);
        item.setEnabled(false);
        //inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
            progressBar.setVisibility(View.VISIBLE);
            return true;
        } else if (id == R.id.action_direction) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
