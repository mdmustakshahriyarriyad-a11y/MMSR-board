package com.mmsr.board;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private SharedPreferences sp;
    private LinearLayout box;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("mmsr_prefs", MODE_PRIVATE);

        ScrollView sv = new ScrollView(this);
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(28), dp(18), dp(40));
        sv.addView(box);
        setContentView(sv);

        TextView title = new TextView(this);
        title.setText("MMSR Board Settings");
        title.setTextSize(26);
        box.addView(title);

        EditText test = new EditText(this);
        test.setHint("Tap here to test your keyboard");
        box.addView(test);

        header("Languages");
        toggle("Bangla keyboard (বাংলা)", "lang_bn", true);
        toggle("Bangla Phonetic (English to Bangla)", "lang_ph", true);
        choice("Default language", "default_lang",
                new String[]{"English", "Bangla", "Bangla Phonetic"},
                new String[]{"en", "bn", "ph"}, "en");
        note("Tip: long-press or swipe the space bar to switch language.");

        header("Keyboard themes");
        choice("Theme", "theme",
                new String[]{"Follow system", "Light", "Dark", "Blue", "Green"},
                new String[]{"system", "light", "dark", "blue", "green"}, "system");

        header("Layout");
        toggle("Show number row", "numrow", true);
        toggle("Show toolbar", "toolbar", true);
        heightSlider();

        header("Text correction");
        toggle("Auto-capitalization", "autocap", true);
        toggle("Double-space inserts a full stop", "doublespace", true);

        header("Sound and vibration");
        toggle("Vibrate on keypress", "vibrate", true);
        toggle("Sound on keypress", "sound", false);

        header("Privacy");
        note("MMSR Board has no internet permission. Nothing you type is stored, "
                + "collected or sent anywhere.");

        header("About MMSR Board");
        note("Version 1.0.0 (Phase 1)\nPackage: com.mmsr.board\n\n"
                + "Coming in later phases: word suggestions, auto-correction, clipboard "
                + "history, personal dictionary, voice typing, swipe typing, floating "
                + "keyboard and one-handed mode.");
    }

    private void header(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(15);
        t.setAllCaps(true);
        t.setPadding(0, dp(24), 0, dp(4));
        box.addView(t);
    }

    private void note(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(14);
        t.setPadding(0, dp(6), 0, dp(6));
        box.addView(t);
    }

    private void toggle(String title, final String key, boolean def) {
        Switch sw = new Switch(this);
        sw.setText(title);
        sw.setTextSize(16);
        sw.setPadding(0, dp(10), 0, dp(10));
        sw.setChecked(sp.getBoolean(key, def));
        sw.setOnCheckedChangeListener((b, checked) -> sp.edit().putBoolean(key, checked).apply());
        box.addView(sw);
    }

    private void choice(final String title, final String key, final String[] labels,
                        final String[] values, final String def) {
        final TextView tv = new TextView(this);
        tv.setTextSize(16);
        tv.setPadding(0, dp(12), 0, dp(12));
        final Runnable refresh = () -> {
            String cur = sp.getString(key, def);
            String lab = labels[0];
            for (int i = 0; i < values.length; i++) if (values[i].equals(cur)) lab = labels[i];
            tv.setText(title + ": " + lab);
        };
        refresh.run();
        tv.setOnClickListener(v -> {
            String cur = sp.getString(key, def);
            int idx = 0;
            for (int i = 0; i < values.length; i++) if (values[i].equals(cur)) idx = i;
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setSingleChoiceItems(labels, idx, (d, which) -> {
                        sp.edit().putString(key, values[which]).apply();
                        refresh.run();
                        d.dismiss();
                    })
                    .show();
        });
        box.addView(tv);
    }

    private void heightSlider() {
        final TextView lbl = new TextView(this);
        lbl.setTextSize(16);
        lbl.setPadding(0, dp(12), 0, 0);
        int cur = sp.getInt("key_height", 52);
        lbl.setText("Keyboard height: " + cur + " dp per row");
        box.addView(lbl);

        SeekBar sb = new SeekBar(this);
        sb.setMax(30);
        sb.setProgress(cur - 40);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = 40 + progress;
                sp.edit().putInt("key_height", val).apply();
                lbl.setText("Keyboard height: " + val + " dp per row");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        box.addView(sb);
    }
}
