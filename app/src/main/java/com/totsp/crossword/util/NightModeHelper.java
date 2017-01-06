package com.totsp.crossword.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Source: https://gist.github.com/slightfoot/c508cdc8828a478572e0
 * <p/>
 * Night Mode Helper
 * <p/>
 * <p>Helps use utilise the night and notnight resource qualifiers without
 * being in car or dock mode.
 * <p/>
 * <p>Implementation is simple. Add the follow line at the top of your
 * activity's onCreate just after the super.onCreate(); The idea here
 * is to do it before we create any views. So the new views will use
 * the correct Configuration.
 * <p/>
 * <pre>
 * NightModeHelper.bind(Activity activity);
 * </pre>
 * <p/>
 * You can now use your instance of NightModeHelper to control which mode
 * you are in. You can choose to persist the current setting and hand
 * it back to this class as the defaultUiMode, otherwise this is done
 * for you automatically.
 * <p/>
 * <p>I'd suggest you setup your Theme as follows:
 * <p/>
 * <ul>
 * <li>
 * <b>res\values\styles.xml</b>
 * <pre>&lt;style name=&quot;AppTheme&quot; parent=&quot;AppBaseTheme&quot;&gt;&lt;/style&gt;</pre>
 * </li>
 * <li>
 * <b>res\values-night\styles.xml</b>
 * <pre>&lt;style name=&quot;AppBaseTheme&quot; parent=&quot;@android:style/Theme.Holo&quot;&gt;&lt;/style&gt;</pre>
 * </li>
 * <li>
 * <b>res\values-notnight\styles.xml</b>
 * <pre>&lt;style name=&quot;AppBaseTheme&quot; parent=&quot;@android:style/Theme.Holo.Light&quot;&gt;&lt;/style&gt;</pre>
 * </li>
 * </ul>
 *
 * @author Simon Lightfoot <simon@demondevelopers.com>
 *
 * This code has been heavily modified from original source. The bind method will use reflection
 * to get the current theme of the activity and "re-set" it on the activity.
 *
 * There is now an "unbind" method that clears the reference within the instance. This should be
 * called in your Activity's {@link Activity#onDestroy()} method. WeakReference wrapper should
 * prevent a mem-leak, so unbind is a bit redundant, but just to be sure.
 */
public class NightModeHelper {

    private static int sUiNightMode = Configuration.UI_MODE_NIGHT_NO;

    private WeakReference<Activity> mActivity;

    public static NightModeHelper bind(Activity activity) {
        NightModeHelper helper = new NightModeHelper(activity);
        helper.updateConfig(sUiNightMode);

        // This may seem pointless but it forces the Theme to be reloaded
        // with new styles that would change due to new Configuration.
        int activityTheme = getTheme(activity);
        activity.setTheme(activityTheme);

        return helper;
    }

    /**
     * Default behaviour is to automatically save the setting and restore it.
     */
    private NightModeHelper(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public int getUiNightMode() {
        return getActivity().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void toggle() {
        if (isNightMode()) {
            notNight();
        } else {
            night();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public boolean isNightMode(){
        return getUiNightMode() == Configuration.UI_MODE_NIGHT_YES;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void notNight() {
        updateConfig(Configuration.UI_MODE_NIGHT_NO);
        getActivity().recreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void night() {
        updateConfig(Configuration.UI_MODE_NIGHT_YES);
        getActivity().recreate();
    }


    public void unbind() {
        mActivity.clear();
    }

    public void updateConfig(int uiNightMode) {
        Activity activity = getActivity();

        // Check if different mode
        int currentMode = (activity.getResources().getConfiguration()
                .uiMode & Configuration.UI_MODE_NIGHT_MASK);
        if (currentMode == uiNightMode) {
            return;
        }

        // Store ui-mode for recreate
        sUiNightMode = uiNightMode;

        // Update config
        Configuration newConfig = new Configuration(activity.getResources().getConfiguration());
        newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        newConfig.uiMode |= uiNightMode;
        activity.getResources().updateConfiguration(newConfig, null);
    }

    @NonNull
    private Activity getActivity() {
        Activity activity = mActivity.get();
        if (activity == null) {
            throw new IllegalStateException("No activity is currently bound.");
        }
        return activity;
    }

    /**
     * Helper method to get the theme resource id. Warning, accessing non-public methods is
     * a no-no and there is no guarantee this will work.
     *
     * @param context the context you want to extract the theme-resource-id from
     * @return The themeId associated w/ the context
     */
    @Deprecated
    private static int getTheme(Context context) {
        try {
            Class<?> wrapper = Context.class;
            Method method = wrapper.getMethod("getThemeResId");
            method.setAccessible(true);
            return (Integer) method.invoke(context);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Exception getting theme", e);
        }
        return 0;
    }
}
