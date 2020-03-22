package fr.cocoraid.prodigybank.listener;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class RobberToolPlaceEvent implements Listener {


    private ProdigyBank instance;
    private Language lang;
    public RobberToolPlaceEvent(ProdigyBank instance) {
        this.instance = instance;
        this.lang = instance.getLanguage();
    }


    @EventHandler
    public void place(BlockPlaceEvent e) {
        if(Utils.isSmilar(e.getItemInHand(), Driller.getDrillerItem())) {
            e.setCancelled(true);
            Bank.getHoldups().keySet().stream().filter(b -> b.getHoldUp().isHoldup()
                    && b.getHoldUp().getSquad().isFromSquad(e.getPlayer())
                    && b.getBankCuboid().isIn(e.getBlock().getLocation())).findAny().ifPresent(bank -> {

                if(bank.getHoldUp().getDriller() != null) {
                    e.getPlayer().sendMessage(lang.driller_already);
                    return;
                }

                if (bank.getVaultDoor().getCuboid().getCenter().distanceSquared(e.getPlayer().getLocation()) < 5 * 5) {
                    e.getPlayer().sendMessage(lang.driller_too_close);
                    return;
                }

                if (bank.getVaultDoor().getCuboid().getCenter().distanceSquared(e.getPlayer().getLocation()) > 8 * 8) {
                    e.getPlayer().sendMessage(lang.driller_too_far);
                    return;
                }
                Player p = e.getPlayer();
                ItemStack i = e.getItemInHand();
                if (i.getAmount() > 1) i.setAmount(i.getAmount() - 1);
                else p.getInventory().clear(p.getInventory().getHeldItemSlot());
                bank.getHoldUp().setDriller(new Driller(bank));
                Location l = e.getBlock().getLocation().add(0.5, 0, 0.5);
                l.setDirection(bank.getVaultDoor().getCuboid().getCenter().toVector().subtract(l.toVector()));
                l.setPitch(0);
                bank.getHoldUp().getDriller().build(e.getPlayer(), l);
                e.getPlayer().sendMessage(lang.driller_building);
            });
            e.getPlayer().updateInventory();
        }
    }
}
