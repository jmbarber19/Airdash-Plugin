package com.sewermonk.airdash;

public class PlayerStatus {

    public boolean isOccupied = false;
    public boolean canDash = true;
    public boolean canGrapple = true;
    public boolean hasLanded = true;

    public PlayerStatus(boolean isOccupied, boolean canDash, boolean canGrapple, boolean hasLanded) {
        this.isOccupied = isOccupied;
        this.canDash = canDash;
        this.canGrapple = canGrapple;
        this.hasLanded = hasLanded;
    }

    public boolean isOccupied () {
        return isOccupied;
    }
}
