package fr.cocoraid.prodigybank.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(message));
    }

    public static void sendActionBar(List<Player> list, String message) {
        list.forEach(cur -> {
            cur.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(message));
        });

    }

    public static double getRandom(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);

    }


    public static double getRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);

    }

    public static void sendTitle(Player cur, String msg) {
        String[] message = msg.split(":");
        if(message.length > 1)
            cur.sendTitle(message[0],message[1],20 * 5,0,1);
        else
            cur.sendTitle("",message[0],20 * 5,0,1);
    }


    public static Vector getBumpVector(final Entity entity, final Location from, final double power) {
        final Vector bump = entity.getLocation().toVector().subtract(from.toVector()).normalize();
        if (Double.isNaN(bump.getX())) {
            bump.setX(0);
        }
        if (Double.isNaN(bump.getZ())) {
            bump.setZ(0);
        }
        if (Double.isNaN(bump.getY())) {
            bump.setY(0);
        }
        bump.multiply(power);
        return bump;
    }

    public static Vector getPullVector(final Entity entity, final Location to, final double power) {
        final Vector pull = to.toVector().subtract(entity.getLocation().toVector()).normalize();
        if (Double.isNaN(pull.getX())) {
            pull.setX(0);
        }
        if (Double.isNaN(pull.getZ())) {
            pull.setZ(0);
        }
        if (Double.isNaN(pull.getY())) {
            pull.setY(0);
        }
        pull.multiply(power);
        return pull;
    }

    public static void bumpEntity(final Entity entity, final Location from, final double power) {
        entity.setVelocity(getBumpVector(entity, from, power));
    }

    public static void bumpEntity(final Entity entity, final Location from, final double power, final double fixedY) {
        if (entity instanceof Player && entity.hasMetadata("NPC")) {
            return;
        }
        final Vector vector = getBumpVector(entity, from, power);
        vector.setY(fixedY);
        entity.setVelocity(vector);
    }

    public static void pullEntity(final Entity entity, final Location to, final double power) {
        entity.setVelocity(getPullVector(entity, to, power));
    }

    public static void pullEntity(final Entity entity, final Location from, final double power, final double fixedY) {
        final Vector vector = getPullVector(entity, from, power);
        vector.setY(fixedY);
        entity.setVelocity(vector);
    }

    public static byte toPackedByte(float f) {
        return (byte) ((int) (f * 256.0F / 360.0F));
    }

}
