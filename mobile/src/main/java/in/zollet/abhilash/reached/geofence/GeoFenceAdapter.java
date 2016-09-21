package in.zollet.abhilash.reached.geofence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import in.zollet.abhilash.reached.R;
import in.zollet.abhilash.reached.data.GeoFenceColumns;


public class GeoFenceAdapter extends RecyclerView.Adapter<GeoFenceAdapter.ViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    View mEmptyViewText;
    View mEmptyViewImage;
    private DataSetObserver mDataSetObserver;
    private boolean dataIsValid;
    private int rowIdColumn;


    private static OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {

        void onClickDelete(View itemView, int position);

        void onClickEdit(View itemView, int position);

        void onClickCard(View itemView, int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        GeoFenceAdapter.listener = listener;
    }

    public GeoFenceAdapter(Cursor cursor, View emptyViewText, View emptyViewImage) {
        mCursor = cursor;
        mEmptyViewText = emptyViewText;
        mEmptyViewImage = emptyViewImage;

    }

    public GeoFenceAdapter() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        final ViewHolder vh = new ViewHolder(view);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        String sd = mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.GEONAME));
        holder.name.setText(mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.GEONAME)));

        holder.longitude.setText("Co-ordinates: \n"+ formatLatitude(mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.LATITUDE)))
                +"  "+formatLongitude(mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.LONGITUDE))));

        String Address = mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.GEO_ADDRESS))== null ? "Couldn't find the address." : mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.GEO_ADDRESS));

         holder.latitude.setText(/*mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.LONGITUDE))*/"Address : " +Address);
                //holder.radius.setText(mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.RADIUS)) + " km");
        holder.radius.setText(/*mContext.getResources().getString(R.string.detail_text),asdew,sdsd,sdsdsds,adasdac))*/
        "Alert me ahead of : "+mCursor.getString(mCursor.getColumnIndex(GeoFenceColumns.RADIUS)) +" km.");
    }

    private String formatLatitude(String latitude) {
        String secondFormatLatitude = Location.convert(Double.parseDouble(latitude), Location.FORMAT_SECONDS);
        String[] LatitudeArray = secondFormatLatitude.split(":");
        String formattedLatitude = Math.abs(Double.parseDouble(LatitudeArray[0]))+"\u00B0" + LatitudeArray[1]+ "'"+String.format("%.2f",Double.parseDouble(LatitudeArray[2]))+"\"";
        if(Double.parseDouble(latitude)>=0)
            return formattedLatitude + "N";
        else
            return formattedLatitude + "S";
    }
    private String formatLongitude(String longitude) {
        String secondFormatLongitude = Location.convert(Double.parseDouble(longitude), Location.FORMAT_SECONDS);
        String[] LongitudeArray = secondFormatLongitude.split(":");
        String formattedLatitude = Math.abs(Double.parseDouble(LongitudeArray[0]))+"\u00B0" + LongitudeArray[1]+ "'"+String.format("%.2f",Double.parseDouble(LongitudeArray[2]))+"\"";
        if(Double.parseDouble(longitude)>=0)
            return formattedLatitude + "E";
        else
            return formattedLatitude + "W";
    }

    @Override
    public int getItemCount() {

        int count = (mCursor == null ? 0 : mCursor.getCount());
        mEmptyViewText.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        mEmptyViewImage.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        return count;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            rowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            dataIsValid = true;
            notifyDataSetChanged();
        } else {
            rowIdColumn = -1;
            dataIsValid = false;
            notifyDataSetChanged();
        }

        mEmptyViewText.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        mEmptyViewImage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

        return oldCursor;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name, latitude, longitude, radius, edit, delete;


        public ViewHolder(/*final*/ View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            latitude = (TextView) view.findViewById(R.id.latitude);
            longitude = (TextView) view.findViewById(R.id.longitude);
            radius = (TextView) view.findViewById(R.id.radius);
            edit = (TextView) view.findViewById(R.id.edit);
            delete = (TextView) view.findViewById(R.id.delete);
            //final ImageView textView = (ImageView) view.findViewById(R.id.test);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Do you want to select " + name.getText() + " ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (listener != null)
                                        listener.onClickCard(itemView, getLayoutPosition());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();

                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                   /* if (listener != null)
                        listener.onClickEdit(itemView, getLayoutPosition());*/

                    //ImageView textView = (ImageView) view.findViewById(R.id.test);

                    /*if ( textView.getVisibility() == View.GONE)
                    {
                        //expandedChildList.set(arg2, true);
                        textView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        //expandedChildList.set(arg2, false);
                        textView.setVisibility(View.GONE);
                    }*/
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Are You Sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (listener != null) {
                                        listener.onClickDelete(itemView, getLayoutPosition());
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }

    }

}

