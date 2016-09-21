package in.zollet.abhilash.reached.UI;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.List;

import in.zollet.abhilash.reached.R;

public class ShareActivity extends AppCompatActivity {

    ImageView whatsapp,facebook,twitter,share;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        whatsapp = (ImageView) findViewById(R.id.whatsapp);
        facebook = (ImageView) findViewById(R.id.facebook);
        twitter = (ImageView) findViewById(R.id.twitter);
        share = (ImageView) findViewById(R.id.share);

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setQuote("Download this awesome app: which alert you about your destination and works without internet.")
                        // .setContentTitle("Download this awesome app")
                        // .setContentDescription(
                               // "Just set the destination and it'll alert you when you are about to reach at your destination so that you can spend that time productively, not worrying of missing the destination")
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()))
                        .build();
                ShareDialog shareDialog = new ShareDialog(ShareActivity.this);
                shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);


                /*Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                sendIntent.setType("text/plain");
                //sendIntent.setPackage("com.facebook");
                //startActivity(sendIntent);
                PackageManager pm = v.getContext().getPackageManager();
                List<ResolveInfo> activityList = pm.queryIntentActivities(sendIntent, 0);
                for (final ResolveInfo app : activityList)
                {
                    if ((app.activityInfo.name).startsWith("com.facebook.katana"))
                    {
                        final ActivityInfo activity = app.activityInfo;
                        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                        sendIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        sendIntent.setComponent(name);
                        v.getContext().startActivity(sendIntent);
                        break;
                    }
                }*/
            }
        });
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                sendIntent.setType("text/plain");
                *//*sendIntent.setPackage("com.twitter");
                startActivity(sendIntent);*//*
                PackageManager pm = v.getContext().getPackageManager();
                List<ResolveInfo> activityList = pm.queryIntentActivities(sendIntent, 0);
                for (final ResolveInfo app : activityList)
                {
                    if ("com.twitter.android.PostActivity".equals(app.activityInfo.name))
                    {
                        final ActivityInfo activity = app.activityInfo;
                        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                        sendIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        sendIntent.setComponent(name);
                        v.getContext().startActivity(sendIntent);
                        break;
                    }
                }*/
                try
                {
                    // Check if the Twitter app is installed on the phone.
                    getPackageManager().getPackageInfo("com.twitter.android", 0);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setClassName("com.twitter.android", "com.twitter.android.composer.ComposerActivity");
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT,("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                    startActivity(intent);

                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Twitter is not installed on this device", Toast.LENGTH_LONG).show();

                }
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Download");
                sendIntent.putExtra(Intent.EXTRA_TEXT, Uri.parse("https://www.google.co.in/"));

                //sendIntent.setData(Uri.parse("https://www.google.co.in/"));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via"));*/
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download : ");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

    }

}
