package fr.cocoraid.prodigybank.nms;

import fr.cocoraid.prodigybank.utils.CC;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cocoraid on 30/06/2016.
 */
public class ReflectedArmorStand {


    /*
     * Class
     */
    private static Class<?> craftItemStackClass = Reflection.getClass("{obc}.inventory.CraftItemStack");
    private static Class<?> packetEquipClass = Reflection.getClass("{nms}.PacketPlayOutEntityEquipment");
    private static Class<?> packetTeleportClass = Reflection.getMinecraftClass("PacketPlayOutEntityTeleport");
    private static Class<?> packetRotationClass = Reflection.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
    private static Class<?> nmsWorldField = Reflection.getClass("{nms}.World");
    private static Class<?> packetSpawnEntityLivingClass = Reflection.getClass("{nms}.PacketPlayOutSpawnEntityLiving");
    private static Class<?> entityArmorStandClass = Reflection.getClass("{nms}.EntityArmorStand");
    private static Class<?> entityClass = Reflection.getClass("{nms}.Entity");
    private static Class<?> entityLivingClass = Reflection.getClass("{nms}.EntityLiving");
    private static Class<?> itemStackClass = Reflection.getMinecraftClass("ItemStack");
    private static Class<?> enumItemSlotClass = Reflection.getMinecraftClass("EnumItemSlot");
    private static Class<?> vector3fClass = Reflection.getMinecraftClass("Vector3f");
    private static Class<?> datawatcherClass = Reflection.getMinecraftClass("DataWatcher");
    private static Class<?> nbtTagCompoundClass = Reflection.getMinecraftClass("NBTTagCompound");
    /**
     * Method
     */
    //Getter
    private static Reflection.MethodInvoker getIdMethod = Reflection.getMethod(entityClass, "getId");
    private static Reflection.MethodInvoker asNMSCopyMethod = Reflection.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static Reflection.MethodInvoker getHandleWorldMethod = Reflection.getMethod("{obc}.CraftWorld", "getHandle");
    //Setter
    private static Reflection.MethodInvoker setPositionMethod = Reflection.getMethod(entityClass, "setLocation", double.class, double.class, double.class, float.class, float.class);
    private static Reflection.MethodInvoker setCustomeNameVisibleMethod = Reflection.getMethod(entityArmorStandClass, "setCustomNameVisible", boolean.class);
    private static Reflection.MethodInvoker setInvisibleMethod = Reflection.getMethod(entityArmorStandClass, "setInvisible", boolean.class);
    private static Reflection.MethodInvoker setSmallMethod = Reflection.getMethod(entityArmorStandClass, "setSmall", boolean.class);
    private static Reflection.MethodInvoker setHeadPoseMethod = Reflection.getMethod(entityArmorStandClass, "setHeadPose", vector3fClass);
    private static Reflection.MethodInvoker setRightArmPosePoseMethod = Reflection.getMethod(entityArmorStandClass, "setRightArmPose", vector3fClass);
    private static Reflection.MethodInvoker setArmsMethod = Reflection.getMethod(entityArmorStandClass, "setArms", boolean.class);
    private static Reflection.MethodInvoker setBasePlateMethod = Reflection.getMethod(entityArmorStandClass, "setBasePlate", boolean.class);
    private static Reflection.MethodInvoker setMarkerMethod = Reflection.getMethod(entityArmorStandClass, "setMarker", boolean.class);
    private static Reflection.MethodInvoker getBukkitEntityMethod = Reflection.getMethod(entityClass, "getBukkitEntity");
    private static Reflection.MethodInvoker setEntityNBTTag = Reflection.getMethod(entityClass, "f", nbtTagCompoundClass);
    private static Reflection.MethodInvoker setArmorStandNBTTag = Reflection.getMethod(entityArmorStandClass, "a", nbtTagCompoundClass);
    private static Reflection.MethodInvoker getEquipmentMethod = Reflection.getMethod(entityArmorStandClass, "getEquipment", enumItemSlotClass);

    private static Reflection.MethodInvoker setCustomeNameMethod;
    private static Reflection.MethodInvoker chatSerializerMethod;
    private static Class<?> chatbasecomponent;

    static {
        if(VersionChecker.isHigherOrEqualThan(VersionChecker.v1_13_R1)) {
            chatbasecomponent = Reflection.getMinecraftClass("IChatBaseComponent");
            setCustomeNameMethod = Reflection.getMethod(entityArmorStandClass, "setCustomName",chatbasecomponent);
            chatSerializerMethod = Reflection.getMethod(Reflection.getMinecraftClass("IChatBaseComponent$ChatSerializer"),"a",String.class);
        } else {
            setCustomeNameMethod = Reflection.getMethod(entityArmorStandClass, "setCustomName", String.class);
        }
    }
    /*
     * Constructor
     */
    private static Reflection.ConstructorInvoker packetmetaConstructor = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutEntityMetadata"), int.class, datawatcherClass, boolean.class);
    private static Reflection.ConstructorInvoker armorStandConstructor = Reflection.getConstructor(entityArmorStandClass, nmsWorldField, double.class, double.class, double.class);



