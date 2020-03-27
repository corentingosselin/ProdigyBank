package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Squad;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("prodigybank|pb")
public class SquadCMD extends BaseCommand {

    private Language lang;
    private ProdigyBank instance;
    public SquadCMD(ProdigyBank instance) {
        this.instance = instance;
        this.lang = instance.getLanguage();

        instance.getCMDManager().getCommandConditions().addCondition("holdup", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                checkForHoldup(issuer.getPlayer());
            }
        });
    }


    public void checkForHoldup(Player player) {
        if(instance.getBank() != null && instance.getBank().getHoldUp().isHoldup()) {
            Squad s = instance.getSquad(player);
            if(s != null && (s.getOwner().equals(player) || s.getSquadMembers().contains(player)))
                throw new ConditionFailedException(lang.holdup_running);
        }
    }

    @CommandPermission("prodigybank.squad.help")
    @Subcommand("squad")
    public void help(Player player, String[] args) {
        if (args.length == 0) {
            lang.squad_help.forEach(s -> {
                player.sendMessage(s);
            });
        }
    }

    @CommandPermission("prodigybank.squad.create")
    @Subcommand("squad create")@Conditions("holdup")
    public void create(Player player) {
        if(instance.getSquad(player) != null) {
            player.sendMessage(lang.squad_already_member_or);
            return;
        }
        instance.getSquads().put(player.getUniqueId(), new Squad(player));
        player.sendMessage(lang.squad_just_created);
    }

    @Subcommand("squad disband")@Conditions("holdup")
    public void disband(Player player) {
        if(!instance.getSquads().containsKey(player.getUniqueId())) {
            player.sendMessage(lang.squad_any);
            return;
        }
        instance.getSquads().remove(player.getUniqueId());
        player.sendMessage(lang.squad_disband);
    }

    @CommandPermission("prodigybank.squad.list")
    @Subcommand("squad list")
    public void list(Player player) {
        Squad squad = instance.getSquad(player);
        if(squad == null) {
            player.sendMessage(lang.squad_any);
            return;
        }
        player.sendMessage(new StringBuilder(lang.squad_list_leader).toString().replace("%player",squad.getOwner().getName()));
        player.sendMessage(lang.squad_list);
        squad.getSquadMembers().forEach(cur -> {
            player.sendMessage(new StringBuilder(lang.squad_list_key).toString().replace("%player",cur.getName()));
        });
    }


    private Map<UUID, Squad> tempAccept = new HashMap<>();
    @CommandPermission("prodigybank.squad.accept")
    @Subcommand("squad accept")
    public void accept(Player player) {
        if(!tempAccept.containsKey(player.getUniqueId())) {
            player.sendMessage(lang.squad_no_invit);
            return;
        }
        Squad tempSquad = tempAccept.get(player.getUniqueId());
        Squad checkSquad = instance.getSquad(player);
        if(checkSquad != null) {
            if(checkSquad.equals(tempSquad)) {
                player.sendMessage(lang.squad_already_member);
            } else {
                player.sendMessage(lang.squad_cant_accept);
            }
            return;
        }

        player.sendMessage(new StringBuilder(lang.squad_now_member).toString().replace("%player",tempSquad.getOwner().getName()));
        tempAccept.remove(player.getUniqueId());
        //notify others
        tempSquad.getOwner().sendMessage(new StringBuilder(lang.squad_has_joined).toString().replace("%player", player.getName()));
        tempSquad.getSquadMembers().forEach(cur -> {
            cur.sendMessage(new StringBuilder(lang.squad_has_joined).toString().replace("%player", player.getName()));
        });
        instance.getSquads().get(tempSquad.getOwner().getUniqueId()).getSquadMembers().add(player);
    }

    @CommandPermission("prodigybank.squad.invite")
    @Subcommand("squad invite")  @Conditions("holdup")
    @CommandCompletion("@players")
    public void invite(Player player, OnlinePlayer toadd) {
        if(player.equals(toadd.getPlayer())) {
            player.sendMessage(lang.squad_invite_yourself);
            return;
        }

        Squad squad = instance.getSquad(player);
        if(squad == null) {
            player.sendMessage(lang.squad_any);
            return;
        } else {
            if(!squad.getOwner().equals(player)) {
                player.sendMessage(lang.squad_not_leader);
                return;
            }
        }
        if(tempAccept.containsKey(toadd.getPlayer().getUniqueId())) {
            player.sendMessage(lang.squad_invitation_already_sent);
            return;
        }
        Squad checkSquad = instance.getSquad(toadd.getPlayer());
        if(checkSquad != null) {
            if(checkSquad.equals(squad)) {
                player.sendMessage(lang.squad_player_already_member);
            } else {
                player.sendMessage(lang.squad_player_already_member_of);
            }
            return;
        }

        player.sendMessage(lang.squad_invit_sent);
        tempAccept.put(toadd.getPlayer().getUniqueId(),squad);
        toadd.getPlayer().sendMessage(new StringBuilder(lang.squad_join_request).toString().replace("%player" , player.getName()));
        new BukkitRunnable() {
            @Override
            public void run() {
                if(tempAccept.containsKey(toadd.getPlayer().getUniqueId())) {
                    if(player.isOnline()) {
                        player.sendMessage(new StringBuilder(lang.squad_expired_invit).toString().replace("%player" , toadd.getPlayer().getName()));
                        tempAccept.remove(toadd.getPlayer().getUniqueId());
                    }
                }
            }
        }.runTaskLater(instance,20 * 60);
    }


    @CommandPermission("prodigybank.squad.remove")
    @Subcommand("squad remove") @Conditions("holdup")
    @CommandCompletion("@players")
    public void add(Player player, OnlinePlayer toremove) {
        if(!instance.getSquads().containsKey(player.getUniqueId())) {
            player.sendMessage(lang.squad_any);
            return;
        }
        Squad squad = instance.getSquads().get(player.getUniqueId());
        if(!squad.getSquadMembers().contains(toremove.getPlayer())) {

            player.sendMessage(lang.squad_not_member);
            return;
        }

        instance.getSquads().get(player.getUniqueId()).getSquadMembers().remove(toremove.getPlayer());
        player.sendMessage(new StringBuilder(lang.squad_remove).toString().replace("%player",toremove.getPlayer().getName()));
    }




}
