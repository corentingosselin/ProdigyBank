package fr.cocoraid.prodigybank.bank.staff.authority;

import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.staff.Staff;
import fr.cocoraid.prodigybank.filemanager.skin.SkinData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
                npc.addTrait(SentinelTrait.class);

                ItemStack weapon;
                if(Math.random() >= 0.1) {
                    weapon = new ItemStack(Material.DIAMOND_SWORD, 1);
                    weapon.addEnchantment(Enchantment.DAMAGE_ALL, 3);
                } else {
                    weapon = new ItemStack(Material.BOW, 1);
                }
                npc.getTrait(Equipment.class)
                        .set(Equipment.EquipmentSlot.HAND, weapon);

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
                npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
                sentinel.fightback = true;
                sentinel.chaseRange = 100;
                sentinel.range = 100;
                sentinel.health = 40;
                sentinel.attackRate = 6;
                sentinel.speed = 1.25F;
                npc.getNavigator().getLocalParameters().baseSpeed(1.25F);
                sentinel.squad = "swat";
                sentinel.respawnTime = -1;
                npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);
                npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA, true);

                npc.spawn(s);
                SkinData data = config.getRandomSwatSkin();
                ((SkinnableEntity) npc.getEntity()).setSkinPersistent(UUID.randomUUID().toString(),data.getSignature(),data.getTexture());
                new SentinelTargetLabel("uuid:" + holdup.getSquad().getOwner().getUniqueId()).addToList(sentinel.allTargets);
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
        if(!entity.hasMetadata("NPC")) return false;
        SentinelTrait sentinel = SentinelUtilities.tryGetSentinel(entity);
        if (sentinel != null) {
           return swats.stream().filter(npc -> npc.equals(sentinel.getNPC())).findAny().isPresent();
        }
        return false;

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
