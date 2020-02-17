package fr.cocoraid.prodigybank.nms;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Created by cocoraid on 02/07/2016.
 */
public class NMS {

    private static Class<?> packetDestroyClass = Reflection.getMinecraftClass("PacketPlayOutEntityDestroy");
    private static Reflection.ConstructorInvoker packetDestroyConstructor = Reflection.getConstructor(packetDestroyClass);
    private static Reflection.FieldAccessor<?> packetDestroyIDField = Reflection.getField(packetDestroyClass, Object.class, 0);

    private static Reflection.ConstructorInvoker blockpositionContrustuctor = Reflection.getConstructor(Reflection.getMinecraftClass("BlockPosition"), int.class, int.class, int.class);
    private static Reflection.ConstructorInvoker blockactionContrustuctor = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutBlockAction"), Reflection.getMinecraftClass("BlockPosition"), Reflection.getMinecraftClass("Block"), int.class, int.class);

    public static void setChestOpen(Block b, boolean open) {
        Object c = blockpositionContrustuctor.invoke(b.getX(), b.getY(), b.getZ());
        Object p = blockactionContrustuctor.invoke(c, Reflection.getField(Reflection.getMinecraftClass("Blocks"), "ENDER_CHEST", Reflection.getMinecraftClass("Block")).get(Reflection.getMinecraftClass("Blocks")), 1, open ? 1 : 0);
        NMSPlayer.sendPacketNearby(b.getLocation(), p);
    }


    public static void destroyEntity(World w, int... id) {
        Object packet = packetDestroyConstructor.invoke();
        packetDestroyIDField.set(packet, id);
        NMSPlayer.sendPacket(w, packet);
    }

    public static void destroyEntity(Player player, int... id) {
        Object packet = packetDestroyConstructor.invoke();
        packetDestroyIDField.set(packet, id);
        NMSPlayer.sendPacket(player, packet);
    }









}
