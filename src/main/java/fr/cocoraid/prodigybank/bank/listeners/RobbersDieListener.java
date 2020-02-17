package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        if(!bank.isHoldup()) return;
        if(bank.getSquad().getSquad().contains(e.getEntity())) {
            bank.getSquad().getSquad().remove(e.getEntity());
            if(bank.getSquad().getOwner().equals(e.getEntity())) {
                e.setDeathMessage("");
                bank.getSquad().getSquad().forEach(s -> {
                    bank.failSquadMember(s,config.getPercentDie());
                });
                //30% of money
                bank.getSquad().sendTeamSubTitle(new StringBuilder(lang.owner_died).toString()
                        .replace("%percentage", String.valueOf(config.getPercentDie())));

                bank.getSquad().sendOwnerSubTitle(new StringBuilder(lang.go_to_jail).toString()
                        .replace("%percentage", String.valueOf(config.getPercentDie())));
                bank.endHoldUp();

            }
        }
    }

}