package bitshift.wordpad;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;

/**
 * Created by Bronson on 14/11/13.
 */
public class SettingsMgr implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private ArrayList<SettingsMgrListener> mListeners = new ArrayList<SettingsMgrListener> ();

    static private SettingsMgr mSingleInstance;
    static private SharedPreferences mPreferences;
    final private DisplayMetrics mDisplayMetrics;
    final static int RESET_CODE = -1; // less or equal to this and it wipes the settings

    SettingsMgr(Context context)
    {
        mSingleInstance = this;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mDisplayMetrics = context.getResources().getDisplayMetrics();

        // version check
        int lastVersion = getInt("app_version_code", 0);
        if (lastVersion <= RESET_CODE)
        {
            clearAll();
            Log.i("wordpad","old version, resetting preferences");
        }

        try
        {
            int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            putInt("app_version_code", version);
        } catch (PackageManager.NameNotFoundException e) {e.printStackTrace();}


    }

    static SettingsMgr instance()
    {
        return mSingleInstance;
    }

    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface SettingsMgrListener
    {
        void onSettingsChanged(SharedPreferences sharedPreferences, String key);
    }

    public void addSettingsChangeListener(SettingsMgrListener listener)
    {
        mListeners.add(listener);
    }

    public void removeSettingsChangeListener(SettingsMgrListener listener)
    {
        mListeners.remove(listener);
    }

    // shared pref listener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        // notify listeners
        for (SettingsMgrListener listener : mListeners)
            listener.onSettingsChanged(sharedPreferences, key);
    }

    DisplayMetrics displayMetrics()
    {
        return mDisplayMetrics;
    }

    public void clearAll()
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.clear();
        editor.commit();
    }

    // gets the int & checks its ok :) else return and set default value :D
    int parseInt(String key, int defaultValue, int min, int max)
    {
        int value = min - 1;
        try
        {
            value = Integer.parseInt(getString(key, Integer.toString(defaultValue)));
        }
        catch(Exception e) {e.printStackTrace();}

        if (value > max || value < min)
        {
            putString(key, Integer.toString(defaultValue));
            value = defaultValue;
        }

        return value;
    }

    void putBoolean(String key, boolean val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    boolean getBoolean(String key, boolean def)
    {
        return preferences().getBoolean(key, def);
    }

    void putString(String key, String val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putString(key, val);
        editor.commit();
    }

    String getString(String key, String def)
    {
        return preferences().getString(key, def);
    }

    void putInt(String key, int val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putInt(key, val);
        editor.commit();
    }

    int getInt(String key, int def)
    {
        return preferences().getInt(key, def);
    }

    void putFloat(String key, float val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putFloat(key, val);
        editor.commit();
    }

    float getFloat(String key, float def)
    {
        return preferences().getFloat(key, def);
    }

    void putLong(String key, long val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putLong(key, val);
        editor.commit();
    }

    long getLong(String key, long def)
    {
        return preferences().getLong(key, def);
    }

    public SharedPreferences preferences()
    {
        return mPreferences;
    }

    float dpToPixels(float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics());
    }

    // GRENADE LAUNCHER SPECIFIC

    int fontSize()
    {
        return parseInt("key_font_size", 18, 0, 100);
    }

    int fontColor()
    {
        int fontColor = Integer.parseInt(preferences().getString("key_font_color", "0"));
        int color;

        switch (fontColor)
        {
            case 0:
                color = Color.WHITE;
                break;

            case 1:
                color = Color.BLACK;
                break;

            case 2:
                color = Color.BLUE;
                break;

            case 3:
                color = Color.CYAN;
                break;

            case 4:
                color = Color.DKGRAY;
                break;

            case 5:
                color = Color.GRAY;
                break;

            case 6:
                color = Color.GREEN;
                break;

            case 7:
                color = Color.LTGRAY;
                break;

            case 8:
                color = Color.MAGENTA;
                break;

            case 9:
                color = Color.RED;
                break;

            case 10:
                color = Color.YELLOW;
                break;

            default:
                color = Color.WHITE; //Color.parseColor(fontColor);
                break;
        }

        return color;
    }

    Typeface fontTypeface()
    {
        int face = Integer.parseInt(preferences().getString("key_font_typeface", "0"));
        Typeface fontface = Typeface.create(Typeface.DEFAULT, 0);
        switch (face)
        {
            case 0:
                fontface = Typeface.create(Typeface.DEFAULT, 0);
                break;

            case 1:
                fontface = Typeface.create(Typeface.SANS_SERIF, 0);
                break;

            case 2:
                fontface = Typeface.create(Typeface.SERIF, 0);
                break;

            case 3:
                fontface = Typeface.create(Typeface.MONOSPACE, 0);
                break;
        }

        return fontface;
    }

    int lineColor()
    {
        int lineColor = Integer.parseInt(preferences().getString("key_line_color", "2"));
        int color;

        switch (lineColor)
        {
            case 0:
                color = Color.WHITE;
                break;

            case 1:
                color = Color.BLACK;
                break;

            case 2:
                color = Color.BLUE;
                break;

            case 3:
                color = Color.CYAN;
                break;

            case 4:
                color = Color.DKGRAY;
                break;

            case 5:
                color = Color.GRAY;
                break;

            case 6:
                color = Color.GREEN;
                break;

            case 7:
                color = Color.LTGRAY;
                break;

            case 8:
                color = Color.MAGENTA;
                break;

            case 9:
                color = Color.RED;
                break;

            case 10:
                color = Color.YELLOW;
                break;

            default:
                color = Color.WHITE; //Color.parseColor(fontColor);
                break;
        }

        return color;
    }

    int backgroundColor()
    {
        int backgroundColor = Integer.parseInt(preferences().getString("key_background_color", "1"));
        int color;

        switch (backgroundColor)
        {
            case 0:
                color = Color.WHITE;
                break;

            case 1:
                color = Color.BLACK;
                break;

            case 2:
                color = Color.BLUE;
                break;

            case 3:
                color = Color.CYAN;
                break;

            case 4:
                color = Color.DKGRAY;
                break;

            case 5:
                color = Color.GRAY;
                break;

            case 6:
                color = Color.GREEN;
                break;

            case 7:
                color = Color.LTGRAY;
                break;

            case 8:
                color = Color.MAGENTA;
                break;

            case 9:
                color = Color.RED;
                break;

            case 10:
                color = Color.YELLOW;
                break;

            default:
                color = Color.WHITE; //Color.parseColor(fontColor);
                break;
        }

        return color;
    }


    boolean wordWrap()
    {
        return preferences().getBoolean("key_word_wrap", true);
    }

    boolean checkSpelling()
    {
        return preferences().getBoolean("key_check_spelling", true);
    }

    boolean lineNumbers()
    {
        return preferences().getBoolean("key_line_numbers", false);
    }

    boolean screenAwake()
    {
        return preferences().getBoolean("key_screen_awake", false);
    }

    String drawerButtonVisibility() { return preferences().getString("key_drawer_button_visibility", "left|right"); }


    String documentFolder()
    {
        String folder = preferences().getString("key_document_folder", "");
        if (!folder.startsWith("/"))
        {
            folder = Environment.getExternalStorageDirectory().toString();
            putString("key_document_folder", folder);
        }
        return folder;
    }

    boolean autoSave()
    {
        return preferences().getBoolean("key_autosave", false);
    }

    boolean openLastDocument()
    {
        return preferences().getBoolean("key_open_last_document", true);
    }

    boolean clearHistory()
    {
        return preferences().getBoolean("key_clear_history", false);
    }

    boolean readOnly()
    {
        return preferences().getBoolean("key_read_only", false);
    }

    String charSet()
    {
        return preferences().getString("key_charset", "UTF-8");
    }

    boolean resumeState()
    {
        return getBoolean("doc_state_enabled", true);
    }

    public void setResumeState(boolean state)
    {
        putBoolean("doc_state_enabled", state);
    }

}
