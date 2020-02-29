package fr.cocoraid.prodigybank.bank.staff.member;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;

public class BankerStaff extends Staff  {

    private NPC banker;
    public BankerStaff(Bank bank, NPC banker) {
        super(bank);
        this.banker = banker;
    }


    @Override
    public void refreshStaff() {
        super.refreshStaff();
        banker.despawn(DespawnReason.PENDING_RESPAWN);
        banker.spawn(bankLoader.getBankerSpawnPoint(), SpawnReason.RESPAWN);
    }


    public NPC getBanker() {
        return banker;
    }

    public boolean isBanker(NPC npc) {
        return npc.getId() == banker.getId();
    }
}
