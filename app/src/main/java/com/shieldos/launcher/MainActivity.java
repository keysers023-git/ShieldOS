package com.shieldos.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(9, 16, 29);
    private static final int CARD = Color.rgb(26, 39, 61);
    private static final int ACCENT = Color.rgb(56, 202, 228);
    private final Handler clockHandler = new Handler();
    private TextView clock;

    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    private GradientDrawable shape(int color, int corner, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(corner));
        if (stroke != 0) d.setStroke(dp(2), stroke);
        return d;
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        clockHandler.removeCallbacks(updateClock);
        clockHandler.post(updateClock);
    }

    @Override protected void onPause() {
        clockHandler.removeCallbacks(updateClock);
        super.onPause();
    }

    private final Runnable updateClock = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(new SimpleDateFormat("EEE, d MMM  •  HH:mm", Locale.getDefault()).format(new Date()));
            clockHandler.postDelayed(this, 30000);
        }
    };

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(48), dp(34), dp(48), dp(48));
        scroll.addView(body);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = label("SHIELD  /  OS", 30, Color.WHITE, true);
        header.addView(brand, new LinearLayout.LayoutParams(0, dp(58), 1));
        brand.setGravity(Gravity.CENTER_VERTICAL);
        clock = label("", 17, 0xffb0c4dc, false);
        header.addView(clock);
        body.addView(header);
        TextView subtitle = label("YOUR SCREEN. YOUR RULES.", 13, ACCENT, true);
        body.addView(subtitle);
        TextView heading = label("Your apps", 24, Color.WHITE, true);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(36);
        headingParams.bottomMargin = dp(18);
        body.addView(heading, headingParams);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        body.addView(grid);

        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> available = new ArrayList<>(pm.queryIntentActivities(query, 0));
        if (available.isEmpty()) {
            available.addAll(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0));
        }
        available.sort(Comparator.comparing(i -> i.loadLabel(pm).toString().toLowerCase(Locale.ROOT)));
        Set<String> added = new HashSet<>();
        int count = 0;
        for (ResolveInfo info : available) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !added.add(pkg)) continue;
            final Intent launch = pm.getLeanbackLaunchIntentForPackage(pkg) != null
                ? pm.getLeanbackLaunchIntentForPackage(pkg) : pm.getLaunchIntentForPackage(pkg);
            if (launch == null) continue;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(10), dp(16), dp(10), dp(12));
            card.setBackground(shape(CARD, 15, 0));
            card.setFocusable(true);
            card.setOnFocusChangeListener((v, focused) -> {
                v.setBackground(shape(focused ? 0xff173d58 : CARD, 15, focused ? ACCENT : 0));
                v.setScaleX(focused ? 1.06f : 1f);
                v.setScaleY(focused ? 1.06f : 1f);
            });
            card.setOnClickListener(v -> { try { startActivity(launch); } catch (Exception e) {
                android.widget.Toast.makeText(this, "Could not open app", android.widget.Toast.LENGTH_SHORT).show();
            }});
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(info.loadIcon(pm));
            card.addView(icon, new LinearLayout.LayoutParams(dp(62), dp(62)));
            TextView name = label(info.loadLabel(pm).toString(), 14, Color.WHITE, false);
            name.setGravity(Gravity.CENTER);
            name.setMaxLines(2);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-1, dp(45));
            textParams.topMargin = dp(12);
            card.addView(name, textParams);

            GridLayout.LayoutParams cell = new GridLayout.LayoutParams();
            cell.width = dp(185);
            cell.height = dp(152);
            cell.setMargins(dp(5), dp(5), dp(5), dp(5));
            grid.addView(card, cell);
            if (++count == 1) card.requestFocus();
        }
        if (count == 0) body.addView(label("No launchable apps found.", 18, Color.WHITE, false));

        TextView settings = label("⚙  Android Settings", 17, Color.WHITE, true);
        settings.setGravity(Gravity.CENTER);
        settings.setPadding(dp(24), dp(12), dp(24), dp(12));
        settings.setBackground(shape(CARD, 12, 0));
        settings.setFocusable(true);
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); } catch (Exception ignored) {}
        });
        settings.setOnFocusChangeListener((v, focused) -> v.setBackground(shape(focused ? 0xff173d58 : CARD, 12, focused ? ACCENT : 0)));
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(dp(250), dp(58));
        settingsParams.topMargin = dp(25);
        body.addView(settings, settingsParams);
        setContentView(scroll);
        clockHandler.removeCallbacks(updateClock);
        clockHandler.post(updateClock);
    }
}
