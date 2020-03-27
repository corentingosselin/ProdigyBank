package fr.cocoraid.prodigybank.bank.tools;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.HoldUp;
import fr.cocoraid.prodigybank.bank.protection.doors.BankDoor;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.nms.ReflectedArmorStand;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class C4 {

    protected static ConfigLoader config = ProdigyBank.getInstance().getConfigLoader();
    protected static Language lang = ProdigyBank.getInstance().getLanguage();
    private static ItemStack item = Utils.createSkull(lang.tool_c4_displayname,lang.tool_c4_lores,config.getC4Texture());


    private BankDoor door;
    private Block stickedBlock;
    private ReflectedArmorStand c4;
    public C4(BankDoor door) {
        this.door = door;
    }

    public void place(HoldUp h, Block block) {
        //clicked block
        stickedBlock = block;
        Location l = block.getLocation().add(0.5,-1.7 + 0.5,0.5);
        c4 = new ReflectedArmorStand(l);
        c4.setEquipment(5,item);
        c4.setVisible(false);
        c4.spawnArmorStand();
        c4.updateMetadata();
        l.getWorld().playSound(l, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,1,2);
        block.getWorld().spawnParticle(Particle.SLIME,block.getLocation().add(0.5,0.5,0.5),6,0.5,0.5,0.5,0.1F);
        new BukkitRunnable() {
            @Override
            public void run() {
                l.getWorld().playSound(l, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,1,2);
            }
        }.runTaskLater(ProdigyBank.getInstance(),5);
        l.getWorld().playSound(l, Sound.BLOCK_TRIPWIRE_ATTACH,2,2);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(c4 != null && h.isHoldup()) {
                    explode(h);
                }

            }
        }.runTaskLater(ProdigyBank.getInstance(),20 * 5);

    }

    public void explode(HoldUp h) {
        if(door.isDestroyed()) return;
        if(c4 == null) return;
        door.damage(h,config.getC4Damage());
        if(c4 == null) return;
        c4.remove();
        door.getCuboid().getCenter().getWorld().playSound(door.getCuboid().getCenter(), Sound.ENTITY_GENERIC_EXPLODE,2,1);
        c4.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE,c4.getLocation(),1,0.5,0.5,0.5,0.1F);
        stickedBlock = null;
    }

    public void reset() {
        if(c4 != null) {
            c4.remove();
            this.c4 = null;
        }
    }

    public Block getStickedBlock() {
        return stickedBlock;
    }

    public static ItemStack getItem() {
        return item;
    }
}
