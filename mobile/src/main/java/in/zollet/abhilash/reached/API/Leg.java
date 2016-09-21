package in.zollet.abhilash.reached.API;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhilash on 7/31/2016.
 */
public class Leg {
    private Distance distance;
    private Duration duration;
    private DurationInTraffic duration_in_traffic;
    private String end_address;
    private EndLocation end_location;
    private String start_address;
    private StartLocation start_location;

    public Distance getDistance() {
        return distance;
    }


    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public DurationInTraffic getDurationInTraffic() {
        return duration_in_traffic;
    }

    public void setDurationInTraffic(DurationInTraffic durationInTraffic) {
        this.duration_in_traffic = durationInTraffic;
    }


    public String getEndAddress() {
        return end_address;
    }

    public void setEndAddress(String end_address) {
        this.end_address = end_address;
    }

    public EndLocation getEndLocation() {
        return end_location;
    }

    public void setEndLocation(EndLocation end_location) {
        this.end_location = end_location;
    }

    public String getStartAddress() {
        return start_address;
    }

    public void setStartAddress(String start_address) {
        this.start_address = start_address;
    }


    public StartLocation getStartLocation() {
        return start_location;
    }


    public void setStartLocation(StartLocation start_location) {
        this.start_location = start_location;
    }
}
