package fr.cocoraid.prodigybank.filemanager;


import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.filemanager.skin.SkinData;
import fr.cocoraid.prodigybank.filemanager.skin.SkinType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class ConfigLoader {


    private ProdigyBank plugin;
    private FileConfiguration config;



    private Map<SkinType, List<SkinData>> skins = new HashMap<>();

    public ConfigLoader(ProdigyBank plugin) {
        this.plugin = plugin;

        File prodigyBankFolder = new File(plugin.getDataFolder().getPath());
        if (!prodigyBankFolder.isDirectory()) {
            prodigyBankFolder.mkdirs();
            try {
                plugin.saveResource(  "config.yml", false);
            } catch (Exception ex) {
                // Shhh...
            }
        }
        plugin.saveDefaultConfig();
        config = plugin.getConfig();


        skins.putIfAbsent(SkinType.POLICE,new ArrayList<>());
        for (String id : config.getConfigurationSection("police-skin").getKeys(false)) {
            String texture = config.getString("police-skin." + id + ".texture");
            String signature = config.getString("police-skin." + id + ".signature");
            skins.get(SkinType.POLICE).add(new SkinData(texture,signature));
        }

        skins.putIfAbsent(SkinType.SWAT,new ArrayList<>());
        for (String id : config.getConfigurationSection("swat-skin").getKeys(false)) {
            String texture = config.getString("swat-skin." + id + ".texture");
            String signature = config.getString("swat-skin." + id + ".signature");
            skins.get(SkinType.SWAT).add(new SkinData(texture,signature));
        }

    }

    public int getPercentDie() {
        return config.getInt("percentage-money-confiscated-die");
    }

    public int getPercentLeave() {
        return config.getInt("percentage-money-confiscated-leave");
    }

    public int getPercentJailed() {
        return config.getInt("percentage-money-confiscated-jail");
    }

    public String getLanguage() {
        return config.getString("language");
    }


    public SkinData getHostessSkin() {
        SkinData data = new SkinData(config.getString("hostess-skin.texture"),config.getString("hostess-skin.signature"));
        return data;
    }
    public SkinData getBankerSkin() {
        SkinData data = new SkinData(config.getString("banker-skin.texture"),config.getString("banker-skin.signature"));
        return data;
    }

    public Material getMoneyType() {
        return Material.valueOf(config.getString("money-material","GOLD_INGOT"));
    }

    public Sound getSoundMoneyCollect() {
        return Sound.valueOf(config.getString("money-collected-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
    }

    public int getBankDoorHealth() {
        return config.getInt("bank-door-health",50);
    }
    public int getVaultDoorHealth() {
        return config.getInt("vault-door-health",100);
    }
    public int getC4Damage() {
        return config.getInt("c4-damage",5);
    }
    public int getDrillerDamage() {
        return config.getInt("driller-damage",5);
    }
    public String getC4Texture() {
        return config.getString("c4-texture","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlZTY5NGI1ZGYzNGY0MGQ1ZTc3NGJkMzA0NmRiODQ5ZTM0ZmY1NWE0ODJkMDczMWU5ZDdhN2JiNzRhMTIifX19");
    }


    public Material getBankDoorMaterial() {
        return Material.valueOf(config.getString("bank-door-material","IRON_BARS"));
    }


    public int getOwnerRewardPercent() {
        return config.getInt("reward-percent-owner",30);
    }

    public int getTimeBeforeSwat() {
        return config.getInt("time-limit-before-swatting",120) * 20;
    }

    public int getTimeBeforeDoors() {
        return config.getInt("time-limit-before-doors",60) * 20;
    }

    public int getTimeBeforeJail() {
        return config.getInt("time-limit-before-jail",60) * 20;
    }

    public int getSwatPerPoint() {
        return config.getInt("number-of-swat-per-point",1);
    }

    public int getMinMoney() {
        return config.getInt("min-chest-money",100);
    }

    public int getMaxMoney() {
        return config.getInt("max-chest-money",1000);
    }

    Random r = new Random();
    public SkinData getRandomPoliceSkin() {
        List<SkinData> list =  skins.get(SkinType.POLICE);
        return list.get(r.nextInt(list.size()));
    }

    public SkinData getRandomSwatSkin() {
        List<SkinData> list =  skins.get(SkinType.SWAT);
        return list.get(r.nextInt(list.size()));
    }






}
