package com.tvremote.prayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.time.ZonedDateTime;

public class Scheduler {
    /** Sets one exact alarm for the next prayer that has the adhan switched on. */
    public static void scheduleNext(Context c, long afterMillis) {
        Context app = c.getApplicationContext();
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        ZonedDateTime now = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(afterMillis), Cities.ZONE);
        long best = -1;
        int bestIdx = -1;
        for (int dayOffset = 0; dayOffset <= 2 && best < 0; dayOffset++) {
            ZonedDateTime[] d = PrayerData.day(app, now.toLocalDate().plusDays(dayOffset));
            for (int i = 0; i < 6; i++) {
                if (!Prefs.adhan(app, i)) continue;
                if (d[i].isAfter(now)) {
                    best = d[i].toInstant().toEpochMilli();
                    bestIdx = i;
                    break;
                }
            }
        }

        Intent in = new Intent(app, AdhanReceiver.class);
        in.putExtra("idx", bestIdx);
        in.putExtra("when", best);
        PendingIntent pi = PendingIntent.getBroadcast(app, 1001, in, PendingIntent.FLAG_UPDATE_CURRENT);
        if (best < 0) {
            am.cancel(pi);
            return;
        }
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, best, pi);
    }
}
