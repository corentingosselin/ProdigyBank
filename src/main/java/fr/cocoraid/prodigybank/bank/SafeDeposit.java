package fr.cocoraid.prodigybank.bank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.nms.NMS;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SafeDeposit {

    private Language lang = ProdigyBank.getInstance().getLanguage();
    private ConfigLoader config = ProdigyBank.getInstance().getConfigLoader();

    private Block chest;
    private ItemStack key;

    private BukkitTask task;

    public SafeDeposit(Block b) {
        this.chest = b;
    }

    public ItemStack generateKey() {
        this.key = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName(lang.chest_key_name);
        String uuid = UUID.randomUUID().toString();
        meta.setLocalizedName(uuid);
        key.setItemMeta(meta);
        chest.setMetadata("key",new FixedMetadataValue(ProdigyBank.getInstance(),uuid));
        return key;
    }

    public void openChest(Bank bank) {

        chest.getWorld().playSound(chest.getLocation(),Sound.BLOCK_IRON_DOOR_OPEN,2F,2F);

        int randomNum = ThreadLocalRandom.current().nextInt(config.getMinMoney(), config.getMaxMoney() + 1);
        int unit = (10 * randomNum / 1000);

        ItemStack item = new ItemStack(config.getMoneyType());
        ItemMeta meta = item.getItemMeta();
        meta.setLocalizedName(String.valueOf(unit));
        item.setItemMeta(meta);

        task = new BukkitRunnable() {
            int amount = 100;
            @Override
            public void run() {
                NMS.setChestOpen(chest,true);
                meta.setDisplayName(UUID.randomUUID().toString());
                item.setItemMeta(meta);
                Item drop = chest.getWorld().dropItem(chest.getLocation().getBlock().getLocation().add(0.5,0.4,0.5),item);
                drop.setVelocity(new Vector(Utils.getRandom(-0.5D,0.5D), Utils.getRandom(0.1D,0.7D), Utils.getRandom(-0.5D,0.5D)));
                bank.getAllMoney().add(drop);

                chest.getWorld().playSound(chest.getLocation(),Sound.ITEM_ARMOR_EQUIP_GOLD,0.5F,2F);

                amount--;
                if(amount <= 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(ProdigyBank.getInstance(),3,3);
    }

    public void cancel() {
        if(isChestOpened()) {
            if(!task.isCancelled())
                task.cancel();
            this.task = null;
            NMS.setChestOpen(chest,false);
        }
    }

    public boolean isChestOpened() {
        return task != null;
    }

    public ItemStack getKey() {
        return key;
    }

    public Block getChest() {
        return chest;
    }
}
