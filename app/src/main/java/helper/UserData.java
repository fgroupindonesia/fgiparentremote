package helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

public class UserData {

    public static String CurrentVoucherCode = null;
    public static String BroadCastTag = "shared";
    public static int MINUTES_CURRENT = 0;

    public static String HOUR_MINUTE_SECOND_SAVED = null;
    public static String HOUR_MINUTE_SECOND_ELAPSED_SAVED = null;

    private static Activity actTemp;
    private static SharedPreferences sharedPreference;

    public static void setPreference(Activity act) {
        sharedPreference = act.getApplicationContext().getSharedPreferences(BroadCastTag, Context.MODE_PRIVATE);
        actTemp = act;
    }

    public static void destroyAll(){
        File dir = new File(actTemp.getFilesDir().getParent() + "/shared_prefs/");
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            // clear each preference file
            actTemp.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
            //delete the file
            new File(dir, children[i]).delete();
        }

    }

    public static void savePreference(String keyName, String valHere) {

        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(keyName, valHere);
        editor.commit();

    }

    public static void savePreference(String keyName, int valHere) {

        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putInt(keyName, valHere);
        editor.commit();

    }

    public static void savePreference(String keyName, boolean valHere) {

        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(keyName, valHere);
        editor.commit();

    }

    public static String getPreferenceString(String keyName){
    	return sharedPreference.getString(keyName, null);
	}

    public static int getPreferenceInt(String keyName){
        return sharedPreference.getInt(keyName, -1);
    }

	public static boolean getPreferenceBoolean(String keyName){
    	return sharedPreference.getBoolean(keyName, false);
	}

}
