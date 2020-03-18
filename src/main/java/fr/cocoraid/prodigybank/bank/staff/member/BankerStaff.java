package fr.cocoraid.prodigybank.bank.staff.member;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

public class BankerStaff extends Staff  {

    private NPC banker;
    public BankerStaff(Bank bank, NPC banker) {
        super(bank);
        this.banker = banker;
    }


    @Override
    public void refreshStaff() {
        super.refreshStaff();
        SentinelTrait bankerTrait = banker.getTrait(SentinelTrait.class);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).removeFromList(bankerTrait.allAvoids);
        for (Player player : holdup.getSquad().getSquadMembers()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).removeFromList(bankerTrait.allAvoids);
        }
        banker.despawn(DespawnReason.PENDING_RESPAWN);
        banker.spawn(bankLoader.getBankerSpawnPoint(), SpawnReason.RESPAWN);
    }


    public void scared() {
        SentinelTrait bankerTrait = banker.getTrait(SentinelTrait.class);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).addToList(bankerTrait.allAvoids);
        for (Player player : holdup.getSquad().getSquadMembers()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(bankerTrait.allAvoids);
        }
    }

    public NPC getBanker() {
        return banker;
    }

    public boolean isBanker(NPC npc) {
        return npc.getId() == banker.getId();
    }
}
