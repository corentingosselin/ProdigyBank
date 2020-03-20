package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.nms.NMSPlayer;
import fr.cocoraid.prodigybank.setupbank.Cuboid;
import fr.cocoraid.prodigybank.utils.Utils;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.*;

public class VaultDoor {

    private ConfigLoader config = ProdigyBank.getInstance().getConfigLoader();

    private Cuboid cuboid;
    private List<BlockState> saved_blocks = new ArrayList<>();
    private List<Block> blocks;
    private int health = 100;
    private boolean destroyed;

    public VaultDoor(Cuboid cuboid) {
        this.cuboid = cuboid;
        this.blocks = cuboid.getBlockList();
        blocks.removeIf(b -> b.getType() == Material.AIR);
        blocks.forEach(b -> {
            saved_blocks.add(b.getState());
        });
    }

    private int break_state = 0;
    public void breach() {
        Location l = cuboid.getPoint1();
        this.health -= 5;
        if(health <= 0) {
            blocks.forEach(b -> {
                l.getWorld().spawnParticle(Particle.EXPLOSION_HUGE,b.getLocation(),1,0.5,0.5,0.5,0.1F);
                b.setType(Material.AIR);
            });
            l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE,1,1);
            l.getWorld().playSound(l, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,2F,0);
        } else {
            try {
                if (health % (health / 9) == 0) {
                    break_state++;
                    blocks.stream().filter(b -> Utils.getRandom(0, 5) >= 4).forEach(b -> {
                        BlockPosition bp = new BlockPosition(b.getX(), b.getY(), b.getZ());
                        PacketPlayOutBlockBreakAnimation anim = new PacketPlayOutBlockBreakAnimation(new Random().nextInt(2000), bp, break_state);
                        NMSPlayer.sendPacketNearby(b.getLocation(), anim);
                    });
                    l.getWorld().playSound(l, Sound.BLOCK_ANVIL_LAND, 0.5F, 0);
                    l.getWorld().playSound(l, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
                    destroyed = true;
                }
            } catch (ArithmeticException e) {

            }

        }


    }

    public int getHealth() {
        return health;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void reset() {
        saved_blocks.forEach(l -> {
            l.getBlock().setType(l.getType());
            l.getBlock().setBlockData(l.getBlockData());
        });
        this.health = 100;
        destroyed = false;
    }

    public boolean isVaultDoorBlock(Block b) {
        return blocks.contains(b);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }
}
