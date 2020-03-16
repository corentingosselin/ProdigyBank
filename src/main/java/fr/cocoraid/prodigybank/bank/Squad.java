package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bridge.EconomyBridge;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Squad {

    private int moneyCollected = 0;
    private Player owner;
    private List<Player> squad = new ArrayList<>();
    private Bank bank;

    public Squad(Bank bank, Player owner) {
        this.bank = bank;
        this.owner = owner;
        squad.add(owner);
    }

    public Player getOwner() {
        return owner;
    }

    public List<Player> getSquadMembers() {
        return squad;
    }




    public void reward() {
        //not the right algo
        //need to divide between each member, the owner has a bigger percentage (configurable)

        new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTaskLater(ProdigyBank.getInstance(),20*3);

       /* getSquadMembers().forEach(s -> {
            EconomyBridge.giveMoney(s,moneycollected);
        });*/

    }



    public void failSquadMember(Player player, int percentage) {
        player.teleport(bank.getJail());
        int money = (int) EconomyBridge.getMoney(player) ;
        EconomyBridge.takeMoney(player, money * percentage / 100);
    }


    public void sendActionBarMessage(String message) {
        squad.forEach(cur -> {
            cur.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(message));
        });
    }

    public void sendSubTitle(String msg) {
        String[] message = msg.split(":");
        squad.forEach(cur -> {
            if(message.length > 1) {
                cur.sendTitle(message[0], message[1], 20 * 5, 0, 1);
            } else
                cur.sendTitle("",message[0],20 * 5,0,1);
        });
    }

    public void sendTeamSubTitle(String msg) {
        String[] message = msg.split(":");
        squad.stream().filter(cur -> !cur.equals(owner)).forEach(cur -> {
            if(message.length > 1)
                cur.sendTitle(message[0],message[1],20 * 5,0,1);
            else
                cur.sendTitle("",message[0],20 * 5,0,1);
        });
    }

    public void sendOwnerSubTitle(String msg) {
        String[] message = msg.split(":");
            if(message.length > 1)
                owner.sendTitle(message[0],message[1],20 * 5,0,1);
            else
                owner.sendTitle("",message[0],20 * 5,0,1);
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
