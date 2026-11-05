package com.tvremote.prayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private interface Val { String get(); }

    private LinearLayout list;
    private final List<Runnable> refreshers = new ArrayList<Runnable>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        root.setBackground(Ui.background());
        root.setLayoutDirection(Texts.rtl(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 48);
        list.setPadding(pad, Ui.dp(this, 24), pad, Ui.dp(this, 24));
        sv.addView(list, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        TextView title = Ui.text(this, Texts.pick(this, "Settings", "الإعدادات", "Ayarlar"), 36, Ui.GOLD, true);
        list.addView(title);

        addRow(Texts.pick(this, "City", "المدينة", "Şehir"), new Val() {
            public String get() { return Cities.NAMES[Prefs.city(SettingsActivity.this)]; }
        }, new Runnable() {
            public void run() { chooseCity(); }
        });

        addRow(Texts.pick(this, "Language", "اللغة", "Dil"), new Val() {
            public String get() {
                String l = Prefs.lang(SettingsActivity.this);
                return "ar".equals(l) ? "العربية" : ("tr".equals(l) ? "Türkçe" : "English");
            }
        }, new Runnable() {
            public void run() {
                String l = Prefs.lang(SettingsActivity.this);
                String next = "ar".equals(l) ? "tr" : ("tr".equals(l) ? "en" : "ar");
                Prefs.setLang(SettingsActivity.this, next);
                recreate();
            }
        });

        for (int i = 0; i < 6; i++) {
            final int p = i;
            addRow(Texts.prayer(this, p) + Texts.pick(this, " - adhan alert", " - تنبيه الأذان", " - ezan uyarısı"), new Val() {
                public String get() {
                    return Prefs.adhan(SettingsActivity.this, p)
                            ? Texts.pick(SettingsActivity.this, "ON", "مفعّل", "AÇIK")
                            : Texts.pick(SettingsActivity.this, "OFF", "معطّل", "KAPALI");
                }
            }, new Runnable() {
                public void run() { Prefs.setAdhan(SettingsActivity.this, p, !Prefs.adhan(SettingsActivity.this, p)); }
            });
        }

        for (int i = 0; i < 6; i++) {
            final int p = i;
            addRow(Texts.prayer(this, p) + Texts.pick(this, " - adjust (minutes)", " - تعديل (دقائق)", " - ayar (dakika)"), new Val() {
                public String get() {
                    int v = Prefs.offset(SettingsActivity.this, p);
                    return (v > 0 ? "+" : "") + v;
                }
            }, new Runnable() {
                public void run() { chooseOffset(p); }
            });
        }

        addRow(Texts.pick(this, "Test the adhan now", "تجربة الأذان الآن", "Ezanı şimdi dene"), new Val() {
            public String get() { return ""; }
        }, new Runnable() {
            public void run() {
                Intent in = new Intent(SettingsActivity.this, AdhanActivity.class);
                in.putExtra("idx", 2);
                startActivity(in);
            }
        });

        File dir = getExternalFilesDir(null);
        String path = (dir != null) ? dir.getAbsolutePath() : "";
        TextView info = Ui.text(this,
                Texts.pick(this, "Adhan audio file: ", "ملف صوت الأذان: ", "Ezan ses dosyası: ") + path + "/adhan.mp3\n"
                        + Texts.pick(this, "Optional texts (one per line): ", "نصوص اختيارية (نص في كل سطر): ", "İsteğe bağlı metinler (her satır bir metin): ") + path + "/quotes.txt",
                16, Ui.SOFT, false);
        info.setGravity(Gravity.START);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ilp.topMargin = Ui.dp(this, 14);
        list.addView(info, ilp);

        addRow(Texts.pick(this, "Back", "رجوع", "Geri"), new Val() {
            public String get() { return ""; }
        }, new Runnable() {
            public void run() { finish(); }
        });

        refreshAll();
    }

    private void addRow(final String label, final Val val, final Runnable action) {
        final TextView t = Ui.text(this, "", 22, Ui.WHITE, false);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        t.setBackground(Ui.focusable(this));
        t.setFocusable(true);
        t.setClickable(true);
        t.setPadding(Ui.dp(this, 20), Ui.dp(this, 14), Ui.dp(this, 20), Ui.dp(this, 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, 8);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
                refreshAll();
                Scheduler.scheduleNext(SettingsActivity.this, System.currentTimeMillis());
            }
        });
        list.addView(t, lp);
        refreshers.add(new Runnable() {
            @Override
            public void run() {
                String v = val.get();
                t.setText(v.length() > 0 ? label + "      " + v : label);
            }
        });
    }

    private void refreshAll() {
        for (Runnable r : refreshers) r.run();
    }

    private void chooseCity() {
        new AlertDialog.Builder(this)
                .setTitle(Texts.pick(this, "City", "المدينة", "Şehir"))
                .setSingleChoiceItems(Cities.NAMES, Prefs.city(this), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setCity(SettingsActivity.this, which);
                        d.dismiss();
                        refreshAll();
                        Scheduler.scheduleNext(SettingsActivity.this, System.currentTimeMillis());
                    }
                }).show();
    }

    private void chooseOffset(final int prayer) {
        final String[] items = new String[21];
        for (int i = 0; i < 21; i++) {
            int v = i - 10;
            items[i] = (v > 0 ? "+" : "") + v;
        }
        new AlertDialog.Builder(this)
                .setTitle(Texts.prayer(this, prayer))
                .setSingleChoiceItems(items, Prefs.offset(this, prayer) + 10, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setOffset(SettingsActivity.this, prayer, which - 10);
                        d.dismiss();
                        refreshAll();
                        Scheduler.scheduleNext(SettingsActivity.this, System.currentTimeMillis());
                    }
                }).show();
    }
}
