package com.tvremote.prayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Full-screen adhan: takes audio focus so other apps pause, plays the adhan, then gives the focus back. */
public class AdhanActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private AudioManager audio;
    private AudioFocusRequest focus;
    private Ringtone fallbackTone;
    private boolean closed = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        int idx = getIntent().getIntExtra("idx", 2);
        if (idx < 0 || idx > 5) idx = 2;

        FrameLayout root = new FrameLayout(this);
        root.setBackground(Ui.background());
        root.setLayoutDirection(Texts.rtl(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        root.addView(col, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = Ui.text(this, Texts.pick(this, "Time for prayer", "حان وقت الصلاة", "Namaz vakti"), 32, Ui.SOFT, false);
        col.addView(title);

        TextView name = Ui.text(this, Texts.prayer(this, idx), 110, Ui.GOLD, true);
        col.addView(name);

        String when;
        try {
            when = PrayerData.day(this, ZonedDateTime.now(Cities.ZONE).toLocalDate())[idx].format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            when = "";
        }
        TextView time = Ui.text(this, when, 56, Ui.WHITE, true);
        col.addView(time);

        final TextView hint = Ui.text(this, Texts.pick(this, "Press Back to stop", "اضغط رجوع للإيقاف", "Durdurmak için Geri"), 20, Ui.SOFT, false);
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hl.topMargin = Ui.dp(this, 40);
        col.addView(hint, hl);

        ObjectAnimator breathe = ObjectAnimator.ofPropertyValuesHolder(name,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.06f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.06f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.85f, 1f));
        breathe.setDuration(2200);
        breathe.setRepeatCount(ObjectAnimator.INFINITE);
        breathe.setRepeatMode(ObjectAnimator.REVERSE);
        breathe.setInterpolator(new AccelerateDecelerateInterpolator());
        breathe.start();

        setContentView(root);
        startAudio(hint);
    }

    private File findAudio() {
        String[] names = {"adhan.mp3", "adhan.ogg", "adhan.m4a", "adhan.wav", "azan.mp3"};
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            for (String n : names) {
                File f = new File(dir, n);
                if (f.exists() && f.length() > 0) return f;
            }
        }
        try {
            File alt = new File(Environment.getExternalStorageDirectory(), "Adhan");
            for (String n : names) {
                File f = new File(alt, n);
                if (f.exists() && f.length() > 0 && f.canRead()) return f;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private void startAudio(TextView hint) {
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        try {
            focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(attrs)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int change) { }
                    })
                    .build();
            audio.requestAudioFocus(focus);
        } catch (Exception e) {
            focus = null;
        }

        File f = findAudio();
        if (f == null) {
            File dir = getExternalFilesDir(null);
            String where = (dir != null) ? dir.getAbsolutePath() + "/adhan.mp3" : "adhan.mp3";
            hint.setText(Texts.pick(this, "No adhan file. Put adhan.mp3 in: ", "لا يوجد ملف أذان. ضع adhan.mp3 في: ", "Ezan dosyası yok. Şuraya adhan.mp3 koyun: ") + where);
            try {
                fallbackTone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                if (fallbackTone != null) fallbackTone.play();
            } catch (Exception e) {
                // silent
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() { closeAdhan(); }
            }, 15000);
            return;
        }

        try {
            player = new MediaPlayer();
            player.setAudioAttributes(attrs);
            player.setDataSource(f.getAbsolutePath());
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) { closeAdhan(); }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) { closeAdhan(); return true; }
            });
            player.prepare();
            player.start();
        } catch (Exception e) {
            hint.setText("Audio error: " + e.getMessage());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() { closeAdhan(); }
            }, 6000);
        }
    }

    private void closeAdhan() {
        if (closed) return;
        closed = true;
        handler.removeCallbacksAndMessages(null);
        try { if (player != null) { player.stop(); } } catch (Exception e) { }
        try { if (player != null) { player.release(); } } catch (Exception e) { }
        player = null;
        try { if (fallbackTone != null) fallbackTone.stop(); } catch (Exception e) { }
        try { if (audio != null && focus != null) audio.abandonAudioFocusRequest(focus); } catch (Exception e) { }
        finish();
    }

    @Override
    public void onBackPressed() { closeAdhan(); }

    @Override
    protected void onDestroy() {
        if (!closed) {
            closed = true;
            try { if (player != null) player.release(); } catch (Exception e) { }
            try { if (audio != null && focus != null) audio.abandonAudioFocusRequest(focus); } catch (Exception e) { }
        }
        super.onDestroy();
    }
}
