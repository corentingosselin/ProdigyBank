package fr.cocoraid.prodigybank.filemanager.language;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Language {


    private transient File file;
    private transient FileConfiguration langFile;
    public Language(File file, FileConfiguration fc) {
        this.file = file;
        this.langFile = fc;
    }


    //idea
    //the banker has the key of the bank, can force to give,  it's random


    //Default english

    //admin message
    public String no_permission = "§cYou do not have the permission to do that";
    public String bank_remove_first = "§cYou are trying to setup a new bank while a current already existing, please remove it first with §6/pb delete bank";
    public String bank_already_created = "§cA bank has been already created !";
    public String setup_in_progress = "§cAdministrator is currently setting up the bank, you can force cancelling this process by using §6/pb setup forcecancel";
    public String setup_already_in_progress = "§cYou have already starting a setup process, if you would like to stop please user §6/pb cancel";
    public String no_process_owner = "§cYou are not yet entered on setup mode, please use §6/pb setup";
    public String no_process = "§cThere is not current setup process...";
    public String process_cancelled = "§cThe bank setup process has been cancelled";
    public String bank_not_created = "§cCan't find the bank please create one first §6/pb setup";
    public String different_world = "§cThe world where you are trying to setup the bank is not the same as the first attempt";
    public String worldedit_no_selection = "§cYou must select §4two points §cwith worldedit if you want to continue...";
    public String need_air = "§cYour selection does not contain air block, you need at least 1 block of air to create door to lock";
    public String similar_worldedit_selection = "§cPlease select another region, your selection is similar with the previous one, did you make a mistake ?";
    public String start_bank_setup = "§bLet's starting the setup bank process:  \n    -§6> §7Select a region with worldedit and use §6/pb setup bank";
    public String next_vaultarea_setup = "§bNice let's define vault area now:  \n    -§6> §7Select a region with worldedit and use §6/pb setup vault";
    public String next_banker_setup = "§bCongratz we need to place staff members:  \n    -§6> §7Go where you want and use §6/pb setup banker §7to place the banker";
    public String next_deposit_setup = "§bTwo staff member left:  \n    -§6> §7Go where you want and use §6/pb setup deposit§7 to place the deposit hostess";
    public String next_withdraw_setup = "§bHey One we staff member left:  \n    -§6> §7Go where you want and use §6/pb setup withdraw §7to place the withdraw hostess";
    public String next_vaultdoor_setup = "§bFine, we need a vault door to protect the money:  \n    -§6> §7Select the door with worldedit and use §6/pb setup vaultdoor";
    public String next_addchest_setup = "§bWe need safe deposit:  \n    -§6> §7Use §6/pb setup chest §7Then take enderchest block and place it";
    public String chest_setup = "§bYou can now place chest or enderchest exclusively inside the vault area you just created previously";
    public String next_endpoint_setup = "§bWhen you have place enough chest, this is the last step, we need the end point if the player is jailed:  \n    -§6> §7Go where you want and use §6/pb setup jail";
    public String object_added = "§bNew %type with id: §3%id";
    public String chest_removed = "§bSafe deposit has been removed";
    public String chest_outside = "§cYou are trying to add a chest outside the vault area you created, please go inside the area first and place your chest";
    public String chest_missing = "§cYou need at least 1 chest inside your vault if you want to continue, use §6/pb setup chest §cand place a chest...";
    public String end_setup = "§bAmazing job ! The bank is ready, however you can add more options: " +
            "\n   -§6> §7Select any window with worldedit you want to lock if the player has reach the escape time and use §6/pb add doortolock" +
            "\n   -§6> §7Select any window with worldedit you want to lock if the player has reach the escape time and use §6/pb add doortolock";
    public String step_missing = "§cYou can't setup this because a previous step is missing: §6%step";
    public String step_already_done = "§cThe command you are trying to enter has been already initialized !";
    public String staff_removed = "&cStaff member removed from the bank";

    public String deposit_hostess_name = "§bDeposit";
    public String withdraw_hostess_name = "§bWithdraw";
    public String banker_name = "§4Banker";

    public String title_holdup_starting = "§4Hold up : §7Tell me where is the banker right now !";
    public String title_holdup_help = "§3Info : §cFind the vault §f> §aStart Driller §f> §2Collect keys from the banker";

    public String title_swatting = " : §3The swat team is coming now...";
    public String title_doors_closed = "§4Locked up ! : §cYou must use a bomb to escape...";

    public String title_time_left_notify_doors = "§cEscape ! : §7%time minutes to escape before jail !";
    public String title_time_left_notify_swat = "§3You now have  : §b%time minutes before swat team coming";
    public String title_time_left_notify_escape = "§cEscape ! : §7%time minutes to escape before jail !";

    //ACTION BAR TIMER
    public String time_left_type = "§cTIME LEFT: §4%time §cBEFORE %type ";
    public String time_swat_type = "§3SWAT";
    public String time_doors_type = "§7DOORS";
    public String time_jail_type = "§4JAIL";

    public String time_over = "§cTIME OVER ! You are sent to jail...";

    public String title_left_warning = "§cWarning : §7The bank has not been robbed";
    public String title_bank_left = "§cBank left : §7While the bank has not been robbed";
    public String title_bank_leader_left = "§cLeader surrender : §7While the bank has not been robbed";

    public String title_bank_owner_success = "§3Congratulation : §bYou have successfully robbed the bank !";
    public String title_bank_member_success = "§3Congratulation : §bThe leader has left the bank...";
    public String title_reward = "§2Reward : §bYou get now %amount dollars";

    public String title_failed = "§4You failed ! : §cYou just lose %amount of your money";
    public String title_owner_died = "§4Leader Died ! : §cYou are now jailed....";
    public String title_owner_self_died = "§4You Died ! : §cYour squad has been jailed...";
    public String title_member_self_died = "§4You Died ! : §cYou were excluded from the squad";
    public String title_member_notify_died = " : §c%player has been killed by %staff";

    public String title_area_restricted = "§4Area restricted ! : §cRobbery in progress, SWAT is coming...";
    public String title_hostage_notify = "§4Hold UP ! : §cYou are now hostage (pvp enabled)";


    public String chest_key_name = "§6Safe Deposit Key";
    public String money_collected = "§2+%amount$";
    public String no_more_key = "§cI do not have key anymore...";


    public File getFile() {
        return file;
    }

    public FileConfiguration getLangFile() {
        return langFile;
    }
}
