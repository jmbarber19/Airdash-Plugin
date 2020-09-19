package com.sewermonk.airdash;

public enum StyleAction {
    SwordHit (1),
    AxeHit (10),
    ArrowHit (20),
    FriendlyFired (30),
    ItemSwap (0),
    MissAttack (-10),
    Hit (-20),
    SmokinSickStyle (90);

    private final int styleValue;

    StyleAction(int styleValue) {
        this.styleValue = styleValue;
    }

    public int getStyleValue() {
        return styleValue;
    }

}
