package com.tvremote.prayer;

import android.content.Context;
import java.util.Locale;

public class Texts {
    /** Picks the text for the language chosen in the settings: "ar", "tr" or "en". */
    public static String pick(Context c, String en, String ar, String tr) {
        String l = Prefs.lang(c);
        if ("ar".equals(l)) return ar;
        if ("tr".equals(l)) return tr;
        return en;
    }

    public static boolean rtl(Context c) { return "ar".equals(Prefs.lang(c)); }

    public static Locale locale(Context c) {
        String l = Prefs.lang(c);
        if ("ar".equals(l)) return new Locale("ar");
        if ("tr".equals(l)) return new Locale("tr", "TR");
        return Locale.ENGLISH;
    }

    private static final String[] AR = {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"};
    private static final String[] TR = {"İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"};
    private static final String[] EN = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};

    public static String prayer(Context c, int i) {
        String l = Prefs.lang(c);
        if ("ar".equals(l)) return AR[i];
        if ("tr".equals(l)) return TR[i];
        return EN[i];
    }

    private static final String[] H_AR = {"محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};
    private static final String[] H_TR = {"Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"};
    private static final String[] H_EN = {"Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"};

    public static String hijriMonth(Context c, int month1to12) {
        int i = Math.max(1, Math.min(12, month1to12)) - 1;
        String l = Prefs.lang(c);
        if ("ar".equals(l)) return H_AR[i];
        if ("tr".equals(l)) return H_TR[i];
        return H_EN[i];
    }
}
