package com.sewermonk.airdash;

import java.util.Queue;

public class StyleRankingStatus {
    /*
Dismal PURPLE
Crazy BLUE
Badass GREEN
Apocalyptic RED
Savage! YELLOW
Smokin Style!! WHITE
Smokin Sick Style!!! PINK
     */

    private StyleRanking currentStyle = StyleRanking.Dismal;

    private Queue<StyleAction> pastStyleActions = new Queue<>();

    public StyleRanking getCurrentStyle() {
        return currentStyle;
    }

    public void addStyle(int styleAmount) {

    }
}
