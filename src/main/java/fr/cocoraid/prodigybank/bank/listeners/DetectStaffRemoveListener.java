package fr.cocoraid.prodigybank.bank.listeners;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DetectStaffRemoveListener implements Listener {

    private ProdigyBank instance;
    private Bank bank;
    public DetectStaffRemoveListener(ProdigyBank instance) {
        this.bank = instance.getBank();
        this.instance = instance;
    }

    @EventHandler
    public void detectRemove(NPCRemoveEvent e) {
        if(bank.getPolice().contains(e.getNPC())) {
            bank.removePoliceMember(e.getNPC());
        } else if(bank.isBankMember(e.getNPC())) {
        }
    }

}
