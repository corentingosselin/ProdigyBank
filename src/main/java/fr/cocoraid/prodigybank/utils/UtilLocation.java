package fr.cocoraid.prodigybank.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by cocoraid on 02/07/2016.
 */
public class UtilLocation {


    /**
     * @param location
     * @param distance
     * @return
     */
    public static List<Player> getClosestPlayersFromLocation(Location location, double distance) {
        List<Player> result = new ArrayList<Player>();
        double d2 = distance * distance;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().add(0, 0.85D, 0).distanceSquared(location) <= d2) {
                if (!player.hasMetadata("NPC"))
                    result.add(player);
            }
        }
        return result;
    }

    public static Entity[] getNearbyEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk().getEntities()) {
                    if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock() != l.getBlock())
                        radiusEntities.add(e);
                }
            }
        }
        return radiusEntities.toArray(new Entity[radiusEntities.size()]);
    }

}
