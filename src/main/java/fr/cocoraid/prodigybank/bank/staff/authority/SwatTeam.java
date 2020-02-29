package fr.cocoraid.prodigybank.bank.staff.authority;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.ArrayList;
import java.util.List;

public class SwatTeam extends Staff {


    private List<Location> swat_points = new ArrayList<>();
    private List<NPC> swats = new ArrayList<>();


    private Bank bank;
    public SwatTeam(Bank bank) {
        super(bank);
    }


    @Override
    public void refreshStaff() {
        super.refreshStaff();
        swats.stream().filter(s -> s.isSpawned()).forEach(s -> s.destroy());
        swats.clear();

    }

    public void spawnSwat() {
        swat_points.forEach(s -> {
            for (int k = 0; k < config.getSwatPerPoint(); k++) {
                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, config.getRandomSwatSkin());
                npc.addTrait(SentinelTrait.class);
                npc.getTrait(Equipment.class)
                        .set(Equipment.EquipmentSlot.HAND,  new ItemStack(Material.DIAMOND_SWORD));
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta shieldMeta = shield.getItemMeta();
                BlockStateMeta bmeta = (BlockStateMeta) shieldMeta;
                Banner banner = (Banner) bmeta.getBlockState();
                banner.setBaseColor(DyeColor.BLACK);
                banner.update();
                bmeta.setBlockState(banner);
                shield.setItemMeta(bmeta);
                npc.getTrait(Equipment.class)
                        .set(Equipment.EquipmentSlot.OFF_HAND, shield);

                npc.getNavigator().getDefaultParameters().range((float) 100);
                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                sentinel.fightback = true;
                sentinel.chaseRange = 100;
                sentinel.range = 100;
                sentinel.health = 30;
                sentinel.attackRate = 5;
                sentinel.speed = 1.25F;
                npc.getNavigator().getLocalParameters().baseSpeed(1.25F);
                sentinel.squad = "swat";
                sentinel.respawnTime = -1;
                npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);
                npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA, true);
                npc.data().setPersistent("nameplate-visible", false);
                npc.spawn(s);

                holdup.getSquad().getSquadMembers().forEach(cur -> {
                    new SentinelTargetLabel("uuid:" + cur.getUniqueId()).addToList(sentinel.allTargets);
                });
                swats.add(npc);
            }
        });
    }

    public void setSwat_points(List<Location> swat_points) {
        this.swat_points = swat_points;
    }


    public boolean isSwatMember(NPC npc) {
        return swats.contains(npc);
    }

    public boolean isSwatMember(Entity entity) {
        //death entity event, npc is called 2 times, 1 with null
        if(entity == null) return false;
        return entity.hasMetadata("NPC") && swats.stream().filter(npc -> npc.getEntity().getName().equals(entity.getName())).findAny().isPresent();

    }

    public void addSwatSpawnPoint(Player player, Location location) {
        swat_points.add(location);
        player.sendMessage(
                new StringBuilder(lang.object_added).toString()
                        .replace("%id",String.valueOf(swat_points.size() - 1))
                        .replace("%type", "swat spawn point")
        );
        bankLoader.saveSwat(true);
    }

    public List<Location> getSwat_points() {
        return swat_points;
    }
}
