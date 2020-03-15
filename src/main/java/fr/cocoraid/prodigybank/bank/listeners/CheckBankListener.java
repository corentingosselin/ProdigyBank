package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.customevents.EnterBankEvent;
import fr.cocoraid.prodigybank.utils.Utils;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckBankListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public CheckBankListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
    }


    //cancel enderchest breaking vault
    @EventHandler
    public void chestBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.ENDER_CHEST) {
            if(!bank.getChests().stream().filter(c -> c.getChest().equals(e.getBlock())).findAny().isPresent()) return;
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
        if(e.getBank().getHoldUp().getSquad().getSquadMembers().contains(e.getPlayer())) return;
        if(e.getBank().getHoldUp().getHostages().contains(e.getPlayer())) return;
        e.setCancelled(true);
        //Utils.bumpEntity(cur,cur.getLocation(),3,0.1D);
        String msg = instance.getLanguage().area_restricted;
        Utils.sendTitle(e.getPlayer(),msg);
    }

    @EventHandler
    public void quitTheBank(EnterBankEvent e) {
        if(!e.getBank().equals(bank)) return;
        HoldUp holdup = e.getBank().getHoldUp();
        if(holdup.getHostages().contains(e.getPlayer())) {
            //hostage escape

        } else if(holdup.getSquad().getOwner().equals(e.getPlayer())) {

            if(bank.getVaultDoor().isDestroyed() && bank.getChests().stream().filter(c -> c.isChestOpened()).findAny().isPresent()) {
                //sucess
                squad.reward();
                squad.sendSubTitle(lang.bank_left_robbed);
                holdup.endHoldUp();
                return;
            } else {
                squad.getSquadMembers().forEach(s -> squad.failSquadMember(s, config.getPercentJailed()));
                squad.sendTeamSubTitle(lang.bank_leader_left_not_robbed);
                squad.sendOwnerSubTitle(lang.bank_left_not_robbed);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        squad.sendSubTitle(new StringBuilder(lang.go_to_jail).toString().replace("%percentage",String.valueOf(config.getPercentJailed())));
                    }
                }.runTaskLater(instance, 20 * 3);
                endHoldUp();
                return;

            }

        }

    }
}
