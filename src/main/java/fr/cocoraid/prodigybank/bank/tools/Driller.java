package fr.cocoraid.prodigybank.bank.tools;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.filemanager.model.Model;
import fr.cocoraid.prodigybank.filemanager.model.ModelType;
import fr.cocoraid.prodigybank.nms.ReflectedArmorStand;
import fr.cocoraid.prodigybank.utils.UtilMath;
import fr.cocoraid.prodigybank.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driller {


    private static Language lang = ProdigyBank.getInstance().getLanguage();

    private static ItemStack drillerItem = new ItemStack(Material.GRINDSTONE);
    static {
        ItemMeta meta = drillerItem.getItemMeta();
        meta.setDisplayName(lang.tool_driller_displayname);
        meta.setLore(lang.tool_driller_lores);
        drillerItem.setItemMeta(meta);
    }

    private BukkitTask task;
    private BukkitTask laserTask;
    private boolean ready;

    private int damage;


    private Bank bank;
    private List<Model> models;
    private ArrayDeque<Model> queue = new ArrayDeque();
    private List<ReflectedArmorStand> armorStands = new ArrayList<>();

    public Driller(Bank bank) {
        this.bank = bank;
        this.models = bank.getInstance().getArmorStandModel().getModels().get(ModelType.DRILLER);
        this.damage = bank.getConfig().getDrillerDamage();
        Collections.sort(models, (a, b) ->
                a.getVector().getY() < b.getVector().getY() ? -1 : a.getVector().getY() == b.getVector().getY() ? 0 : 1);
    }


    private List<Location> points = new ArrayList<>();
    public void initTarget(Location location) {
        ready = true;
        Location l = location.add(0,0.55,0);
        Vector dir = location.getDirection().normalize().multiply(0.2);
        for(int k = 0; k < 100; k++) {
            l.add(dir);
            points.add(new Location(l.getWorld(),l.getX(), l.getY(),l.getZ()));
            if(bank.getVaultDoor().isDoorBlock(l.getBlock()))
                break;
        }
        this.laserTask = new BukkitRunnable() {
            int time = 0;
            int i = 0;
            boolean shooting = false;
            @Override
            public void run() {
                if(points.isEmpty() || bank.getVaultDoor().getHealth() <= 0) {
                    this.cancel();
                    laserTask = null;
                    return;
                }
                points.forEach(p -> {
                    p.getWorld().spawnParticle(Particle.REDSTONE, p, 1, new Particle.DustOptions(Color.fromBGR(0, 0, 254), 1));

                });

                if(shooting) {
                    for(int k = i; k < (i + 3); k++) {
                        int index = k;
                        if(k > points.size() - 1)
                            index = i;

                        Location p = points.get(index);
                        for (Player cur : Bukkit.getOnlinePlayers()) {
                            if(!cur.getWorld().equals(location.getWorld())) return;
                            cur.getWorld().spawnParticle(Particle.CLOUD, p, 0, 0, 0, 0, 0);
                            location.getWorld().spawnParticle(Particle.FLASH, p,1);
                        }
                    }

                    i++;
                    if(i >= points.size()) {
                        bank.getVaultDoor().getBlocks().forEach(b -> {
                            for (Player cur : Bukkit.getOnlinePlayers()) {
                                if(!cur.getWorld().equals(location.getWorld())) return;
                                cur.getWorld().spawnParticle(Particle.CLOUD, b.getLocation(), 5, 1, 1, 1, 0.4F);
                                cur.getWorld().spawnParticle(Particle.CRIT_MAGIC, b.getLocation(), 5, 1, 1, 1, 0.4F);
                            }
                        });
                        this.shooting = false;
                    }

                    i%=points.size();
                }

                if(time % (2 * 20) == 0) {
                    bank.getVaultDoor().damage(bank.getHoldUp(),damage);
                    location.getWorld().playSound(location, Sound.BLOCK_BEACON_AMBIENT, 1f, 0F);
                }

                time++;
                if(time % (20 * 3) == 0) {
                    this.shooting = true;
                    this.i = 0;
                    location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 2f, 0F);
                    //LAUNCH TRAIL
                    location.getWorld().spawnParticle(Particle.CLOUD, points.get(0), 4, 0.2, 0.2, 0.2, 0.1F);
                    location.getWorld().spawnParticle(Particle.FLASH, points.get(0), 4, 0.2, 0.2, 0.2, 0.1F);

                }


            }
        }.runTaskTimer(bank.getInstance(),0,0);

    }

    public void build(Player builder, Location location) {
        if(ready) return;
        queue.addAll(models);
        Location spawn = location.clone();
        spawn.setPitch(0);
        spawn.add(0,3,0);
        this.task = new BukkitRunnable() {
            Location targeter = null;
            @Override
            public void run() {
                if(ready) return;

                if(bank.getHoldUp().getSquad().getSquadMembers().contains(builder) || bank.getHoldUp().getSquad().getOwner().equals(builder)) {

                    if (builder.getLocation().distanceSquared(location) < 6 * 6) {
                        Utils.sendTitle(builder, lang.title_driller_build);
                    } else return;

                    Model m = queue.removeFirst();
                    Vector v = UtilMath.rotateAroundAxisY(m.getVector().clone(), Math.toRadians(-location.getYaw() - 90));
                    Location l = spawn.clone().add(v);
                    ReflectedArmorStand as = new ReflectedArmorStand(l);
                    as.setVisible(false);
                    as.setHeadPose(m.getAngle());
                    as.setSmall(m.isSmall());
                    as.setEquipment(5, m.getItem());
                    as.spawnArmorStand();
                    as.setRotation(location.getYaw() + 90, 0);
                    as.updateMetadata();
                    armorStands.add(as);
                    location.getWorld().playSound(location, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.3F, 0F);
                    if (m.getItem().getType() == Material.REDSTONE_TORCH) {
                        targeter = l;
                        targeter.setDirection(location.getDirection());
                        targeter.setPitch(0);
                    }
                    if (queue.isEmpty()) {
                        location.getWorld().playSound(location, Sound.BLOCK_ANVIL_LAND, 1, 1);
                        initTarget(targeter);
                        this.cancel();
                    }

                } else {
                    //notify driller has been destroyed because the builder has died
                    destroy();
                    bank.getHoldUp().getSquad().sendSubTitle(lang.driller_destroyed);
                    bank.getHoldUp().setDriller(null);
                }
            }
        }.runTaskTimerAsynchronously(bank.getInstance(),0,3);

    }

    public void destroy() {
        this.ready = false;
        if(task != null && !task.isCancelled())
            task.cancel();

        if(laserTask != null && !laserTask.isCancelled())
            laserTask.cancel();

        queue.clear();
        points.clear();

        armorStands.forEach(as -> as.remove());
        armorStands.clear();
    }

    public static ItemStack getDrillerItem() {
        return drillerItem;
    }
}
