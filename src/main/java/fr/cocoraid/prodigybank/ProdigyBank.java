package fr.cocoraid.prodigybank;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.Squad;
import fr.cocoraid.prodigybank.bank.listeners.*;
import fr.cocoraid.prodigybank.bank.tools.C4;
import fr.cocoraid.prodigybank.bank.tools.Driller;
import fr.cocoraid.prodigybank.bridge.EconomyBridge;
import fr.cocoraid.prodigybank.commands.MainCMD;
import fr.cocoraid.prodigybank.commands.ProdigyBankSetupCMD;
import fr.cocoraid.prodigybank.commands.SquadCMD;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.filemanager.language.LanguageLoader;
import fr.cocoraid.prodigybank.filemanager.model.ArmorStandModel;
import fr.cocoraid.prodigybank.listener.PlaceC4Listener;
import fr.cocoraid.prodigybank.setupbank.ChestPlaceListener;
import fr.cocoraid.prodigybank.listener.RobberToolPlaceEvent;
import fr.cocoraid.prodigybank.setupbank.SetupBankProcess;
import fr.cocoraid.prodigybank.utils.CC;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProdigyBank extends JavaPlugin implements Listener {


    private Map<UUID, Squad> squads = new HashMap<>();
    public void createSquad(Player owner) {
        squads.put(owner.getUniqueId(),new Squad(owner));
    }
    public Squad getSquad(Player player) {
        if(instance.getSquads().containsKey(player.getUniqueId())) {
            return instance.getSquads().get(player.getUniqueId());
        } else if(instance.getSquads().values().stream().filter(s -> s.getSquadMembers().contains(player)).findAny().isPresent()) {
            return instance.getSquads().values().stream().filter(s -> s.getSquadMembers().contains(player)).findAny().get();
        }
        return null;

    }

    public Map<UUID, Squad> getSquads() {
        return squads;
    }

    private static ProdigyBank instance;

    private ArmorStandModel armorStandModel;

    private BankLoader bankLoader;
    private Bank bank;
    private SetupBankProcess setupBankProcess;
    private Language language;
    private ConfigLoader configLoader;
    private PaperCommandManager manager;

    @Override
    public void onEnable() {
        ConsoleCommandSender c = Bukkit.getServer().getConsoleSender();
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            setEnabled(false);
            c.sendMessage("§cWorlEdit is required to run ProdigyBank !");
        } else if(Bukkit.getPluginManager().getPlugin("Citizens") == null) {
            setEnabled(false);
            c.sendMessage("§cCitizens is required to run ProdigyBank !");
        } else if(Bukkit.getPluginManager().getPlugin("Sentinel") == null) {
            setEnabled(false);
            c.sendMessage("§cSentinel is required to run ProdigyBank !");
        }

        if (!EconomyBridge.setupEconomy()) {
            setEnabled(false);
            getLogger().warning("Vault with a compatible economy plugin was not found!");
        }

        instance = this;

        this.configLoader = new ConfigLoader(this);
        loadLanguage(c);
        loadCustomRecipes();
        setupBankProcess = new SetupBankProcess(this);
        loadCommands();
        registerEvents();

        this.armorStandModel = new ArmorStandModel(this);
        armorStandModel.loadModels();

    }

    /**
     * On startup
     */
    private boolean citizensLoaded = false;
    @EventHandler
    public void citizenEnable(CitizensEnableEvent e) {
        if(citizensLoaded) return;
        loadBank();
        this.citizensLoaded = true;
        checkForInvalidsCitizens();
    }

    public void loadBank() {
        this.bankLoader = new BankLoader(this);
        bankLoader.load();
        registerBankEvents();

    }


    @Override
    public void onDisable() {
        super.onDisable();
        if(bank != null) {
            bank.getHoldUp().endHoldUp();
        }
        if(bankLoader != null) {
            bankLoader.save();
        }
    }


    //reset vaultdoor, and bankdoors
    public void checkForInvalidSession() {

    }


    public void checkForInvalidsCitizens() {
        CitizensAPI.getNPCRegistry().iterator().forEachRemaining(npc -> {
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            if(sentinel.squad == "swat") {
                npc.destroy();
            }
        });

    }

    public void registerBankEvents() {
        if(bank != null) {
            Bukkit.getPluginManager().registerEvents(new DetectHoldUpListener(this),this);
            Bukkit.getPluginManager().registerEvents(new CheckBankListener(this),this);
            Bukkit.getPluginManager().registerEvents(new DetectStaffRemoveListener(this),this);
            Bukkit.getPluginManager().registerEvents(new OpenSafeDepositListener(this),this);
            Bukkit.getPluginManager().registerEvents(new RobbersDieListener(this),this);
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new PlaceC4Listener(this),this);
        Bukkit.getPluginManager().registerEvents(new ChestPlaceListener(this),this);
        Bukkit.getPluginManager().registerEvents(new RobberToolPlaceEvent(this),this);
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    private void loadCommands() {
        this.manager = new PaperCommandManager(this);
        manager.registerCommand(new MainCMD(this));
        manager.registerCommand(new ProdigyBankSetupCMD(this));
        manager.registerCommand(new SquadCMD(this));
        manager.getCommandConditions().addCondition("bank", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                checkForBank();
            }
        });
    }

    private void loadLanguage(ConsoleCommandSender c) {
        new LanguageLoader(this);
        if(!LanguageLoader.getLanguages().containsKey(configLoader.getLanguage().toLowerCase())) {
            c.sendMessage("§c Language not found ! Please check your language folder");
        } else
            language = LanguageLoader.getLanguage(configLoader.getLanguage().toLowerCase());
        c.sendMessage(CC.d_green + "Language: " + (language == null ? "english" : configLoader.getLanguage().toLowerCase()));
        if(language == null)
            language = LanguageLoader.getLanguage("english");
    }


    private void loadCustomRecipes() {
        NamespacedKey drillerKey = new NamespacedKey(this, "driller");
        ShapedRecipe drillerRecipe = new ShapedRecipe(drillerKey, Driller.getDrillerItem());
        drillerRecipe.shape(
                "BTB"
                , "CAC"
                , "SGS");
        drillerRecipe.setIngredient('B', Material.BLAZE_ROD);
        drillerRecipe.setIngredient('C', Material.END_CRYSTAL);
        drillerRecipe.setIngredient('T', Material.REDSTONE_TORCH);
        drillerRecipe.setIngredient('G', Material.GRINDSTONE);
        drillerRecipe.setIngredient('A', Material.BEACON);
        drillerRecipe.setIngredient('S', Material.TOTEM_OF_UNDYING);
        Bukkit.addRecipe(drillerRecipe);

        NamespacedKey c4Key = new NamespacedKey(this, "c4");
        ShapedRecipe c4Recipe = new ShapedRecipe(c4Key, C4.getItem());
        c4Recipe.shape(
                "TRT"
                , "MGM"
                , "TRT");
        c4Recipe.setIngredient('T', Material.TNT);
        c4Recipe.setIngredient('M', Material.HONEY_BLOCK);
        c4Recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        c4Recipe.setIngredient('G', Material.BLAZE_POWDER);
        Bukkit.addRecipe(c4Recipe);
    }

    public static ProdigyBank getInstance() {
        return instance;
    }

    public Language getLanguage() {
        return language;
    }

    public SetupBankProcess getSetupBankProcess() {
        return setupBankProcess;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public PaperCommandManager getCMDManager() {
        return manager;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    private void checkForBank() {
        if(bank == null) throw new ConditionFailedException(language.bank_not_created);
    }

    public Bank getBank() {
        return bank;
    }

    public void setBankLoader(BankLoader bankLoader) {
        this.bankLoader = bankLoader;
    }

    public BankLoader getBankLoader() {
        return bankLoader;
    }

    public ArmorStandModel getArmorStandModel() {
        return armorStandModel;
    }
}
