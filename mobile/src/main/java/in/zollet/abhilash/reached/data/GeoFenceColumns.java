package in.zollet.abhilash.reached.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;


public class GeoFenceColumns {
    @DataType(DataType.Type.INTEGER)
    @AutoIncrement  @PrimaryKey
    public static final String _ID = "_id";

    @DataType(DataType.Type.INTEGER)
    public static final String ID = "id";

    @DataType(DataType.Type.TEXT)
    public static final String GEONAME = "name";

    @DataType(DataType.Type.TEXT)  @NotNull
    public static final String LATITUDE = "latitude";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String LONGITUDE = "longitude";

    @DataType(DataType.Type.TEXT)
    public static final String GEO_ADDRESS = "address";

    @DataType(DataType.Type.TEXT)
    public static final String RADIUS = "radius";

    @DataType(DataType.Type.INTEGER)
    public static final String COUNT = "count";



}
