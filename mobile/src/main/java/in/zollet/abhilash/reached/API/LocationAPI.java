package in.zollet.abhilash.reached.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Abhilash on 7/30/2016.
 */
public interface LocationAPI {

    @GET("maps/api/directions/json")
    Call<LocationData> getLocation(
            @Query("origin") String Origin, @Query("destination") String Destination
            ,@Query("key") String Key, @Query("units") String Units,
            @Query("departure_time") String DepartureTime,@Query("mode") String Mode,
            @Query("language") String Language,@Query("traffic_model") String TrafficModel,
            @Query("transit_mode ") String TransitMode);


}
