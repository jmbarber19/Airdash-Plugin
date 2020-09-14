package com.sewermonk.airdash;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.*;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("deprecation")
public class AirdashEvents implements Listener {
    private static AirdashController controller;
    private static Sound[] soundList = { Sound.ITEM_TRIDENT_RIPTIDE_1, Sound.ITEM_TRIDENT_RIPTIDE_2, Sound.ITEM_TRIDENT_RIPTIDE_3 };
    private static Map<UUID, PlayerPositionHistory> playersPositionHistoryMap = new HashMap<>();
    private static Map<UUID, Boolean> playersCanDashMap = new HashMap<>();

    private static Map<UUID, Boolean> playerCanGrappleMap = new HashMap<>();
    private static Map<UUID, Boolean> playersIsGrapplingMap = new HashMap<>();

    private static EffectManager effectManager;

    public AirdashEvents(AirdashController controller, EffectManager effectManager) {
        this.controller = controller;
        this.effectManager = effectManager;
    }

    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        playersPositionHistoryMap.put(playerId, new PlayerPositionHistory(
                new Vector(
                        player.getLocation().getX(),
                        0,
                        player.getLocation().getZ()),
                LocalDateTime.now()));
        Material belowFeet = player.getWorld().getBlockAt(player.getLocation().add(0,-0.1,0)).getType();
        Boolean lastGrounded = playersCanDashMap.get(playerId);
        if (null == lastGrounded || !lastGrounded && player.isOnGround()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 2.5f);
            playersCanDashMap.put(playerId, true);
        }
    }

    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        playersIsGrapplingMap.putIfAbsent(player.getUniqueId(), false);
        Boolean isGrappling = playersIsGrapplingMap.get(player.getUniqueId());

        if (
                !isGrappling && (Action.RIGHT_CLICK_AIR == event.getAction() || Action.RIGHT_CLICK_BLOCK == event.getAction())
                && (player.getEquipment().getItemInOffHand().getData().getItemType() == Material.LEGACY_GOLD_PICKAXE)
                    || player.getEquipment().getItemInOffHand().getData().getItemType() == Material.GOLDEN_PICKAXE
        ) {

            RayTraceResult rayTraceResult = player.getWorld().rayTrace(player.getEyeLocation(),
                    player.getLocation().getDirection(), 9, FluidCollisionMode.NEVER,
                    true, 0.5, i -> (i instanceof Mob));

            if (null != rayTraceResult) {
                Block block = rayTraceResult.getHitBlock();
                Entity hitEntity = rayTraceResult.getHitEntity();
                if (
                        (null != hitEntity && null == block) || (null != hitEntity
                        && player.getLocation().toVector().distance(block.getLocation().toVector())
                        > player.getLocation().toVector().distance(hitEntity.getLocation().toVector())
                        && player.getLocation().toVector().distance(hitEntity.getLocation().toVector()) > 3)
                ) {
                    player.sendMessage("" + player.getLocation().toVector().distance(hitEntity.getLocation().toVector()));

                    // Mob Effects
                    Mob mob = (Mob) hitEntity;
                    mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 7));
                    mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 25, 1));
                    mob.setAware(false);
                    mob.setTarget(null);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            mob.setAware(true);
                            mob.setTarget(player);
                        }
                    }, 20);

                    // Player Effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 50));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 4, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15, 2));

                    Effect effect = new FlameEffect(effectManager);
                    effect.setDynamicOrigin(new DynamicLocation(mob.getLocation()));
                    effect.iterations = 1;
                    effect.period = 1;
                    effect.duration = 1;
                    effect.particleCount = 50;
                    effect.start();

                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            player.setVelocity(new Vector(0,0,0));
                        }
                    }, 3);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            playersIsGrapplingMap.put(player.getUniqueId(), false);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 0.5f);
                        }
                    }, 20);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0.6f);
                        }
                    }, 21);

                    // Player Move
                    Vector direction = mob.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    double distance = mob.getLocation().toVector().distance(player.getLocation().toVector());
                    player.setVelocity(direction.multiply(distance/(-0.10 * distance + 2.9)));

                    playersIsGrapplingMap.put(player.getUniqueId(), true);

                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.6f, 1.5f);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.6f);
                } else {
                    // Not hit enemy or enemy not closer than block
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f);
                }
            } else {
                // ray trace result is null
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f);
            }
        }
    }

    @EventHandler
    public static void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        playersCanDashMap.putIfAbsent(playerId, true);
        playersIsGrapplingMap.putIfAbsent(playerId, false);
        Boolean canDash = playersCanDashMap.get(playerId);
        Boolean isGrappling = playersIsGrapplingMap.get(playerId);

        Material playerIn = player.getWorld().getBlockAt(player.getLocation()).getBlockData().getMaterial();

        try {
            if (
                    event.isSneaking() && canDash && !isGrappling
                    && !player.isOnGround()
//                    && !player.isSwimming() && player.getRemainingAir() == player.getMaximumAir()
//                    && playerIn != Material.WATER && playerIn != Material.LEGACY_WATER
                    && playerIn != Material.LADDER && playerIn != Material.LEGACY_LADDER
                    && playerIn != Material.SCAFFOLDING
            ) {
                playersCanDashMap.put(playerId, false);

                PlayerPositionHistory prevVel = playersPositionHistoryMap.get(playerId);
                Duration duration = Duration.between(prevVel.time, LocalDateTime.now());

                Vector velocity = new Vector(0,0,0);
                double timeDifference = (double)duration.getSeconds() + 0.000000001 * duration.getNano();

                if (Double.isFinite(timeDifference)) {
                    velocity = new Vector(
                            (event.getPlayer().getLocation().getX() - prevVel.position.getX())/timeDifference,
                            0,
                            (event.getPlayer().getLocation().getZ() - prevVel.position.getZ())/timeDifference
                    );
                }

                if (Math.abs(velocity.getX()) + Math.abs(velocity.getX()) == 0) {
                    velocity = player.getLocation().getDirection().multiply(new Vector(1,0,1));
                }

                player.setVelocity(velocity.normalize().multiply(0.7).add(new Vector(0,0.2,0)));

                player.playSound(player.getLocation(), soundList[(int)(Math.random() * soundList.length)], 0.5f, 1.5f);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 3, 50));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2, 3));

                Effect effect = new DashEffect(effectManager);
                effect.setDynamicOrigin(new DynamicLocation(event.getPlayer().getLocation()));
                effect.setDynamicOrigin(new DynamicLocation(event.getPlayer().getLocation().add(velocity.multiply(0.4))));
                effect.start();
            }
        } catch (IllegalArgumentException exception) {
            // Do nothing lol
        }
    }



}
