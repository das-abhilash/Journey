package in.zollet.abhilash.reached.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;



@Database(version = GeoFenceDatabase.VERSION)
public class GeoFenceDatabase {
    private GeoFenceDatabase() {
    }

    public static final int VERSION = 2;

    @Table(GeoFenceColumns.class)
    public static final String GEOFENCE = "GeoFence";

    @OnCreate
    public static void onCreate(Context context, SQLiteDatabase db) {
        //db.execSQL(context,DATABASE_CREATE_TEAM);
        //GeneratedDatabase.getInstance(context).onCreate(db);
    }

    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion,
                                 int newVersion) {

        // db.execSQL("DELETE TABLE IF EXISTS " + GEOFENCE);
        /*for (int i = oldVersion; i < newVersion; i++) {

        }*/
        db.beginTransaction();
        try {
            db.execSQL("ALTER TABLE "+GEOFENCE + " ADD COLUMN " + GeoFenceColumns.GEO_ADDRESS+" TEXT   ;" /*"DROP TABLE "+GEOFENCE*/);
            //db.execSQL("CREATE TABLE "+GEOFENCE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }



    }
}
