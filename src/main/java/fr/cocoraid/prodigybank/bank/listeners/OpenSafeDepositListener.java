package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class OpenSafeDepositListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public OpenSafeDepositListener(ProdigyBank instance) {
        this.instance = instance;
        this.bank = instance.getBank();
    }

    @EventHandler
    public void pickupMoney(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player) {
            if(!bank.getHoldUp().isHoldup()) return;
            Player p = (Player) e.getEntity();
            if(!bank.getHoldUp().getSquad().isFromSquad(p)) {
                e.setCancelled(true);
                return;
            }
            if(bank.getHoldUp().getAllDroppedMoney().contains(e.getItem())) {
                e.setCancelled(true);
                e.getItem().remove();
                bank.getHoldUp().getSquad().addMoney(Integer.valueOf(e.getItem().getItemStack().getItemMeta().getLocalizedName()));
            }
        }
    }


    //todo when key dropped, add to dropped list key

    @EventHandler
    public void cancelKeyPlace(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Bank b = instance.getBank();
            if (!b.getHoldUp().isHoldup()) return;
            if (b.getHoldUp().getKeys().contains(e.getPlayer().getInventory().getItemInMainHand())
                    || b.getHoldUp().getKeys().contains(e.getPlayer().getInventory().getItemInOffHand())) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void chestOpen(PlayerInteractEvent e) {
        if(!bank.getHoldUp().isHoldup()) return;
        if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
            if(!bank.getChests().stream().filter(c -> c.getChest().equals(e.getClickedBlock())).findAny().isPresent()) return;
            e.setCancelled(true);
            Block chest = e.getClickedBlock();
            String chestKey = chest.getMetadata("key").get(0).asString();
            Player p = e.getPlayer();

            bank.getChests().stream().filter(c -> !c.isChestOpened() && c.getKey().getItemMeta().getLocalizedName().equalsIgnoreCase(chestKey)).findAny().ifPresent(c -> {
                if(p.getInventory().getItemInMainHand().hasItemMeta()
                        && p.getInventory().getItemInMainHand().getItemMeta().getLocalizedName() != null
                        && p.getInventory().getItemInMainHand().getItemMeta().getLocalizedName().equalsIgnoreCase(chestKey)) {
                    p.getInventory().remove(p.getInventory().getItemInMainHand());
                    c.openChest(bank);
                } else {
                    chest.getWorld().playSound(chest.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,1F,2F);
                }
            });

        }
    }
}
