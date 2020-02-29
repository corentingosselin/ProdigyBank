package fr.cocoraid.prodigybank.filemanager;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.SafeDeposit;
import fr.cocoraid.prodigybank.setupbank.Cuboid;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BankLoader {

    private NPCRegistry registry = CitizensAPI.getNPCRegistry();
    private File file;
    private FileConfiguration data;
    private final String fileName = "bank.yml";
    private ProdigyBank instance;

    public BankLoader(ProdigyBank plugin) {
        this.instance = plugin;
        file = new File(plugin.getDataFolder(), fileName);
        if(file.exists())
            data = YamlConfiguration.loadConfiguration(file);
    }


    //cuboids
    private static final String BANK_POINT1 = "bank.point1";
    private static final String BANK_POINT2 = "bank.point2";
    private static final String VAULT_POINT1 = "vault.point1";
    private static final String VAULT_POINT2 = "vault.point2";
    private static final String VAULT_DOOR_POINT1 = "vault-door.point1";
    private static final String VAULT_DOOR_POINT2 = "vault-door.point2";

    private static final String JAIL = "jail";

    //chests
    private static final String CHESTS = "chests";

    private static final String DOORS = "doors";

    //Staff
    private static final String BANKER = "staff.banker.ID";
    private static final String BANKER_LOC = "staff.banker.Pos";
    private static final String DEPOSIT = "staff.deposit-hostess.ID";
    private static final String DEPOSIT_LOC = "staff.deposit-hostess.Pos";
    private static final String WITHDRAW = "staff.withdraw-hostess.ID";
    private static final String WITHDRAW_LOC = "staff.withdraw-hostess.Pos";

    private static final String POLICE = "staff.police";

    private static final String SWAT_POINTS = "staff.swat-points";

    public void load() {
        if(!file.exists()) return;
        Cuboid bankArea = new Cuboid(data.getLocation(BANK_POINT1), data.getLocation(BANK_POINT2));
        Cuboid vaultArea = new Cuboid(data.getLocation(VAULT_POINT1), data.getLocation(VAULT_POINT2));
        Cuboid vaultDoor = new Cuboid(data.getLocation(VAULT_DOOR_POINT1), data.getLocation(VAULT_DOOR_POINT2));

        Location jail = data.getLocation(JAIL);
        List<Location> locations = (List<Location>) data.getList(CHESTS);
        List<SafeDeposit> chests = new ArrayList<>();
        locations.forEach(l -> {
            if(l.getBlock().getType() != Material.ENDER_CHEST) {
                l.getBlock().setType(Material.ENDER_CHEST);
            }
            chests.add(new SafeDeposit(l.getBlock()));
        });


        List<Integer> notFound = new ArrayList<>();

        NPC banker = checkForNPC(data.getInt(BANKER));
        if(banker == null) notFound.add(data.getInt(BANKER));
        NPC deposit = checkForNPC(data.getInt(DEPOSIT));
        if(deposit == null) notFound.add(data.getInt(DEPOSIT));
        NPC withdraw = checkForNPC(data.getInt(WITHDRAW));
        if(withdraw == null)notFound.add(data.getInt(WITHDRAW));

        if(!notFound.isEmpty()) {
            System.out.println("ProdigyBank could not load bank.yml because some citizens are not found !");
            System.out.println("If you just reloaded the server ignore this error...");
            for (int i : notFound) {
                System.out.println("   - citizen with id " + i + " not found !");
            }
            return;
        }
        if(!banker.isSpawned())
            banker.spawn(data.getLocation(BANKER_LOC));
        else
            banker.teleport(data.getLocation(BANKER_LOC), PlayerTeleportEvent.TeleportCause.PLUGIN);

        if(!deposit.isSpawned())
            deposit.spawn(data.getLocation(DEPOSIT_LOC));
        else
            deposit.teleport(data.getLocation(DEPOSIT_LOC), PlayerTeleportEvent.TeleportCause.PLUGIN);
        if(!withdraw.isSpawned())
            withdraw.spawn(data.getLocation(WITHDRAW_LOC));
        else
            withdraw.teleport(data.getLocation(WITHDRAW_LOC), PlayerTeleportEvent.TeleportCause.PLUGIN);

        List<NPC> police = new ArrayList<>();
        ConfigurationSection sectionPolice = data.getConfigurationSection(POLICE);
        if(sectionPolice != null) {
            for (String id : sectionPolice.getKeys(false)) {
                NPC npc = checkForNPC(Integer.valueOf(id));
                if(npc != null) {
                    Location loc = sectionPolice.getLocation(id);
                    if(!npc.isSpawned())
                        npc.spawn(loc);
                    else
                        npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    police.add(npc);
                }
            }
        }


        List<Location> swats = (List<Location>) data.getList(SWAT_POINTS);

        List<Cuboid> doors = new ArrayList<>();
        ConfigurationSection section = data.getConfigurationSection(DOORS);
        if(section != null) {
            for (String id : section.getKeys(false)) {
                Location point1 = section.getLocation(id + ".point1");
                Location point2 = section.getLocation(id + ".point2");
                Cuboid door = new Cuboid(point1, point2);
                doors.add(door);
            }
        }

        Bank bank = new Bank(
                bankArea,
                vaultArea,
                vaultDoor,
                deposit,
                withdraw,
                banker,
                jail,
                chests
        );

        bank.getPoliceStaff().setPolice(police);
        bank.getSwatTeam().setSwat_points(swats);
        bank.setChests(chests);
        bank.setDoors_to_lock(doors);

        instance.setBank(bank);
        System.out.println("[ProdigyBank] Bank successfully loaded !");


    }


    public void save() {
        if(instance.getBank() == null) return;

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        Bank b = instance.getBank();
        data.set(BANK_POINT1,b.getBankCuboid().getPoint1());
        data.set(BANK_POINT2,b.getBankCuboid().getPoint2());
        data.set(VAULT_POINT1,b.getVaultCuboid().getPoint1());
        data.set(VAULT_POINT2,b.getVaultCuboid().getPoint2());
        data.set(VAULT_DOOR_POINT1,b.getVaultDoor().getCuboid().getPoint1());
        data.set(VAULT_DOOR_POINT2,b.getVaultDoor().getCuboid().getPoint2());

        data.set(BANKER,b.getBankerStaff().getBanker().getId());
        data.set(DEPOSIT,b.getHostessStaff().getDepositHostess().getId());
        data.set(WITHDRAW,b.getHostessStaff().getWithdrawHostess().getId());

        data.set(JAIL,b.getJail());

        saveChests(false);
        saveDoors(false);
        savePolice(false);
        saveSwat(false);

        saveData();
    }

    public void initDefaultStaffPos() {
        if(instance.getBank() == null) return;
        Bank b = instance.getBank();

        setBankerPos(b.getBankerStaff().getBanker().getStoredLocation());
        setDepositHostessPos(b.getHostessStaff().getDepositHostess().getStoredLocation());
        setWithdrawHostessPos(b.getHostessStaff().getWithdrawHostess().getStoredLocation());
    }

    public Location getDepositHostessSpawnPoint() {
        if(data == null) data = YamlConfiguration.loadConfiguration(file);

        return data.getLocation(DEPOSIT_LOC);
    }

    public Location getWithdrawHostessSpawnPoint() {
        if(data == null) data = YamlConfiguration.loadConfiguration(file);

        return data.getLocation(WITHDRAW_LOC);
    }

    public Location getBankerSpawnPoint() {
        if(data == null) data = YamlConfiguration.loadConfiguration(file);
        return data.getLocation(BANKER_LOC);
    }

    public Location getPolicePos(NPC npc) {
        return data.getLocation(POLICE + "." + npc.getId());
    }

    public void setPolicePos(NPC npc) {
        if(data == null) data = YamlConfiguration.loadConfiguration(file);
        data.set(POLICE + "." + npc.getId(), npc.getStoredLocation());
        saveData();
    }

    public void setBankerPos(Location location) {
        data.set(BANKER_LOC,location);
        saveData();
    }

    public void setDepositHostessPos(Location location) {
        data.set(DEPOSIT_LOC,location);
        saveData();
    }

    public void setWithdrawHostessPos(Location location) {
        data.set(WITHDRAW_LOC,location);
        saveData();
    }


    //Optionals
    public void saveChests(boolean write) {
        Bank b = instance.getBank();
        List<Location> chests = new ArrayList<>();
        b.getChests().forEach(c -> chests.add(c.getChest().getLocation()));
        data.set(CHESTS,chests);
        if(write) saveData();

    }


    public void savePolice(boolean write) {
        Bank b = instance.getBank();
        b.getPoliceStaff().getPolice().stream().filter(npc -> npc.isSpawned()).forEach(npc -> {
            data.set(POLICE + "." + npc.getId(),npc.getStoredLocation());
        });
        if(write) saveData();

    }

    public void saveSwat(boolean write) {
        Bank b = instance.getBank();
        data.set(SWAT_POINTS,b.getSwatTeam().getSwat_points());
        if(write) saveData();

    }

    public void saveDoors(boolean write) {
        Bank b = instance.getBank();
        int i = 0;
        for (Cuboid cuboid : b.getDoors_to_lock()) {
            data.set(DOORS + "." + i + ".point1",cuboid.getPoint1());
            data.set(DOORS + "." + i + ".point2",cuboid.getPoint2());
            i++;
        }
        if(write) saveData();
    }




    public void saveData() {
        if (data == null || file == null) {
            return;
        }
        try {
            data.save(file);
        } catch (IOException ex) {
            instance.getLogger().log(Level.WARNING, "Could not save config to " + data.getName(), ex);
        }
    }


    private NPC checkForNPC(int id) {
        NPC npc = registry.getById(id);
        return npc;
    }

}
