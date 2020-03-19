package fr.cocoraid.prodigybank.bridge;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.cocoraid.prodigybank.ProdigyBank;
import org.bukkit.Location;

public class WorldGuardBridge {

    private ProdigyBank instance;
    public WorldGuardBridge(ProdigyBank instance) {
        this.instance = instance;
    }


    public void setPvp(Location location, boolean enabled) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        assert regions != null;

        regions.getApplicableRegions(BukkitAdapter.asBlockVector(location)).forEach(r -> {
            r.setFlag(Flags.PVP, enabled ? StateFlag.State.ALLOW : StateFlag.State.DENY);
        });


    }
}
