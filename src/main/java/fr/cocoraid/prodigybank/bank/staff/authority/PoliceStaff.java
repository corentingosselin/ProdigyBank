package fr.cocoraid.prodigybank.bank.staff.authority;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import fr.cocoraid.prodigybank.filemanager.skin.SkinData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).addToList(sentinel.allTargets);
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
            p.getTrait(LookClose.class).setRandomLook(true);
            new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).removeFromList(sentinel.allTargets);
            holdup.getSquad().getSquadMembers().forEach(s -> {
                new SentinelTargetLabel("uuid:" + s.getUniqueId()).removeFromList(sentinel.allTargets);
            });
        });
    }



    public void addPoliceMember(Player player, Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
        npc.addTrait(LookClose.class);
        npc.getTrait(LookClose.class).setRange(2);
        npc.getTrait(LookClose.class).setRandomLook(true);
        npc.getTrait(LookClose.class).setRealisticLooking(true);
        npc.getTrait(LookClose.class).toggle();
        npc.addTrait(SentinelTrait.class);

        ItemStack weapon;
        if(Math.random() >= 0.2) {
            weapon = new ItemStack(Material.DIAMOND_SWORD, 1);
            weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        } else {
            weapon = new ItemStack(Material.BOW, 1);
        }
        npc.getTrait(Equipment.class)
                .set(Equipment.EquipmentSlot.HAND, weapon);

        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
        sentinel.fightback = true;
        sentinel.speed = 1.26F;
        npc.getNavigator().getLocalParameters().baseSpeed(1.26F);
        sentinel.squad = "police";
        sentinel.respawnTime = 0;
        npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);
        sentinel.range = 100;
        sentinel.chaseRange = 100;
        npc.getNavigator().getDefaultParameters().range((float) 100);
        npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA,true);
        npc.data().setPersistent("nameplate-visible", false);
        npc.spawn(location);
        SkinData data = config.getRandomPoliceSkin();
        ((SkinnableEntity) npc.getEntity()).setSkinPersistent(data.getUuid(),data.getSignature(),data.getTexture());
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
