package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import fr.cocoraid.prodigybank.filemanager.skin.SkinData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import java.util.UUID;

@CommandAlias("prodigybank|pb")
public class MainCMD extends BaseCommand {

    private static ProdigyBank instance;
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
            System.out.println("1 ");
            npc.addTrait(SentinelTrait.class);
            System.out.println("2 ");
            ItemStack weapon;
            // if(Math.random() >= 0.1) {
            weapon = new ItemStack(Material.DIAMOND_SWORD, 1);
            System.out.println("3 ");
            //weapon.addEnchantment(Enchantment.DAMAGE_ALL, 3);
               /* } else {
                    weapon = new ItemStack(Material.BOW, 1);
                }*/
            npc.getTrait(Equipment.class)
                    .set(Equipment.EquipmentSlot.HAND, weapon);
            System.out.println("4");

            ItemStack shield = new ItemStack(Material.SHIELD);
            ItemMeta shieldMeta = shield.getItemMeta();
            BlockStateMeta bmeta = (BlockStateMeta) shieldMeta;
            Banner banner = (Banner) bmeta.getBlockState();
            banner.setBaseColor(DyeColor.BLACK);
            banner.update();
            bmeta.setBlockState(banner);
            shield.setItemMeta(bmeta);
            System.out.println("5 ");
            //npc.getTrait(Equipment.class)
                   // .set(Equipment.EquipmentSlot.OFF_HAND, shield);

            npc.getNavigator().getDefaultParameters().range((float) 100);
            System.out.println("6");
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            System.out.println("7");
            npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            System.out.println("8");
            sentinel.fightback = true;
            System.out.println("9");
            sentinel.chaseRange = 100;
            System.out.println("10");
            sentinel.range = 100;
            System.out.println("11");
            sentinel.health = 60;
            System.out.println("12");
            sentinel.attackRate = 0;
            System.out.println("13");
            sentinel.avoidRange = 0;
            System.out.println("14");
            sentinel.closeChase = true;
            System.out.println("15");
            sentinel.reach = 3.2;

            System.out.println("16");
            sentinel.speed = 1.28F;
            System.out.println("17");
            npc.getNavigator().getLocalParameters().baseSpeed(1.28F);
            System.out.println("18");
            sentinel.squad = "swat";
            System.out.println("19");
            sentinel.respawnTime = -1;
            System.out.println("20");
            npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, -1);
            System.out.println("21");
            npc.data().setPersistent(NPC.PATHFINDER_OPEN_DOORS_METADATA, true);
            System.out.println("22");

            npc.spawn(player.getLocation());
            System.out.println("23");
            SkinData data = instance.getConfigLoader().getRandomSwatSkin();
            ((SkinnableEntity) npc.getEntity()).setSkinPersistent(UUID.randomUUID().toString(),data.getSignature(),data.getTexture());
            System.out.println("24");
            new SentinelTargetLabel("uuid:" + player.getUniqueId()).addToList(sentinel.allTargets);
            System.out.println("25");

            //ProdigyBank.getInstance().getBank().getHoldUp().endHoldUp();

        }
    }

    @Subcommand("test") @Conditions("bank")
    public void onTest(Player player) {
        Bukkit.broadcastMessage("build ");
        //instance.getArmorStandModel().getDriller().build(player.getLocation());
    }

    @Subcommand("give driller")
    public void giveDriller(Player player) {
        player.getInventory().addItem(Driller.getDrillerItem());
    }

    @Subcommand("give c4")
    public void giveC4(Player player) {
        player.getInventory().addItem(C4.getItem());
    }
}
