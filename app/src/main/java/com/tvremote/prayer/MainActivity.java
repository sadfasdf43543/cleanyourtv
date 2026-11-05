package com.tvremote.prayer;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView clock, dateView, hijriView, cityView, nextName, countdown, quoteView;
    private View barFill;
    private final LinearLayout[] cards = new LinearLayout[6];
    private final TextView[] nameViews = new TextView[6];
    private final TextView[] timeViews = new TextView[6];
    private int highlighted = -1;
    private ObjectAnimator pulse;
    private String lang;
    private LocalDate cachedDay;
    private final List<String> quotes = new ArrayList<String>();
    private int quoteIdx = 0;
    private long lastQuoteAt = 0;
    private boolean introDone = false;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            try { update(); } catch (Exception e) { /* keep ticking */ }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang = Prefs.lang(this);
        setContentView(build());
        loadQuotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Prefs.lang(this).equals(lang)) { recreate(); return; }
        cachedDay = null;
        Scheduler.scheduleNext(this, System.currentTimeMillis());
        handler.removeCallbacks(tick);
        handler.post(tick);
        if (!introDone) { introDone = true; animateIn(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
        if (pulse != null) { pulse.cancel(); pulse = null; }
        highlighted = -1;
        for (int i = 0; i < 6; i++) { cards[i].setScaleX(1f); cards[i].setScaleY(1f); }
    }

    private View build() {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(Ui.background());
        root.setLayoutDirection(Texts.rtl(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = Ui.dp(this, 36);
        col.setPadding(pad, Ui.dp(this, 22), pad, Ui.dp(this, 20));
        root.addView(col, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        cityView = Ui.text(this, "", 20, Ui.SOFT, false);
        col.addView(cityView);

        clock = Ui.text(this, "--:--", 104, Ui.WHITE, true);
        clock.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        col.addView(clock);

        dateView = Ui.text(this, "", 24, Ui.WHITE, false);
        col.addView(dateView);
        hijriView = Ui.text(this, "", 22, Ui.GOLD, false);
        col.addView(hijriView);

        col.addView(spacer());

        TextView nextLabel = Ui.text(this, Texts.pick(this, "Next prayer", "الصلاة القادمة", "Sonraki vakit"), 22, Ui.SOFT, false);
        col.addView(nextLabel);
        nextName = Ui.text(this, "", 52, Ui.GOLD, true);
        col.addView(nextName);
        countdown = Ui.text(this, "--:--:--", 64, Ui.WHITE, true);
        col.addView(countdown);

        FrameLayout bar = new FrameLayout(this);
        bar.setBackground(Ui.shape(this, 0x33FFFFFF, 4));
        barFill = new View(this);
        barFill.setBackground(Ui.shape(this, Ui.GOLD, 4));
        barFill.setPivotX(0f);
        barFill.setScaleX(0f);
        bar.addView(barFill, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.dp(this, 520), Ui.dp(this, 7));
        blp.topMargin = Ui.dp(this, 10);
        col.addView(bar, blp);

        col.addView(spacer());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 6; i++) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setBackground(Ui.shape(this, 0x22FFFFFF, 18));
            int p = Ui.dp(this, 12);
            card.setPadding(p, p, p, p);
            TextView n = Ui.text(this, Texts.prayer(this, i), 22, Ui.SOFT, false);
            TextView t = Ui.text(this, "--:--", 40, Ui.WHITE, true);
            card.addView(n);
            card.addView(t);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
            row.addView(card, lp);
            cards[i] = card;
            nameViews[i] = n;
            timeViews[i] = t;
        }
        col.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        quoteView = Ui.text(this, "", 24, Ui.SOFT, false);
        quoteView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        quoteView.setMaxLines(2);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qlp.topMargin = Ui.dp(this, 16);
        col.addView(quoteView, qlp);

        TextView settings = Ui.text(this, Texts.pick(this, "Settings", "الإعدادات", "Ayarlar"), 20, Ui.WHITE, false);
        settings.setBackground(Ui.focusable(this));
        settings.setFocusable(true);
        settings.setClickable(true);
        settings.setPadding(Ui.dp(this, 28), Ui.dp(this, 10), Ui.dp(this, 28), Ui.dp(this, 10));
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, SettingsActivity.class)); }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = Ui.dp(this, 14);
        col.addView(settings, slp);

        return root;
    }

    private View spacer() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, 0, 1f));
        return v;
    }

    private void animateIn() {
        View[] first = {cityView, clock, dateView, hijriView, nextName, countdown};
        for (int i = 0; i < first.length; i++) {
            first[i].setAlpha(0f);
            first[i].animate().alpha(1f).setStartDelay(i * 70L).setDuration(500).start();
        }
        float rise = Ui.dp(this, 36);
        for (int i = 0; i < 6; i++) {
            cards[i].setAlpha(0f);
            cards[i].setTranslationY(rise);
            cards[i].animate().alpha(1f).translationY(0f).setStartDelay(300L + i * 90L).setDuration(550)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void loadQuotes() {
        quotes.clear();
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) return;
            File f = new File(dir, "quotes.txt");
            if (!f.exists()) return;
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) quotes.add(line);
            }
            r.close();
        } catch (Exception e) {
            // no quotes file: nothing to show
        }
    }

    private void refreshDay(LocalDate today) {
        Locale loc = Texts.locale(this);
        cityView.setText(Cities.NAMES[Prefs.city(this)]);
        dateView.setText(today.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", loc)));
        try {
            HijrahDate h = HijrahDate.from(today);
            int d = h.get(ChronoField.DAY_OF_MONTH);
            int m = h.get(ChronoField.MONTH_OF_YEAR);
            int y = h.get(ChronoField.YEAR);
            hijriView.setText(d + " " + Texts.hijriMonth(this, m) + " " + y + Texts.pick(this, " AH", " هـ", " H"));
        } catch (Exception e) {
            hijriView.setText("");
        }
        ZonedDateTime[] t = PrayerData.day(this, today);
        for (int i = 0; i < 6; i++) timeViews[i].setText(t[i].format(HHMM));
    }

    private static String two(long v) { return v < 10 ? "0" + v : String.valueOf(v); }

    private void update() {
        ZonedDateTime now = ZonedDateTime.now(Cities.ZONE);
        clock.setText(now.format(HHMM));
        LocalDate today = now.toLocalDate();
        if (!today.equals(cachedDay)) {
            cachedDay = today;
            refreshDay(today);
        }
        PrayerData.Pos pos = PrayerData.position(this, now);
        long secs = Math.max(0, Duration.between(now, pos.next).getSeconds());
        countdown.setText(two(secs / 3600) + ":" + two((secs % 3600) / 60) + ":" + two(secs % 60));
        nextName.setText(Texts.prayer(this, pos.index));

        long total = Math.max(1, Duration.between(pos.prev, pos.next).getSeconds());
        long done = Math.max(0, Duration.between(pos.prev, now).getSeconds());
        float fraction = Math.min(1f, (float) done / (float) total);
        barFill.animate().scaleX(fraction).setDuration(900).start();

        if (pos.index != highlighted) setHighlight(pos.index);

        long ms = System.currentTimeMillis();
        if (!quotes.isEmpty() && ms - lastQuoteAt > 25000) {
            lastQuoteAt = ms;
            final String next = quotes.get(quoteIdx % quotes.size());
            quoteIdx++;
            quoteView.animate().alpha(0f).setDuration(350).withEndAction(new Runnable() {
                @Override
                public void run() {
                    quoteView.setText(next);
                    quoteView.animate().alpha(1f).setDuration(600).start();
                }
            }).start();
        }
    }

    private void setHighlight(int idx) {
        if (pulse != null) { pulse.cancel(); pulse = null; }
        if (highlighted >= 0) { cards[highlighted].setScaleX(1f); cards[highlighted].setScaleY(1f); }
        highlighted = idx;
        for (int i = 0; i < 6; i++) {
            boolean on = (i == idx);
            GradientDrawable g = Ui.shape(this, on ? 0x44E0B44C : 0x22FFFFFF, 18);
            if (on) g.setStroke(Ui.dp(this, 2), Ui.GOLD);
            cards[i].setBackground(g);
            nameViews[i].setTextColor(on ? Ui.GOLD : Ui.SOFT);
        }
        pulse = ObjectAnimator.ofPropertyValuesHolder(cards[idx],
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.05f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.05f));
        pulse.setDuration(1300);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();
    }
}
