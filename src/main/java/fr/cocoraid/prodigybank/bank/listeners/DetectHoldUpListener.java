package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.Squad;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class DetectHoldUpListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    private Language lang;
    public DetectHoldUpListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
        this.lang = instance.getLanguage();
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


    //Allow or prevent damage is holdup or not
    //IS TRIGGERED the LAST
    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamage(EntityDamageByEntityEvent e) {
        Bukkit.broadcastMessage("test5");
        if(bank.getBankCuboid().isIn(e.getEntity().getLocation()) || bank.getBankCuboid().isIn(e.getDamager().getLocation())) {
            Bukkit.broadcastMessage("test6");
        if(bank.getHoldUp().isHoldup()) {
            if(bank.getHoldUp().getSquad().getOwner().equals(e.getEntity())) {
                Player owner = (Player) e.getEntity();
                if(owner.getHealth() <= (owner.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue() / 3)) {
                    owner.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,20*7,2,false));
                    bank.getHoldUp().getSquad().sendTeamSubTitle(lang.title_leader_low_health);
                }
            }
            e.setCancelled(false);
        } else {
                e.setCancelled(true);
                Bukkit.broadcastMessage("test");
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

    @EventHandler
    public void dropKey(EntityDropItemEvent e) {
        if(e.getEntity() instanceof Player) {
            if(bank.getHoldUp().isHoldup()) {
                if(bank.getHoldUp().getSquad().isFromSquad((Player)e.getEntity())) {
                    //is key
                    if(bank.getHoldUp().getKeys().contains(e.getItemDrop().getItemStack())) {
                        bank.getHoldUp().getDroppedKeys().add(e.getItemDrop());
                    }
                }
            }
        }
    }


    @EventHandler
    public void detectDamage(NPCDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            Bukkit.broadcastMessage("test1");
            if(bank.isStaffMember(e.getNPC())) {
                Bukkit.broadcastMessage("test3");
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
                }else {
                    Bukkit.broadcastMessage("test2");
                    e.setCancelled(true);
                }
            }
        }
    }


}
