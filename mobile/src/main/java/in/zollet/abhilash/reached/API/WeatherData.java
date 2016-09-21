package in.zollet.abhilash.reached.API;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhilash on 9/4/2016.
 */
public class WeatherData {
    private List<Weather> weather = new ArrayList<Weather>();
    private Main main;
    private Integer visibility;


    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
}
