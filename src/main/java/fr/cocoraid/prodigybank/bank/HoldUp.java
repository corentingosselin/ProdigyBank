package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.staff.authority.SwatTeam;
import fr.cocoraid.prodigybank.customevents.EnterBankEvent;
import fr.cocoraid.prodigybank.customevents.QuitBankEvent;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.nms.NMS;
import fr.cocoraid.prodigybank.nms.NMSPlayer;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HoldUp {

    protected ProdigyBank instance = ProdigyBank.getInstance();
    protected Language lang = instance.getLanguage();
    protected ConfigLoader config = instance.getConfigLoader();
    protected BankLoader bankLoader = instance.getBankLoader();

    private enum RobberyStep {
        POLICE, SWAT, DOORS_LOCKED, JAIL;
    }

    private RobberyStep robberyStep;
    private int time = config.getRobberyLimit();
    private ArrayDeque<ItemStack> keys = new ArrayDeque<>();

    private Bank bank;
    private boolean isHoldup;
    private Squad squad;
    private BukkitTask holdUpTask;

    private SwatTeam swatTeam;
    private List<Player> playersInside = new ArrayList<>();
    private List<Player> hostages = new ArrayList<>();
    private List<Item> allDroppedMoney = new ArrayList<>();

    public HoldUp(Bank bank) {
        this.bank = bank;
    }


    public void startHoldup(Squad squad) {

        //send start messages
        squad.sendSubTitle(lang.holdup_starting);

        //add target for every police: player and its squad
        this.squad = squad;


        if(!bank.getSwatTeam().getSwat_points().isEmpty()) {
            this.robberyStep = RobberyStep.SWAT;
        } else if(!bank.getDoors_to_lock().isEmpty()) {
            this.robberyStep = RobberyStep.DOORS_LOCKED;
        } else {
            this.robberyStep = RobberyStep.JAIL;
        }


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
        bank.getPoliceStaff().addSquadTarget();
        bank.getHostessStaff().addSquadTarget();


        bank.getChests().forEach(c -> {
            ItemStack i = c.generateKey();
            keys.add(i);
        });


        //bank notify
        Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {

            //add hostages
            if(bank.getBankCuboid().isIn(cur) && !squad.getSquadMembers().contains(cur)) {
                //send msg
                Utils.sendTitle(cur,lang.hostage_notify);
                getHostages().add(cur);
            }
            cur.playSound(bank.getBankCuboid().getCenter(), Sound.BLOCK_CONDUIT_ACTIVATE,2,0);
        });

      startTask();

        this.isHoldup = true;


    }

    private String timeMessage;
    private List<UUID> warned = new ArrayList<>();
    private void startTask() {
        this.holdUpTask = new BukkitRunnable() {
            boolean warning = false;

            @Override
            public void run() {


                int minutes = time / (60 * 20);
                int seconds = (time / 20) % 60;
                int tick = (time % 20) * 3;
                timeMessage = String.format("%02d:%02d:%02d", minutes, seconds,tick);
                if(time % 20 == 0) {




                    //register custom events
                    Bukkit.getOnlinePlayers().stream().filter(cur -> cur.getWorld().equals(bank.getWorld())).forEach(cur -> {

                        //send warnings:
                        if(!warned.contains(cur.getUniqueId())) {

                            if (squad.getSquadMembers().contains(cur) && bank.getBankCuboid().isInWithMarge(cur.getLocation(), -5)) {
                                warned.add(cur.getUniqueId());
                                String msg = lang.out_warning;
                                Utils.sendTitle(cur, msg);
                            }

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    warned.remove(cur.getUniqueId());
                                }
                            }.runTaskLater(instance,20*5);
                        }




                        if(!getPlayersInside().contains(cur) && bank.getBankCuboid().isIn(cur)) {
                            EnterBankEvent e = new EnterBankEvent(cur, bank);
                            Bukkit.getPluginManager().callEvent(e);
                            if(e.isCancelled()) {
                                cur.teleport(NMSPlayer.getLastLocation(cur));
                                return;
                            }
                            getPlayersInside().add(cur);
                        } else if(!bank.getBankCuboid().isIn(cur) && getPlayersInside().contains(cur)) {
                            getPlayersInside().remove(cur);
                            Bukkit.getPluginManager().callEvent(new QuitBankEvent(cur, bank));
                        }

                    });






                    //check owner leave area
                    if (!bank.getBankCuboid().isIn(squad.getOwner().getLocation())) {
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
                    bank.getChests().stream().filter(chest -> chest.isChestOpened()).forEach(chest -> {
                        NMS.setChestOpen(chest.getChest(),true);
                    });



                if(robberyStep == RobberyStep.SWAT) {
                    if(time % 2 == 0) {
                        Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {
                            if(playersInside.contains(cur))
                                cur.playSound(cur.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 0);
                            else
                                cur.playSound(bank.getBankCuboid().getCenter(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 0);
                        });
                    }

                    squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                            .toString().replace("%time",timeMessage).replace("%type",lang.time_swat_type));


                    if(time <= 0) {
                        time = config.getTimeBeforeJail();
                        squad.sendSubTitle(lang.swatting);
                        bank.getSwatTeam().spawnSwat();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                squad.sendSubTitle(new StringBuilder(lang.time_left_notify_escape).toString()
                                        .replace("%time", String.valueOf(config.getRobberyLimit() / (60 * 20))));
                            }
                        }.runTaskLater(instance, 40);
                        if(!bank.getDoors_to_lock().isEmpty()) {
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
                            Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {
                                cur.playSound(bank.getBankCuboid().getCenter(), Sound.ENTITY_ELDER_GUARDIAN_CURSE,2,2);
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
    }


    private void updateBeforeSwat() {

    }


    private void updateBeforeDoors() {

    }

    private void updateBeforeJail() {

    }

    public void endHoldUp() {
        if(!isHoldup()) return;
        if(squad != null) {
            bank.getPoliceStaff().resetSquadTargets();
            bank.getBankerStaff().resetSquadTargets();
            bank.getHostessStaff().resetSquadTargets();
        }

        bank.getPoliceStaff().refreshStaff();
        bank.getBankerStaff().refreshStaff();
        bank.getHostessStaff().refreshStaff();


        if(holdUpTask != null && !holdUpTask.isCancelled())
            holdUpTask.cancel();

        bank.getChests().forEach(c -> c.cancel());

        getAllDroppedMoney().forEach(item -> item.remove());
        getAllDroppedMoney().clear();


        this.robberyStep = null;
        this.time = config.getRobberyLimit();
        getSquad().getSquadMembers().forEach(s -> {
            keys.forEach(k -> {
                s.getInventory().remove(k);
            });

        });
        keys.clear();


        this.squad = null;

        instance.getArmorStandModel().getDriller().destory();
        bank.getVaultDoor().reset();

        isHoldup = false;
    }


    public ArrayDeque<ItemStack> getKeys() {
        return keys;
    }

    public List<Item> getAllDroppedMoney() {
        return allDroppedMoney;
    }

    public boolean isHoldup() {
        return isHoldup;
    }

    public Squad getSquad() {
        return squad;
    }
    public void setSquad(Squad squad) {
        this.squad = squad;
    }

    public List<Player> getHostages() {
        return hostages;
    }

    public void setHoldup(boolean holdup) {
        isHoldup = holdup;
    }

    public List<Player> getPlayersInside() {
        return playersInside;
    }
}
