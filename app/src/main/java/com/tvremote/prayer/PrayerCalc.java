package com.tvremote.prayer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.LocalDateTime;

/**
 * Prayer times with the Turkish Diyanet approximation:
 * Fajr 18 deg, Isha 17 deg, Asr (standard shadow factor 1),
 * method adjustments in minutes: sunrise -7, dhuhr +5, asr +4, maghrib +7.
 * Algorithm: classic PrayTimes (solar position + hour angles).
 */
public class PrayerCalc {
    public static final int FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHA = 5;

    private static final double FAJR_ANGLE = 18.0;
    private static final double ISHA_ANGLE = 17.0;
    private static final double RISE_SET = 0.833;
    private static final int[] METHOD_ADJ = {0, -7, 5, 4, 7, 0};

    private static double rad(double d) { return Math.toRadians(d); }
    private static double deg(double r) { return Math.toDegrees(r); }
    private static double sin(double d) { return Math.sin(rad(d)); }
    private static double cos(double d) { return Math.cos(rad(d)); }
    private static double tan(double d) { return Math.tan(rad(d)); }
    private static double arcsin(double x) { return deg(Math.asin(x)); }
    private static double arccos(double x) { return deg(Math.acos(x)); }
    private static double arccot(double x) { return deg(Math.atan(1.0 / x)); }
    private static double arctan2(double y, double x) { return deg(Math.atan2(y, x)); }

    private static double fixAngle(double a) { return a - 360.0 * Math.floor(a / 360.0); }
    private static double fixHour(double a) { return a - 24.0 * Math.floor(a / 24.0); }

    private static double julian(int year, int month, int day) {
        if (month <= 2) { year -= 1; month += 12; }
        double a = Math.floor(year / 100.0);
        double b = 2 - a + Math.floor(a / 4.0);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5;
    }

    // returns {declination, equation of time (hours)}
    private static double[] sunPosition(double jd) {
        double d = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * d);
        double q = fixAngle(280.459 + 0.98564736 * d);
        double l = fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = arctan2(cos(e) * sin(l), cos(l)) / 15.0;
        double eqt = q / 15.0 - fixHour(ra);
        double decl = arcsin(sin(e) * sin(l));
        return new double[]{decl, eqt};
    }

    private static double midDay(double jd, double time) {
        double eqt = sunPosition(jd + time)[1];
        return fixHour(12 - eqt);
    }

    private static double sunAngleTime(double jd, double lat, double angle, double time, boolean ccw) {
        double decl = sunPosition(jd + time)[0];
        double noon = midDay(jd, time);
        double t = (1.0 / 15.0) * arccos((-sin(angle) - sin(decl) * sin(lat)) / (cos(decl) * cos(lat)));
        return noon + (ccw ? -t : t);
    }

    private static double asrTime(double jd, double lat, double factor, double time) {
        double decl = sunPosition(jd + time)[0];
        double angle = -arccot(factor + tan(Math.abs(lat - decl)));
        return sunAngleTime(jd, lat, angle, time, false);
    }

    /** Local clock times (hours, decimal) for the 6 times, before rounding. */
    public static double[] hours(int year, int month, int day, double lat, double lng, double tzHours) {
        double jd = julian(year, month, day) - lng / (15.0 * 24.0);
        double[] t = {5, 6, 12, 13, 18, 18};
        double[] r = new double[6];
        for (int iter = 0; iter < 2; iter++) {
            double fajr = sunAngleTime(jd, lat, FAJR_ANGLE, t[0] / 24.0, true);
            double sunrise = sunAngleTime(jd, lat, RISE_SET, t[1] / 24.0, true);
            double dhuhr = midDay(jd, t[2] / 24.0);
            double asr = asrTime(jd, lat, 1.0, t[3] / 24.0);
            double maghrib = sunAngleTime(jd, lat, RISE_SET, t[4] / 24.0, false);
            double isha = sunAngleTime(jd, lat, ISHA_ANGLE, t[5] / 24.0, false);
            r = new double[]{fajr, sunrise, dhuhr, asr, maghrib, isha};
            t = r.clone();
        }
        double shift = tzHours - lng / 15.0;
        for (int i = 0; i < 6; i++) r[i] += shift;
        return r;
    }

    /** The 6 prayer times of one day, rounded to the minute, with Diyanet and user adjustments. */
    public static LocalTime[] compute(LocalDate date, double lat, double lng, ZoneId zone, int[] userOffsetMinutes) {
        double tz = zone.getRules().getOffset(LocalDateTime.of(date, LocalTime.NOON)).getTotalSeconds() / 3600.0;
        double[] h = hours(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), lat, lng, tz);
        LocalTime[] out = new LocalTime[6];
        for (int i = 0; i < 6; i++) {
            int user = (userOffsetMinutes != null && i < userOffsetMinutes.length) ? userOffsetMinutes[i] : 0;
            long minutes = Math.round(h[i] * 60.0) + METHOD_ADJ[i] + user;
            minutes = ((minutes % 1440) + 1440) % 1440;
            out[i] = LocalTime.of((int) (minutes / 60), (int) (minutes % 60));
        }
        return out;
    }
}
