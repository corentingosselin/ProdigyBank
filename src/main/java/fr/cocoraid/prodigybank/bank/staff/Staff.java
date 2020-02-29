package fr.cocoraid.prodigybank.bank.staff;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Staff {


    protected ProdigyBank instance = ProdigyBank.getInstance();
    protected Language lang = instance.getLanguage();
    protected ConfigLoader config = instance.getConfigLoader();
    protected BankLoader bankLoader = instance.getBankLoader();


    protected HoldUp holdup;

    protected Bank bank;
    public Staff(Bank bank) {
        this.bank = bank;
        this.holdup = Bank.getHoldups().get(bank);
    }


    public void resetSquadTargets() {

    }


    public void addSquadTarget() {
        if(!holdup.isHoldup()) return;
    }

    public void refreshStaff() {
        if(!holdup.isHoldup()) return;
    }


    /*public boolean isBankMember(NPC npc) {
        return depositHostess.getId() == npc.getId()
                || withdrawHostess.getId() == npc.getId()
                || banker.getId() == npc.getId();
    }

    public boolean isStaffMember(NPC npc) {
        return depositHostess.getId() == npc.getId()
                || withdrawHostess.getId() == npc.getId()
                || banker.getId() == npc.getId()
                || swats.contains(npc)
                || police.contains(npc);

    }


    public boolean isPoliceOrSwat(Entity entity) {
        if(police.stream().filter(p -> p.getName().equals(entity.getName())).findAny().isPresent()) return true;
        else if(swats.stream().filter(s -> s.getName().equals(entity.getName())).findAny().isPresent()) return true;
        else return false;
    }*/


}
