package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamage(EntityDamageByEntityEvent e) {
        if(bank.getHoldUp().isHoldup()) {
            if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                Player damager = (Player) e.getDamager();
                Player victim = (Player) e.getEntity();
                if(bank.getBankCuboid().isIn(damager) && bank.getBankCuboid().isIn(victim)) {
                    e.setCancelled(false);
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
                if(!bank.getHoldUp().isHoldup()) {
                    if(!instance.getSquads().containsKey(p.getUniqueId()))
                        instance.createSquad(p);

                    bank.getHoldUp().startHoldup(instance.getSquads().get(p.getUniqueId()));
                } else {
                    if(bank.getBankerStaff().getBanker().equals(e.getNPC())) {
                        if(!bank.getHoldUp().getKeys().isEmpty()) {
                            int randomNum = ThreadLocalRandom.current().nextInt(0, 100);
                            if (randomNum >= 80) {
                                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG,1,1);
                                e.getNPC().getStoredLocation().getWorld()
                                        .dropItemNaturally(e.getNPC().getStoredLocation(), bank.getHoldUp().getKeys().getFirst());
                                bank.getHoldUp().getKeys().removeFirst();
                            }
                        } else {
                            p.sendTitle("", instance.getLanguage().no_more_key, 20 * 2, 5, 5);
                            p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_NO,1,2);
                        }
                    }

                }
                if(bank.isNonViolentStaffMember(e.getNPC())) {
                    e.setDamage(0);
                }
            }
        }
    }


}
