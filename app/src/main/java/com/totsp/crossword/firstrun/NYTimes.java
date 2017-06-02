package com.totsp.crossword.firstrun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.totsp.crossword.nyt.LoginActivity;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;

/**
 *
 */
public class NYTimes extends SlideFragment {

    private View loginForm;
    private TextView textView;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (thisView != null) {
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide_nytimes, container, false);
        initView(v);
        this.loginForm = v.findViewById(R.id.loginForm);
        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        this.textView = (TextView) v.findViewById(R.id.slideText);
        prefs = ((ShortyzApplication) getActivity().getApplication()).getSettings();
        if (prefs.getBoolean("didNYTLogin", false)) {
            success();
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean("didNYTLogin", false)) {
            success();
        }
    }

    private void checkLogin() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        getActivity().startActivity(i);
    }

    private void success() {
        loginForm.setVisibility(View.INVISIBLE);
        textView.setText("You're all set with the NYT puzzle!");
        prefs.edit().putBoolean("downloadNYT", true)
                .apply();
    }

}
