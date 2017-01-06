package com.totsp.crossword.firstrun;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.totsp.crossword.GameHelper;
import com.totsp.crossword.shortyz.R;

/**
 *
 * Created by rcooper on 6/27/15.
 */
public class PlayGames extends SlideFragment {

    private View signInButton;
    private TextView text;

    public PlayGames() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(thisView != null){
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide_playgames, container, false);
        initView(v);
        text = (TextView) v.findViewById(R.id.slideText);
        (this.signInButton = v.findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // start the sign-in flow
                ((FirstrunActivity) getActivity()).beginUserInitiatedSignIn();
            }

        });
        if(((FirstrunActivity) getActivity()).isSignedIn() ){
            done();
        } else {
            this.signInButton.setVisibility(View.VISIBLE);
        }
        ((FirstrunActivity) getActivity()).setGameHelperListener(new GameHelper.GameHelperListener() {

            @Override
            public void onSignInFailed() {
                signInButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSignInSucceeded() {
                done();
            }
        });
        return v;
    }

    private void done() {
        signInButton.setVisibility(View.INVISIBLE);
        text.setText("You're all set with Play Games!");
    }
}
