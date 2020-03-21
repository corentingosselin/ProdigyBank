package fr.cocoraid.prodigybank.setupbank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class RobberToolPlaceEvent implements Listener {


    private ProdigyBank instance;
    public RobberToolPlaceEvent(ProdigyBank instance) {
        this.instance = instance;
    }


    @EventHandler
    public void place(BlockPlaceEvent e) {
        if(e.getItemInHand().equals(Driller.getDrillerItem())) {
            e.setCancelled(true);
            Bank.getHoldups().keySet().stream().filter(b -> b.getHoldUp().isHoldup()
                    && b.getHoldUp().getSquad().isFromSquad(e.getPlayer())
                    && b.getBankCuboid().isIn(e.getBlock().getLocation())).findAny().ifPresent(bank -> {
                if (bank.getVaultCuboid().getCenter().distanceSquared(e.getPlayer().getLocation()) > 200) {
                    e.getPlayer().sendMessage("§cYou are too far from the vault door to place the driller");
                    return;
                }
                e.getPlayer().getInventory().remove(e.getItemInHand());
                bank.getHoldUp().setDriller(new Driller(bank));
                Location l = e.getBlock().getLocation().add(0.5, 0, 0.5);
                l.setDirection(bank.getVaultDoor().getCuboid().getCenter().toVector().subtract(l.toVector()));
                l.setPitch(0);
                bank.getHoldUp().getDriller().build(e.getPlayer(), l);
                e.getPlayer().sendMessage("§6Building started ! please stay near from the driller to build it...");
            });
            e.getPlayer().updateInventory();
        }
    }
}
