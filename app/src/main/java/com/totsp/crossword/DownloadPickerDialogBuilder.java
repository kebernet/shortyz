package com.totsp.crossword;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.totsp.crossword.BrowseActivity.Provider;
import com.totsp.crossword.net.Downloader;
import com.totsp.crossword.net.Downloaders;
import com.totsp.crossword.net.DummyDownloader;
import com.totsp.crossword.shortyz.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;


/**
 * Custom dialog for choosing puzzles to download.
 */
public class DownloadPickerDialogBuilder {
    private static final Logger LOGGER = Logger.getLogger(DownloadPickerDialogBuilder.class.getCanonicalName());
    private Activity mActivity;
    private Dialog mDialog;
    private List<Downloader> mAvailableDownloaders;
    private OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                LOGGER.info("OnDateChanged " + year + " " + monthOfYear + " " + dayOfMonth);
                downloadDate.set(year, monthOfYear, dayOfMonth);
                updateDayOfWeek();
                updatePuzzleSelect();
            }
        };

    private Provider<Downloaders> mDownloaders;
    private Spinner mPuzzleSelect;
    private Calendar downloadDate;
    private int selectedItemPosition = 0;
    private final TextView dayOfWeek;

    public DownloadPickerDialogBuilder(Activity a, final OnDownloadSelectedListener downloadButtonListener, int year,
        int monthOfYear, int dayOfMonth, Provider<Downloaders> provider) {
        mActivity = a;

        downloadDate = Calendar.getInstance();
        downloadDate.set(year, monthOfYear,  dayOfMonth);

        mDownloaders = provider;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.download_dialog, (ViewGroup) mActivity.findViewById(R.id.download_root));


        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        dayOfWeek = (TextView) layout.findViewById(R.id.dayOfWeek);
        updateDayOfWeek();

        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);

        mPuzzleSelect = (Spinner) layout.findViewById(R.id.puzzleSelect);
        mPuzzleSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               selectedItemPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedItemPosition = 0;
            }
        });
        updatePuzzleSelect();

        OnClickListener clickHandler = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dateChangedListener.onDateChanged(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    downloadButtonListener.onDownloadSelected(getCurrentDate(), mAvailableDownloaders,
                           selectedItemPosition);
                }
            };

        layout.findViewById(R.id.browse).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.setClass(mActivity, WebBrowserActivity.class);
                    mActivity.startActivityForResult(i, 0);
                }
            });

        AlertDialog.Builder builder = (new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.Base_Theme_AppCompat_Light_Dialog))).setPositiveButton("Download", clickHandler)
                                       .setNegativeButton("Cancel", null);

        builder.setView(layout);
        mDialog = builder.create();
        mDialog.setOnShowListener(new OnShowListener() {
                public void onShow(DialogInterface arg0) {
                    updatePuzzleSelect();
                }
            });
    }

    private void updateDayOfWeek() {
        if (dayOfWeek == null) return;

        String dayName = downloadDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        dayOfWeek.setText(dayName);
    }

    public Dialog getInstance() {
        return mDialog;
    }

	private Date getCurrentDate() {
        return downloadDate.getTime();
    }


    private void updatePuzzleSelect() {
        mAvailableDownloaders = mDownloaders.get()
                                            .getDownloaders(getCurrentDate());
        mAvailableDownloaders.add(0, new DummyDownloader());

        ArrayAdapter<Downloader> adapter = new ArrayAdapter<Downloader>(mActivity,
                android.R.layout.simple_spinner_item, mAvailableDownloaders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPuzzleSelect.setAdapter(adapter);
    }

    public interface OnDownloadSelectedListener {
        void onDownloadSelected(Date date, List<Downloader> availableDownloaders, int selected);
    }
}