    private static Reflection.ConstructorInvoker packetEquipConstructor = Reflection.getConstructor(packetEquipClass);
    private static Reflection.ConstructorInvoker packetSpawnConstructor = Reflection.getConstructor(packetSpawnEntityLivingClass, entityLivingClass);
    private static Reflection.ConstructorInvoker packetRotationContrustuctor = Reflection.getConstructor(packetRotationClass, int.class, byte.class, byte.class, boolean.class);
    private static Reflection.ConstructorInvoker packetTeleportContrustuctor = Reflection.getConstructor(packetTeleportClass, entityClass);
    private static Reflection.ConstructorInvoker vector3fConstructor = Reflection.getConstructor(vector3fClass, float.class, float.class, float.class);
    /**
     * Field
     */
    private static Reflection.FieldAccessor<?> IDPacketEquipField = Reflection.getField(packetEquipClass, int.class, 0);
    private static Reflection.FieldAccessor<?> enumItemSlotPacketEquipField = Reflection.getField(packetEquipClass, enumItemSlotClass, 0);
    private static Reflection.FieldAccessor<?> itemStackPacketEquipField = Reflection.getField(packetEquipClass, itemStackClass, 0);
    private static Reflection.FieldAccessor<?> datawatcherField = Reflection.getField(entityClass, "datawatcher", datawatcherClass);
    /**
     * Packet
     */
    private Object packetSpawn;
    private Entity bukkitEntity;
    private List<Object> packetEquip = new ArrayList<>();
    private Object packetRotation;

    private List<Object> packets;


    private int ID;
    private Location location;
    private String displayName = "";
    private boolean visible = true;
    private boolean hide = false;
    private boolean small = false;
    private boolean arms = false;
    private boolean marker = false;
    private boolean basePlate = false;
    private float yaw = 0;
    private float pitch = 0;


    /**
     * Object
     */
    private Object armorStand;

