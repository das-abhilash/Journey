package in.zollet.abhilash.reached.API;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhilash on 7/31/2016.
 */
public class Route {

    private String copyrights;
    private List<Leg> legs = new ArrayList<Leg>();
    private String summary;

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public List<Leg> getLegs() {
        return legs;
    }


    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }

}
