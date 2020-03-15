package fr.cocoraid.prodigybank.nms;

import fr.cocoraid.prodigybank.utils.UtilLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by cocoraid on 01/07/2016.
 */
public class NMSPlayer {


    private static Reflection.FieldAccessor<?> playerConnectionField = Reflection.getField("{nms}.EntityPlayer", "playerConnection", Object.class);
    private static Reflection.FieldAccessor<?> lastX = Reflection.getField("{nms}.EntityPlayer", "lastX", double.class);
    private static Reflection.FieldAccessor<?> lastY = Reflection.getField("{nms}.EntityPlayer", "lastY", double.class);
    private static Reflection.FieldAccessor<?> lastZ = Reflection.getField("{nms}.EntityPlayer", "lastZ", double.class);
    private static Reflection.FieldAccessor<?> lastYaw = Reflection.getField("{nms}.EntityPlayer", "lastYaw", float.class);
    private static Reflection.FieldAccessor<?> lastPitch = Reflection.getField("{nms}.EntityPlayer", "lastPitch", float.class);

    private static Reflection.MethodInvoker getHandlePlayerMethod = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static Reflection.MethodInvoker getHandleMethod = Reflection.getMethod("{obc}.entity.CraftEntity", "getHandle");
    private static Reflection.MethodInvoker sendPacket = Reflection.getMethod("{nms}.PlayerConnection", "sendPacket", Reflection.getMinecraftClass("Packet"));



    /**
     * Send packets
     */
    public static void sendPacket(Player player, Object... packets) {
        for (Object packet : packets) {
            if (packet != null)
                sendPacket.invoke(playerConnectionField.get(getHandleMethod.invoke(player)), packet);
        }
    }


    public static void sendPacket(World w, Object... packets) {
        for (Object packet : packets) {
            if (packet != null) {
                Bukkit.getOnlinePlayers().stream().filter(cur -> cur.getWorld().equals(w)).forEach(player -> {
                    sendPacket.invoke(playerConnectionField.get(getHandleMethod.invoke(player)), packet);
                });
            }
        }
    }


    public static void sendPacketNearby(Location from, Object... packets) {
        UtilLocation.getClosestPlayersFromLocation(from, 64).forEach(viewers -> {
            sendPacket(viewers,packets);
        });
    }

    public static Location getLastLocation(Player player) {
        Object craftPlayer = getHandle(player);
        double x = Double.valueOf(lastX.get(craftPlayer).toString()).doubleValue();
        double y = Double.valueOf(lastY.get(craftPlayer).toString()).doubleValue();
        double z = Double.valueOf(lastZ.get(craftPlayer).toString()).doubleValue();
        float yaw = Double.valueOf(lastYaw.get(craftPlayer).toString()).floatValue();
        float pitch = Float.valueOf(lastPitch.get(craftPlayer).toString()).floatValue();
        return new Location(player.getWorld(),x, y,z,yaw,pitch);

    }




    public static Object getHandle(Player p) {
        return getHandlePlayerMethod.invoke(p);
    }


}
