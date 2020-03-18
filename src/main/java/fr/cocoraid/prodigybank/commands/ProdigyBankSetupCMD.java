package fr.cocoraid.prodigybank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.SafeDeposit;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.setupbank.SetupBankProcess;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

@CommandAlias("prodigybank|pb")
public class ProdigyBankSetupCMD extends BaseCommand {

    private Language lang;
    private SetupBankProcess process;
    private BankLoader bankLoader;
    private ProdigyBank instance;
    public ProdigyBankSetupCMD(ProdigyBank instance) {
        this.process = instance.getSetupBankProcess();
        this.lang = instance.getLanguage();
        this.instance = instance;
        this.bankLoader = instance.getBankLoader();

        instance.getCMDManager().getCommandConditions().addCondition("admin", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                checkForAdmin(issuer.getPlayer(),process.getAdmin());
            }
        });

        instance.getCMDManager().getCommandConditions().addCondition("world", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                if(instance.getBank() != null)
                    checkWorld(issuer.getPlayer(),instance.getBank().getWorld());
                else
                    checkWorld(issuer.getPlayer(),process.getWorld());
            }
        });

        instance.getCMDManager().getCommandConditions().addCondition("bank_not_created", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                checkBankNotExisting();
            }
        });
    }

    public void checkWorld(Player player, World world) {
        if(!player.getWorld().equals(world)) {
            throw new ConditionFailedException(lang.different_world);
        }
    }

    public void checkBankNotExisting() {
        if(instance.getBank() != null) {
            throw new ConditionFailedException(lang.bank_already_created);
        }
    }


    public void checkForAdmin(Player player, Player admin) {
        if (admin == null) {
            throw new ConditionFailedException(lang.no_process_owner);
        } else if(!admin.equals(player)) {
            throw new ConditionFailedException(lang.setup_already_in_progress);
        }
    }


    /**
     * Required
     * @param player
     */
    @Subcommand("setup")
    public void onBankSetup(Player player) {
        if(instance.getBank() != null) {
            player.sendMessage(lang.bank_remove_first);
            return;
        }
        process.startBankCreation(player);
    }

    @Subcommand("delete bank")
    public void onBankDelete(Player player) {
        if(instance.getBank() == null) {
            player.sendMessage(lang.bank_not_created);
            return;
        }

    }

    @Subcommand("setup cancel") @Conditions("admin|bank_not_created")
    public void setupCancel(Player player) {
        process.cancelCreation();
        process.reset();
        player.sendMessage(lang.process_cancelled);
    }
    @Subcommand("setup forcecancel") @Conditions("bank_not_created")
    public void setupForceCancel(Player player) {
        if(process.getAdmin() == null) {
            player.sendMessage(lang.no_process);
        }
        process.cancelCreation();
        process.reset();
        player.sendMessage(lang.process_cancelled);
    }


    @Subcommand("setup bank") @Conditions("admin|world")
    public void onBankAreaSetup(Player player) {
        process.setupBankArea();
    }

    @Subcommand("setup vault") @Conditions("admin|world")
    public void onVaultAreaSetup(Player player) {
        process.setupVaultArea();
    }

    @Subcommand("setup banker|deposit|withdraw") @Conditions("admin|world")
    public void onBankerSetup(Player player) {
        process.setupHostess(player.getLocation());
    }


    @Subcommand("setup vaultdoor") @Conditions("admin|world")
    public void onVaultDoorSetup(Player player) {
        process.setupVaultDoor();
    }

    @Subcommand("setup chest") @Conditions("admin|world")
    public void onChestSetup(Player player) {
        process.setWaitingForChest(true);
    }

    @Subcommand("setup jail") @Conditions("admin|world")
    public void onJailSetup(Player player) {
        process.setupJail(player.getLocation());
    }


    @Subcommand("setup exit") @Conditions("admin|world")
    public void onExitSetup(Player player) {
        process.setupExit(player.getLocation());
    }

    /**
     * Optional
     */
    @Subcommand("add police") @Conditions("bank")
    public void onPoliceAdd(Player player) {
        instance.getBank().getPoliceStaff().addPoliceMember(player,player.getLocation());
    }

    @Subcommand("add swat") @Conditions("bank")
    public void onSwatAdd(Player player) {
        instance.getBank().getSwatTeam().addSwatSpawnPoint(player,player.getLocation());
    }

    @Subcommand("add door") @Conditions("bank")
    public void onDoorAdd(Player player) {
        instance.getBank().addDoorToLockRegion(player);
    }


    @Subcommand("new chest") @Conditions("bank")
    public void onChestAdd(Player player) {
        if(bankLoader == null) this.bankLoader = instance.getBankLoader();
        Block target = player.getTargetBlock(new HashSet<>(Arrays.asList(Material.AIR)), 5);
        if(target == null) return;
        if(!instance.getBank().getVaultCuboid().isIn(target.getLocation())) {
            player.sendMessage(lang.chest_outside);
            return;
        }
        if (target.getType() == Material.ENDER_CHEST) {
            instance.getBank().getChests().add(new SafeDeposit(target));

            bankLoader.saveChests(true);
            player.sendMessage(new StringBuilder(lang.object_added).toString()
                    .replace("%type","safe deposit")
                    .replace("%id", String.valueOf(instance.getBank().getChests().size() - 1)));
        }
    }




}