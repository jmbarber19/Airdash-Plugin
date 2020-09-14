package com.sewermonk.airdash;

import org.bukkit.util.Vector;

import java.time.LocalDateTime;

public class PlayerPositionHistory {
    public Vector position;
    public LocalDateTime time;
    public PlayerPositionHistory(Vector position, LocalDateTime time) {
        this.position = position;
        this.time = time;
    }
}
