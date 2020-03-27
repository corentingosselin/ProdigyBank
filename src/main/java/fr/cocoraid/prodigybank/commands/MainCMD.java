package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelTrait;

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
            npc.addTrait(SentinelTrait.class);
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            npc.spawn(l);

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
