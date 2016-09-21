package in.zollet.abhilash.reached.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {

    @GET("data/2.5/weather")
    Call<WeatherData> getWeather(
            @Query("lat") String Lat, @Query("lon") String Lon
            ,@Query("appid") String AppID, @Query("units") String Units);


}