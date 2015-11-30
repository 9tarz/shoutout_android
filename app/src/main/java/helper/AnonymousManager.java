package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class AnonymousManager {
    // LogCat tag
    private static String TAG = AnonymousManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "ShoutOutAnonymous";

    private static final String KEY_ANONYMOUS_DEFAULT = "user_anonymous_default";

    public AnonymousManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }

    public void setDefault(int user_anonymous_default) {
        Log.d(TAG, "setDefault!");
        Log.d(TAG, "is anonymous : " + Integer.toString(user_anonymous_default));
        editor.putInt(KEY_ANONYMOUS_DEFAULT, user_anonymous_default);

        // commit changes
        editor.commit();

        Log.d(TAG, "User anonymous default modified!");
    }

    public int getDefault(){
        return pref.getInt(KEY_ANONYMOUS_DEFAULT, 1);
    }
}
