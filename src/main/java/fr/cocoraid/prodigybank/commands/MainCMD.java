package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigybank.ProdigyBank;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

@CommandAlias("prodigybank|pb")
public class MainCMD extends BaseCommand {

    private ProdigyBank instance;
    public MainCMD(ProdigyBank instance) {
        this.instance = instance;
    }

    @Default
    @Description("Show Help menu")
    public static void help(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("ceci est une aide");
            Location l = player.getLocation();
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
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
            //ProdigyBank.getInstance().getBank().getHoldUp().endHoldUp();

        }
    }

    @Subcommand("test") @Conditions("bank")
    public void onTest(Player player) {
        Bukkit.broadcastMessage("build ");
        //instance.getArmorStandModel().getDriller().build(player.getLocation());
    }
}
