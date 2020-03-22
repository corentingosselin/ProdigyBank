package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.Squad;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RobbersDieListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    private Language lang;
    private ConfigLoader config;
    public RobbersDieListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
        this.lang = instance.getLanguage();
        this.config = instance.getConfigLoader();
    }

    @EventHandler
    public void rooberDie(PlayerDeathEvent e) {
        if(!bank.getHoldUp().isHoldup()) return;
        Squad squad = bank.getHoldUp().getSquad();
        if(squad.getOwner().equals(e.getEntity())) {
            e.setDeathMessage("");
            bank.getHoldUp().failOwnerDied(e.getEntity());
        } else if(squad.getSquadMembers().contains(e.getEntity())) {
            squad.removeSquadMember(e.getEntity(), Squad.MemberFailedType.DIED);
            squad.sendSubTitle(e.getEntity(),lang.title_member_self_died);
            String msg = new StringBuilder(lang.title_member_notify_died).toString().replace("%player",e.getEntity().getName());
            squad.sendOwnerSubTitle(msg);
            squad.getSquadMembers().stream().filter(cur -> !cur.equals(e.getEntity())).forEach(cur -> {
                squad.sendSubTitle(cur,msg);
            });
        }
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent e) {
        if(bank.getHoldUp().isHoldup()) {
                if(bank.getHoldUp().getSquad().getOwner().equals(e.getPlayer())) {
                    //notify squad
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(e.getPlayer().isOnline()) {

                            } else {
                                bank.getHoldUp().abandon(e.getPlayer());

                            }
                        }
                    }.runTaskLater(instance,20 * 10);
                } else if(bank.getHoldUp().getSquad().getSquadMembers().contains(e.getPlayer())) {
                    Squad squad = bank.getHoldUp().getSquad();
                    squad.getSquadMembers().remove(e.getPlayer());
                    String msg = new StringBuilder(lang.title_member_notify_died).toString().replace("%player",e.getPlayer().getName());
                    squad.sendOwnerSubTitle(msg);
                    squad.getSquadMembers().stream().filter(cur -> !cur.equals(e.getPlayer())).forEach(cur -> {
                        squad.sendSubTitle(cur,msg);
                    });
                }



        }

    }

}