    public ReflectedArmorStand(Location location) {
        this.location = location;
        this.armorStand = armorStandConstructor.invoke(getHandleWorldMethod.invoke(location.getWorld()), location.getX(),location.getY(),location.getZ());
        setPositionMethod.invoke(armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();

        packetSpawn = packetSpawnConstructor.invoke(armorStand);
        this.ID = (int) getIdMethod.invoke(armorStand);

        this.bukkitEntity = (Entity) getBukkitEntityMethod.invoke(armorStand);

    }

    public void setCustomNameVisible(boolean visible) {
        setCustomeNameVisibleMethod.invoke(armorStand, visible);
        updateMetadata();
    }


    private void setCustomName(String name) {
        if(VersionChecker.isHigherOrEqualThan(VersionChecker.v1_13_R1)) {
            Object chat = chatSerializerMethod.invoke(chatbasecomponent,"{\"text\":\"" + CC.colored(name)  + "\"}");
            setCustomeNameMethod.invoke(armorStand,chat);
        } else {
            setCustomeNameMethod.invoke(armorStand, CC.colored(name));
        }
    }

    public void updateName(String name) {
        setCustomName(name);
        Object meta = packetmetaConstructor.invoke(ID, datawatcherField.get(armorStand), true);
        NMSPlayer.sendPacket(location.getWorld(),meta);

    }

    public void updateName(String name, Player player) {
        setCustomName(name);
        Object meta = packetmetaConstructor.invoke(ID, datawatcherField.get(armorStand), true);
        NMSPlayer.sendPacket(player, meta);

    }



    public ReflectedArmorStand setBasePlate(boolean base) {
        setBasePlateMethod.invoke(armorStand, base);
        this.basePlate = basePlate;
        return this;
    }

    public ReflectedArmorStand setHeadPose(EulerAngle ea) {
        setHeadPoseMethod.invoke(armorStand, vector3fConstructor.invoke((float) ea.getX(), (float) ea.getY(), (float) ea.getZ()));
        return this;
    }

    public ReflectedArmorStand setRightArmPose(EulerAngle ea) {
        setRightArmPosePoseMethod.invoke(armorStand, vector3fConstructor.invoke((float) ea.getX(), (float) ea.getY(), (float) ea.getZ()));
        return this;
    }

    public ReflectedArmorStand setRotation(float yaw, float pitch) {
        packetRotation = packetRotationContrustuctor.invoke(ID, Utils.toPackedByte(yaw),
                Utils.toPackedByte(pitch), false);
        this.yaw = yaw;
        this.pitch = pitch;
        NMSPlayer.sendPacket(location.getWorld(), packetRotation);

        return this;
    }

    public ReflectedArmorStand setRotation(Player p, float yaw, float pitch) {
        packetRotation = packetRotationContrustuctor.invoke(ID, Utils.toPackedByte(yaw),
                Utils.toPackedByte(pitch), false);
        this.yaw = yaw;
        this.pitch = pitch;
        NMSPlayer.sendPacket(p, packetRotation);
        return this;
    }

    public void updateMetadata() {
        Object meta = packetmetaConstructor.invoke(ID, datawatcherField.get(armorStand), true);
        NMSPlayer.sendPacket(location.getWorld(),meta);

    }

    private ItemStack head;
    /*On 1.9: 0 = Main Hand ; 1 = Off Hand ; 2 = Boots ; 3 = Leggings ; 4 = Chest ; 5 = Helmet */
    public ReflectedArmorStand setEquipment(int slot, ItemStack item) {
        if(slot == 5)
            head = item;
        Object packet = packetEquipConstructor.invoke();
        IDPacketEquipField.set(packet, ID);
        enumItemSlotPacketEquipField.set(packet, enumItemSlotClass.getEnumConstants()[slot]);
        itemStackPacketEquipField.set(packet, asNMSCopyMethod.invoke(craftItemStackClass, item));
        packetEquip.add(packet);
        return this;
    }

    public ReflectedArmorStand setMarker(boolean marker) {
        setMarkerMethod.invoke(armorStand, marker);
        this.marker = marker;
        return this;
    }

    public ReflectedArmorStand setArms(boolean arms) {
        setArmsMethod.invoke(armorStand, arms);
        this.arms = arms;
        return this;
    }

    /**
     * Getters
     */
    public int getID() {
        return ID;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Setters
     */
    public ReflectedArmorStand setVisible(boolean visible) {
        setInvisibleMethod.invoke(armorStand, !visible);
        this.visible = visible;
        return this;
    }

    public boolean isSmall() {
        return small;
    }

    public ReflectedArmorStand setSmall(boolean small) {
        setSmallMethod.invoke(armorStand, small);
        this.small = small;
        return this;
    }

    /**
     * Main methods
     */
    public void teleport(Player player, Location newLocation) {
        setPositionMethod.invoke(armorStand, newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch);
        Object packetTeleport = packetTeleportContrustuctor.invoke(armorStand);
        NMSPlayer.sendPacket(player, packetTeleport);
        this.location = newLocation;
    }

    public void faketeleport(Player player, Location newLocation) {
        setPositionMethod.invoke(armorStand, newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch);
        Object packetTeleport = packetTeleportContrustuctor.invoke(armorStand);
        NMSPlayer.sendPacket(player, packetTeleport);
    }

    public void faketeleport(Location newLocation) {
        setPositionMethod.invoke(armorStand, newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch);
        Object packetTeleport = packetTeleportContrustuctor.invoke(armorStand);
        NMSPlayer.sendPacket(newLocation.getWorld(), packetTeleport);
    }

    public void teleport(Location newLocation) {
        setPositionMethod.invoke(armorStand, newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch);
        Object packetTeleport = packetTeleportContrustuctor.invoke(armorStand);
        NMSPlayer.sendPacket(location.getWorld(),packetTeleport);
        this.location = newLocation;
    }

    public void hide() {
        NMS.destroyEntity(location.getWorld(),ID);
        this.hide = true;
    }

    public void hide(Player p) {
        NMS.destroyEntity(p, ID);
        hide = true;
    }

    public void show(Player p) {
        NMSPlayer.sendPacket(p, packetSpawn, packetRotation);
        packetEquip.forEach(equip -> NMSPlayer.sendPacket(p, equip));
        hide = false;
    }

    public void show() {
        NMSPlayer.sendPacket(location.getWorld(),packetSpawn, packetRotation);
        packetEquip.forEach(equip -> NMSPlayer.sendPacket(location.getWorld(),equip));
        hide = false;
    }

    public void remove() {
        NMS.destroyEntity(location.getWorld(),ID);
    }

    public ReflectedArmorStand spawnArmorStand() {
        NMSPlayer.sendPacketNearby(location, packetSpawn, packetRotation);

        packetEquip.forEach(equip -> NMSPlayer.sendPacketNearby(location, equip));
        return this;
    }


    public ReflectedArmorStand spawnArmorStand(Player player) {
        NMSPlayer.sendPacket((Player) player, packetSpawn, packetRotation);
        packetEquip.forEach(equip -> NMSPlayer.sendPacket(location.getWorld(),equip));
        return this;
    }

    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    /**
     * Util convert
     *
     * @return f
     */


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void resetLocation(Location location) {
        setPositionMethod.invoke(armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        packetSpawn = packetSpawnConstructor.invoke(armorStand);
        this.location = location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getHead() {
        return this.head;
    }

    public ReflectedArmorStand setDisplayName(String displayName) {
        setCustomName(displayName);
        setCustomeNameVisibleMethod.invoke(armorStand, true);
        this.displayName = displayName;
        return this;
    }

    public float getYaw() {
        return yaw;
    }


}
