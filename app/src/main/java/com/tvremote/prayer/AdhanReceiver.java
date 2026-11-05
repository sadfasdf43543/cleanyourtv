package com.tvremote.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AdhanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        int idx = intent.getIntExtra("idx", -1);
        long when = intent.getLongExtra("when", 0);
        long now = System.currentTimeMillis();
        // do not play an old adhan late (for example after the TV was off)
        if (idx >= 0 && when > 0 && Math.abs(now - when) < 3 * 60 * 1000L) {
            Intent a = new Intent(ctx, AdhanActivity.class);
            a.putExtra("idx", idx);
            a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(a);
        }
        // schedule the next one, at least a minute after this one (so it cannot fire twice)
        Scheduler.scheduleNext(ctx, Math.max(now, when) + 60 * 1000L);
    }
}
