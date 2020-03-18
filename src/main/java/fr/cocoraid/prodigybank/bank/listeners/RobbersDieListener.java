package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.Squad;
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
        if(!bank.getHoldUp().isHoldup()) return;
        Squad squad = bank.getHoldUp().getSquad();
        if(squad.getOwner().equals(e.getEntity())) {
            e.setDeathMessage("");
            bank.getHoldUp().failOwnerDied(e.getEntity());
        } else if(squad.getSquadMembers().contains(e.getEntity())) {
            squad.removeSquadMember(e.getEntity(), Squad.MemberFailedType.DIED);
        }
    }

}