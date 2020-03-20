package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bridge.EconomyBridge;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Squad {

    private static ProdigyBank instance = ProdigyBank.getInstance();
    private static Language lang = instance.getLanguage();
    private static ConfigLoader config = instance.getConfigLoader();

    private int moneyCollected = 0;
    private Player owner;
    private List<Player> squad = new ArrayList<>();
    private Bank bank;

    public Squad(Player owner) {
        this.owner = owner;
    }

    public void startAttacking(Bank bank) {
        this.bank = bank;
    }

    public Player getOwner() {
        return owner;
    }

    public List<Player> getSquadMembers() {
        return squad;
    }


    public void reward() {


        int ownerReward = squad.isEmpty() ? moneyCollected : config.getOwnerRewardPercent() * moneyCollected / 100;
        int memberReward = (moneyCollected - ownerReward) / (!squad.isEmpty() ? squad.size() : 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                sendOwnerSubTitle(new StringBuilder(lang.title_reward).toString().replace("%amount",String.valueOf(ownerReward)));
                sendTeamSubTitle(new StringBuilder(lang.title_reward).toString().replace("%amount",String.valueOf(memberReward)));

            }
        }.runTaskLater(ProdigyBank.getInstance(),20*3);


        EconomyBridge.giveMoney(owner,ownerReward);
        if(!squad.isEmpty())
            getSquadMembers().stream().filter(cur -> !cur.equals(owner)).forEach(s -> {
                EconomyBridge.giveMoney(s,memberReward);
            });
    }



    public int failSquadMember(Player player,int percentage) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,80,10,false,false,false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,80,10,false,false,false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,40,10,false,false,false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,80,10,false,false,false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,80,10,false,false,false));
        int money = (int) EconomyBridge.getMoney(player) ;
        int lost = money * percentage / 100;
        EconomyBridge.takeMoney(player, lost);
        return lost;
    }

    public enum MemberFailedType {
        DIED(config.getPercentDie()),
        JAILED(config.getPercentJailed()),
        LEFT(config.getPercentLeave());


        int percent;
        MemberFailedType(int percent) {
            this.percent = percent;
        }
    }

    public void removeSquadMember(Player player, MemberFailedType type) {
        int lost = failSquadMember(player,type.percent);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(bank.getJail());
                sendSubTitle(player,new StringBuilder(lang.title_failed).toString().replace("%amount",String.valueOf(lost)));
            }
        }.runTaskLater(instance,20*3);

    }

    public void sendActionBarMessage(String message) {
        squad.forEach(cur -> {
            cur.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(message));
        });
        owner.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(message));
    }

    public void sendSubTitle(Player player, String msg) {
        String[] message = msg.split(":");
        if(message.length > 1) {
            player.sendTitle(message[0], message[1], 20 * 4, 0, 0);
        } else
            player.sendTitle("",message[0],20 * 4,0,0);

    }

    public void sendSubTitle(String msg) {
        String[] message = msg.split(":");
        squad.forEach(cur -> {
            if(message.length > 1) {
                cur.sendTitle(message[0], message[1], 20 * 4, 0, 0);
            } else
                cur.sendTitle("",message[0],20 * 4,0,0);
        });
        sendOwnerSubTitle(msg);
    }

    public void sendTeamSubTitle(String msg) {
        String[] message = msg.split(":");
        squad.forEach(cur -> {
            if(message.length > 1)
                cur.sendTitle(message[0],message[1],20 * 4,0,0);
            else
                cur.sendTitle("",message[0],20 * 4,0,0);
        });
    }

    public void sendOwnerSubTitle(String msg) {
        String[] message = msg.split(":");
        if(message.length > 1)
            owner.sendTitle(message[0],message[1],20 * 4,0,0);
        else
            owner.sendTitle("",message[0],20 * 4,0,0);
    }


    public boolean isFromSquad(Player player) {
        return squad.contains(player) || player.equals(owner);
    }

    public void addMoney(int amount) {
        moneyCollected+=amount;
        ProdigyBank instance = ProdigyBank.getInstance();
        squad.forEach(s -> {
            s.playSound(s.getLocation(),instance.getConfigLoader().getSoundMoneyCollect(),1F,1F);
        } );

    }



    public int getMoneyCollected() {
        return moneyCollected;
    }
}
