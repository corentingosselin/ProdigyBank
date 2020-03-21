package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlaceC4Listener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public PlaceC4Listener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
    }

    @EventHandler
    public void place(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = e.getPlayer();
            if (p.getInventory().getItemInMainHand().equals(C4.getItem()) || p.getInventory().getItemInOffHand().equals(C4.getItem())) {
                e.setCancelled(true);
                if (bank.getHoldUp().isHoldup()) {
                    if (bank.getHoldUp().getSquad().isFromSquad(p)) {
                        bank.getDoors_to_lock().stream().filter(d -> d.isDoorBlock(e.getClickedBlock()) && bank.getHoldUp().isC4Sticked(e.getClickedBlock())).findAny().ifPresent(d -> {
                            C4 c4 = new C4(d);
                            c4.place(e.getClickedBlock());
                            bank.getHoldUp().getC4s().add(c4);
                        });
                    }
                }
            }
        }
    }


}
