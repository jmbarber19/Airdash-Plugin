package com.sewermonk.airdash;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.RandomUtils;
import de.slikey.effectlib.util.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class DashEffect extends Effect {
    public Particle particle;
    public Color color;

    public DashEffect(EffectManager effectManager) {
        super(effectManager);
        particle = Particle.FIREWORKS_SPARK;
        color = null;
        type = EffectType.INSTANT;
        particleCount = 20;
        period = 1;
        iterations = 1;
    }

    public void onRun() {
        Location location = this.getLocation();

        for(int i = 0; i < particleCount; ++i) {
            Vector v = RandomUtils.getRandomVector().multiply(1);
            location.add(v);
            display(particle, location, 0.1f, 1);
            location.subtract(v);
        }

    }
}
