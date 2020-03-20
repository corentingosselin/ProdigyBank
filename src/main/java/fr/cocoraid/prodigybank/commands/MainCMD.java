package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.cocoraid.prodigybank.ProdigyBank;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
            npc.spawn(l);
            ((SkinnableEntity) npc.getEntity()).setSkinPersistent(UUID.randomUUID().toString(),
                    "hYsWnVE/XEPHDRjpMlblOoqGKI3jsUMBSBM9pG4Z91un7Y0TflPPyYVxBl3gnCL9dkO2iSEp4xuATMfFFUo3KvRaN47HUklL86KZSctDqej7CGzn3uGar+KP29uacyNZCuYFo98kwP8KHYaHLLCoEdoOUKOvFqZZkFJbRJbuhy2Fj+Kr47zfIDOiZfsacyAH2OQ0vbBg5+tRZi9lANniGUD6hc9zp660uOUu6B8yYBnUprCPnM2MUkV9lV9J7J7jd5oaOtj6rm5BDmYNwgWjLEJCdw5McqH6/KdMwj86fFs/2AGh8KVPXKzr6m7kMTJbpTFQDDKoo0aXXpHT/zAgAgq9Sx/sKn9ZN+hsJhDZqNvDV/h2GZe0bDyJ9t319mwkzMXLdN8QFYr3I+916eWmrztnc0Bj4drft+5EaEKTRDfgzrCHDVtguAUcOHk160qNApumzBfj6L3amOeiwmOwAktBZZCCiD+0Gz9MlVZ5eGHm2iruYi6SujIG6bIT+9/gzUnHo4mQSRzLBsKuYeWDLlNrwof6xleSM8WXV2RaUZDjJ7VOedkyzhEOa2ShP+D8CrYgwM+tv5nHzN/yajiOvQ1gPcx4UiTHMBXHzpUykjRyWDRQgEbpAdHwnrjq75C3QZlIsNr3KRtZ4CTfGKE+rJqFNufFHONoiX4kTNnn9YU=",
                    "eyJ0aW1lc3RhbXAiOjE1ODMxMjIwMDA0MzQsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E3MDM0YzIzNDczNzhhOTBjMDkxMTNiYWM3ODI5YzkzYWIzMTBjNDNiOTI0N2ZiMDcxZWE0M2U5MTA2NGE0ZDkifX19");


            //ProdigyBank.getInstance().getBank().getHoldUp().endHoldUp();

        }
    }

    @Subcommand("test") @Conditions("bank")
    public void onTest(Player player) {
        Bukkit.broadcastMessage("build ");
        //instance.getArmorStandModel().getDriller().build(player.getLocation());
    }
}
