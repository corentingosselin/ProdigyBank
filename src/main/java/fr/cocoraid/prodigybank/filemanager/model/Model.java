package fr.cocoraid.prodigybank.filemanager.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;


public class Model {

    private Vector vector;
    private ItemStack item;
    private EulerAngle angle;
    private boolean small;
    public Model(ItemStack item, Vector vector, EulerAngle angle, boolean small) {
        this.item = item;
        this.vector = vector;
        this.angle = angle;
        this.small = small;
    }

    public EulerAngle getAngle() {
        return angle;
    }

    public ItemStack getItem() {
        return item;
    }

    public Vector getVector() {
        return vector;
    }

    public boolean isSmall() {
        return small;
    }
}
