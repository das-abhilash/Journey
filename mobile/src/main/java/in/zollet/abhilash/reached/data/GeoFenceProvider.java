package in.zollet.abhilash.reached.data;


import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;




@ContentProvider(authority = GeoFenceProvider.AUTHORITY, database = GeoFenceDatabase.class)
public class GeoFenceProvider {
    public static final String AUTHORITY = "in.zollet.abhilash.reached.data";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String GeoFence= "geofence";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = GeoFenceDatabase.GEOFENCE)
    public static class GeoFence {
        @ContentUri(
                path = Path.GeoFence,
                type = "vnd.android.cursor.dir/news"
        )
        public static final Uri CONTENT_URI = buildUri(Path.GeoFence);

        @InexactContentUri(
                name = "NEWS_ID",
                path = Path.GeoFence + "/*",
                type = "vnd.android.cursor.item/news",
                whereColumn = GeoFenceColumns._ID,
                pathSegment = 1
        )
        public static Uri ID(String ID) {
            return buildUri(Path.GeoFence, ID);
        }
    }
}

