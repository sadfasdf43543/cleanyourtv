package com.tvremote.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Scheduler.scheduleNext(ctx, System.currentTimeMillis());
    }
}
