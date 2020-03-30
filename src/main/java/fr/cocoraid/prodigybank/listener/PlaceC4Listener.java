package fr.cocoraid.prodigybank.listener;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceC4Listener implements Listener {

    private ProdigyBank instance;
    public PlaceC4Listener(ProdigyBank instance) {
        this.instance = instance;
    }

    @EventHandler
    public void place(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = e.getPlayer();
            if(e.getItem() == null) return;
            if(!e.getItem().hasItemMeta()) return;
            if (Utils.isSmilar(e.getItem(), C4.getItem())) {
                e.setCancelled(true);
                Bank.getHoldups().keySet().stream().filter(b -> b.getHoldUp().isHoldup()
                        && b.getBankCuboid().isIn(e.getClickedBlock().getLocation())).findAny().ifPresent(bank -> {

                    if(!bank.getHoldUp().getSquad().isFromSquad(e.getPlayer())) {
                        return;
                    }

                    if (!bank.getHoldUp().isC4Sticked(e.getClickedBlock())) {
                        bank.getDoors_to_lock().stream().filter(d -> d.isDoorBlock(e.getClickedBlock())).findAny().ifPresent(d -> {
                            ItemStack i = e.getItem();
                            if (i.getAmount() > 1) i.setAmount(i.getAmount() - 1);
                            else p.getInventory().clear(p.getInventory().getHeldItemSlot());
                            C4 c4 = new C4(d);
                            c4.place(bank.getHoldUp(), e.getClickedBlock());
                            bank.getHoldUp().getC4s().add(c4);
                        });
                    } else {
                        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 2, 0);
                    }


                });
            }
        }
    }


}
