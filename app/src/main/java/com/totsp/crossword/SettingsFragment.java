package com.totsp.crossword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.totsp.crossword.firstrun.FirstrunActivity;
import com.totsp.crossword.gmail.GMConstants;
import com.totsp.crossword.service.BackgroundDownloadService;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.versions.AndroidVersionUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, PreferenceFragmentCompat.OnPreferenceStartScreenCallback  {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.background_light));
        return view;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen preferenceScreen) {
        try {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
            fragment.setArguments(args);
            ft.add(R.id.settings_container, fragment, preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return true;
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        super.onNavigateToScreen(preferenceScreen);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String pref) {
        if (pref.equals("backgroundDownload") ||
                pref.equals("backgroundDownloadRequireUnmetered") ||
                pref.equals("backgroundDownloadAllowRoaming") ||
                pref.equals("backgroundDownloadRequireCharging")) {
            Context context = getActivity().getApplicationContext();
            BackgroundDownloadService.updateJob(context);
        }
    }

    public void onResume() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
            if (!AndroidVersionUtils.Factory.getInstance().isBackgroundDownloadAvaliable()) {
                Preference backgroundDownload = findPreference("backgroundDownload");
                if(backgroundDownload != null) {
                    backgroundDownload.setSelectable(false);
                    backgroundDownload.setEnabled(false);
                    backgroundDownload.setSummary("Requires Android Lollipop or later");
                }
            }
            if(findPreference("releaseNotes") != null)
                findPreference("releaseNotes")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"),
                                    getActivity(), HTMLActivity.class);
                            getActivity().startActivity(i);

                            return true;
                        }
                    });


            if(findPreference("license") != null)
                findPreference("license")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/license.html"),
                                    getActivity(), HTMLActivity.class);
                            getActivity().startActivity(i);

                            return true;
                        }
                    });

            if(findPreference("nytSubscribe") != null)
                findPreference("nytSubscribe")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com/puzzle"));
                            getActivity().startActivity(i);

                            return true;
                        }
                    });

            if(findPreference("nytClear") != null)
                findPreference("nytClear")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                    .putBoolean("didNYTLogin", false)
                                    .apply();
                            Toast.makeText(getActivity(), "Cleared", Toast.LENGTH_LONG)
                                    .show();
                            return true;
                        }
                    });

            if(findPreference("aboutScrapes") != null)
                findPreference("aboutScrapes")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/scrapes.html"),
                                    getActivity(), HTMLActivity.class);
                            getActivity().startActivity(i);

                            return true;
                        }
                    });
            if(findPreference("firstRun") != null)
                findPreference("firstRun")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference arg0) {
                            Intent i = new Intent(Intent.ACTION_VIEW, null,
                                    getActivity(), FirstrunActivity.class);
                            getActivity().startActivity(i);

                            return true;
                        }
                    });
            if(findPreference("clearGmail") != null)
                findPreference("clearGmail")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            ShortyzApplication application = ((ShortyzApplication) getActivity().getApplication());
                            application.getSettings().edit()
                                    .putString(GMConstants.PREF_ACCOUNT_NAME, null)
                                    .apply();
                            application.updateCredential(application.getSettings());
                            return true;
                        }
                    });

    }
}
