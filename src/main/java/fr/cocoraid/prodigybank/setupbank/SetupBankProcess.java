package fr.cocoraid.prodigybank.setupbank;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Bank;
import fr.cocoraid.prodigybank.bank.SafeDeposit;
import fr.cocoraid.prodigybank.filemanager.BankLoader;
import fr.cocoraid.prodigybank.filemanager.ConfigLoader;
import fr.cocoraid.prodigybank.filemanager.language.Language;
import fr.cocoraid.prodigybank.filemanager.skin.SkinData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.LinkedList;
import java.util.UUID;

public class SetupBankProcess {



    private ProdigyBank instance;
    private ConfigLoader config;
    private Language lang;
    private Player admin;
    private BankProcessStep currentStep;

    private World world;

    //REQUIRED
    private boolean waitingForChest;
    private Cuboid bankCuboid;
    private Cuboid vaultCuboid;
    private Cuboid vaultDoorCuboid;
    private NPC depositHostess;
    private NPC withdrawHostess;
    private NPC banker;
    private Location exit;
    private Location jail;
    private LinkedList<SafeDeposit> chests = new LinkedList<>();


    public void reset() {
        this.admin = null;
        this.currentStep = null;
        this.bankCuboid = null;
        this.vaultCuboid = null;
        this.vaultDoorCuboid = null;
        this.depositHostess = null;
        this.withdrawHostess = null;
        this.banker = null;
        this.jail = null;
        this.exit = null;
        this.world = null;
        this.waitingForChest = false;
        chests.clear();
    }


    public SetupBankProcess(ProdigyBank instance) {
        this.lang = instance.getLanguage();
        this.config = instance.getConfigLoader();
        this.instance = instance;
    }



    private enum BankProcessStep {
        BANK_AREA(0),
        VAULT_AREA(1),
        VAULT_DOOR(2),
        BANKER(3),
        DEPOSIT_HOSTESS(4),
        WITHDRAW_HOSTESS(5),
        CHEST_POINTS(6),
        JAIL(7);

        int index;
        BankProcessStep(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }



    public void startBankCreation(Player player) {
        if(admin != null) {
            if(admin.equals(player)) {
                player.sendMessage(lang.setup_already_in_progress);
                return;
            } else {
                player.sendMessage(lang.setup_in_progress);
                return;
            }
        }
        this.world = player.getWorld();
        this.admin = player;
        player.sendMessage(lang.start_bank_setup);
        currentStep = BankProcessStep.BANK_AREA;
    }



    // /pb setup bank
    public void setupBankArea() {
        if(this.currentStep != BankProcessStep.BANK_AREA) {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","pb setup"));
            return;
        }
        Cuboid cuboid = Cuboid.setupCuboid(admin);
        if(cuboid == null) {
            return;
        }

        bankCuboid = cuboid;
        admin.sendMessage(lang.next_vaultarea_setup);
        this.currentStep = BankProcessStep.VAULT_AREA;
    }

    public void setupVaultArea() {
        if(currentStep.getIndex() > BankProcessStep.VAULT_AREA.getIndex()) {
            admin.sendMessage(lang.step_already_done);
            return;
        }
        if(this.currentStep != BankProcessStep.VAULT_AREA) {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","Bank Area"));
            return;
        }
        Cuboid cuboid = Cuboid.setupCuboid(admin);
        if(cuboid == null) {
            return;
        }

        if(isCuboidSimilar(cuboid, bankCuboid)) {
            admin.sendMessage(lang.similar_worldedit_selection);
            return;
        }
        vaultCuboid = cuboid;
        admin.sendMessage(lang.next_vaultdoor_setup);
        this.currentStep = BankProcessStep.VAULT_DOOR;
    }

    public void setupVaultDoor() {
        if(currentStep.getIndex() > BankProcessStep.VAULT_DOOR.getIndex()) {
            admin.sendMessage(lang.step_already_done);
            return;
        }
        if(this.currentStep != BankProcessStep.VAULT_DOOR) {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","Vault Door"));
            return;
        }
        Cuboid cuboid = Cuboid.setupCuboid(admin);
        if(cuboid == null) {
            return;
        }
        if(isCuboidSimilar(cuboid, vaultCuboid)) {
            admin.sendMessage(lang.similar_worldedit_selection);
            return;
        }
        vaultDoorCuboid = cuboid;
        admin.sendMessage(lang.next_banker_setup);
        this.currentStep = BankProcessStep.BANKER;
    }


