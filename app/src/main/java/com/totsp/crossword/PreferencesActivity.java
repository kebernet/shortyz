package com.totsp.crossword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.totsp.crossword.firstrun.FirstrunActivity;
import com.totsp.crossword.gmail.GMConstants;
import com.totsp.crossword.service.BackgroundDownloadService;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.versions.AndroidVersionUtils;


public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if (!AndroidVersionUtils.Factory.getInstance().isBackgroundDownloadAvaliable()) {
            Preference backgroundDownload = findPreference("backgroundDownload");
            backgroundDownload.setSelectable(false);
            backgroundDownload.setEnabled(false);
            backgroundDownload.setSummary("Requires Android Lollipop or later");
        }

        findPreference("releaseNotes")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        findPreference("license")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/license.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        findPreference("nytSubscribe")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com/puzzle"));
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        findPreference("nytClear")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this).edit()
                        .putBoolean("didNYTLogin", false)
                        .apply();
                Toast.makeText(PreferencesActivity.this, "Cleared", Toast.LENGTH_LONG)
                        .show();
                return true;
            }
        });


        findPreference("aboutScrapes")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/scrapes.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        findPreference("firstRun")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                Intent i = new Intent(Intent.ACTION_VIEW, null,
                        PreferencesActivity.this, FirstrunActivity.class);
                PreferencesActivity.this.startActivity(i);

                return true;
            }
        });

        findPreference("clearGmail")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShortyzApplication application = ((ShortyzApplication) getApplication());
                application.getSettings().edit()
                        .putString(GMConstants.PREF_ACCOUNT_NAME, null)
                        .apply();
                application.updateCredential(application.getSettings());
                return true;
            }
        });
    }

    protected void onResume() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String pref) {
        if (pref.equals("backgroundDownload") ||
                pref.equals("backgroundDownloadRequireUnmetered") ||
                pref.equals("backgroundDownloadAllowRoaming") ||
                pref.equals("backgroundDownloadRequireCharging")) {
            Context context = PreferencesActivity.this.getApplicationContext();
            BackgroundDownloadService.updateJob(context);
        }
    }
}
