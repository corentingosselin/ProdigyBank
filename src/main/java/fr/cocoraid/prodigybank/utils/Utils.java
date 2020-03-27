package fr.cocoraid.prodigybank.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
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



    public static boolean isSmilar(ItemStack itemA, ItemStack itemB) {
        return itemA.hasItemMeta() && itemB.hasItemMeta() && itemA.getItemMeta().equals(itemB.getItemMeta());
    }

    public static ItemStack createSkull(String displayname, List<String> lores, String url) {
        ItemStack item = skullTextured(url);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayname);
        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack skullTextured( String base64) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        ItemStack newItem = Bukkit.getUnsafe().modifyItemStack(item,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}"
        );
        newItem.setItemMeta(item.getItemMeta());
        return newItem;
    }



}
