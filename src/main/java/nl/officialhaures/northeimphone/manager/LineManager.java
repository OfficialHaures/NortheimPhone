package nl.officialhaures.northeimphone.manager;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


public class LineManager {
    private static final double PARTICLE_STEP = 0.5;
    private static final Particle PARTICLE_TYPE = Particle.REDSTONE;
    private static final Particle.DustOptions DUST_OPTIONS_RED = new Particle.DustOptions(Color.RED, 1.0f);
    private static final Particle.DustOptions DUST_OPTIONS_GREEN = new Particle.DustOptions(Color.GREEN, 1.0f);
    private static final double HEIGHT_OFFSET = 2.5;
    public static Map<Player, Player> playerLines = new HashMap<>();


    public static void drawLine(Player sender, Player target) {
        Location senderLoc = sender.getLocation().clone().add(0, HEIGHT_OFFSET, 0);
        Location targetLoc = target.getLocation().clone().add(0, HEIGHT_OFFSET, 0);

        Vector direction = targetLoc.toVector().subtract(senderLoc.toVector());
        double length = direction.length();
        direction.normalize();

        double traveled = 0;
        boolean reachedTarget = false;
        while (traveled < length) {
            Location particleLoc = senderLoc.clone().add(direction.clone().multiply(traveled));

            Block block = particleLoc.getBlock();
            if (block.getType().isSolid()) {
                direction = direction.multiply(-1); // Keer de richting om
            }

            if (particleLoc.distanceSquared(targetLoc) < 1) {
                reachedTarget = true;
                drawReturnLine(sender, target, targetLoc, senderLoc);
                break;
            }

            sender.getWorld().spawnParticle(PARTICLE_TYPE, particleLoc, 0, DUST_OPTIONS_RED);
            traveled += PARTICLE_STEP;
        }
    }

    private static void drawReturnLine(Player sender, Player target, Location startLoc, Location endLoc) {
        Vector direction = endLoc.toVector().subtract(startLoc.toVector());
        double length = direction.length();
        direction.normalize();

        double traveled = 0;
        while (traveled < length) {
            Location particleLoc = startLoc.clone().add(direction.clone().multiply(traveled));
            sender.getWorld().spawnParticle(PARTICLE_TYPE, particleLoc, 0, DUST_OPTIONS_GREEN);
            traveled += PARTICLE_STEP;

        }
    }

    private static void removeLine(Player player) {
        Player target = null;
        for (Map.Entry<Player, Player> entry : playerLines.entrySet()) {
            if (entry.getKey().equals(player) || entry.getValue().equals(player)) {
                target = entry.getKey().equals(player) ? entry.getValue() : entry.getKey();
                playerLines.remove(entry.getKey(), entry.getValue());
                break;
            }
        }

        if (target != null && player.hasLineOfSight(target)) {
            removeLine(target);
        }
    }
}