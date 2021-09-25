package com.sleepestapp.sleepest.util;

public class SmileySelectorUtil {
    private final int smileyAttention = 0x26A0;
    private final int smileyAlarmActive = 0x1F514;
    private final int smileySleep = 0x1F634;
    private final int smileyTime = 0x231B;
    private final int smileySleepState = 0x1F4CA;
    private final int smileyAlarmClock = 0x23F0;
    private final int smileyHeart = 0x2764;
    private static final int smileyLowActivity = 0x1F949;
    private static final int smileyMediumActivity = 0x1F948;
    private static final int smileyHighActivity = 0x1F947;
    private static final int smileyIteration = 0x1F536;

    public String getSmileyAttention() { return new String(Character.toChars(smileyAttention)); }

    public String getSmileyAlarmActive() { return new String(Character.toChars(smileyAlarmActive)); }

    public String getSmileyAlarmNotActive() {
        int smileyAlarmNotActive = 0x1F515;
        return new String(Character.toChars(smileyAlarmNotActive)); }

    public String getSmileySleep() {
        return new String(Character.toChars(smileySleep));
    }

    public String getSmileyTime() {
        return new String(Character.toChars(smileyTime));
    }

    public String getSmileySleepState() {
        return new String(Character.toChars(smileySleepState));
    }

    public String getSmileyAlarmClock() {
        return new String(Character.toChars(smileyAlarmClock));
    }

    public String getSmileyHeart() {
        return new String(Character.toChars(smileyHeart));
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