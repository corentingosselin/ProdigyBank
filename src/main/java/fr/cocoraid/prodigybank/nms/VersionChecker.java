package fr.cocoraid.prodigybank.nms;

import org.bukkit.Bukkit;

/**
 * Created by cocoraid on 14/01/2018.
 */
public enum VersionChecker {

    v1_9_R1(0), v1_9_R2(2), v1_10_R1(3), v1_10_R2(4), v1_11_R1(5), v1_11_R2(6), v1_12_R1(7), v1_12_R2(8), v1_13_R1(9), v1_14_R1(10), v1_15_R1(11);

    private static VersionChecker currentVersion = VersionChecker.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
    private int index;

    private VersionChecker(int index) {
        this.index = index;
    }


    public static boolean isHigherOrEqualThan(VersionChecker v) {
        return currentVersion.getIndex() >= v.getIndex();
    }

    public static boolean isLowerOrEqualThan(VersionChecker v) {
        return currentVersion.getIndex() <= v.getIndex();
    }

    public int getIndex() {
        return index;
    }
}
