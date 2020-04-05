package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
            player.sendMessage("La banque existe " + (instance.getBank() != null));


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
