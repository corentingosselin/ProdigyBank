package fr.cocoraid.prodigybank.bank.protection.doors;

import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.bank.protection.Door;
import fr.cocoraid.prodigybank.setupbank.Cuboid;
import org.bukkit.Material;

public class BankDoor extends Door {

    public BankDoor(Cuboid cuboid) {
        super(cuboid,config.getBankDoorHealth());
    }

    @Override
    public void close() {
        super.close();
        blocks.stream().filter(b -> b.getType() == Material.AIR).forEach(b -> {
            b.setType(config.getBankDoorMaterial());
        });
    }

    @Override
    public void reset() {
        super.reset();
        blocks.stream().filter(b -> b.getType() == config.getBankDoorMaterial()).forEach(b -> {
            b.setType(Material.AIR);
        });
    }

    @Override
    public void explode(HoldUp h) {
        super.explode(h);
        h.getC4s().forEach(c4 -> {
            c4.reset();
        });

    }
}