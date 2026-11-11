package com.mmsr.board;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MMSRBoardService extends InputMethodService {

    private static final int T_CHAR = 0, T_SHIFT = 1, T_DEL = 2, T_SPACE = 3, T_ENTER = 4,
            T_SYM = 5, T_ABC = 6, T_EMOJI = 7, T_SYM2 = 8, T_SPACER = 99;
    private static final int L_EN = 0, L_BN = 1, L_PH = 2, L_SYM = 3, L_EMOJI = 4;

    private static final String COMBINING =
            "\u09BE\u09BF\u09C0\u09C1\u09C2\u09C3\u09C7\u09C8\u09CB\u09CC\u0981\u09CD";
    private static final String BN_KARS = "\u09BE\u09BF\u09C0\u09C1\u09C2\u09C3\u09C7\u09C8\u09CB\u09CC";

    private static final String[] EMOJI_TABS = {"😀", "👍", "💖", "🐶", "🍎", "📱"};
    private static final String[] EMOJI_DATA = {
            "😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 🙃 😉 😌 😍 😘 😗 😋 😛 😜 🤪 🤓 😎 🤩 😏 😒 😞 😔 😟 😕 🙁 😣 😖 😫 😩 😢 😭 😤 😠 😡 🤯 😳 😱 😨 😰 😥 😓 🤗 🤔 🤭 🤫 😶 😐 😑 😬 🙄 😯 😴 😷 🤒 🤕 🤢 🤮 😵 🤠 😈 💀 👻 👽 🤖",
            "👍 👎 👌 🤞 🤟 🤘 🤙 👈 👉 👆 👇 👋 🤝 🙏 👏 🙌 💪 👀 👂 👃 👄 👅 👶 👦 👧 👨 👩 👴 👵 🙋 🙆 🙅 🤦 🤷 💃 🏃",
            "\u2764\uFE0F 🧡 💛 💚 💙 💜 🖤 💔 💕 💞 💓 💗 💖 💘 💝 💯 💢 💥 💫 💦 🔥 ⭐ 🌟 ✨ ⚡ 🎉 🎊 🎈 🎁 🏆 ✅ ❌ ❓ ❗ 💤 🔔",
            "🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🦆 🦅 🦉 🐺 🐴 🦄 🐝 🐛 🦋 🐌 🐢 🐍 🐙 🦀 🐠 🐬 🐳 🐘 🌸 🌹 🌻 🌲 🌴 🍀 🌍 🌙 🌈",
            "🍎 🍊 🍋 🍌 🍉 🍇 🍓 🍒 🍑 🍍 🥝 🍅 🥑 🍆 🥕 🌽 🍞 🧀 🍗 🍖 🍔 🍟 🍕 🌭 🌮 🍜 🍝 🍣 🍚 🍦 🍩 🍪 🎂 🍰 🍫 🍬 ☕ 🍵 🍺 🍷",
            "📱 💻 ⌚ 📷 🎧 🎤 🎮 ⚽ 🏀 🏏 🚗 🚌 🚀 🏠 🏫 🏥 💰 💳 📚 📌 🔑 🔒 💡 🔋 🎵 🎶 📞 📧"
    };

    private static final HashMap<String, String[]> EN_ALTS = new HashMap<>();
    private static final HashMap<String, String[]> BN_ALTS = new HashMap<>();
    private static final HashMap<String, String[]> PH_ALTS = new HashMap<>();

    private static String[] split(String chars) {
        String[] a = new String[chars.length()];
        for (int i = 0; i < a.length; i++) a[i] = String.valueOf(chars.charAt(i));
        return a;
    }

    private static void alt(HashMap<String, String[]> m, String key, String chars) {
        m.put(key, split(chars));
    }

    static {
        alt(EN_ALTS, "a", "àáâäæãåā");
        alt(EN_ALTS, "e", "èéêëēėę");
        alt(EN_ALTS, "i", "ìíîïī");
        alt(EN_ALTS, "o", "òóôöœøō");
        alt(EN_ALTS, "u", "ùúûüū");
        alt(EN_ALTS, "c", "çćč");
        alt(EN_ALTS, "n", "ñń");
        alt(EN_ALTS, "s", "śš");
        alt(EN_ALTS, "y", "ÿý");
        alt(EN_ALTS, "z", "žźż");
        alt(EN_ALTS, "l", "ł");
        alt(EN_ALTS, "1", "¹½⅓");
        alt(EN_ALTS, "2", "²⅔");
        alt(EN_ALTS, "3", "³¾");
        alt(EN_ALTS, "0", "°ⁿ");
        alt(EN_ALTS, ".", "!?:;…");

        alt(BN_ALTS, "ড", "\u09DC");
        alt(BN_ALTS, "ঢ", "\u09DD");
        alt(BN_ALTS, "য", "\u09DF");
        alt(BN_ALTS, "ও", "ঔ");

        alt(PH_ALTS, "n", "\u0982\u0983\u0981");
        alt(PH_ALTS, "t", "\u09CE");
    }

    private static class Key {
        String label;
        String out;
        int type;
        float w;
        String[] alts;
        boolean letter;

        Key(String label, String out, int type, float w) {
            this.label = label;
            this.out = out;
            this.type = type;
            this.w = w;
        }
    }

    private final Handler h = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private LinearLayout root, toolbarBox, keysBox;
    private final ArrayList<TextView> letterViews = new ArrayList<>();
    private final ArrayList<Key> letterKeys = new ArrayList<>();
    private TextView shiftView;
    private PopupWindow altPopup;
    private final StringBuilder phBuf = new StringBuilder();

    private int layout = L_EN, letterLayout = L_EN, bnPage = 0, symPage = 0, emojiCat = 0;
    private boolean shift, caps, passwordMode;
    private long lastShiftTime, lastSpaceTime;

    private boolean pVibrate = true, pSound = false, pAutoCap = true, pNumRow = true,
            pToolbar = true, pDoubleSpace = true;
    private int pHeight = 52;
    private int cBg, cKey, cSpecial, cText, cAccent;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("mmsr_prefs", MODE_PRIVATE);
        loadPrefs();
        String dl = prefs.getString("default_lang", "en");
        letterLayout = "bn".equals(dl) ? L_BN : ("ph".equals(dl) ? L_PH : L_EN);
        if (!langEnabled(letterLayout)) letterLayout = L_EN;
        layout = letterLayout;
    }

    @Override
    public View onCreateInputView() {
        loadPrefs();
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(4), dp(4), dp(4), dp(6));
        toolbarBox = new LinearLayout(this);
        keysBox = new LinearLayout(this);
        keysBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(toolbarBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(keysBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rebuild();
        return root;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        loadPrefs();
        phBuf.setLength(0);
        shift = false;
        caps = false;
        bnPage = 0;
        int cls = info.inputType & InputType.TYPE_MASK_CLASS;
        int var = info.inputType & InputType.TYPE_MASK_VARIATION;
        passwordMode = cls == InputType.TYPE_CLASS_TEXT
                && (var == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || var == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                || var == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        boolean numeric = cls == InputType.TYPE_CLASS_NUMBER
                || cls == InputType.TYPE_CLASS_PHONE
                || cls == InputType.TYPE_CLASS_DATETIME;
        if (!langEnabled(letterLayout)) letterLayout = L_EN;
        if (numeric) {
            layout = L_SYM;
            symPage = 0;
        } else if (!restarting) {
            layout = letterLayout;
        }
        if (passwordMode && (layout == L_BN || layout == L_PH)) layout = L_EN;
        rebuild();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        commitPhonetic();
        dismissAlts();
    }

    @Override
    public void onUpdateSelection(int oldSs, int oldSe, int newSs, int newSe, int cs, int ce) {
        super.onUpdateSelection(oldSs, oldSe, newSs, newSe, cs, ce);
        if (phBuf.length() > 0 && (ce < 0 || newSs != ce || newSe != ce)) {
            phBuf.setLength(0);
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.finishComposingText();
        }
    }

    private void loadPrefs() {
        if (prefs == null) prefs = getSharedPreferences("mmsr_prefs", MODE_PRIVATE);
        pVibrate = prefs.getBoolean("vibrate", true);
        pSound = prefs.getBoolean("sound", false);
        pAutoCap = prefs.getBoolean("autocap", true);
        pNumRow = prefs.getBoolean("numrow", true);
        pToolbar = prefs.getBoolean("toolbar", true);
        pDoubleSpace = prefs.getBoolean("doublespace", true);
        pHeight = prefs.getInt("key_height", 52);
        applyTheme();
    }

    private boolean langEnabled(int l) {
        if (l == L_EN) return true;
        if (l == L_BN) return prefs.getBoolean("lang_bn", true);
        if (l == L_PH) return prefs.getBoolean("lang_ph", true);
        return true;
    }

    private void applyTheme() {
        String t = prefs.getString("theme", "system");
        if (t.equals("system")) {
            boolean night = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            t = night ? "dark" : "light";
        }
        if (t.equals("dark")) {
            cBg = 0xFF1F2023; cKey = 0xFF3C4043; cSpecial = 0xFF2B2D30;
            cText = 0xFFE8EAED; cAccent = 0xFF8AB4F8;
        } else if (t.equals("blue")) {
            cBg = 0xFF0D47A1; cKey = 0xFF1976D2; cSpecial = 0xFF1565C0;
            cText = 0xFFFFFFFF; cAccent = 0xFFFFC107;
        } else if (t.equals("green")) {
            cBg = 0xFF1B5E20; cKey = 0xFF2E7D32; cSpecial = 0xFF256D2A;
            cText = 0xFFFFFFFF; cAccent = 0xFFFFEB3B;
        } else {
            cBg = 0xFFECEFF1; cKey = 0xFFFFFFFF; cSpecial = 0xFFCFD8DC;
            cText = 0xFF202124; cAccent = 0xFF1A73E8;
        }
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(8));
        return g;
    }

    private int blend(int a, int b, float t) {
        int r = (int) (Color.red(a) * (1 - t) + Color.red(b) * t);
        int g = (int) (Color.green(a) * (1 - t) + Color.green(b) * t);
        int bl = (int) (Color.blue(a) * (1 - t) + Color.blue(b) * t);
        return Color.rgb(r, g, bl);
    }

    private int contrast(int c) {
        double l = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0;
        return l > 0.6 ? 0xFF000000 : 0xFFFFFFFF;
    }

    private StateListDrawable keyBg(int color) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, shape(blend(color, cAccent, 0.45f)));
        s.addState(new int[]{}, shape(color));
        return s;
    }

    private void requestRebuild() {
        h.post(new Runnable() {
            @Override
            public void run() {
                rebuild();
            }
        });
    }

    private void rebuild() {
        if (root == null) return;
        dismissAlts();
        keysBox.removeAllViews();
        letterViews.clear();
        letterKeys.clear();
        shiftView = null;
        root.setBackgroundColor(cBg);
        buildToolbar();
        if (layout == L_EMOJI) buildEmoji();
        else if (layout == L_SYM) buildSymbols();
        else buildLetters();
        updateShiftView();
        updateAutoCap();
    }

    private TextView toolButton(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(17);
        t.setTextColor(cText);
        t.setGravity(Gravity.CENTER);
        t.setBackground(keyBg(cBg));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(52), dp(38));
        lp.setMargins(dp(2), dp(1), dp(2), dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    private void buildToolbar() {
        toolbarBox.removeAllViews();
        if (!pToolbar) {
            toolbarBox.setVisibility(View.GONE);
            return;
        }
        toolbarBox.setVisibility(View.VISIBLE);
        toolbarBox.setOrientation(LinearLayout.HORIZONTAL);
        toolbarBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView settings = toolButton("\u2699");
        settings.setOnClickListener(v -> {
            Intent i = new Intent(this, SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
        toolbarBox.addView(settings);

        TextView lang = toolButton(letterLayout == L_EN ? "EN" : (letterLayout == L_BN ? "বাং" : "ফো"));
        lang.setOnClickListener(v -> cycleLanguage());
        toolbarBox.addView(lang);

        TextView emoji = toolButton("😀");
        emoji.setOnClickListener(v -> {
            commitPhonetic();
            layout = (layout == L_EMOJI) ? letterLayout : L_EMOJI;
            requestRebuild();
        });
        toolbarBox.addView(emoji);

        View spacerView = new View(this);
        toolbarBox.addView(spacerView, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView hide = toolButton("\u25BE");
        hide.setOnClickListener(v -> requestHideSelf(0));
        toolbarBox.addView(hide);
    }

    private String label(String c) {
        return COMBINING.contains(c) ? "\u25CC" + c : c;
    }

    private ArrayList<Key> chars(String s, boolean letter, HashMap<String, String[]> alts) {
        ArrayList<Key> l = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            Key k = new Key(label(c), c, T_CHAR, 1f);
            k.letter = letter;
            if (alts != null) k.alts = alts.get(c);
            l.add(k);
        }
        return l;
    }

    private Key spacer(float w) {
        return new Key("", "", T_SPACER, w);
    }

    private Key delKey(float w) {
        return new Key("\u232B", "", T_DEL, w);
    }

    private Key shiftKey(float w) {
        String lab = "\u21E7";
        if (layout == L_BN) lab = (bnPage == 0) ? "অআ" : "কখ";
        return new Key(lab, "", T_SHIFT, w);
    }

    private String langName() {
        if (letterLayout == L_BN) return "বাংলা";
        if (letterLayout == L_PH) return "বাংলা (Phonetic)";
        return "English";
    }

    private String enterLabel() {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) return "\u23CE";
        boolean multi = (ei.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        if (multi || (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return "\u23CE";
        switch (ei.imeOptions & EditorInfo.IME_MASK_ACTION) {
            case EditorInfo.IME_ACTION_SEND: return "\u27A4";
            case EditorInfo.IME_ACTION_GO: return "\u2192";
            case EditorInfo.IME_ACTION_SEARCH: return "🔍";
            case EditorInfo.IME_ACTION_NEXT: return "\u2192";
            case EditorInfo.IME_ACTION_DONE: return "\u2713";
            default: return "\u23CE";
        }
    }

    private TextView makeKeyView(Key k) {
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setIncludeFontPadding(false);
        tv.setSingleLine(true);
        String text = k.letter ? (shift ? k.out.toUpperCase() : k.out) : k.label;
        tv.setText(text);
        int bg = (k.type == T_CHAR || k.type == T_SPACE) ? cKey : cSpecial;
        int fg = cText;
        if (k.type == T_ENTER) {
            bg = cAccent;
            fg = contrast(cAccent);
        }
        tv.setTextColor(fg);
        tv.setBackground(keyBg(bg));
        float size;
        if (k.type == T_CHAR) size = text.length() > 1 ? 15 : 20;
        else if (k.type == T_SPACE) size = 12;
        else size = 16;
        tv.setTextSize(size);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(pHeight), k.w);
        lp.setMargins(dp(2.5f), dp(3), dp(2.5f), dp(3));
        tv.setLayoutParams(lp);
        return tv;
    }

    private void addRow(ArrayList<Key> keys) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        for (Key k : keys) {
            if (k.type == T_SPACER) {
                r.addView(new View(this), new LinearLayout.LayoutParams(0, 1, k.w));
                continue;
            }
            TextView v = makeKeyView(k);
            attach(v, k);
            r.addView(v);
            if (k.letter) {
                letterViews.add(v);
                letterKeys.add(k);
            }
            if (k.type == T_SHIFT && layout != L_BN) shiftView = v;
        }
        keysBox.addView(r, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void buildLetters() {
        if (pNumRow) {
            if (layout == L_BN) addRow(chars("১২৩৪৫৬৭৮৯০", false, null));
            else addRow(chars("1234567890", false, EN_ALTS));
        }
        if (layout == L_BN) {
            if (bnPage == 0) {
                addRow(chars("কখগঘঙচছজঝঞ", false, BN_ALTS));
                addRow(chars("টঠডঢণতথদধন", false, BN_ALTS));
                ArrayList<Key> r3 = new ArrayList<>();
                r3.add(shiftKey(1f));
                r3.addAll(chars("পফবভমযরল", false, BN_ALTS));
                r3.add(delKey(1f));
                addRow(r3);
            } else {
                addRow(chars("কখগঘঙচছজঝঞ", false, BN_ALTS));
                addRow(chars("টঠডঢণতথদধন", false, BN_ALTS));
                ArrayList<Key> r3 = new ArrayList<>();
                r3.add(shiftKey(1f));
                r3.addAll(chars("পফবভমযরল", false, BN_ALTS));
                r3.add(delKey(1f));
                addRow(r3);
            } else {
                addRow(chars("অআইঈউঊঋএঐও", false, BN_ALTS));
                addRow(chars(BN_KARS, false, null));
                ArrayList<Key> r3 = new ArrayList<>();
                r3.add(shiftKey(1f));
                r3.addAll(chars("শষসহ\u0982\u0983\u0981\u09CE", false, null));
                r3.add(delKey(1f));
                addRow(r3);
            }
        } else {
            HashMap<String, String[]> alts = (layout == L_PH) ? PH_ALTS : EN_ALTS;
            addRow(chars("qwertyuiop", true, alts));
            ArrayList<Key> r2 = new ArrayList<>();
            r2.add(spacer(0.5f));
            r2.addAll(chars("asdfghjkl", true, alts));
            r2.add(spacer(0.5f));
            addRow(r2);
            ArrayList<Key> r3 = new ArrayList<>();
            r3.add(shiftKey(1.5f));
            r3.addAll(chars("zxcvbnm", true, alts));
            r3.add(delKey(1.5f));
            addRow(r3);
        }
        ArrayList<Key> b = new ArrayList<>();
        b.add(new Key("?123", "", T_SYM, 1.3f));
        b.add(new Key("😀", "", T_EMOJI, 1f));
        if (layout == L_BN) {
            b.add(new Key("\u25CC\u09CD", "\u09CD", T_CHAR, 1f));
        } else {
            b.add(new Key(",", ",", T_CHAR, 1f));
        }
        b.add(new Key(langName(), "", T_SPACE, 4f));
        if (layout == L_EN) {
            Key dot = new Key(".", ".", T_CHAR, 1f);
            dot.alts = EN_ALTS.get(".");
            b.add(dot);
        } else {
            Key danda = new Key("\u0964", "\u0964", T_CHAR, 1f);
            danda.alts = split(",.!?:;'\"");
            b.add(danda);
        }
        b.add(new Key(enterLabel(), "", T_ENTER, 1.4f));
        addRow(b);
    }

    private void buildSymbols() {
        addRow(chars("1234567890", false, EN_ALTS));
        ArrayList<Key> r3 = new ArrayList<>();
        if (symPage == 0) {
            addRow(chars("@#$_&-+()/", false, null));
            addRow(chars("*\"':;!?,.%", false, null));
            r3.add(new Key("=\\<", "", T_SYM2, 1.5f));
            r3.addAll(chars("{}…±«»¡", false, null));
        }
        r3.add(delKey(1.5f));
        addRow(r3);
        ArrayList<Key> b = new ArrayList<>();
        b.add(new Key("ABC", "", T_ABC, 1.5f));
        b.add(new Key(",", ",", T_CHAR, 1f));
        b.add(new Key(langName(), "", T_SPACE, 4f));
        b.add(new Key(".", ".", T_CHAR, 1f));
        b.add(new Key(enterLabel(), "", T_ENTER, 1.4f));
        addRow(b);
    }

    private void buildEmoji() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < EMOJI_TABS.length; i++) {
            final int idx = i;
            TextView t = new TextView(this);
            t.setText(EMOJI_TABS[i]);
            t.setTextSize(20);
            t.setGravity(Gravity.CENTER);
            t.setBackground(keyBg(i == emojiCat ? cAccent : cSpecial));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(40), 1f);
            lp.setMargins(dp(2), dp(2), dp(2), dp(3));
            t.setLayoutParams(lp);
            t.setOnClickListener(v -> {
                emojiCat = idx;
                requestRebuild();
            });
            tabs.addView(t);
        }
        keysBox.addView(tabs, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ScrollView sv = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        String[] list = EMOJI_DATA[emojiCat].split(" ");
        int cols = 8;
        for (int i = 0; i < list.length; i += cols) {
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < cols; j++) {
                TextView t = new TextView(this);
                t.setGravity(Gravity.CENTER);
                t.setTextSize(24);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(pHeight), 1f);
                t.setLayoutParams(lp);
                if (i + j < list.length) {
                    final String e = list[i + j];
                    t.setText(e);
                    t.setOnClickListener(v -> {
                        feedback(T_CHAR);
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) ic.commitText(e, 1);
                    });
                }
                r.addView(t);
            }
            col.addView(r);
        }
        sv.addView(col);
        keysBox.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(pHeight) * 4));

        ArrayList<Key> b = new ArrayList<>();
        b.add(new Key("ABC", "", T_ABC, 1.5f));
        b.add(new Key(langName(), "", T_SPACE, 4f));
        b.add(delKey(1.5f));
        addRow(b);
    }

    private void attach(final View v, final Key k) {
        v.setOnTouchListener(new View.OnTouchListener() {
            boolean longDone;
            float downX;
            Runnable lp;
            Runnable rep;

            private void cancel() {
                if (lp != null) h.removeCallbacks(lp);
                if (rep != null) h.removeCallbacks(rep);
            }

            @Override
            public boolean onTouch(final View view, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dismissAlts();
                        longDone = false;
                        downX = e.getX();
                        view.setPressed(true);
                        feedback(k.type);
                        if (k.type == T_DEL) {
                            doKey(k);
                            rep = new Runnable() {
                                @Override
                                public void run() {
                                    doKey(k);
                                    h.postDelayed(this, 45);
                                }
                            };
                            h.postDelayed(rep, 400);
                        } else if (k.type == T_SPACE) {
                            lp = new Runnable() {
                                @Override
                                public void run() {
                                    longDone = true;
                                    cycleLanguage();
                                }
                            };
                            h.postDelayed(lp, 450);
                        } else if (k.type == T_CHAR && k.alts != null) {
                            lp = new Runnable() {
                                @Override
                                public void run() {
                                    longDone = true;
                                    view.setPressed(false);
                                    showAlts(view, k);
                                }
                            };
                            h.postDelayed(lp, 450);
                        } else if (k.type == T_CHAR && k.alts != null) {
                            lp = new Runnable() {
                                @Override
                                public void run() {
                                    longDone = true;
                                    view.setPressed(false);
                                    showAlts(view, k);
                                }
                            };
                            h.postDelayed(lp, 350);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (k.type == T_SPACE && !longDone && Math.abs(e.getX() - downX) > dp(70)) {
                            cancel();
                            longDone = true;
                            cycleLanguage();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        cancel();
                        if (!longDone && k.type != T_DEL) doKey(k);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        view.setPressed(false);
                        cancel();
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void showAlts(View anchor, Key k) {
        dismissAlts();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable bg = shape(cKey);
        bg.setStroke(dp(1), cAccent);
        box.setBackground(bg);
        for (String a : k.alts) {
            final String shown = (k.letter && shift) ? a.toUpperCase() : a;
            TextView t = new TextView(this);
            t.setText(label(shown));
            t.setTextSize(22);
            t.setTextColor(cText);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(11), dp(8), dp(11), dp(8));
            t.setOnClickListener(v -> {
                insertText(shown);
                dismissAlts();
            });
            box.addView(t);
        }
        box.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int w = box.getMeasuredWidth();
        int ph = box.getMeasuredHeight();
        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);
        int screenW = getResources().getDisplayMetrics().widthPixels;
        int x = loc[0] + anchor.getWidth() / 2 - w / 2;
        if (x + w > screenW) x = screenW - w;
        if (x < 0) x = 0;
        int y = loc[1] - ph - dp(6);
        if (y < 0) y = 0;
        try {
            altPopup = new PopupWindow(box, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            altPopup.setBackgroundDrawable(new ColorDrawable(0));
            altPopup.setOutsideTouchable(true);
            altPopup.showAtLocation(anchor, Gravity.TOP | Gravity.START, x, y);
        } catch (Exception ex) {
            altPopup = null;
        }
    }

    private void dismissAlts() {
        if (altPopup != null) {
            try {
                altPopup.dismiss();
            } catch (Exception ignored) {
            }
            altPopup = null;
        }
    }

    private void feedback(int type) {
        if (pVibrate) {
            Vibrator vb = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vb != null && vb.hasVibrator()) {
                vb.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
        if (pSound) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (am != null) {
                int fx = AudioManager.FX_KEYPRESS_STANDARD;
                if (type == T_SPACE) fx = AudioManager.FX_KEYPRESS_SPACEBAR;
                else if (type == T_DEL) fx = AudioManager.FX_KEYPRESS_DELETE;
                else if (type == T_ENTER) fx = AudioManager.FX_KEYPRESS_RETURN;
                am.playSoundEffect(fx, 0.6f);
            }
        }
    }

    private void doKey(Key k) {
        switch (k.type) {
            case T_CHAR:
                typeText(k.letter && shift ? k.out.toUpperCase() : k.out);
                break;
            case T_DEL:
                handleDelete();
                break;
            case T_SPACE:
                handleSpace();
                break;
            case T_ENTER:
                handleEnter();
                break;
            case T_SHIFT:
                handleShift();
                break;
            case T_SYM:
                commitPhonetic();
                layout = L_SYM;
                symPage = 0;
                requestRebuild();
                break;
            case T_SYM2:
                symPage = 1 - symPage;
                requestRebuild();
                break;
            case T_ABC:
                layout = letterLayout;
                requestRebuild();
                break;
            case T_EMOJI:
                commitPhonetic();
                layout = (layout == L_EMOJI) ? letterLayout : L_EMOJI;
                requestRebuild();
                break;
            default:
                break;
        }
    }

    private boolean isAsciiLetter(String s) {
        if (s.length() != 1) return false;
        char c = s.charAt(0);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void typeText(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        if (layout == L_PH && !passwordMode && isAsciiLetter(s)) {
            phBuf.append(s);
            ic.setComposingText(PhoneticEngine.transliterate(phBuf.toString()), 1);
        } else {
            commitPhonetic();
            ic.commitText(s, 1);
        }
        if ((layout == L_EN || layout == L_PH) && shift && !caps) {
            shift = false;
            refreshLabels();
        }
        if (layout == L_BN && bnPage == 1) {
            bnPage = 0;
            requestRebuild();
        }
        updateAutoCap();
    }

    private void insertText(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        commitPhonetic();
        ic.commitText(s, 1);
        if ((layout == L_EN || layout == L_PH) && shift && !caps) {
            shift = false;
            refreshLabels();
        }
        updateAutoCap();
    }

    private void commitPhonetic() {
        if (phBuf.length() == 0) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(PhoneticEngine.transliterate(phBuf.toString()), 1);
        phBuf.setLength(0);
    }

    private void handleDelete() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        if (layout == L_PH && phBuf.length() > 0) {
            phBuf.setLength(phBuf.length() - 1);
            if (phBuf.length() == 0) {
                ic.setComposingText("", 1);
                ic.finishComposingText();
            } else {
                ic.setComposingText(PhoneticEngine.transliterate(phBuf.toString()), 1);
            }
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
        updateAutoCap();
    }

    private void handleSpace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        commitPhonetic();
        long now = SystemClock.uptimeMillis();
        if (pDoubleSpace && now - lastSpaceTime < 500 && layout != L_SYM && layout != L_EMOJI) {
            CharSequence b = ic.getTextBeforeCursor(2, 0);
            if (b != null && b.length() == 2 && b.charAt(1) == ' ') {
                char c = b.charAt(0);
                int type = Character.getType(c);
                if (Character.isLetterOrDigit(c)
                        || type == Character.NON_SPACING_MARK
                        || type == Character.COMBINING_SPACING_MARK) {
                    ic.deleteSurroundingText(1, 0);
                    ic.commitText(letterLayout == L_EN ? ". " : "\u0964 ", 1);
                    lastSpaceTime = 0;
                    updateAutoCap();
                    return;
                }
            }
        }
        ic.commitText(" ", 1);
        lastSpaceTime = now;
        updateAutoCap();
    }

    private void handleEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        commitPhonetic();
        EditorInfo ei = getCurrentInputEditorInfo();
        int action = ei == null ? EditorInfo.IME_ACTION_NONE : (ei.imeOptions & EditorInfo.IME_MASK_ACTION);
        boolean noEnter = ei != null && (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
        boolean multi = ei != null && (ei.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        if (!noEnter && !multi && action != EditorInfo.IME_ACTION_NONE
                && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action);
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        }
        updateAutoCap();
    }

    private void handleShift() {
        if (layout == L_BN) {
            bnPage = 1 - bnPage;
            requestRebuild();
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (caps) {
            caps = false;
            shift = false;
        } else if (shift && now - lastShiftTime < 350) {
            caps = true;
        } else {
            shift = !shift;
        }
        lastShiftTime = now;
        refreshLabels();
    }

    private void refreshLabels() {
        for (int i = 0; i < letterViews.size(); i++) {
            String o = letterKeys.get(i).out;
            letterViews.get(i).setText(shift ? o.toUpperCase() : o);
        }
        updateShiftView();
    }

    private void updateShiftView() {
        if (shiftView == null) return;
        boolean on = shift || caps;
        shiftView.setText(caps ? "\u21EA" : "\u21E7");
        shiftView.setBackground(keyBg(on ? cAccent : cSpecial));
        shiftView.setText(on ? contrast(cAccent) : cText);
    }

    private void updateAutoCap() {
        if (!pAutoCap || layout != L_EN || caps) return;
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ei == null || ic == null || ei.inputType == 0) return;
        boolean s = ic.getCursorCapsMode(ei.inputType) != 0;
        if (s != shift) {
            shift = s;
            refreshLabels();
        }
    }

    private void cycleLanguage() {
        if (passwordMode) return;
        commitPhonetic();
        int[] order = {L_EN, L_BN, L_PH};
        int idx = 0;
        for (int i = 0; i < order.length; i++) if (order[i] == letterLayout) idx = i;
        for (int i = 1; i <= order.length; i++) {
            int c = order[(idx + i) % order.length];
            if (langEnabled(c)) {
                letterLayout = c;
                break;
            }
        }
        layout = letterLayout;
        shift = false;
        caps = false;
        bnPage = 0;
        requestRebuild();
    }
}
