package com.totsp.crossword;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.totsp.crossword.shortyz.R;

public class GamesSignIn extends ShortyzActivity  {

    private View signInButton;
    private View signOutButton;
    private View ignoreButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_games_sign_in);
        (this.signInButton = findViewById(R.id.sign_in_button)).setOnClickListener(new OnClickListener(){

			public void onClick(View view) {
				// start the sign-in flow
	            beginUserInitiatedSignIn();
			}
			
		});
        (this.signOutButton = findViewById(R.id.sign_out_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                signOut();
                finish();
            }
        });
        (this.ignoreButton = findViewById(R.id.dont_want_play)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(signInButton.getVisibility() == View.VISIBLE){
                    signOut();
                }
                prefs.edit().putBoolean("no_play_games", Boolean.TRUE).commit();
                finish();

            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.games_sign_in, menu);
		return true;
	}

	public void onSignInFailed() {
		Toast.makeText(this, "Not signed in.", Toast.LENGTH_LONG).show();
        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.GONE);
	}

	public void onSignInSucceeded() {
        Toast.makeText(this, "You are signed into Play Games!", Toast.LENGTH_LONG).show();
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
	}

}
