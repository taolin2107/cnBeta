package app.taolin.cnbeta.utils;

import android.preference.PreferenceManager;

import app.taolin.cnbeta.App;

/**
 * @author taolin
 * @version v1.0
 * @date Jun 28, 2016.
 * @description
 */

public class SharedPreferenceUtil {

    public static final String KEY_FONT_SIZE = "font_size";

    public static void write(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().putInt(key, value).apply();
    }

    public static int read(String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(key, defValue);
    }

    public static void write(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().putString(key, value).apply();
    }

    public static String read(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(key, defValue);
    }
}
