package com.totsp.crossword.firstrun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;

/**
 * Created by rcooper on 6/28/15.
 */
public class Gmail extends SlideFragment {

    Button signin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(thisView != null){
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide_gmail, container, false);
        initView(v);
        signin = (Button) v.findViewById(R.id.sign_in_button);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FirstrunActivity) getActivity()).chooseAccount();
            }
        });
        if(((ShortyzApplication) getActivity().getApplication()).getGmailService() != null){
            done();
        }
        ((FirstrunActivity) getActivity()).gmailListener = new FirstrunActivity.GmailListener() {
            @Override
            public void onGmailCredentialed() {
                done();
            }
        };
        return v;
    }

    private void done() {
        signin.setVisibility(View.INVISIBLE);
        ((TextView) thisView.findViewById(R.id.slideText)).setText("You're all set with Gmail!");
    }


}
