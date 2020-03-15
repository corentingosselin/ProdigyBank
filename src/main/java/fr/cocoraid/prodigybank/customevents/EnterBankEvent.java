package fr.cocoraid.prodigybank.customevents;

import fr.cocoraid.prodigybank.bank.Bank;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EnterBankEvent extends Event implements Cancellable {

    private Player player;
    private Bank bank;
    private boolean isCancelled;

    public EnterBankEvent(Player player, Bank bank) {
        this.player = player;
        this.bank = bank;
        this.isCancelled = false;

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


    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
