package com.doitstudio.sleepest_master.util;

public class SmileySelectorUtil {
    private final int smileyAttention = 0x26A0;
    private final int smileyAlarmActive = 0x1F514;
    private final int smileyAlarmNotActive = 0x1F515;
    private final int smileySleep = 0x1F634;
    private final int smileyTime = 0x231B;
    private final int smileySleepState = 0x1F4CA;
    private final int smileyAlarmClock = 0x23F0;

    public String getSmileyAttention() { return new String(Character.toChars(smileyAttention)); }

    public String getSmileyAlarmActive() { return new String(Character.toChars(smileyAlarmActive)); }

    public String getSmileyAlarmNotActive() { return new String(Character.toChars(smileyAlarmNotActive)); }

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
}