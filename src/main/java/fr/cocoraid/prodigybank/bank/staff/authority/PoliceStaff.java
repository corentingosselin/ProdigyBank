package fr.cocoraid.prodigybank.bank.staff.authority;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.ArrayList;
import java.util.List;

public class PoliceStaff extends Staff  {

    private List<NPC> police = new ArrayList<>();
    public PoliceStaff(Bank bank) {
        super(bank);
    }

    @Override
    public void addSquadTarget() {
        super.addSquadTarget();
        bank.getPoliceStaff().getPolice().forEach(p -> {
            SentinelTrait sentinel = p.getTrait(SentinelTrait.class);
            p.getTrait(LookClose.class).setRandomLook(false);
            p.getTrait(LookClose.class).toggle();
            for (Player player : holdup.getSquad().getSquadMembers()) {
                new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(sentinel.allTargets);
            }
        });
    }

    @Override
    public void refreshStaff() {
        super.refreshStaff();
        police.forEach(p -> {
            p.despawn(DespawnReason.PENDING_RESPAWN);
            p.spawn(bankLoader.getPolicePos(p), SpawnReason.RESPAWN);
        });
    }

    @Override
    public void resetSquadTargets() {
        super.resetSquadTargets();
        police.forEach(p -> {
            SentinelTrait sentinel = p.getTrait(SentinelTrait.class);
            holdup.getSquad().getSquadMembers().forEach(s -> {
                new SentinelTargetLabel("uuid:" + s.getUniqueId()).removeFromList(sentinel.allTargets);
            });
        });
    }



    public void addPoliceMember(Player player, Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, config.getRandomPoliceSkin());
        npc.addTrait(LookClose.class);
        npc.getTrait(LookClose.class).setRange(2);
        npc.getTrait(LookClose.class).setRandomLook(true);
        npc.getTrait(LookClose.class).setRealisticLooking(true);
        npc.getTrait(LookClose.class).toggle();
        npc.addTrait(SentinelTrait.class);
        npc.getTrait(Equipment.class)
                .set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_SWORD, 1));

        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
        sentinel.fightback = true;
        sentinel.realistic = false;

        sentinel.speed = 1.2F;
        npc.getNavigator().getLocalParameters().baseSpeed(1.2F);
        sentinel.squad = "police";
        sentinel.respawnTime = 0;
        npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);

        sentinel.attackRate = 4;
        sentinel.range = 100;
        sentinel.chaseRange = 100;
        npc.getNavigator().getDefaultParameters().range((float) 100);
        npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA,true);
        npc.data().setPersistent("nameplate-visible", false);
        npc.spawn(location);
        police.add(npc);
        player.sendMessage(
                new StringBuilder(lang.object_added).toString()
                        .replace("%id",String.valueOf(npc.getId()))
                        .replace("%type", "police member")
        );
        bankLoader.setPolicePos(npc);
    }


    public void setPolice(List<NPC> police) {
        this.police = police;
    }

    public void removePoliceMember(NPC npc) {
        police.remove(npc);
        bankLoader.savePolice(true);
    }

    public boolean isPoliceMember(NPC npc) {
       return police.contains(npc);

    }

    public boolean isPoliceMember(Entity entity) {
        //death entity event, npc is called 2 times, 1 with null
        if(entity == null) return false;
        return entity.hasMetadata("NPC") && police.stream().filter(npc -> npc.getName().equals(entity.getName())).findAny().isPresent();

    }


    public List<NPC> getPolice() {
        return police;
    }
}