    public void setupHostess(Location l) {
        String name = "";
        if(currentStep == BankProcessStep.DEPOSIT_HOSTESS) {
            if(currentStep.getIndex() > BankProcessStep.DEPOSIT_HOSTESS.getIndex()) {
                admin.sendMessage(lang.step_already_done);
                return;
            }
            name = lang.deposit_hostess_name;
        } else if (currentStep == BankProcessStep.WITHDRAW_HOSTESS) {
            if(currentStep.getIndex() > BankProcessStep.WITHDRAW_HOSTESS.getIndex()) {
                admin.sendMessage(lang.step_already_done);
                return;
            }

            name = lang.withdraw_hostess_name;
        } else if(currentStep == BankProcessStep.BANKER) {
            if(currentStep.getIndex() > BankProcessStep.BANKER.getIndex()) {
                admin.sendMessage(lang.step_already_done);
                return;
            }

            name = lang.banker_name;
        } else {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","Staff (Banker, Deposit hostess, Withdraw hostess)"));
            return;

        }

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
        npc.addTrait(LookClose.class);
        npc.getTrait(LookClose.class).setRange(2);
        npc.getTrait(LookClose.class).setRandomLook(true);
        npc.getTrait(LookClose.class).setRealisticLooking(true);
        npc.getTrait(LookClose.class).toggle();
        npc.data().setPersistent("nameplate-visible", true);
        npc.addTrait(SentinelTrait.class);
        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
        sentinel.speed = 1.25F;
        npc.getNavigator().getLocalParameters().baseSpeed(1.25F);
        npc.spawn(l);
        npc.setName(name);
        BankProcessStep savedStep = currentStep;
        new BukkitRunnable() {
            @Override
            public void run() {
                SkinData skinData = config.getHostessSkin();
                if(savedStep == BankProcessStep.BANKER)
                    skinData = config.getBankerSkin();
                ((SkinnableEntity) npc.getEntity()).setSkinPersistent(UUID.randomUUID().toString(),skinData.getSignature(),skinData.getTexture());
            }
        }.runTaskLater(instance,20 * 10);


        if(currentStep == BankProcessStep.BANKER) {
            banker = npc;
            sentinel.fightback = false;
            currentStep = BankProcessStep.DEPOSIT_HOSTESS;
            admin.sendMessage(lang.next_deposit_setup);

        } else if(currentStep == BankProcessStep.DEPOSIT_HOSTESS) {
            depositHostess = npc;
            currentStep = BankProcessStep.WITHDRAW_HOSTESS;
            sentinel.squad = "hostess";
            admin.sendMessage(lang.next_withdraw_setup);

        } else if(currentStep == BankProcessStep.WITHDRAW_HOSTESS) {
            withdrawHostess = npc;
            sentinel.squad = "hostess";
            admin.sendMessage(lang.next_addchest_setup);
            currentStep = BankProcessStep.CHEST_POINTS;
        }
    }

    public void addChest(Block chest) {
        if(currentStep.getIndex() > BankProcessStep.CHEST_POINTS.getIndex()) {
            admin.sendMessage(lang.step_already_done);
            return;
        }
        if(currentStep != BankProcessStep.CHEST_POINTS) {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","Chests"));
            return;
        }
        if(!vaultCuboid.isIn(chest.getLocation())) {
            admin.sendMessage(lang.chest_outside);
            return;
        }

        chests.add(new SafeDeposit(chest));
        admin.sendMessage(new StringBuilder(lang.object_added)
                .toString()
                .replace("%id", String.valueOf(chests.size() - 1))
                .replace("%type","chest"));

        if(chests.size() == 1) {
            admin.sendMessage(lang.next_endpoint_setup);
        }
    }

    public void setupJail(Location location) {
        if(chests.isEmpty()) {
            admin.sendMessage(lang.chest_missing);
            return;
        }
        if(currentStep.getIndex() > BankProcessStep.JAIL.getIndex()) {
            admin.sendMessage(lang.step_already_done);
            return;
        }
        this.currentStep = BankProcessStep.JAIL;
        setWaitingForChest(false);
        this.jail = location;


    }


    public void setupExit(Location location) {
        if(jail == null) {
            admin.sendMessage(new StringBuilder(lang.step_missing)
                    .toString()
                    .replace("%step","Jail"));
            return;
        }
        this.exit = location;
        Bank bank = new Bank(bankCuboid,
                vaultCuboid,
                vaultDoorCuboid,
                depositHostess,
                withdrawHostess,
                banker,
                jail,
                exit,
                chests);

        instance.setBank(bank);
        instance.setBankLoader(new BankLoader(instance));
        instance.getBankLoader().save();
        instance.getBankLoader().initDefaultStaffPos();

        instance.loadBank();
        admin.sendMessage(lang.end_setup);
        reset();

    }


    public void removeChest(Block chest) {
        chests.remove(chest);
    }



    public void cancelCreation() {
        if(withdrawHostess != null) withdrawHostess.destroy();
        if(depositHostess != null) depositHostess.destroy();
        if(banker != null) banker.destroy();
        chests.forEach(c -> c.getChest().setType(Material.AIR));
    }

    private boolean isCuboidSimilar(Cuboid selected, Cuboid compare) {
        return selected.getPoint1().getBlock().equals(compare.getPoint1().getBlock())
                && selected.getPoint2().getBlock().equals(compare.getPoint2().getBlock());
    }

    public Player getAdmin() {
        return admin;
    }

    public void setWaitingForChest(boolean waitingForChest) {
        this.waitingForChest = waitingForChest;
        admin.sendMessage(lang.chest_setup);
    }

    public boolean isWaitingForChest() {
        return waitingForChest;
    }

    public boolean isSafeDeposit(Block b) {
        return chests.stream().filter(s -> s.getChest() == b).findAny().isPresent();
    }

    public World getWorld() {
        return world;
    }
}
