package in.zollet.abhilash.reached.geofence;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;

import java.util.UUID;

public class UserGeofence implements Parcelable {

    // region Properties

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String  radius;
    private String address;

    public UserGeofence(String name,String latitude,String longitude,String address){
        this.name = name;
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
        this.address = address;
    }
    public UserGeofence(String name,String latitude,String longitude){
        this.name = name;
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
    }
    public UserGeofence(String name,String latitude,String longitude,String radius,String address){
        this.name = name;
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
        this.radius = radius;/*Float.parseFloat(radius) * 1000.0f;*/
        this.address = address;
    }

    protected UserGeofence(Parcel in) {
        id = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        radius = in.readString();
        address = in.readString();
    }

    public static final Creator<UserGeofence> CREATOR = new Creator<UserGeofence>() {
        @Override
        public UserGeofence createFromParcel(Parcel in) {
            return new UserGeofence(in);
        }

        @Override
        public UserGeofence[] newArray(int size) {
            return new UserGeofence[size];
        }
    };

    public Geofence geofence() {
        id = UUID.randomUUID().toString();
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, Float.parseFloat(radius) * 1000.0f )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(radius);
        dest.writeString(address);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String  getRadius() {
        return radius;
    }

    public String getAddress() {
        return address;
    }
}
