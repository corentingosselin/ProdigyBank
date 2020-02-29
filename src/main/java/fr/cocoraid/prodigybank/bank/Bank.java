package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.staff.authority.PoliceStaff;
import fr.cocoraid.prodigybank.bank.staff.authority.SwatTeam;
import fr.cocoraid.prodigybank.bank.staff.member.BankerStaff;
import fr.cocoraid.prodigybank.bank.staff.member.HostessStaff;
import fr.cocoraid.prodigybank.bridge.EconomyBridge;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.nms.NMS;
import fr.cocoraid.prodigybank.setupbank.Cuboid;
import fr.cocoraid.prodigybank.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.*;

public class Bank {

    private static Map<Bank, HoldUp> holdups = new HashMap<>();


    private ProdigyBank instance = ProdigyBank.getInstance();
    private Language lang = instance.getLanguage();
    private ConfigLoader config = instance.getConfigLoader();
    private BankLoader bankLoader = instance.getBankLoader();

    private World world;


    private SwatTeam swatTeam;
    private PoliceStaff policeStaff;
    private BankerStaff bankerStaff;
    private HostessStaff hostessStaff;

    //REQUIRED
    private Cuboid bankCuboid;
    private Cuboid vaultCuboid;
    private VaultDoor vaultDoor;

    private Location jail;
    private List<SafeDeposit> chests;

    //OPTIONAL
    private List<Cuboid> doors_to_lock = new ArrayList<>();

    public Bank(Cuboid bankArea,
                Cuboid vaultArea,
                Cuboid vaultDoor,
                NPC depositHostess,
                NPC withdrawHostess,
                NPC banker,
                Location jail,
                List<SafeDeposit> chests) {
        holdups.putIfAbsent(this, new HoldUp(this));
        this.bankCuboid = bankArea;
        this.vaultCuboid = vaultArea;
        this.vaultDoor = new VaultDoor(vaultDoor);
        this.hostessStaff = new HostessStaff(this,depositHostess,withdrawHostess);
        this.bankerStaff = new BankerStaff(this, banker);
        this.policeStaff = new PoliceStaff(this);
        this.swatTeam = new SwatTeam(this);
        this.jail = jail;

        this.chests = chests;
        this.world = bankCuboid.getPoint1().getWorld();
    }



    public void addDoorToLockRegion(Player admin) {
        Cuboid cuboid = Cuboid.setupCuboid(admin);
        if(cuboid == null) {
            return;
        }

        if(!Cuboid.checkForAir(cuboid,admin)) return;

        doors_to_lock.add(cuboid);
        admin.sendMessage(
                new StringBuilder(lang.object_added).toString()
                        .replace("%id",String.valueOf(doors_to_lock.size() - 1))
                        .replace("%type", "door to lock")
        );

        bankLoader.saveDoors(true);
    }


    public Cuboid getBankCuboid() {
        return bankCuboid;
    }

    public Cuboid getVaultCuboid() {
        return vaultCuboid;
    }

    public VaultDoor getVaultDoor() {
        return vaultDoor;
    }

    public List<SafeDeposit> getChests() {
        return chests;
    }

    public List<Cuboid> getDoors_to_lock() {
        return doors_to_lock;
    }


    public Location getJail() {
        return jail;
    }



    public HoldUp getHoldUp() {
        return holdups.get(this);
    }

    public boolean isStaffMember(NPC npc) {
        return getPoliceStaff().getPolice().contains(npc)
                || getHostessStaff().isHostessMember(npc)
                || getBankerStaff().isBanker(npc);
    }

    public boolean isNonViolentStaffMember(NPC npc) {
        return getHostessStaff().isHostessMember(npc)
                || getBankerStaff().isBanker(npc);
    }






    public void setDoors_to_lock(List<Cuboid> doors_to_lock) {
        this.doors_to_lock = doors_to_lock;
    }



    public void setChests(List<SafeDeposit> chests) {
        this.chests = chests;
    }

    public World getWorld() {
        return world;
    }


    public PoliceStaff getPoliceStaff() {
        return policeStaff;
    }

    public BankerStaff getBankerStaff() {
        return bankerStaff;
    }

    public HostessStaff getHostessStaff() {
        return hostessStaff;
    }


    public SwatTeam getSwatTeam() {
        return swatTeam;
    }


    public static Map<Bank, HoldUp> getHoldups() {
        return holdups;
    }
}
