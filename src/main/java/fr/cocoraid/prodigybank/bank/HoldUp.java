package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import fr.cocoraid.prodigybank.customevents.EnterBankEvent;
import fr.cocoraid.prodigybank.customevents.QuitBankEvent;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.nms.NMS;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HoldUp {

    protected ProdigyBank instance = ProdigyBank.getInstance();
    protected Language lang = instance.getLanguage();
    protected ConfigLoader config = instance.getConfigLoader();
    protected BankLoader bankLoader = instance.getBankLoader();

    private enum RobberyStep {
        POLICE, DOORS_LOCKED, SWAT, JAIL;
    }


    private List<C4> C4s = new ArrayList<>();
    private int time;
    private LinkedList<RobberyStep> phases = new LinkedList<>();

    private ArrayDeque<ItemStack> virtualKeys = new ArrayDeque<>();
    private List<ItemStack> keys = new ArrayList<>();
    private List<Item> droppedKeys = new ArrayList<>();

    private Bank bank;
    private Driller driller;

    private boolean isHoldup;
    private Squad squad;
    private BukkitTask holdUpTask;

    private List<Player> playersInside = new ArrayList<>();
    private List<Player> hostages = new ArrayList<>();
    private List<Item> allDroppedMoney = new ArrayList<>();

    public HoldUp(Bank bank) {
        this.bank = bank;

    }


    //call this when leader has reached the exit
    public void succed() {
        squad.reward();
        squad.sendOwnerSubTitle(lang.title_bank_owner_success);
        squad.sendTeamSubTitle(lang.title_bank_member_success);
        endHoldUp();
    }

    //when the leader died
    public void failOwnerDied(Player owner) {

        //notify other member
        squad.sendTeamSubTitle(lang.title_owner_died);
        //jail other member
        squad.getSquadMembers().forEach(s -> squad.removeSquadMember(s, Squad.MemberFailedType.JAILED));


        //notify owner
        squad.sendOwnerSubTitle(lang.title_owner_self_died);
        squad.removeSquadMember(owner, Squad.MemberFailedType.DIED);

        endHoldUp();
    }



    //when the leader left the area
    public void abandon(Player owner) {
        //notify other member
        squad.sendTeamSubTitle(lang.title_bank_leader_left);
        //jail other member
        squad.getSquadMembers().forEach(s -> squad.removeSquadMember(s, Squad.MemberFailedType.JAILED));


        //notify owner
        squad.sendOwnerSubTitle(lang.title_bank_left);
        squad.removeSquadMember(owner, Squad.MemberFailedType.LEFT);

        endHoldUp();
    }

    public void timeOver() {
        squad.getOwner().playSound(squad.getOwner().getLocation(), Sound.BLOCK_END_GATEWAY_SPAWN, 2, 0);
        //jail other member
        squad.getSquadMembers().forEach(s -> squad.removeSquadMember(s, Squad.MemberFailedType.JAILED));
        //notify owner
        squad.removeSquadMember(squad.getOwner(), Squad.MemberFailedType.JAILED);
        endHoldUp();
    }


    private void nextPhase() {
        phases.removeFirst();
        if(phases.getFirst() == RobberyStep.JAIL) {
            timeOver();
            return;
        }
        updateTiming();
        RobberyStep current = phases.getFirst();
        if(current == RobberyStep.DOORS_LOCKED) {
            bank.getDoors_to_lock().forEach(d -> {
               d.close();
            });
            squad.sendSubTitle(lang.title_doors_closed);
        } else if(current == RobberyStep.SWAT) {
            bank.getSwatTeam().spawnSwat();
            squad.sendSubTitle(lang.title_swatting);
        }
    }


    private void updateTiming() {
        RobberyStep nextStep = phases.get(1);
        if(nextStep == null) return;

        if(nextStep == RobberyStep.DOORS_LOCKED) {
            time = config.getTimeBeforeDoors();
            new BukkitRunnable() {
                @Override
                public void run() {
                    squad.sendSubTitle(new StringBuilder(lang.title_time_left_notify_doors).toString()
                            .replace("%time", String.valueOf(config.getTimeBeforeDoors() / 20)));

                }
            }.runTaskLater(instance,20*4);
        } else if(nextStep == RobberyStep.SWAT) {
            time = config.getTimeBeforeSwat();
            new BukkitRunnable() {
                @Override
                public void run() {
                    squad.sendSubTitle(new StringBuilder(lang.title_time_left_notify_swat).toString()
                            .replace("%time", String.valueOf(config.getTimeBeforeSwat() / 20)));
                }
            }.runTaskLater(instance,20*4);
        } else if(nextStep == RobberyStep.JAIL) {
            time = config.getTimeBeforeJail();
            new BukkitRunnable() {
                @Override
                public void run() {
                    squad.sendSubTitle(new StringBuilder(lang.title_time_left_notify_escape).toString()
                            .replace("%time", String.valueOf(config.getTimeBeforeJail() / 20)));
                }
            }.runTaskLater(instance,20*4);
        }
    }



    private void sendCountDownMessage() {
        // prevent over limit list size error
        if(phases.size() < 2) return;
        RobberyStep nextStep = phases.get(1);
        if(nextStep == null) return;
        if(nextStep == RobberyStep.DOORS_LOCKED) {
            squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                    .toString().replace("%time",timeMessage).replace("%type",lang.time_doors_type));
        } else if(nextStep == RobberyStep.SWAT) {
            squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                    .toString().replace("%time",timeMessage).replace("%type",lang.time_swat_type));
        } else if(nextStep == RobberyStep.JAIL) {
            squad.sendActionBarMessage(new StringBuilder(lang.time_left_type)
                    .toString().replace("%time",timeMessage).replace("%type",lang.time_jail_type));
        }
    }


    private void loadRobberySteps() {
        //load phases:
        phases.add(RobberyStep.POLICE);
        if(!bank.getDoors_to_lock().isEmpty()) {
            phases.add(RobberyStep.DOORS_LOCKED);
        }
        if(!bank.getSwatTeam().getSwat_points().isEmpty()) {
            phases.add(RobberyStep.SWAT);
        }
        phases.add(RobberyStep.JAIL);
    }


    public void startHoldup(Squad squad) {

        //add target for every police: player and its squad
        this.squad = squad;
        squad.startAttacking(bank);


        //load all steps and prepare the first timing
        loadRobberySteps();
        updateTiming();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(isHoldup)
                    squad.sendSubTitle(lang.title_holdup_help);
            }
        }.runTaskLater(instance,20*5);


        //send start messages
        squad.sendSubTitle(lang.title_holdup_starting);
        //send message to the server

        //init targets
        bank.getPoliceStaff().addSquadTarget();
        bank.getHostessStaff().addSquadTarget();


        //prepare new encrypted key for safe chest
        bank.getChests().forEach(c -> {
            ItemStack i = c.generateKey();
            virtualKeys.add(i);
        });


        //bank notify
        Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {

            //add hostages
            if(bank.getBankCuboid().isIn(cur) && !squad.getSquadMembers().contains(cur) && !squad.getOwner().equals(cur)) {
                //send msg
                Utils.sendTitle(cur,lang.title_hostage_notify);
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


            @Override
            public void run() {

                if(phases.isEmpty()) return;

                int minutes = time / (60 * 20);
                int seconds = (time / 20) % 60;
                int tick = (time % 20) * 3;
                timeMessage = String.format("%02d:%02d:%02d", minutes, seconds,tick);

                Bukkit.getOnlinePlayers().stream().filter(cur -> cur.getWorld().equals(bank.getWorld()) && squad.isFromSquad(cur)).forEach(cur -> {
                    //send warnings:
                    if (!bank.getBankCuboid().isIn(cur.getLocation()) && getPlayersInside().contains(cur)) {
                        if(!warned.contains(cur.getUniqueId())) {
                            warned.add(cur.getUniqueId());
                            String msg = lang.title_left_warning;
                            Utils.sendTitle(cur, msg);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    warned.remove(cur.getUniqueId());
                                }
                            }.runTaskLater(instance,20*5);
                        }
                    }


                    //register custom events
                    if(!getPlayersInside().contains(cur) && bank.getBankCuboid().isIn(cur)) {
                        EnterBankEvent e = new EnterBankEvent(cur, bank);
                        Bukkit.getPluginManager().callEvent(e);
                        if(e.isCancelled()) {
                            cur.teleport(bank.getExit());
                            return;
                        }
                        getPlayersInside().add(cur);
                    } else if(!bank.getBankCuboid().isInWithMarge(cur.getLocation(), 5) && getPlayersInside().contains(cur)) {
                        getPlayersInside().remove(cur);
                        Bukkit.getPluginManager().callEvent(new QuitBankEvent(cur, bank));
                    }

                });

                //keep chest open
                if(time % 5 == 0)
                    bank.getChests().stream().filter(chest -> chest.isChestOpened()).forEach(chest -> {
                        NMS.setChestOpen(chest.getChest(),true);
                    });


                //update cooldown message
                sendCountDownMessage();

                if(phases.isEmpty()) return;
                RobberyStep current = phases.getFirst();
                if(current == RobberyStep.POLICE) {
                    updatePolice();
                } else if(current == RobberyStep.DOORS_LOCKED) {
                    updateDoors();
                } else if(current == RobberyStep.SWAT) {
                    updateSwat();
                }



                time--;
                if(time == 0) {
                    nextPhase();
                    return;
                }
            }
        }.runTaskTimer(instance,0,0);
    }

    private void updatePolice() {
        //ring alarm
        if(time % 2 == 0) {
            Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {
                if(playersInside.contains(cur))
                    cur.playSound(cur.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3F, 0);
                else
                    cur.playSound(bank.getBankCuboid().getCenter(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 0);
            });
        }


    }


    private void updateDoors() {
        //ring different alarm
        if(time % 20 == 0)
            Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {
                if(playersInside.contains(cur))
                    cur.playSound(cur.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 2);
                else
                    cur.playSound(bank.getBankCuboid().getCenter(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 2);
            });




    }


    private void updateSwat() {
        //ring bigger alarm
        if(time % 20 == 0)
            Bukkit.getOnlinePlayers().stream().filter(cur -> ((Player) cur).getWorld().equals(bank.getWorld())).forEach(cur -> {
                if(playersInside.contains(cur))
                    cur.playSound(cur.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.2F, 2);
                else
                    cur.playSound(bank.getBankCuboid().getCenter(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2, 2);
            });


    }

    public void endHoldUp() {
        if(!isHoldup()) return;
        if(squad != null) {
            bank.getPoliceStaff().resetSquadTargets();
            bank.getBankerStaff().resetSquadTargets();
            bank.getHostessStaff().resetSquadTargets();
        }

        bank.getSwatTeam().refreshStaff();

        bank.getPoliceStaff().refreshStaff();
        bank.getBankerStaff().refreshStaff();
        bank.getHostessStaff().refreshStaff();


        if(holdUpTask != null && !holdUpTask.isCancelled())
            holdUpTask.cancel();

        bank.getChests().forEach(c -> c.cancel());

        getAllDroppedMoney().forEach(item -> item.remove());
        getAllDroppedMoney().clear();

        bank.getDoors_to_lock().forEach(d -> {
            d.reset();
        });

        phases.clear();
        this.time = 0;

        droppedKeys.forEach(k -> k.remove());
        getSquad().getSquadMembers().forEach(s -> {
            keys.forEach(k -> {
                s.getInventory().remove(k);
            });
        });
        keys.forEach(k -> {
            getSquad().getOwner().getInventory().remove(k);
        });
        virtualKeys.clear();
        keys.clear();
        droppedKeys.clear();
        this.squad = null;

        if(getDriller() != null)
            getDriller().destory();


        getC4s().forEach(c4 -> {
            c4.reset();
        });

        getC4s().clear();


        bank.getVaultDoor().close();
        bank.getVaultDoor().reset();

        isHoldup = false;
    }

    public Driller getDriller() {
        return driller;
    }

    public void setDriller(Driller driller) {
        this.driller = driller;
    }

    public ArrayDeque<ItemStack> getVirtualKeys() {
        return virtualKeys;
    }

    public List<Item> getAllDroppedMoney() {
        return allDroppedMoney;
    }

    public List<ItemStack> getKeys() {
        return keys;
    }

    public boolean isHoldup() {
        return isHoldup;
    }

    public Squad getSquad() {
        return squad;
    }

    public List<Player> getHostages() {
        return hostages;
    }

    public List<Player> getPlayersInside() {
        return playersInside;
    }

    public List<C4> getC4s() {
        return C4s;
    }

    public boolean isC4Sticked(Block block) {
        return getC4s().stream().filter(c4 -> c4.getStickedBlock() != null && c4.getStickedBlock().equals(block)).findAny().isPresent();
    }

    public List<Item> getDroppedKeys() {
        return droppedKeys;
    }
}
