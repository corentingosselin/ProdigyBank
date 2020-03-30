package fr.cocoraid.prodigybank.bank.staff.member;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

public class HostessStaff extends Staff {
    private NPC depositHostess;
    private NPC withdrawHostess;


    public HostessStaff(Bank bank, NPC depositHostess, NPC withdrawHostess) {
        super(bank);
        this.depositHostess = depositHostess;
        this.withdrawHostess = withdrawHostess;

    }

    @Override
    public void addSquadTarget() {
        super.addSquadTarget();
        SentinelTrait hostessB = depositHostess.getTrait(SentinelTrait.class);
        SentinelTrait hostessA = withdrawHostess.getTrait(SentinelTrait.class);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).addToList(hostessA.allAvoids);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).addToList(hostessB.allAvoids);
        for (Player player : holdup.getSquad().getSquadMembers()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(hostessA.allAvoids);
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(hostessB.allAvoids);
        }

    }

    @Override
    public void resetSquadTargets() {
        super.resetSquadTargets();
        SentinelTrait hostessB = depositHostess.getTrait(SentinelTrait.class);
        SentinelTrait hostessA = withdrawHostess.getTrait(SentinelTrait.class);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).removeFromList(hostessA.allAvoids);
        new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).removeFromList(hostessB.allAvoids);
        for (Player player : holdup.getSquad().getSquadMembers()) {
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).removeFromList(hostessA.allAvoids);
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).removeFromList(hostessB.allAvoids);
        }
    }

    @Override
    public void refreshStaff() {
        super.refreshStaff();

        depositHostess.teleport(bankLoader.getDepositHostessSpawnPoint(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        withdrawHostess.teleport(bankLoader.getWithdrawHostessSpawnPoint(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        //depositHostess.despawn(DespawnReason.PENDING_RESPAWN);
        //depositHostess.spawn(bankLoader.getDepositHostessSpawnPoint(), SpawnReason.RESPAWN);

        //withdrawHostess.despawn(DespawnReason.PENDING_RESPAWN);
        //withdrawHostess.spawn(bankLoader.getWithdrawHostessSpawnPoint(), SpawnReason.RESPAWN);


    }

    public NPC getDepositHostess() {
        return depositHostess;
    }

    public NPC getWithdrawHostess() {
        return withdrawHostess;
    }

    public boolean isHostessMember(NPC npc) {
        return depositHostess.getId() == npc.getId() || withdrawHostess.getId() == npc.getId();
    }
}
