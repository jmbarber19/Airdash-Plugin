package com.sewermonk.airdash;

public enum StyleRanking {
    NoStyle (0),
    Dismal (10),
    Crazy (20),
    Badass (30),
    Apocalyptic (40),
    Savage (50),
    SmokinStyle (70),
    SmokinSickStyle (80),
    Cap (90);

    private final double styleValue;

    StyleRanking(double skillLevel) {
        this.styleValue = skillLevel;
    }

    public double getStyleValue() {
        return styleValue;
    }

    public StyleRanking getStyle() {
        if (styleValue > SmokinStyle.styleValue) {
            return SmokinSickStyle;
        } else if (styleValue > Savage.styleValue) {
            return SmokinStyle;
        } else if (styleValue > Apocalyptic.styleValue) {
            return Savage;
        } else if (styleValue > Badass.styleValue) {
            return Apocalyptic;
        } else if (styleValue > Crazy.styleValue) {
            return Badass;
        } else if (styleValue > Dismal.styleValue) {
            return Crazy;
        } else if (styleValue > NoStyle.styleValue) {
            return Dismal;
        } else {
            return NoStyle;
        }
    }

    public double getStyleValueRemainder() {
        if (styleValue > SmokinSickStyle.styleValue) {
            return styleValue - SmokinSickStyle.styleValue;
        } else if (styleValue > SmokinStyle.styleValue) {
            return styleValue - SmokinStyle.styleValue;
        } else if (styleValue > Savage.styleValue) {
            return styleValue - Savage.styleValue;
        } else if (styleValue > Apocalyptic.styleValue) {
            return styleValue - Apocalyptic.styleValue;
        } else if (styleValue > Badass.styleValue) {
            return styleValue - Badass.styleValue;
        } else if (styleValue > Crazy.styleValue) {
            return styleValue - Crazy.styleValue;
        } else if (styleValue > Dismal.styleValue) {
            return styleValue - Dismal.styleValue;
        } else {
            return styleValue;
        }
    }

    public double getStyleRankValue() {
        if (styleValue > SmokinSickStyle.styleValue) {
            return SmokinSickStyle.styleValue - SmokinStyle.styleValue;
        } else if (styleValue > SmokinStyle.styleValue) {
            return SmokinStyle.styleValue - Savage.styleValue;
        } else if (styleValue > Savage.styleValue) {
            return Savage.styleValue - Savage.styleValue;
        } else if (styleValue > Apocalyptic.styleValue) {
            return Apocalyptic.styleValue - Apocalyptic.styleValue;
        } else if (styleValue > Badass.styleValue) {
            return Badass.styleValue - Badass.styleValue;
        } else if (styleValue > Crazy.styleValue) {
            return Crazy.styleValue - Crazy.styleValue;
        } else if (styleValue > Dismal.styleValue) {
            return Dismal.styleValue - Dismal.styleValue;
        } else {
            return NoStyle.styleValue;
        }
    }
}
