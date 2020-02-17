package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Bank {



    //cleanup this class
    //better lose /win detection
    // title send only send title in subtitle
    //swat sometimes have nametag

    private List<Item> allMoney = new ArrayList<>();

    private boolean holdup = false;

    private ProdigyBank instance = ProdigyBank.getInstance();
    private Language lang = instance.getLanguage();
    private ConfigLoader config = instance.getConfigLoader();
    private BankLoader bankLoader = instance.getBankLoader();

    private World world;

    //REQUIRED
    private Cuboid bankCuboid;
    private Cuboid vaultCuboid;
    private VaultDoor vaultDoor;
    private NPC depositHostess;
    private NPC withdrawHostess;
    private NPC banker;
    private Location jail;
    private List<SafeDeposit> chests;

    //OPTIONAL
    private List<NPC> police = new ArrayList<>();
    private List<Location> swat_points = new ArrayList<>();
    private List<NPC> swats = new ArrayList<>();
    private List<Cuboid> doors_to_lock = new ArrayList<>();

    public Bank(Cuboid bankArea,
                Cuboid vaultArea,
                Cuboid vaultDoor,
                NPC depositHostess,
                NPC withdrawHostess,
                NPC banker,
                Location jail,
                List<SafeDeposit> chests) {

        this.bankCuboid = bankArea;
        this.vaultCuboid = vaultArea;
        this.vaultDoor = new VaultDoor(vaultDoor);
        this.depositHostess = depositHostess;
        this.withdrawHostess = withdrawHostess;
        this.banker = banker;
        this.jail = jail;

        this.chests = chests;
        this.world = bankCuboid.getPoint1().getWorld();
    }


    public void addPoliceMember(Player player, Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, config.getRandomPoliceSkin());
        npc.addTrait(LookClose.class);
        npc.getTrait(LookClose.class).setRange(2);
        npc.getTrait(LookClose.class).setRandomLook(true);
        npc.getTrait(LookClose.class).setRealisticLooking(true);
        npc.getTrait(LookClose.class).toggle();
        npc.addTrait(SentinelTrait.class);
        npc.getTrait(Equipment.class)
                .set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_SWORD, 1));

        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
        sentinel.fightback = true;
        sentinel.realistic = false;

        sentinel.speed = 1.2F;
        npc.getNavigator().getLocalParameters().baseSpeed(1.2F);
        sentinel.squad = "police";
        sentinel.respawnTime = 0;
        npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);

        sentinel.attackRate = 4;
        sentinel.range = 100;
        sentinel.chaseRange = 100;
        npc.getNavigator().getDefaultParameters().range((float) 100);
        npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA,true);
        npc.data().setPersistent("nameplate-visible", false);
        npc.spawn(location);
        police.add(npc);
        player.sendMessage(
                new StringBuilder(lang.object_added).toString()
                        .replace("%id",String.valueOf(npc.getId()))
                        .replace("%type", "police member")
        );
        bankLoader.setPolicePos(npc);
    }

    public void removePoliceMember(NPC npc) {
        police.remove(npc);
        bankLoader.savePolice(true);
    }

    public void addSwatSpawnPoint(Player player, Location location) {
        swat_points.add(location);
        player.sendMessage(
                new StringBuilder(lang.object_added).toString()
                        .replace("%id",String.valueOf(swat_points.size() - 1))
                        .replace("%type", "swat spawn point")
        );
        bankLoader.saveSwat(true);
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

    public List<Location> getSwat_points() {
        return swat_points;
    }

    public List<NPC> getPolice() {
        return police;
    }

    public Location getJail() {
        return jail;
    }

    public NPC getBanker() {
        return banker;
    }

    public NPC getDepositHostess() {
        return depositHostess;
    }

    public NPC getWithdrawHostess() {
        return withdrawHostess;
    }


    public void setDoors_to_lock(List<Cuboid> doors_to_lock) {
        this.doors_to_lock = doors_to_lock;
    }

    public void setSwat_points(List<Location> swat_points) {
        this.swat_points = swat_points;
    }

    public void setPolice(List<NPC> police) {
        this.police = police;

    }

    public void setChests(List<SafeDeposit> chests) {
        this.chests = chests;
    }

    public World getWorld() {
        return world;
    }

    public void endHoldUp() {
        if(!isHoldup()) return;
        if(squad != null) {
            resetTargets();
        }

        refreshStaff();

        chests.forEach(c -> c.cancel());

        getAllMoney().forEach(item -> item.remove());
        getAllMoney().clear();

        this.squad = null;

        instance.getArmorStandModel().getDriller().destory();
        getVaultDoor().reset();

        holdup = false;
    }


    public void refreshStaff() {
        depositHostess.despawn(DespawnReason.PENDING_RESPAWN);
        depositHostess.spawn(bankLoader.getDepositHostessSpawnPoint(), SpawnReason.RESPAWN);

        withdrawHostess.despawn(DespawnReason.PENDING_RESPAWN);
        withdrawHostess.spawn(bankLoader.getWithdrawHostessSpawnPoint(), SpawnReason.RESPAWN);

        banker.despawn(DespawnReason.PENDING_RESPAWN);
        banker.spawn(bankLoader.getBankerSpawnPoint(), SpawnReason.RESPAWN);

        police.forEach(p -> {
            p.despawn(DespawnReason.PENDING_RESPAWN);
            p.spawn(bankLoader.getPolicePos(p),SpawnReason.RESPAWN);
        });

        swats.stream().filter(s -> s.isSpawned()).forEach(s -> s.destroy());
        swats.clear();

        if(holdUpTask != null && !holdUpTask.isCancelled())
            holdUpTask.cancel();

        this.robberyStep = null;
        this.time = config.getRobberyLimit();
        squad.getSquad().forEach(s -> {
            keys.forEach(k -> {
                s.getInventory().remove(k);
            });

        });
        keys.clear();
        this.squad = null;

    }


    //holdup part

    private enum RobberyStep {
        SWAT, DOORS_LOCKED, JAIL;
    }

    private RobberyStep robberyStep;
    private int time = config.getRobberyLimit();
    private ArrayDeque<ItemStack> keys = new ArrayDeque<>();
    private Squad squad;
    private BukkitTask holdUpTask;
    public void startHoldup(Squad squad) {
        //add target for every police: player and its squad
        this.squad = squad;

        if(!swat_points.isEmpty()) {
            this.robberyStep = RobberyStep.SWAT;
        } else if(!doors_to_lock.isEmpty()) {
            this.robberyStep = RobberyStep.DOORS_LOCKED;
        } else {
            this.robberyStep = RobberyStep.JAIL;
        }

        //send start messages
        squad.sendSubTitle(lang.holdup_starting);
        //small delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if(robberyStep == RobberyStep.SWAT)
                    squad.sendSubTitle(new StringBuilder(lang.time_left_notify_swat).toString()
                            .replace("%time", String.valueOf(config.getRobberyLimit())));
                else if(robberyStep == RobberyStep.DOORS_LOCKED)
                    squad.sendSubTitle(new StringBuilder(lang.time_left_notify_escape).toString()
                            .replace("%time", String.valueOf(config.getRobberyLimit())));
            }
        }.runTaskLater(instance, 40);


        //init targets
        addTarget(squad);


        getChests().forEach(c -> {
            ItemStack i = c.generateKey();
            keys.add(i);
        });

        Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(getWorld())).forEach(cur -> {
            cur.playSound(getBankCuboid().getCenter(), Sound.BLOCK_CONDUIT_ACTIVATE,2,0);
        });

        this.holdUpTask = new BukkitRunnable() {
            boolean warning = false;

            @Override
            public void run() {
                if(time % 20 == 0) {
                    // prevent unknow people to enter inside the bank
                    Bukkit.getOnlinePlayers().stream().filter(cur -> cur.getWorld().equals(getWorld())
                            && !squad.getSquad().contains(cur)).filter(cur -> !bankCuboid.isIn(cur.getLocation())).forEach(cur -> {
                        if (getBankCuboid().isInWithMarge(cur.getLocation(), 10)) {
                            Utils.bumpEntity(cur,cur.getLocation(),3,0.1D);
                            String msg = lang.area_restricted;
                            Utils.sendTitle(cur,msg);
                        }
                    });


                    //check squad member leave area
                    squad.getSquad().removeIf(cur -> {
                        if(cur.equals(squad.getOwner())) return false;
                        if( !bankCuboid.isIn(cur)) {
                            if (time % (20 * 2) == 0) {
                                String msg = lang.out_warning;
                                Utils.sendTitle(cur, msg);
                                return false;
                            }
                        } else if(!getBankCuboid().isInWithMarge(cur.getLocation(), 10)) {
                            squad.sendSubTitle(lang.bank_left_robbed);
                            failSquadMember(cur,config.getPercentDie());
                            return true;

                        }
                        return false;
                    });

                    //check owner leave area
                    if (!getBankCuboid().isIn(squad.getOwner().getLocation())) {
                        if (!getBankCuboid().isInWithMarge(squad.getOwner().getLocation(), 10)) {
                            if(vaultDoor.isDestroyed() && chests.stream().filter(c -> c.isChestOpened()).findAny().isPresent()) {
                                //sucess
                                reward();
                                squad.sendSubTitle(lang.bank_left_robbed);
                                endHoldUp();
                                this.cancel();
                                return;
                            } else {
                                squad.getSquad().forEach(s -> failSquadMember(s, config.getPercentJailed()));
                                squad.sendTeamSubTitle(lang.bank_leader_left_not_robbed);
                                squad.sendOwnerSubTitle(lang.bank_left_not_robbed);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        squad.sendSubTitle(new StringBuilder(lang.go_to_jail).toString().replace("%percentage",String.valueOf(config.getPercentJailed())));
                                    }
                                }.runTaskLater(instance, 20 * 3);
                                endHoldUp();
                                return;

                            }
                        }
                        //send warning
                        if (!warning) {
                            squad.sendSubTitle(lang.out_warning);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    warning = false;
                                }
                            }.runTaskLater(instance, 20 * 5);
                        }


                    }
                }


                //chest fixed
                if(time % 5 == 0)
                    chests.stream().filter(chest -> chest.isChestOpened()).forEach(chest -> {
                        NMS.setChestOpen(chest.getChest(),true);
                    });


                int minutes = time / (60 * 20);
                int seconds = (time / 20) % 60;
                int tick = (time % 20) * 3;
                String timeMessage = String.format("%02d:%02d:%02d", minutes, seconds,tick);
                if(robberyStep == RobberyStep.SWAT) {

                    if(time % 2 == 0)
                        Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(getWorld())).forEach(cur -> {
                            cur.playSound(getBankCuboid().getCenter(), Sound.BLOCK_NOTE_BLOCK_BELL,2,0);
                        });

                    squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                            .toString().replace("%time",timeMessage).replace("%type",lang.time_swat_type));
                    if(time <= 0) {
                        time = config.getTimeBeforeJail();
                        squad.sendSubTitle(lang.swatting);
                        spawn_swat_team();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                squad.sendSubTitle(new StringBuilder(lang.time_left_notify_escape).toString()
                                        .replace("%time", String.valueOf(config.getRobberyLimit() / (60 * 20))));
                            }
                        }.runTaskLater(instance, 40);
                        if(!doors_to_lock.isEmpty()) {
                            robberyStep = RobberyStep.DOORS_LOCKED;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    squad.sendSubTitle(lang.doors_closed);
                                }
                            }.runTaskLater(instance, 100);
                        } else robberyStep = RobberyStep.JAIL;
                    }


                } else {
                    if(robberyStep == RobberyStep.JAIL) {
                        if(time % 20 == 0)
                            Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(getWorld())).forEach(cur -> {
                                cur.playSound(getBankCuboid().getCenter(), Sound.ENTITY_ELDER_GUARDIAN_CURSE,2,2);
                            });
                    }

                    squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                            .toString().replace("%time",timeMessage).replace("%type",lang.time_jail_type));
                    if(time <= 0) {
                        //jail
                        Bukkit.broadcastMessage("squad is now jailed !");
                        this.cancel();
                    }
                }
                time--;
            }
        }.runTaskTimer(instance,0,0);

        this.holdup = true;
    }

    public void reward() {
        squad.getSquad().forEach(s -> {
            EconomyBridge.giveMoney(s,squad.getMoneyCollected());
        });
    }

    public void failSquadMember(Player player, int percentage) {
        player.teleport(jail);
        int money = (int) EconomyBridge.getMoney(player) ;
        EconomyBridge.takeMoney(player, money * percentage / 100);
    }

    private void spawn_swat_team() {
        swat_points.forEach(s -> {
            for (int k = 0; k < config.getSwatPerPoint(); k++) {
                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, config.getRandomSwatSkin());
                npc.addTrait(SentinelTrait.class);
                npc.getTrait(Equipment.class)
                        .set(Equipment.EquipmentSlot.HAND,  new ItemStack(Material.DIAMOND_SWORD));
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta shieldMeta = shield.getItemMeta();
                BlockStateMeta bmeta = (BlockStateMeta) shieldMeta;
                Banner banner = (Banner) bmeta.getBlockState();
                banner.setBaseColor(DyeColor.BLACK);
                banner.update();
                bmeta.setBlockState(banner);
                shield.setItemMeta(bmeta);
                npc.getTrait(Equipment.class)
                        .set(Equipment.EquipmentSlot.OFF_HAND, shield);

                npc.getNavigator().getDefaultParameters().range((float) 100);
                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                sentinel.fightback = true;
                sentinel.chaseRange = 100;
                sentinel.range = 100;
                sentinel.health = 30;
                sentinel.attackRate = 5;
                sentinel.speed = 1.25F;
                npc.getNavigator().getLocalParameters().baseSpeed(1.25F);
                sentinel.squad = "swat";
                sentinel.respawnTime = -1;
                npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);
                npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA, true);
                npc.data().setPersistent("nameplate-visible", false);
                npc.spawn(s);

                squad.getSquad().forEach(cur -> {
                    new SentinelTargetLabel("uuid:" + cur.getUniqueId()).addToList(sentinel.allTargets);
                });
                swats.add(npc);
            }
        });
    }



    public ArrayDeque<ItemStack> getKeys() {
        return keys;
    }

    public void resetTargets() {
        police.forEach(p -> {
            SentinelTrait sentinel = p.getTrait(SentinelTrait.class);
            squad.getSquad().forEach(s -> {
                new SentinelTargetLabel("uuid:" + s.getUniqueId()).removeFromList(sentinel.allTargets);
            });
        });

        SentinelTrait hostessB = depositHostess.getTrait(SentinelTrait.class);
        SentinelTrait hostessA = withdrawHostess.getTrait(SentinelTrait.class);
        for (Player player : squad.getSquad()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).removeFromList(hostessA.allAvoids);
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).removeFromList(hostessB.allAvoids);
        }
    }


    public void addTarget(Squad squad) {
        police.forEach(p -> {
            SentinelTrait sentinel = p.getTrait(SentinelTrait.class);
            p.getTrait(LookClose.class).setRandomLook(false);
            p.getTrait(LookClose.class).toggle();
            for (Player player : squad.getSquad()) {
                new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(sentinel.allTargets);
            }
        });

        SentinelTrait hostessB = depositHostess.getTrait(SentinelTrait.class);
        SentinelTrait hostessA = withdrawHostess.getTrait(SentinelTrait.class);
        for (Player player : squad.getSquad()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(hostessA.allAvoids);
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(hostessB.allAvoids);
        }
    }


    public boolean isHoldup() {
        return holdup;
    }

    public boolean isStaffMember(NPC npc) {
        return depositHostess.getId() == npc.getId()
                || withdrawHostess.getId() == npc.getId()
                || banker.getId() == npc.getId()
                || swats.contains(npc)
                || police.contains(npc);

    }


    public boolean isBankMember(NPC npc) {
        return depositHostess.getId() == npc.getId()
                || withdrawHostess.getId() == npc.getId()
                || banker.getId() == npc.getId();
    }

    public boolean isPoliceOrSwat(Entity entity) {
        if(police.stream().filter(p -> p.getName().equals(entity.getName())).findAny().isPresent()) return true;
        else if(swats.stream().filter(s -> s.getName().equals(entity.getName())).findAny().isPresent()) return true;
        else return false;
    }

    public List<Item> getAllMoney() {
        return allMoney;
    }

    public Squad getSquad() {
        return squad;
    }
}
