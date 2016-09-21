package in.zollet.abhilash.reached.UI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import in.zollet.abhilash.reached.Common.Mail;
import in.zollet.abhilash.reached.Location.Constants;
import in.zollet.abhilash.reached.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class FeedbackActivityFragment extends Fragment {

    public FeedbackActivityFragment() {
    }

    String Body,Subject;
    EditText subject,body;
    private TextInputLayout inputLayoutSubject, inputLayoutBody;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView =  inflater.inflate(R.layout.fragment_feedback, container, false);
        setHasOptionsMenu(true);
       // Button send = (Button) rootView.findViewById(R.id.send);
        subject = (EditText) rootView.findViewById(R.id.subject);
        subject.requestFocus();
        if(subject.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        body = (EditText) rootView.findViewById(R.id.body);

        inputLayoutSubject = (TextInputLayout) rootView.findViewById(R.id.input_layout_subject);
        inputLayoutBody = (TextInputLayout) rootView.findViewById(R.id.input_layout_body);

        subject.addTextChangedListener(new FeedbackTextWatcher(subject));
        body.addTextChangedListener(new FeedbackTextWatcher(body));
        return rootView;

    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:

                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("message/rfc822");
                //email.putExtra(Intent.EXTRA_EMAIL  , new String[]{"help.journey.app@gmail.com"});

                email.putExtra(Intent.EXTRA_EMAIL  , new String[]{"abhilashmyworld@gmail.com"});

                email.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
                email.putExtra(Intent.EXTRA_TEXT, body.getText().toString());
                try {
                    startActivity(Intent.createChooser(email, "Send mail using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }

                getActivity().finish();


                /*if (validateSubject() && validateBody()) {
                    Body = body.getText().toString();
                    Subject = subject.getText().toString();

                    if (!isNetworkAvailable()) {
                        Toast.makeText(getActivity(), "Check your Connection Settings and Try again later", Toast.LENGTH_SHORT).show();
                    } else {


                    Mail m = new Mail(Constants.MAIL_ID, Constants.MAIL_PASSWORD);

                    String[] toArr = {Constants.MAIL_ID};
                    m.setTo(toArr);
                    m.setFrom(Constants.MAIL_ID);
                    m.setSubject(Subject);
                    m.setBody(Body);
                    try {

                        if (m.send()) {
                            Toast.makeText(getActivity(), "Thank You for your Valuable Feedback.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Email was not sent. Please try again later", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "There was a problem sending the email. Please try again later. ", Toast.LENGTH_LONG).show();

                    }


                   *//* try {
                        GMailSender sender = new GMailSender("abhilashmyworld@gmail.com", "1947@my_india");
                        sender.sendMail(Subject,
                                Body,
                                "abhilashmyworld@gmail.com",
                                "abhilashmyworld@outlook.com");
                    } catch (Exception e) {

                    }*//*
                    hideKeyboard();
                    Toast.makeText(getActivity(), "Thank You for your Valuable Feedback.", Toast.LENGTH_SHORT).show();
                }
                    getActivity().finish();
        }*/
                //getActivity().finish();

                return true;
            default:
                break;
        }

        return false;
    }


    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private boolean validateSubject() {
        if (subject.getText().toString().trim().isEmpty()) {
            //inputLayoutSubject.setErrorEnabled(true);
            inputLayoutSubject.setError(getString(R.string.err_msg_subject));
            //requestFocus(subject);
            subject.requestFocus();
            return false;
        } else {
            inputLayoutSubject.setError(null);
           // inputLayoutSubject.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateBody() {
       // inputLayoutBody.setErrorEnabled(true);
        if (body.getText().toString().trim().isEmpty()) {
          //  inputLayoutBody.setErrorEnabled(true);
            inputLayoutBody.setError(getString(R.string.err_msg_body));
          // getView().requestFocus(body);
            body.requestFocus();
            return false;
        } else {
            inputLayoutBody.setError(null);
           // inputLayoutBody.setErrorEnabled(false);

        }
        return true;
    }

    private class FeedbackTextWatcher implements TextWatcher {

        private View view;

        private FeedbackTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.body:
                    validateBody();
                    break;
                case R.id.subject:
                    validateSubject();
                    break;
                default:
                    validateBody();
                    validateSubject();
            }
        }
    }
    private boolean isNetworkAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
