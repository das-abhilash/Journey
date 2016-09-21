package in.zollet.abhilash.reached.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import in.zollet.abhilash.reached.R;

public class FAQActivity extends AppCompatActivity {

    RecyclerView recyclerView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_faq);
        String[] question = new String[] { "1.\tWhat is this app all about? What Can I do with this app?",
                "2.\tDo I need internet connection for the app to work?",
        "3.\tWhat is a Location here?",
        "4.\tHow to add a “Location”?",
        "5.\tWhat is the \"alert me ahead of\" means?",
        "6.\tHow can I set the Destination?",
        "7.\tHow can I get my Current Location?",
        "8.\tDo you I have to click on “Start” to make the app alert me? What if I forgot to click on start?",
        "9.\tCan I select multiple Destination?",
        "10.\tWhat if I am waking or bicycling, how to set this?",
        "11.\tWhy I don’t get the distance and time for bicycling mode?",
        "12.\tHow can I change the units?",
        "13.\tHow can I change the Language to my native language?",
        "14.\tWhat is the “Traffic Model” in settings page?",
        "15.\tIs there a way to see the directions for my destination directly from the app?",
        "16.\tHow can I set the destination if I am going in a train?",
        "17.\tIs it gonna work when my battery saver is on?"};

        String[] answer = new String[] {"Ans. The whole purpose of this app is to give the users the liberty to spend their travelling time by reading books or watching videos or let’s just say, finishing off their sleep.\nWhen a person travels in any mode of transportation, s/he spends many minutes wondering and checking whether s/he has reached at the destination. This app, once configured properly, will alert you when you’re about to reach at your Destination.\nIt doesn’t matter if you are travelling in car or Public transit or train or simply walking, it works the same. \n" +
                "Just set the destination in the app, and the app will alert you when you are about to reach at your destination.\nNow you can enjoy your journey without worrying about missing your destination.\n",
                "Ans. No, not at all. The app will work even If you don’t have internet. But the accuracy may vary. If you have internet, then you can check the distance remained and duration remained and your current location.",
                "Ans. We call the destination, you have created, as a Location. Once you get inside the circle, centering your chosen Location, of a radius set by you as \"alert me ahead of\", you will be alerted.",
                "Ans. Click on the “+” icon to add a Location. \n" +
                        "a.\tEither choose anu location from the map or Search for a location in the search bar.\n" +
                        "b.\tBy default the name of the name of the place will be assigned to that destination. But you can change it before saving it.\n" +
                        "c.\tSet the \"alert me ahead of\" for the destination in kilometers. The value of the \"alert me ahead of\" can range from 0.01 km to 20 km\n",
                "Ans. \"Alert me ahead of\" means you’ll be alerted when you enter within the circle of radius (which is alert me ahead of) keeping the destination as the center.",
                "Ans. There are two ways in which you can set your Destination.\n" +
                        "a.\tGo to “Your Places” page and you can click on your desired destination and then click on yes.\n" +
                        "b.\tClick on destination field on home page and you will be redirected to “Your Places” page. And then do the same as (a).\n",
                "Ans. You don’t need to do anything. If the mobile is connected to the internet, the app will do it for you. If you want to refresh your current location, then click on the refresh icon on top of the page.",
                "Ans. Yes, you have to click on “Start” to tell the app to alert you when you’re about to reach at your destination. Otherwise the app couldn’t know which one will be your destination out of all your territories.",
                "Ans. Sorry to inform you that this feature is not available yet. We’re working hard to add this feature. So in next updates, you can add multiple destinations, but not as of now.",
                "Ans. You can set the Travel Mode in the app’s setting under general section. It can be either one of:\n" +
                        "i.\tDriving\n" +
                        "ii.\tWalking\n" +
                        "iii.\tBicycling\n" +
                        "iv.\tTransit\n" +
                        "The Distance and duration to reach to the destination will be shown depending on which travel mode you have chosen.\n",
                "Ans. Bicycling and walking mode is unavailable for some routes. So we can’t display the distance and time remained for those routes, But don’t worry, you sure will be alerted when you are about to reach at the destination.",
                "Ans. If you want the unis to be seen in metric system, then select metric under Unit System  of app’s general settings. If you the data to be in imperial form, then select imperial.",
                "Ans. The distance and duration remained are only be displayed in the supported language not all of the app’s content. There are 8 supported languages to choose from. \n" +
                        "i.\tEnglish\n" +
                        "ii.\tHindi\n" +
                        "iii.\tKannada\n" +
                        "iv.\tTamil\n" +
                        "v.\tTelugu\n" +
                        "vi.\tGujarati\n" +
                        "vii.\tBengali\n" +
                        "viii.\tMarathi\n",
                "Ans. You can use one of available traffic models: \n" +
                        "i.\tBest Guesses\n" +
                        "ii.\tPessimistic\n" +
                        "iii.\tOptimistic\n" +
                        "“Best Guesses” will be selected by default.\n" +
                        "If you chose “best guesses”, then the app will decide which will be the best route and display the data about distance and duration remained. If you chose “Pessimistic”, then it’ll decide the longest route and show you the worst possible time and distance that it can take. And if you chose “Optimistic” then it’s just opposite of pessimistic and show the minimum time and route it can take. \n" +
                        "Note: sometimes best guess route might be greater than pessimistic route or less than optimistic route.\n",
                "Ans. Yes, once you set the destination and click on “Start”, a “direction” icon will appear on the top right of your screen. When you click on this icon, you’ll be redirected to Google maps with your current location and destination set.",
                "Ans. You can set the destination in the same way for other travel modes. But we’ll recommend to set the \"alert me ahead of\" to minimum 10-15 km so that you’ll be alerted appropriately. ","Ans. We are sorry to inform you that it won't work when the power saver is on as all background process will be suspended. But If the app is opened, then it'll work as normal"};
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FAQAdapter faqAdapter = new FAQAdapter( question, answer);
        recyclerView.setAdapter(faqAdapter);

    }

    private class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder>{

        String[]  question, answer;
        public FAQAdapter(String[] question, String[] answer ){
            this.question = question;
            this.answer = answer;
        }

        @Override
        public FAQAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_listitem, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(FAQAdapter.ViewHolder holder, int position) {

            holder.answer.setText(answer[position]);
            holder.question.setText(question[position]);
        }

        @Override
        public int getItemCount() {
            return question.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView answer, question;
            public  ImageView up, down;
            public TextView viewstub;


            public ViewHolder(/*final*/ final View view) {
                super(view);
                answer = (TextView) view.findViewById(R.id.answer);
                question = (TextView) view.findViewById(R.id.question);
                up = (ImageView) view.findViewById(R.id.up_button);
                down = (ImageView) view.findViewById(R.id.down_button);
               // viewstub= (TextView) findViewById(R.id.answerViewStub);

                //RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                //buttonLayoutParams.setMargins(50, 10, 0, 0);
                up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answer.setVisibility(View.GONE);
                        up.setVisibility(View.GONE);
                        //viewstub.setVisibility(View.GONE);
                        down.setVisibility(View.VISIBLE);
                        /*if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            final float scale = getResources().getDisplayMetrics().density;
                            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                            Resources r = getResources();
                            float top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0.5, r.getDisplayMetrics());
                            float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
                            float bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
                            float right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0.5, r.getDisplayMetrics());
                            p.setMargins((int) left,(int)top,(int) right ,(int) bottom);
                            view.requestLayout();
                        }*/

                    }
                    });
                down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answer.setVisibility(View.VISIBLE);
                        up.setVisibility(View.VISIBLE);
                        //viewstub.setVisibility(View.VISIBLE);
                        down.setVisibility(View.GONE);
                       /* if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            final float scale = getResources().getDisplayMetrics().density;
                            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                            Resources r = getResources();
                            float top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
                            float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                            float bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
                            float right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
                            p.setMargins((int) left,(int)top,(int) right ,(int) bottom);
                            // p.setMargins((int) (8* scale + 0.5f),(int)( 1* scale + 0.5f),(int) (2* scale + 0.5f) ,(int) (1* scale + 0.5f));
                            view.requestLayout();

                        }*/
                    }
                });

            }
        }
    }

}
