package com.sleepestapp.sleepest.util;

public class SmileySelectorUtil {
    private static final int smileyAttention = 0x26A0;
    private static final int smileyAlarmActive = 0x1F514;
    private static final int smileySleep = 0x1F634;
    private static final int smileyTime = 0x231B;
    private static final int smileyLowActivity = 0x1F949;
    private static final int smileyMediumActivity = 0x1F948;
    private static final int smileyHighActivity = 0x1F947;
    private static final int smileyIteration = 0x1F536;

    public static String getSmileyAttention() { return new String(Character.toChars(smileyAttention)); }

    public static String getSmileyAlarmActive() { return new String(Character.toChars(smileyAlarmActive)); }

    public static String getSmileyAlarmNotActive() {
        int smileyAlarmNotActive = 0x1F515;
        return new String(Character.toChars(smileyAlarmNotActive)); }

    public static String getSmileySleep() {
        return new String(Character.toChars(smileySleep));
    }

    public static String getSmileyTime() {
        return new String(Character.toChars(smileyTime));
    }

    public static String getSmileyActivity(int activity) {
        switch (activity) {
            case 0: return "";
            case 1: return new String(Character.toChars(smileyLowActivity));
            case 2: return new String(Character.toChars(smileyMediumActivity));
            case 3: return new String(Character.toChars(smileyHighActivity));
        }
        return "";
    }

    public static String getSmileyIteration() {
        return new String(Character.toChars(smileyIteration));
    }

}