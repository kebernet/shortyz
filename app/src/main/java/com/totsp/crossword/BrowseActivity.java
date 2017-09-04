package com.totsp.crossword;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.totsp.crossword.firstrun.FirstrunActivity;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.net.Downloader;
import com.totsp.crossword.net.Downloaders;
import com.totsp.crossword.net.Scrapers;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.view.CircleProgressBar;
import com.totsp.crossword.view.recycler.RecyclerItemClickListener;
import com.totsp.crossword.view.recycler.RemovableRecyclerViewAdapter;
import com.totsp.crossword.view.recycler.SeparatedRecyclerViewAdapter;
import com.totsp.crossword.view.recycler.ShowHideOnScroll;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("SimpleDateFormat")
public class BrowseActivity extends ShortyzActivity implements RecyclerItemClickListener.OnItemClickListener{
    private static final String MENU_ARCHIVES = "Archives";
    private static final int DOWNLOAD_DIALOG_ID = 0;
    private static final int REQUEST_WRITE_STORAGE = 1002;
    private static final long DAY = 24L * 60L * 60L * 1000L;
    private static final Logger LOGGER = Logger.getLogger(BrowseActivity.class.getCanonicalName());
    private Accessor accessor = Accessor.DATE_DESC;
    private SeparatedRecyclerViewAdapter currentAdapter = null;
    private Dialog downloadDialog;
    private File archiveFolder = new File(Environment.getExternalStorageDirectory(), "crosswords/archive");
    private File contextFile;
    private File crosswordsFolder = new File(Environment.getExternalStorageDirectory(), "crosswords");
    private FileHandle lastOpenedHandle = null;
    private Handler handler = new Handler();
    private List<String> sourceList = new ArrayList<String>();
    private RecyclerView puzzleList;
    private ListView sources;
    private NotificationManager nm;
    private View lastOpenedView = null;
    private boolean viewArchive;
    private MenuItem gamesItem;
    private boolean signedIn;
    private boolean hasWritePermissions;
    private int playIcon = R.drawable.ic_play_games_badge_green;
    private FloatingActionButton download;
    private int highlightColor;
    private int normalColor;
    private HashSet<FileHandle> selected = new HashSet<>();
    private ActionMode actionMode;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //4
            actionMode = mode;
            MenuItem item = menu.add("Delete");
            item.setIcon(android.R.drawable.ic_menu_delete);
            utils.onActionBarWithText(item);
            item = menu.add(viewArchive ? "Un-archive" : "Archive");
            utils.onActionBarWithText(item);
            item.setIcon(R.drawable.ic_action_remove);
            download.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if(menuItem.getTitle().equals("Delete")){
                for(FileHandle handle : selected){
                    deleteFile(handle.file);
                }
                puzzleList.invalidate();
                actionMode.finish();
            } else if(menuItem.getTitle().equals("Archive")){
                for(FileHandle handle : selected){
                    moveTo(handle.file, archiveFolder);
                }
                puzzleList.invalidate();
                actionMode.finish();
            } else if(menuItem.getTitle().equals("Un-archive")){
                for(FileHandle handle : selected){
                    moveTo(handle.file, crosswordsFolder);
                }
                puzzleList.invalidate();
                actionMode.finish();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selected.clear();
            render();
            download.setVisibility(View.VISIBLE);
            actionMode = null;
        }
    };
    private int primaryTextColor;


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        File meta = new File(this.contextFile.getParent(),
                contextFile.getName().substring(0, contextFile.getName().lastIndexOf(".")) + ".shortyz");

        if (item.getTitle()
                    .equals("Delete")) {
            this.contextFile.delete();

            if (meta.exists()) {
                meta.delete();
            }

            render();

            return true;
        } else if (item.getTitle()
                           .equals("Archive")) {
            this.archiveFolder.mkdirs();
            moveTo(contextFile, archiveFolder);
            render();

            return true;
        } else if (item.getTitle()
                           .equals("Un-archive")) {
            moveTo(contextFile, crosswordsFolder);
            render();

            return true;
        } else if ("Mark as Updated".equals(item.getTitle())) {
            try {
                Puzzle p = IO.load(this.contextFile);
                p.setUpdatable(false);
                IO.save(p, this.contextFile);
                render();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        utils.onActionBarWithoutText(this.gamesItem = menu.add("Sign In")
                .setIcon(this.playIcon));
        if(utils.isNightModeAvailable()) {
            utils.onActionBarWithoutText(menu.add("Night Mode")
                    .setIcon(R.drawable.night_toggle));
        }
        SubMenu sortMenu = menu.addSubMenu("Sort")
                               .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        sortMenu.add("By Date (Descending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Date (Ascending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Source")
                .setIcon(android.R.drawable.ic_menu_upload);
        utils.onActionBarWithText(sortMenu);
        menu.add("Cleanup")
            .setIcon(android.R.drawable.ic_menu_manage);
        menu.add(MENU_ARCHIVES)
            .setIcon(android.R.drawable.ic_menu_view);
        menu.add("Help")
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings")
            .setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }

    public void onSignInSucceeded(){
        this.playIcon = R.drawable.ic_play_games_badge_white;
        if(this.gamesItem != null){
            this.gamesItem.setIcon( this.playIcon );
        }
        this.signedIn = true;
    }


    private void setListItemColor(View v, boolean selected){
        if(selected) {
            v.setBackgroundColor(highlightColor);
            ((TextView) v.findViewById(R.id.puzzle_name)).setTextColor(Color.WHITE);
        } else {
            v.setBackgroundColor(normalColor);
            ((TextView) v.findViewById(R.id.puzzle_name)).setTextColor(primaryTextColor);
        }
    }


    @SuppressWarnings("deprecation")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null || item.getTitle() == null){
            return false;
        }
        if(item.getTitle().equals("Night Mode")){
            this.utils.toggleNightMode(this);
        } else if(item.getTitle().equals("Sign In")){
            if(this.signedIn && !(this.mHelper == null || this.mHelper.getGamesClient() == null)){
                startActivityForResult(this.mHelper.getGamesClient().getAchievementsIntent(), 0);
            } else {
                Intent i = new Intent(this, GamesSignIn.class);
                this.startActivity(i);
                return true;
            }
    	} else if (item.getTitle()
                    .equals("Download")) {
        	showDialog(DOWNLOAD_DIALOG_ID);

            return true;
        } else if (item.getTitle()
                           .equals("Settings")) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle()
                           .equals("Crosswords") || item.getTitle()
                                                            .equals(MENU_ARCHIVES)) {
            this.viewArchive = !viewArchive;
            item.setTitle(viewArchive ? "Crosswords" : MENU_ARCHIVES); //menu item title
            this.setTitle(!viewArchive ? "Puzzles" : MENU_ARCHIVES); //activity title

            render();

            return true;
        } else if (item.getTitle()
                           .equals("Cleanup")) {
            this.cleanup();

            return true;
        } else if (item.getTitle()
                           .equals("Help")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/filescreen.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);
        } else if (item.getTitle()
                           .equals("By Source")) {
            this.accessor = Accessor.SOURCE;
            prefs.edit()
                 .putInt("sort", 2)
                 .apply();
            this.render();
        } else if (item.getTitle()
                           .equals("By Date (Ascending)")) {
            this.accessor = Accessor.DATE_ASC;
            prefs.edit()
                 .putInt("sort", 1)
                 .apply();
            this.render();
        } else if (item.getTitle()
                           .equals("By Date (Descending)")) {
            this.accessor = Accessor.DATE_DESC;
            prefs.edit()
                 .putInt("sort", 0)
                 .apply();
            this.render();
        } else if("Send Debug Package".equals(item.getTitle())){
        	Intent i = ShortyzApplication.sendDebug();
        	if(i != null)
        		this.startActivity(i);
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == RESULT_OK) && (downloadDialog != null) && downloadDialog.isShowing()) {
            // If the user hit close in the browser download activity, we close the dialog.
            downloadDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!((ShortyzApplication) getApplication()).getSettings().getBoolean("didFirstRun", false)){
            Intent i = new Intent(Intent.ACTION_VIEW, null, this, FirstrunActivity.class);
            this.startActivity(i);
            finish();
            return;
        }
        this.setTitle("Puzzles");
        //this.utils.hideTitleOnPortrait(this);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        this.setContentView(R.layout.browse);
        this.showMenuAlways();
        this.puzzleList = (RecyclerView) this.findViewById(R.id.puzzleList);
        this.puzzleList.setLayoutManager(new LinearLayoutManager(this));
        this.puzzleList.addOnItemTouchListener(new RecyclerItemClickListener(this, this.puzzleList, this));
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END,
                ItemTouchHelper.START | ItemTouchHelper.END) {

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof FileViewHolder) || prefs.getBoolean("disableSwipe", false)) {
                    return 0; // Don't swipe the headers.
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if(!selected.isEmpty()){
                    return;
                }
                if(!(viewHolder instanceof FileViewHolder)){
                    return;
                }
                FileHandle handle = (FileHandle) ((FileViewHolder) viewHolder).itemView.getTag();
                System.out.println(" SWIPED "+handle.file.getAbsolutePath());
                if("DELETE".equals(prefs.getString("swipeAction", "DELETE"))) {
                    deleteFile(handle.file);
                } else {
                    if (viewArchive) {
                        moveTo(handle.file, crosswordsFolder);
                    } else {
                        moveTo(handle.file, archiveFolder);
                    }
                }
                currentAdapter.onItemDismiss(viewHolder.getAdapterPosition());
                puzzleList.invalidate();
            }
        });
        helper.attachToRecyclerView(this.puzzleList);
        this.sources = (ListView) this.findViewById(R.id.sourceList);
        upgradePreferences();
        this.nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        switch (prefs.getInt("sort", 0)) {
        case 2:
            this.accessor = Accessor.SOURCE;

            break;

        case 1:
            this.accessor = Accessor.DATE_ASC;

            break;

        default:
            this.accessor = Accessor.DATE_DESC;
        }


        download = (FloatingActionButton) this.findViewById(R.id.button_floating_action);
        if(download != null) {
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BrowseActivity.this.showDialog(DOWNLOAD_DIALOG_ID);
                }
            });
            download.setImageBitmap(createBitmap("icons1.ttf", ","));
            this.puzzleList.setOnTouchListener(new ShowHideOnScroll(download));

        }

        highlightColor = ContextCompat.getColor(this, R.color.accent);
        normalColor = ContextCompat.getColor(this, R.color.background_light);
        primaryTextColor = ContextCompat.getColor(this, R.color.textColorPrimary);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Allow Permissions")
                        .setMessage("Please allow writing to storage when prompted. Shortyz needs this permission to store downloaded crossword files and cannot work without it.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(BrowseActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_WRITE_STORAGE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_WRITE_STORAGE);
            }

            return;
        } else {
            hasWritePermissions = true;
        }

        startInitialActivityOrFinishLoading();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasWritePermissions = true;
                    startInitialActivityOrFinishLoading();
                }
        }
    }

    private void startInitialActivityOrFinishLoading() {
        if (!crosswordsFolder.exists()) {
            this.downloadTen();

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/welcome.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);

            return;
        } else if (prefs.getBoolean("release_4.3.9", true)) {
            prefs.edit()
                    .putBoolean("release_4.3.9", false)
                    .apply();

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);

            return;
        }

        render();
        this.checkDownload();
        puzzleList.invalidate();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DOWNLOAD_DIALOG_ID:

            DownloadPickerDialogBuilder.OnDownloadSelectedListener downloadButtonListener = new DownloadPickerDialogBuilder.OnDownloadSelectedListener() {
                    public void onDownloadSelected(Date d, List<Downloader> downloaders, int selected) {
                        List<Downloader> toDownload = new LinkedList<Downloader>();
                        boolean scrape;
                        LOGGER.info("Downloaders: " + selected + " of " + downloaders);

                        if (selected == 0) {
                            // Download all available.
                            toDownload.addAll(downloaders);
                            toDownload.remove(0);
                            scrape = true;
                        } else {
                            // Only download selected.
                            toDownload.add(downloaders.get(selected));
                            scrape = false;
                        }

                        download(d, toDownload, scrape);
                    }
                };

            Date d = new Date();

            @SuppressWarnings("deprecation")
			DownloadPickerDialogBuilder dpd = new DownloadPickerDialogBuilder(this, downloadButtonListener,
                    d.getYear() + 1900, d.getMonth(), d.getDate(),
                  new Downloaders(prefs, nm, BrowseActivity.this)
            );

            downloadDialog = dpd.getInstance();

            return downloadDialog;
        }

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.currentAdapter == null) {
            this.render();
        } else {
            if (lastOpenedHandle != null) {
                try {
                    lastOpenedHandle.meta = IO.meta(lastOpenedHandle.file);

                    CircleProgressBar bar = (CircleProgressBar) lastOpenedView.findViewById(R.id.puzzle_progress);

                    if (lastOpenedHandle.meta.updatable) {
                        bar.setPercentComplete(-1);
                    } else {
                        bar.setPercentComplete(lastOpenedHandle.getProgress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



        // A background update will commonly happen when the user turns on the preference for the
        // first time, so check here to ensure the UI is re-rendered when they exit the settings
        // dialog.
        if (utils.checkBackgroundDownload(prefs, hasWritePermissions)) {
            render();
        }

        this.checkDownload();
    }

    private SeparatedRecyclerViewAdapter buildList(final Dialog dialog, File directory, Accessor accessor) {
        directory.mkdirs();

        long incept = System.currentTimeMillis();
        ArrayList<FileHandle> files = new ArrayList<FileHandle>();
        FileHandle[] puzFiles = null;

        if (!directory.exists()) {
            showSDCardHelp();
            return new SeparatedRecyclerViewAdapter(R.layout.puzzle_list_header);
        }
        

        String sourceMatch = null;

        if (this.sources != null) {
            sourceMatch = ((SourceListAdapter) sources.getAdapter()).current;

            if (SourceListAdapter.ALL_SOURCES.equals(sourceMatch)) {
                sourceMatch = null;
            }
        }

        HashSet<String> sourcesTemp = new HashSet<String>();

        for (File f : directory.listFiles()) {
            // if this is taking a while and we are off the EDT, pop up the dialog.
            if ((dialog != null) && ((System.currentTimeMillis() - incept) > 2000) && !dialog.isShowing()) {
                handler.post(new Runnable() {
                        public void run() {
                            try {
                                dialog.show();
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }

            if (f.getName()
                     .endsWith(".puz")) {
                PuzzleMeta m = null;

                try {
                    m = IO.meta(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileHandle h = new FileHandle(f, m);
                sourcesTemp.add(h.getSource());

                if ((sourceMatch == null) || sourceMatch.equals(h.getSource())) {
                    files.add(h);
                }
            }
        }

        puzFiles = files.toArray(new FileHandle[files.size()]);

        try {
            Arrays.sort(puzFiles, accessor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SeparatedRecyclerViewAdapter adapter = new SeparatedRecyclerViewAdapter(R.layout.puzzle_list_header);
        String lastHeader = null;
        ArrayList<FileHandle> current = new ArrayList<FileHandle>();

        for (FileHandle handle : puzFiles) {
            String check = accessor.getLabel(handle);

            if (!((lastHeader == null) || lastHeader.equals(check))) {
                FileAdapter fa = new FileAdapter(current);
                adapter.addSection(lastHeader, fa);
                current = new ArrayList<FileHandle>();
            }

            lastHeader = check;
            current.add(handle);
        }

        if (lastHeader != null) {
            FileAdapter fa = new FileAdapter(current);
            adapter.addSection(lastHeader, fa);
            current = new ArrayList<FileHandle>();
        }

        if (this.sources != null) {
            this.sourceList.clear();
            this.sourceList.addAll(sourcesTemp);
            Collections.sort(this.sourceList);
            this.handler.post(new Runnable(){
            	public void run(){
            		((SourceListAdapter) sources.getAdapter()).notifyDataSetInvalidated();
            	}
            });
        }

        return adapter;
    }

    private void checkDownload() {
        if (!hasWritePermissions) return;

        long lastDL = prefs.getLong("dlLast", 0);

        if (prefs.getBoolean("dlOnStartup", true) &&
                ((System.currentTimeMillis() - (long) (12 * 60 * 60 * 1000)) > lastDL)) {
            this.download(new Date(), null, true);
            prefs.edit()
                    .putLong("dlLast", System.currentTimeMillis())
                    .apply();
        }
    }

    private FileHandle[] getFileHandlesFromDirectory(File directory) {
        ArrayList<FileHandle> files = new ArrayList<>();
        FileHandle[] puzFiles;
        try {
            for (File f : directory.listFiles()) {
                if (f.getName()
                        .endsWith(".puz")) {
                    PuzzleMeta m = null;

                    try {
                        m = IO.meta(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    files.add(new FileHandle(f, m));
                }
            }
        } catch(Exception e){
            LOGGER.log(Level.WARNING, "error listing files!");
        }

        puzFiles = files.toArray(new FileHandle[files.size()]);
        Arrays.sort(puzFiles);
        files.clear();

        return puzFiles;
    }

    private long getMaxAgeMs(String preferenceValue) {
        long cleanupValue = Long.parseLong(preferenceValue) + 1;
        long maxAge = (cleanupValue == 0) ? 0 : (System.currentTimeMillis() - (cleanupValue * 24 * 60 * 60 * 1000));
        return maxAge;
    }

    private void cleanup() {
        boolean deleteOnCleanup = prefs.getBoolean("deleteOnCleanup", false);
        long maxAge = getMaxAgeMs(prefs.getString("cleanupAge", "2"));
        long archiveMaxAge = getMaxAgeMs(prefs.getString("archiveCleanupAge", "-1"));

        ArrayList<FileHandle> toArchive = new ArrayList<FileHandle>();
        ArrayList<FileHandle> toDelete = new ArrayList<FileHandle>();

        for (FileHandle h : getFileHandlesFromDirectory(this.crosswordsFolder)) {
            if ((h.getProgress() == 100) || (h.getDate()
                                                  .getTime() < maxAge)) {
                if (deleteOnCleanup) {
                    toDelete.add(h);
                } else {
                    toArchive.add(h);
                }
            }
        }

        if (archiveMaxAge > 0) {
            for (FileHandle h : getFileHandlesFromDirectory(this.archiveFolder)) {
                if (h.getDate().getTime() < archiveMaxAge) {
                    toDelete.add(h);
                }
            }
        }

        for (FileHandle h : toDelete) {
            File meta = new File(
                    h.file.getPath().substring(0, h.file.getPath().lastIndexOf(".")) + ".shortyz");
            h.file.delete();
            meta.delete();
        }

        for (FileHandle h : toArchive) {
            moveTo(h.file, this.archiveFolder);
        }

        render();
    }

    private void deleteFile(File puzFile){
        File meta = new File(puzFile.getParentFile(), puzFile.getName().substring(0, puzFile.getName().lastIndexOf(".")) + ".shortyz");
        puzFile.delete();
        meta.delete();
    }

    private void moveTo(File puzFile, File directory){
        File meta = new File(puzFile.getParentFile(), puzFile.getName().substring(0, puzFile.getName().lastIndexOf(".")) + ".shortyz");
        puzFile.renameTo(new File(directory, puzFile.getName()));
        meta.renameTo(new File(directory, meta.getName()));
    }

    private void download(final Date d, final List<Downloader> downloaders, final boolean scrape) {
        if (!hasWritePermissions) return;

        final Downloaders dls = new Downloaders(prefs, nm, this);
        LOGGER.info("Downloading from "+downloaders);
        new Thread(new Runnable() {
                public void run() {
                    dls.download(d, downloaders);

                    if (scrape) {
                        Scrapers scrapes = new Scrapers(prefs, nm, BrowseActivity.this);
                        scrapes.scrape();
                    }

                    handler.post(new Runnable() {
                            public void run() {
                                BrowseActivity.this.render();
                            }
                        });
                }
            }).start();
    }

    private void downloadTen() {
        if (!hasWritePermissions) return;

        new Thread(new Runnable() {
                public void run() {
                    Downloaders dls = new Downloaders(prefs, nm, BrowseActivity.this);
                    dls.supressMessages(true);
                    
                    Scrapers scrapes = new Scrapers(prefs, nm, BrowseActivity.this);
                    scrapes.supressMessages(true);
                    scrapes.scrape();

                    Date d = new Date();

                    for (int i = 0; i < 5; i++) {
                        d = new Date(d.getTime() - DAY);
                        dls.download(d);
                        handler.post(new Runnable() {
                                public void run() {
                                    BrowseActivity.this.render();
                                }
                            });
                    }
                }
            }).start();
    }

    private void render() {
        if (!hasWritePermissions) return;

        utils.clearBackgroundDownload(prefs);

        if ((this.sources != null) && (this.sources.getAdapter() == null)) {
            final SourceListAdapter adapter = new SourceListAdapter(this, this.sourceList);
            this.sources.setAdapter(adapter);
            this.sources.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View view, int arg2, long arg3) {
                        String selected = (String) view.getTag();
                        adapter.current = selected;
                        adapter.notifyDataSetInvalidated();
                        render();
                    }
                });
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

        final File directory = viewArchive ? BrowseActivity.this.archiveFolder : BrowseActivity.this.crosswordsFolder;
        directory.mkdirs();
        //Only spawn a thread if there are a lot of puzzles.
        // Using SDK rev as a proxy to decide whether you have a slow processor or not.

        if (((android.os.Build.VERSION.SDK_INT >= 5) && directory.exists() && (directory.list().length > 500)) ||
                ((android.os.Build.VERSION.SDK_INT < 5) && directory.exists() && (directory.list().length > 160))) {
            Runnable r = new Runnable() {
                    public void run() {
                        currentAdapter = BrowseActivity.this.buildList(dialog, directory, BrowseActivity.this.accessor);
                        BrowseActivity.this.handler.post(new Runnable() {
                                public void run() {
                                    BrowseActivity.this.puzzleList.setAdapter(currentAdapter);

                                    if (dialog.isShowing()) {
                                        dialog.hide();
                                    }
                                }
                            });
                    }
                };

            new Thread(r).start();
        } else {
            this.puzzleList.setAdapter(currentAdapter = this.buildList(null, directory, accessor));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void upgradePreferences() {
        if (this.prefs.getString("keyboardType", null) == null) {
            if (this.prefs.getBoolean("useNativeKeyboard", false)) {
                this.prefs.edit()
                          .putString("keyboardType", "NATIVE")
                          .apply();
            } else {
                Configuration config = getBaseContext()
                                           .getResources()
                                           .getConfiguration();

                if ((config.navigation == Configuration.NAVIGATION_NONAV) ||
                        (config.navigation == Configuration.NAVIGATION_UNDEFINED)) {
                    this.prefs.edit()
                              .putString("keyboardType", "CONDENSED_ARROWS")
                              .apply();
                } else {
                    this.prefs.edit()
                              .putString("keyboardType", "CONDENSED")
                              .apply();
                }
            }
        }
    }

    @Override
    public void onItemClick(final View v, int position) {
        if (!(v.getTag() instanceof FileHandle)) {
            return;
        }
        if (!selected.isEmpty()) {
            updateSelection(v);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lastOpenedView = v;
                    lastOpenedHandle = ((FileHandle) v.getTag());
                    if (lastOpenedHandle == null || lastOpenedHandle.file == null) {
                        return;
                    }
                    File puzFile = lastOpenedHandle.file;
                    Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), BrowseActivity.this, PlayActivity.class);
                    startActivity(i);
                }
            }, 450);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void onItemLongClick(View v, int position) {
        if (!(v.getTag() instanceof FileHandle)) {
            return;
        }
        if (actionMode == null) {
            getSupportActionBar().startActionMode(actionModeCallback);
        }
        updateSelection(v);
    }

    private void updateSelection(View v) {
        Object oTag = v.getTag();
        if(oTag == null || !(oTag instanceof FileHandle)){
            return;
        }
        FileHandle tag = (FileHandle) oTag;
        if (selected.contains(tag)) {
            setListItemColor(v, false);
            selected.remove(tag);
        } else {
            setListItemColor(v, true);
            selected.add(tag);
        }
        if (selected.isEmpty()) {
            actionMode.finish();
        }
    }

    public static interface Provider<T> {
        T get();
    }

    private static ArrayList<FileHandle> toArrayList(FileHandle[] o){
        ArrayList<FileHandle> result = new ArrayList<>();
        result.addAll(Arrays.asList(o));
        return result;
    }

    private class FileAdapter extends RemovableRecyclerViewAdapter<FileViewHolder> {
        final SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE\n MMM dd, yyyy");
        final ArrayList<FileHandle> objects;

        public FileAdapter(ArrayList<FileHandle> objects) {
            this.objects = objects;
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.puzzle_list_item, parent, false);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            View view = holder.itemView;
            FileHandle handle = objects.get(position);
            view.setTag(handle);

            TextView date = (TextView) view.findViewById(R.id.puzzle_date);

            date.setText(df.format(handle.getDate()));

            if (accessor == Accessor.SOURCE) {
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }

            TextView title = (TextView) view.findViewById(R.id.puzzle_name);

            title.setText(handle.getTitle());

            CircleProgressBar bar = (CircleProgressBar) view.findViewById(R.id.puzzle_progress);

            bar.setPercentComplete(handle.getProgress());

            TextView caption = (TextView) view.findViewById(R.id.puzzle_caption);

            caption.setText(handle.getCaption());

            setListItemColor(view, selected.contains(handle));
        }

        @Override
        public int getItemCount() {
            return objects.size();
        }

        @Override
        public void remove(int position) {
            objects.remove(position);
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {

        public FileViewHolder(View itemView) {
            super(itemView);
        }


    }
}
