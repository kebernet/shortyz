package com.totsp.crossword.firstrun;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.totsp.crossword.net.NYTDownloader;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

/**
 * Created by rcooper on 6/27/15.
 */
public class NYTimes extends SlideFragment {

    private View loginForm;
    private Button loginButton;
    private AutoCompleteTextView username;
    private EditText password;
    private TextView textView;
    private Handler handler = new Handler();
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(thisView != null){
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide_nytimes, container, false);
        initView(v);
        this.loginForm = v.findViewById(R.id.loginForm);
        this.loginButton = (Button) v.findViewById(R.id.loginButton);
        this.username = (AutoCompleteTextView) v.findViewById(R.id.username);
        this.password = (EditText) v.findViewById(R.id.password);
        this.textView = (TextView) v.findViewById(R.id.slideText);
        prefs = ((ShortyzApplication) getActivity().getApplication()).getSettings();
        if(prefs.getBoolean("downloadNYT", false)){
            success();
        }
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        this.username.setOnFocusChangeListener(new FocusChangeWrapper(this.username.getOnFocusChangeListener()));
        this.password.setOnFocusChangeListener(new FocusChangeWrapper(this.password.getOnFocusChangeListener()));
        LinkedHashSet<String> emailAddresses = new LinkedHashSet<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                emailAddresses.add(account.name);
            }
        }
        this.username.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, emailAddresses.toArray(new String[emailAddresses.size()])));
        return v;
    }


    private void checkLogin() {
        if(username.getText() == null || username.getText().toString().trim().isEmpty()){
            Toast.makeText(this.getActivity(), "Please enter your username.", Toast.LENGTH_LONG).show();
            return;
        }
        if(password.getText() == null || password.getText().toString().trim().isEmpty()){
            Toast.makeText(this.getActivity(), "Please enter your password.", Toast.LENGTH_LONG).show();
            return;
        }
        username.setEnabled(false);
        password.setEnabled(false);
        loginButton.setEnabled(false);
        final NYTDownloader downloader = new NYTDownloader(getActivity(), username.getText().toString(), password.getText().toString());

        Runnable r = new Runnable(){

            @Override
            public void run() {
                try {
                    OkHttpClient client = downloader.login();
                    if(client == null){
                        fail("Login failed. Please check your username and password.");
                    } else {
                        success();
                    }

                } catch(IOException ioe){
                    fail("There was an error attempting to connect to nytimes.com");
                    ioe.printStackTrace();
                }
            }

        };
        new Thread(r).start();
    }

    private void success(){
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                loginForm.setVisibility(View.INVISIBLE);
                textView.setText("You're all set with the NYT puzzle!");
                prefs.edit().putBoolean("downloadNYT", true)
                        .putString("nytUsername", username.getText().toString())
                        .putString("nytPassword", password.getText().toString())
                        .apply();
            }
        });
    }

    private void fail(final String message){
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                username.setEnabled(true);
                password.setEnabled(true);
                loginButton.setEnabled(true);
            }
        });
    }

    private class FocusChangeWrapper implements View.OnFocusChangeListener {
        private final View.OnFocusChangeListener wrapped;

        private FocusChangeWrapper(View.OnFocusChangeListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                username.setHint("");
                password.setHint("");
            }
            if(wrapped != null){
                wrapped.onFocusChange(v, hasFocus);
            }
        }
    }
}
