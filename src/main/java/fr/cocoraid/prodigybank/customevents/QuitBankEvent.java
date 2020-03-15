package fr.cocoraid.prodigybank.customevents;

import fr.cocoraid.prodigybank.bank.Bank;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QuitBankEvent extends Event {

    private Player player;
    private Bank bank;

    public QuitBankEvent(Player player, Bank bank) {
        this.player = player;
        this.bank = bank;

    }


    public Player getPlayer() {
        return this.player;
    }


    public Bank getBank() {
        return bank;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}