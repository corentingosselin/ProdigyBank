package fr.cocoraid.prodigybank.bank.protection.doors;

import fr.cocoraid.prodigybank.bank.protection.Door;
import fr.cocoraid.prodigybank.setupbank.Cuboid;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VaultDoor extends Door {

    protected List<BlockState> saved_blocks = new ArrayList<>();
    public VaultDoor(Cuboid cuboid) {
        super(cuboid,config.getBankDoorHealth());
        blocks.forEach(b -> {
            saved_blocks.add(b.getState());
        });
    }

    @Override
    public void close() {
        super.close();
        saved_blocks.forEach(l -> {
            l.getBlock().setType(l.getType());
            l.getBlock().setBlockData(l.getBlockData());
        });
    }

    @Override
    public void reset() {
        super.reset();
        blocks.stream().filter(b -> b.getType() == config.getBankDoorMaterial()).forEach(b -> {
            b.setType(Material.AIR);
        });
    }
}
