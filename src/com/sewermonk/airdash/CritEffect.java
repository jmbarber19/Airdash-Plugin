package com.sewermonk.airdash;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class CritEffect extends Effect {
    public Particle particle = Particle.REDSTONE;
    public Color color = Color.RED;

    public CritEffect(EffectManager effectManager, Color color) {
        super(effectManager);
        this.color = color;
        type = EffectType.INSTANT;
        particleCount = 15;
        period = 1;
        iterations = 1;
    }

    public void onRun() {
        Location location = this.getLocation();

        for(int i = 0; i < particleCount; ++i) {
            Vector v = RandomUtils.getRandomVector().multiply(1);
            location.add(v);
            display(particle, location, color, 0.1f, 1);
            location.subtract(v);
        }

    }
}
