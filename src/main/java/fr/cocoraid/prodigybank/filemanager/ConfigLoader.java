package fr.cocoraid.prodigybank.filemanager;


import fr.cocoraid.prodigybank.ProdigyBank;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;
import java.util.Random;

public class ConfigLoader {


    private ProdigyBank plugin;
    private FileConfiguration config;


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
    public String getHostessSkinName() {
        return config.getString("hostess-skin-name");
    }
    public String getBankerSkinName() {
        return config.getString("banker-skin-name");
    }

    public Material getMoneyType() {
        return Material.valueOf(config.getString("money-material","GOLD_INGOT"));
    }

    public Sound getSoundMoneyCollect() {
        return Sound.valueOf(config.getString("money-collected-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
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

    public String getRandomPoliceSkin() {
        Random r = new Random();
        List<String> list =  config.getStringList("police-skin-name");
        return list.get(r.nextInt(list.size()));
    }

    public String getRandomSwatSkin() {
        Random r = new Random();
        List<String> list =  config.getStringList("swat-skin-name");
        return list.get(r.nextInt(list.size()));
    }






}
