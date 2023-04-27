package helper;

import org.json.JSONArray;
import org.json.JSONObject;

public class RespondHelper {

    public static boolean isValidRespond(String respond) throws Exception {

        try {
            JSONObject jo = new JSONObject(respond);

            if (jo.getString("status").equalsIgnoreCase("valid")) {
                return true;
            } else if(jo.getString("status").equalsIgnoreCase("ok")) {
                return true;
            } else if(jo.getString("status").equalsIgnoreCase("OK")) {
                return true;
            }

        } catch (Exception ex) {

        }

        return false;
    }

    public static JSONObject getObject(String respond, String key) throws Exception {

        JSONObject jo = new JSONObject(respond);
        return jo.getJSONObject(key);

    }

    public static JSONArray getArray(String respond, String key) throws Exception {

        JSONObject jo = new JSONObject(respond);
        return jo.getJSONArray(key);

    }

    public static String getValue(String respond, String key) throws Exception {

        String val = null;
        JSONObject jo = new JSONObject(respond);

        val = jo.getString(key);

        return val;

    }

}
