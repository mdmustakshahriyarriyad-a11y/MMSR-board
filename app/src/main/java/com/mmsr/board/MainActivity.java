package com.mmsr.board;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(20), dp(32), dp(20), dp(32));
        sv.addView(box);
        setContentView(sv);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        logo.setAdjustViewBounds(true);
        box.addView(logo, new LinearLayout.LayoutParams(dp(110), dp(110)));

        TextView title = new TextView(this);
        title.setText("MMSR Board");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(12), 0, dp(4));
        box.addView(title);

        TextView sub = new TextView(this);
        sub.setText("English \u2022 \u09AC\u09BE\u0982\u09B2\u09BE \u2022 Bangla Phonetic");
        sub.setGravity(Gravity.CENTER);
        box.addView(sub);

        status = new TextView(this);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(20), 0, dp(12));
        box.addView(status);

        Button b1 = new Button(this);
        b1.setText("1. Enable MMSR Board");
        b1.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        box.addView(b1);

        Button b2 = new Button(this);
        b2.setText("2. Switch to MMSR Board");
        b2.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        box.addView(b2);

        Button b3 = new Button(this);
        b3.setText("3. Keyboard Settings");
        b3.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        box.addView(b3);

        EditText test = new EditText(this);
        test.setHint("Tap here to test the keyboard");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(24);
        box.addView(test, lp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        boolean enabled = false;
        if (imm != null) {
            for (InputMethodInfo info : imm.getEnabledInputMethodList()) {
                if (info.getPackageName().equals(getPackageName())) {
                    enabled = true;
                    break;
                }
            }
        }
        String def = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        boolean selected = def != null && def.startsWith(getPackageName() + "/");
        if (selected) {
            status.setText("Status: MMSR Board is active \u2714");
        } else if (enabled) {
            status.setText("Status: Enabled. Now switch to MMSR Board (step 2).");
        } else {
            status.setText("Status: Not enabled yet. Start with step 1.");
        }
    }
}
