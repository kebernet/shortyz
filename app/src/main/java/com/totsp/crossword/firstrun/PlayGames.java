package com.totsp.crossword.firstrun;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.totsp.crossword.shortyz.R;

/**
 *
 * Created by rcooper on 6/27/15.
 */
public class PlayGames extends SlideFragment  {

    private static final String TAG = "Play Games";
    private View signInButton;
    private TextView text;
    private AchievementsClient mAchievementsClient;
    private GoogleSignInClient mGoogleSignInClient;


    public PlayGames() {
    }

    protected boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this.getContext()) != null;
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        this.mAchievementsClient = Games.getAchievementsClient(this.getContext(), googleSignInAccount);
        this.onSignInSucceeded();
        done();
    }

    protected void onSignInSucceeded(){

    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        this.signInButton.setVisibility(View.VISIBLE);
        mAchievementsClient = null;
    }

    protected void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this.getActivity(),
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(thisView != null){
            return thisView;
        }

        this.mGoogleSignInClient = GoogleSignIn.getClient(this.getContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        View v = inflater.inflate(R.layout.slide_playgames, container, false);
        initView(v);
        text = (TextView) v.findViewById(R.id.slideText);
        (this.signInButton = v.findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // start the sign-in flow
                signInSilently();
            }

        });
        if(isSignedIn()){
            done();
        } else {
            this.signInButton.setVisibility(View.VISIBLE);
        }
        return v;
    }

    private void done() {
        signInButton.setVisibility(View.INVISIBLE);
        text.setText("You're all set with Play Games!");
    }
}
