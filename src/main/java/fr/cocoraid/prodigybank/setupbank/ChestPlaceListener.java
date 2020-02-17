package fr.cocoraid.prodigybank.setupbank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class ChestPlaceListener implements Listener {

    private SetupBankProcess process;
    private Language lang;
    public ChestPlaceListener(ProdigyBank instance) {
        this.process = instance.getSetupBankProcess();
        this.lang = instance.getLanguage();
    }

    @EventHandler
    public void chestPlace(BlockPlaceEvent e) {
        if(e.getBlock() != null && (e.getBlock().getType() == Material.ENDER_CHEST)) {
            if (e.getPlayer().equals(process.getAdmin()) && process.isWaitingForChest()) {
                process.addChest(e.getBlock());
            }
        }
    }

    @EventHandler
    public void chestRemove(BlockBreakEvent e) {
        if(e.getBlock() != null && (e.getBlock().getType() == Material.ENDER_CHEST)) {
            if (e.getPlayer().equals(process.getAdmin()) && process.isWaitingForChest()) {
                process.removeChest(e.getBlock());
                e.getPlayer().sendMessage(lang.chest_removed);
            }
        }
    }

}
