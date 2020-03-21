package fr.cocoraid.prodigybank.bank.protection.doors;

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

}
