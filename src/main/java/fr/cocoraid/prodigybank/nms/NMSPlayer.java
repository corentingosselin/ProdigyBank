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




    public static Object getHandle(Player p) {
        return getHandlePlayerMethod.invoke(p);
    }


}
