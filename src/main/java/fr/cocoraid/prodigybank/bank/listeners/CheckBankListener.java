package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.bank.Squad;
import fr.cocoraid.prodigybank.customevents.EnterBankEvent;
import fr.cocoraid.prodigybank.customevents.QuitBankEvent;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.utils.Utils;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckBankListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    private Language lang;
    public CheckBankListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
        this.lang = instance.getLanguage();
    }


    //cancel enderchest breaking vault
    @EventHandler
    public void chestBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.ENDER_CHEST) {
            if(!bank.getChests().stream().filter(c -> c.getChest().equals(e.getBlock())).findAny().isPresent()) return;
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void teleport(PlayerTeleportEvent e) {
        if(e.getPlayer().hasMetadata("NPC")) return;
        if(!bank.getHoldUp().isHoldup()) return;
        if(bank.getHoldUp().getSquad().getSquadMembers().contains(e.getPlayer())
                || bank.getHoldUp().getSquad().getOwner().equals(e.getPlayer())
                || bank.getHoldUp().getHostages().contains(e.getPlayer())) {
            e.getPlayer().sendMessage(lang.tp_not_allowed);
            e.setCancelled(true);
        }
    }




    //prevent death message
    @EventHandler
    public void death(PlayerDeathEvent e) {
        if(bank.getHoldUp().isHoldup())
            if(bank.getPoliceStaff().isPoliceMember(e.getEntity()) || bank.getSwatTeam().isSwatMember(e.getEntity()))e.setDeathMessage("");
    }

    // prevent unknown people to enter inside the bank
    @EventHandler
    public void strangerEnter(EnterBankEvent e) {
        if(!e.getBank().equals(bank)) return;
        //if squad member or hostage, lets ignore
        if(e.getBank().getHoldUp().getSquad().isFromSquad(e.getPlayer())) return;
        if(e.getBank().getHoldUp().getHostages().contains(e.getPlayer())) return;
        e.setCancelled(true);
        //Utils.bumpEntity(cur,cur.getLocation(),3,0.1D);
        String msg = instance.getLanguage().title_area_restricted;
        Utils.sendTitle(e.getPlayer(),msg);
    }

    @EventHandler
    public void quitTheBank(QuitBankEvent e) {
        if(!e.getBank().equals(bank)) return;
        HoldUp holdup = e.getBank().getHoldUp();
        if(holdup.getHostages().contains(e.getPlayer())) {
            holdup.getHostages().remove(e.getPlayer());
            return;
        }
        boolean robbed = bank.getVaultDoor().isDestroyed() && bank.getChests().stream().filter(c -> c.isChestOpened()).findAny().isPresent();
        //check if the leader is leaving the bank
        if(holdup.getSquad().getOwner().equals(e.getPlayer())) {
            //check for vault destroyed and 1 chest opened at least
            if(robbed) {
                holdup.succed();
            } else {
                holdup.abandon(e.getPlayer());
            }
        } else {
            // squad is leaving the bank without robbery
            if(!robbed && holdup.getSquad().getSquadMembers().contains(e.getPlayer())) {
                holdup.getSquad().removeSquadMember(e.getPlayer(), Squad.MemberFailedType.LEFT);
            }
        }

    }
}
