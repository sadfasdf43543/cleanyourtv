package com.tvremote.prayer;

import android.content.Context;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class PrayerData {
    public static class Pos {
        public int index;
        public ZonedDateTime next;
        public ZonedDateTime prev;
    }

    public static ZonedDateTime[] day(Context c, LocalDate d) {
        int city = Prefs.city(c);
        int[] off = new int[6];
        for (int i = 0; i < 6; i++) off[i] = Prefs.offset(c, i);
        LocalTime[] lt = PrayerCalc.compute(d, Cities.LAT[city], Cities.LNG[city], Cities.ZONE, off);
        ZonedDateTime[] z = new ZonedDateTime[6];
        for (int i = 0; i < 6; i++) z[i] = ZonedDateTime.of(d, lt[i], Cities.ZONE);
        return z;
    }

    /** The next prayer time after "now" (the first one of tomorrow when all of today's have passed). */
    public static Pos position(Context c, ZonedDateTime now) {
        LocalDate d = now.toLocalDate();
        ZonedDateTime[] today = day(c, d);
        Pos p = new Pos();
        for (int i = 0; i < 6; i++) {
            if (today[i].isAfter(now)) {
                p.index = i;
                p.next = today[i];
                p.prev = (i > 0) ? today[i - 1] : day(c, d.minusDays(1))[5];
                return p;
            }
        }
        ZonedDateTime[] tomorrow = day(c, d.plusDays(1));
        p.index = 0;
        p.next = tomorrow[0];
        p.prev = today[5];
        return p;
    }
}
