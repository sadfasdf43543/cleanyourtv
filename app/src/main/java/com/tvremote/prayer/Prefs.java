package com.tvremote.prayer;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("prayer", Context.MODE_PRIVATE);
    }

    public static int city(Context c) {
        int v = sp(c).getInt("city", 0);
        if (v < 0 || v >= Cities.NAMES.length) v = 0;
        return v;
    }
    public static void setCity(Context c, int v) { sp(c).edit().putInt("city", v).apply(); }

    public static String lang(Context c) { return sp(c).getString("lang", "ar"); }
    public static void setLang(Context c, String v) { sp(c).edit().putString("lang", v).apply(); }

    public static int offset(Context c, int prayer) { return sp(c).getInt("off" + prayer, 0); }
    public static void setOffset(Context c, int prayer, int v) { sp(c).edit().putInt("off" + prayer, v).apply(); }

    public static boolean adhan(Context c, int prayer) {
        return sp(c).getBoolean("ad" + prayer, prayer != PrayerCalc.SUNRISE);
    }
    public static void setAdhan(Context c, int prayer, boolean v) { sp(c).edit().putBoolean("ad" + prayer, v).apply(); }
}
