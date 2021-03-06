package com.sewermonk.airdash;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.FlameEffect;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class AirdashEvents implements Listener {
    private static AirdashController controller;
    private static Sound[] soundList = {Sound.ITEM_TRIDENT_RIPTIDE_1, Sound.ITEM_TRIDENT_RIPTIDE_2, Sound.ITEM_TRIDENT_RIPTIDE_3};
    private static Map<UUID, PlayerPositionHistory> playersPositionHistoryMap = new HashMap<>();

    private static Map<UUID, PlayerStatus> playerStatusMap = new HashMap<>();

    private static EffectManager effectManager;

    public AirdashEvents(AirdashController controller, EffectManager effectManager) {
        this.controller = controller;
        this.effectManager = effectManager;
    }


    @EventHandler
    public static void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        playerStatusMap.put(player.getUniqueId(), new PlayerStatus());
        putPlayerPositionHistory(player);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        playerDisconnectEvent(event.getPlayer());
    }

    @EventHandler
    public static void onPlayerKick(PlayerKickEvent event) {
        playerDisconnectEvent(event.getPlayer());
    }

    public static void playerDisconnectEvent(Player player) {
        playerStatusMap.remove(player.getUniqueId());
        playersPositionHistoryMap.remove(player.getUniqueId());
    }

    public static void putPlayerPositionHistory(Player player) {
        playersPositionHistoryMap.put(player.getUniqueId(), new PlayerPositionHistory(
                new Vector(
                        player.getLocation().getX(),
                        0,
                        player.getLocation().getZ()),
                LocalDateTime.now()));
    }

    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        PlayerStatus playerStatus = playerStatusMap.get(playerId);

        putPlayerPositionHistory(player);

        if (!playerStatus.hasLanded && player.isOnGround()) {
            playerStatus.hasLanded = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                @Override
                public void run() {
                    playerStatus.canDash = true;
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 2.5f);
                }
            }, 8);
        }
    }

    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        PlayerStatus playerStatus = playerStatusMap.get(playerId);

        if (
                !playerStatus.isOccupied && playerStatus.canGrapple
                        && (Action.RIGHT_CLICK_AIR == event.getAction() || Action.RIGHT_CLICK_BLOCK == event.getAction())
                        && (player.getEquipment().getItemInOffHand().getData().getItemType() == Material.LEGACY_GOLD_PICKAXE)
                        || player.getEquipment().getItemInOffHand().getData().getItemType() == Material.GOLDEN_PICKAXE
        ) {

            RayTraceResult rayTraceResult = player.getWorld().rayTrace(player.getEyeLocation(),
                    player.getLocation().getDirection(), 9, FluidCollisionMode.NEVER,
                    true, 0.5, i -> (i instanceof Mob || (i instanceof Player && i.getUniqueId() != playerId)));

            if (null != rayTraceResult) {
                Block block = rayTraceResult.getHitBlock();
                Entity hitEntity = rayTraceResult.getHitEntity();
                if (
                        (null != hitEntity && null == block) || (null != hitEntity
                                && player.getLocation().toVector().distance(block.getLocation().toVector())
                                > player.getLocation().toVector().distance(hitEntity.getLocation().toVector())
                                && player.getLocation().toVector().distance(hitEntity.getLocation().toVector()) > 3)
                ) {
                    // Mob Effects
                    if (hitEntity instanceof Player) {
                        Player mobPlayer = (Player) hitEntity;
                        mobPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 7));
                        mobPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 25, 1));
                        mobPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 25, 1));
                    } else {
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
                    }

                    // Player Effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 50));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 4, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15, 2));

                    Effect effect = new FlameEffect(effectManager);
                    effect.setDynamicOrigin(new DynamicLocation(hitEntity.getLocation()));
                    effect.iterations = 1;
                    effect.period = 1;
                    effect.duration = 1;
                    effect.particleCount = 50;
                    effect.start();

                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            player.setVelocity(new Vector(0, 0, 0));
                            playerStatus.isOccupied = false;
                        }
                    }, 3);

                    // CHAIN IN SOUNDS
                    int repeatingSoundId = Bukkit.getScheduler().scheduleSyncRepeatingTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, 0.25f, 0.6f);
                        }
                    }, 10, 8);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.getScheduler().cancelTask(repeatingSoundId);
                        }
                    }, 38);

                    // ENDING SOUND
                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            playerStatus.canGrapple = true;
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 0.8f, 0.6f);
                            player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 0.5f);
                        }
                    }, 38);
                    // DELAYED ENDING SOUND
                    Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                        @Override
                        public void run() {
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 0.5f, 1f);
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 0.2f, 1.4f);
                        }
                    }, 41);

                    // Player Move
                    Vector direction = hitEntity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    double distance = hitEntity.getLocation().toVector().distance(player.getLocation().toVector());
                    player.setVelocity(direction.multiply(distance / (-0.10 * distance + 2.9)));

                    playerStatus.isOccupied = true;
                    playerStatus.canGrapple = false;

                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.6f, 1.5f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.6f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void onExitVehicle(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            UUID id = event.getExited().getUniqueId();
            playerStatusMap.get(id).exitingVehicle = true;

            Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                @Override
                public void run() {
                    playerStatusMap.get(id).exitingVehicle = false;
                }
            }, 1);
        }
    }

    @EventHandler
    public static void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        PlayerStatus playerStatus = playerStatusMap.get(playerId);

        Material playerIn = player.getWorld().getBlockAt(player.getLocation()).getBlockData().getMaterial();

        try {
            if (
                    event.isSneaking() && !playerStatus.isOccupied && playerStatus.canDash
                            && !player.isOnGround() && !player.isInsideVehicle() && !playerStatus.exitingVehicle
                            && playerIn != Material.LADDER && playerIn != Material.LEGACY_LADDER
                            && playerIn != Material.SCAFFOLDING
                            && playerIn != Material.LEGACY_VINE && playerIn != Material.VINE
            ) {
                playerStatus.canDash = false;
                playerStatus.hasLanded = false;
                playerStatus.isOccupied = true;

                Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                    @Override
                    public void run() {
                        playerStatus.isOccupied = false;
                    }
                }, 4);

                PlayerPositionHistory prevVel = playersPositionHistoryMap.get(playerId);
                Duration duration = Duration.between(prevVel.time, LocalDateTime.now());

                Vector velocity = new Vector(0, 0, 0);
                double timeDifference = (double) duration.getSeconds() + 0.000000001 * duration.getNano();

                if (Double.isFinite(timeDifference)) {
                    velocity = new Vector(
                            (event.getPlayer().getLocation().getX() - prevVel.position.getX()) / timeDifference,
                            0,
                            (event.getPlayer().getLocation().getZ() - prevVel.position.getZ()) / timeDifference
                    );
                }

                if (Math.abs(velocity.getX()) + Math.abs(velocity.getX()) == 0) {
                    velocity = player.getLocation().getDirection().multiply(new Vector(1, 0, 1));
                }

                float speedMultiplier = 0.7f;
                if (player.isSprinting()) {
                    speedMultiplier += 0.15f;
                }
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    speedMultiplier += 0.1f + player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() * 0.1f;
                }
                if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                    speedMultiplier -= 0.1f + player.getPotionEffect(PotionEffectType.SLOW).getAmplifier() * 0.1f;
                }

                player.setVelocity(velocity.normalize().multiply(speedMultiplier).add(new Vector(0, 0.2, 0)));

                player.getWorld().playSound(player.getLocation(), soundList[(int) (Math.random() * soundList.length)],
                        0.5f,
                        1.5f * ((speedMultiplier + 0.2f)/2f + 0.5f)
                );

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 3, 50));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 8, 3));

                int damageTicks = 4;
                Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                    @Override
                    public void run() {
                        playerStatus.exceedTime = true;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, damageTicks, 1));
                    }
                }, 8);
                Bukkit.getScheduler().scheduleSyncDelayedTask(controller, new Runnable() {
                    @Override
                    public void run() {
                        playerStatus.exceedTime = false;
                    }
                }, 8 + damageTicks);

                Effect effect = new DashEffect(effectManager);
                effect.setDynamicOrigin(new DynamicLocation(event.getPlayer().getLocation()));
                effect.setDynamicOrigin(new DynamicLocation(event.getPlayer().getLocation().add(velocity.multiply(0.4))));
                effect.start();
            }
        } catch (IllegalArgumentException exception) {
            // Do nothing lol
        }
    }


    @EventHandler
    public static void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && playerStatusMap.get(event.getDamager().getUniqueId()).exceedTime) {
            Player player = (Player) event.getDamager();
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, 2f, 0.6f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2f, 0.6f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 0.8f);

            DynamicLocation location = new DynamicLocation(event.getEntity().getLocation());
            location.addOffset(new Vector(0, 0.5f, 0));
            Effect effect1 = new CritEffect(effectManager, Color.YELLOW);
            Effect effect2 = new CritEffect(effectManager, Color.ORANGE);
            Effect effect3 = new CritEffect(effectManager, Color.WHITE);
            effect1.setDynamicOrigin(location);
            effect2.setDynamicOrigin(location);
            effect3.setDynamicOrigin(location);
            effect1.start();
            effect2.start();
            effect3.start();
        }
    }

}
