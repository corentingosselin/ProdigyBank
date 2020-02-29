package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CancelBankListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public CancelBankListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
    }

    @EventHandler
    public void chestBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.ENDER_CHEST) {
            if(!bank.getChests().stream().filter(c -> c.getChest().equals(e.getBlock())).findAny().isPresent()) return;
            e.setCancelled(true);
        }
    }



    @EventHandler
    public void death(PlayerDeathEvent e) {
        if(bank.getHoldUp().isHoldup())

            if(bank.getPoliceStaff().isPoliceMember(e.getEntity()) || bank.getSwatTeam().isSwatMember(e.getEntity()))e.setDeathMessage("");
    }

}
