package fr.cocoraid.prodigybank.bank.protection;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
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

import java.util.List;
import java.util.Random;

public abstract class Door {

    protected static ConfigLoader config = ProdigyBank.getInstance().getConfigLoader();
    protected static Language lang = ProdigyBank.getInstance().getLanguage();
    protected List<Block> blocks;
    protected Cuboid cuboid;
    protected int maxHealth;
    protected int health;
    protected boolean destroyed;

    public Door(Cuboid cuboid, int health) {
        this.health = health;
        this.maxHealth = health;
        this.cuboid = cuboid;
        this.blocks = cuboid.getBlockList();
    }

    public void close() {

    }


    private int break_state = 0;
    public void damage(HoldUp holdUp, int damage) {
        Location l = cuboid.getPoint1();
        this.health -= damage;
        if(health <= 0) {
            explode(holdUp);
        } else {
            try {
                if (health % (health / 9) == 0) {
                    break_state++;
                    blocks.stream().filter(b -> b.getType() != Material.AIR && Utils.getRandom(0, 5) >= 4).forEach(b -> {
                        BlockPosition bp = new BlockPosition(b.getX(), b.getY(), b.getZ());
                        PacketPlayOutBlockBreakAnimation anim = new PacketPlayOutBlockBreakAnimation(new Random().nextInt(2000), bp, break_state);
                        NMSPlayer.sendPacketNearby(b.getLocation(), anim);
                    });
                    l.getWorld().playSound(l, Sound.BLOCK_ANVIL_LAND, 0.5F, 0);
                    l.getWorld().playSound(l, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
                }
            } catch (ArithmeticException e) {

            }

        }
    }

    public void explode(HoldUp h) {
        Location l = cuboid.getPoint1();
        blocks.stream().filter(b -> b.getType() != Material.AIR).forEach(b -> {
            l.getWorld().spawnParticle(Particle.EXPLOSION_HUGE,b.getLocation(),1,0.5,0.5,0.5,0.1F);
            b.setType(Material.AIR);
        });
        l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE,1,1);
        l.getWorld().playSound(l, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,2F,0);
        destroyed = true;
    }


    public int getHealth() {
        return health;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void reset() {
        this.health = maxHealth;
        destroyed = false;
    }

    public boolean isDoorBlock(Block b) {
        return blocks.contains(b);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }
}
