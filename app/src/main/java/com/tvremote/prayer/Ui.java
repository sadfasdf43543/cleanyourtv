package com.tvremote.prayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class Ui {
    public static final int GOLD = 0xFFE0B44C;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int SOFT = 0xB3FFFFFF;

    public static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setIncludeFontPadding(false);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    public static GradientDrawable shape(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable background() {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF07121F, 0xFF0F2D33, 0xFF14403B});
        return g;
    }

    /** Background for focusable rows/buttons: soft normally, bright outline when focused. */
    public static StateListDrawable focusable(Context c) {
        GradientDrawable focused = shape(c, 0x40E0B44C, 14);
        focused.setStroke(dp(c, 3), GOLD);
        GradientDrawable normal = shape(c, 0x22FFFFFF, 14);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, focused);
        s.addState(new int[]{}, normal);
        return s;
    }
}
