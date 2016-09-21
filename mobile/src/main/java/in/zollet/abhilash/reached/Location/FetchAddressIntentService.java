package in.zollet.abhilash.reached.Location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FetchAddressIntentService extends IntentService {

    public FetchAddressIntentService(String name) {
        super(name);
    }

    public FetchAddressIntentService() {
        super(FetchAddressIntentService.class.getName());
    }



    @Override
    protected void onHandleIntent(Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
            int oi = 0;
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";//getString(R.string.service_not_available);
           /* Log.e(TAG, errorMessage, ioException);*/
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage =" invalid lat long used"; //getString(R.string.invalid_lat_long_used);
            /*Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);*/
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "address not found";//getString(R.string.no_address_found);

            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            String locationAddress = addressFragments.get(0);
            if(!addressFragments.get(1).isEmpty())
                locationAddress = locationAddress+"\n "+ addressFragments.get(1);

            /*deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));*/
            deliverResultToReceiver(Constants.SUCCESS_RESULT, locationAddress);
        }

    }

    private void deliverResultToReceiver(int resultCode, String message) {
       /* Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
       // mReceiver.send(resultCode, bundle);

        Intent in = new Intent(this, NotificationReceiver.class);//(this, MyService.class);
        in.putExtra("action",message);
        this.sendBroadcast(in);*/

        Intent intent = new Intent();
        intent.setAction(Constants.CURRENT_LOCATION_ADDRESS_DATA_KEY);
        intent.putExtra("message", message);
        this.sendBroadcast(intent);
    }
}
