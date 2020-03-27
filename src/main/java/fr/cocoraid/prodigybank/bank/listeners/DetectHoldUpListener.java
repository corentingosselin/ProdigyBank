package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.Squad;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.concurrent.ThreadLocalRandom;

public class DetectHoldUpListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public DetectHoldUpListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
    }

    @EventHandler
    public void detectHoldup(NPCLeftClickEvent e) {
        Player p = e.getClicker();
        if(!p.hasPermission("prodigybank.holdup")) {
            e.setCancelled(true);
            return;
        }
        if(!bank.isStaffMember(e.getNPC())) {
            return;
        }
        if(bank.getHoldUp().isHoldup()) {
            return;
        }

        //if player does not have any squad, we create an empty one
        //if player has squad without member
        Squad squad = instance.getSquad(p);
        if(!instance.getSquads().containsKey(p.getUniqueId()) && squad == null)
            instance.createSquad(p);
        else {
            //we check if the member who interact is the leader
            if(squad != null && !squad.getOwner().equals(p)) {
                p.sendMessage("§cYou can't start holdup because you are not the leader");
                return;
            }
        }
        //we use that squad and we check if all member are inside the bank, if not teleport missing player inside the bank to the leader
        bank.getHoldUp().startHoldup(instance.getSquads().get(p.getUniqueId()));
        bank.getHoldUp().getSquad().getSquadMembers().stream().filter(cur -> !bank.getBankCuboid().isIn(cur)).forEach(cur -> {
            cur.teleport(bank.getHoldUp().getSquad().getOwner());
        });

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamage(EntityDamageByEntityEvent e) {

        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();
            if(bank.getHoldUp().isHoldup()) {
                if (bank.getBankCuboid().isIn(damager) && bank.getBankCuboid().isIn(victim)) {
                    e.setCancelled(false);
                }
            }


        }
    }

    @EventHandler
    public void pickupKey(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player) {
            if(bank.getHoldUp().isHoldup()) {
                if(bank.getHoldUp().getSquad().isFromSquad((Player)e.getEntity())) {
                    if (bank.getHoldUp().getDroppedKeys().contains(e.getItem())) {
                        bank.getHoldUp().getDroppedKeys().remove(e.getItem());
                    }
                }
            }
        }
    }

    //todo make other player not squad, can't damage staff
    @EventHandler
    public void detectDamage(NPCDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if(bank.isStaffMember(e.getNPC())) {
                if(bank.getHoldUp().isHoldup()) {
                    if(!bank.getHoldUp().getSquad().isFromSquad(p)) {
                        e.setCancelled(true);
                    }

                    if(bank.getBankerStaff().getBanker().equals(e.getNPC())) {
                        if(!bank.getHoldUp().getVirtualKeys().isEmpty()) {
                            int randomNum = ThreadLocalRandom.current().nextInt(0, 100);
                            if (randomNum >= 80) {
                                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG,1,1);
                                Item item = e.getNPC().getStoredLocation().getWorld()
                                        .dropItemNaturally(e.getNPC().getStoredLocation(), bank.getHoldUp().getVirtualKeys().getFirst());
                                bank.getHoldUp().getKeys().add(bank.getHoldUp().getVirtualKeys().removeFirst());
                                bank.getHoldUp().getDroppedKeys().add(item);
                            }
                        } else {
                            p.sendTitle("", instance.getLanguage().no_more_key, 20 * 2, 5, 5);
                            p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_NO,1,2);
                        }
                    }
                    if(bank.isNonViolentStaffMember(e.getNPC())) {
                        e.setDamage(0);
                    }
                } else {
                    e.setCancelled(true);
                }

            }
        }
    }


}
