package com.mmsr.board;

import java.util.HashMap;

/** Avro-style English to Bangla phonetic transliteration. */
public final class PhoneticEngine {

    private static final HashMap<String, String> CONS = new HashMap<>();
    private static final HashMap<String, String[]> VOW = new HashMap<>();
    private static final HashMap<String, String> OTHER = new HashMap<>();
    private static final String HAS = "\u09CD";

    private static void c(String k, String v) {
        CONS.put(k, v);
    }

    private static void v(String k, String independent, String kar) {
        VOW.put(k, new String[]{independent, kar});
    }

    static {
        c("k", "ক"); c("kh", "খ"); c("g", "গ"); c("gh", "ঘ"); c("Ng", "ঙ");
        c("c", "চ"); c("ch", "ছ"); c("j", "জ"); c("jh", "ঝ"); c("NG", "ঞ");
        c("T", "ট"); c("Th", "ঠ"); c("D", "ড"); c("Dh", "ঢ"); c("N", "ণ");
        c("t", "ত"); c("th", "থ"); c("d", "দ"); c("dh", "ধ"); c("n", "ন");
        c("p", "প"); c("ph", "ফ"); c("f", "ফ"); c("b", "ব"); c("bh", "ভ");
        c("v", "ভ"); c("m", "ম"); c("z", "য"); c("r", "র"); c("l", "ল");
        c("sh", "শ"); c("S", "শ"); c("Sh", "ষ"); c("s", "স"); c("h", "হ");
        c("R", "\u09DC"); c("Rh", "\u09DD"); c("Y", "\u09DF");
        c("kkh", "ক্ষ"); c("x", "ক্স"); c("q", "ক"); c("J", "জ");

        v("o", "অ", "");
        v("a", "আ", "\u09BE");
        v("i", "ই", "\u09BF");
        v("I", "ঈ", "\u09C0");
        v("ii", "ঈ", "\u09C0");
        v("ee", "ঈ", "\u09C0");
        v("u", "উ", "\u09C1");
        v("oo", "উ", "\u09C1");
        v("U", "ঊ", "\u09C2");
        v("uu", "ঊ", "\u09C2");
        v("e", "এ", "\u09C7");
        v("oi", "ঐ", "\u09C8");
        v("O", "ও", "\u09CB");
        v("ou", "ঔ", "\u09CC");
        v("rri", "ঋ", "\u09C3");

        OTHER.put("ng", "\u0982");
        OTHER.put("H", "\u0983");
    }

    private PhoneticEngine() {
    }

    public static String transliterate(String s) {
        StringBuilder out = new StringBuilder();
        boolean prevCons = false;
        int i = 0;
        int n = s.length();
        while (i < n) {
            boolean matched = false;
            for (int len = Math.min(3, n - i); len >= 1 && !matched; len--) {
                String sub = s.substring(i, i + len);
                String r = null;
                String[] vv = null;
                if (sub.equals("y")) {
                    out.append(prevCons ? HAS + "য" : "\u09DF");
                    prevCons = true;
                    matched = true;
                } else if (sub.equals("w")) {
                    if (prevCons) {
                        out.append(HAS).append("ব");
                        prevCons = true;
                    } else {
                        out.append("ও");
                        prevCons = false;
                    }
                    matched = true;
                } else if ((r = CONS.get(sub)) != null) {
                    out.append(prevCons ? HAS + r : r);
                    prevCons = true;
                    matched = true;
                } else if ((vv = VOW.get(sub)) != null) {
                    out.append(prevCons ? vv[1] : vv[0]);
                    prevCons = false;
                    matched = true;
                } else if ((r = OTHER.get(sub)) != null) {
                    out.append(r);
                    prevCons = false;
                    matched = true;
                }
                if (matched) {
                    i += len;
                }
            }
            if (!matched) {
                out.append(s.charAt(i));
                prevCons = false;
                i++;
            }
        }
        return out.toString();
    }
}
