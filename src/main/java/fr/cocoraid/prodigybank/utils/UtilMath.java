package fr.cocoraid.prodigybank.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;


public class UtilMath {

    /**
     * Vecteur qui s'update autour de l'axe X avec un angle
     *
     * @param v
     * @param angle
     * @return
     */
    public static final Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    /**
     * Vecteur qui s'update autour de l'axe Y avec un angle
     *
     * @param v
     * @param angle
     * @return
     */
    public static final Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    /**
     * Vecteur qui s'update autour de l'axe Z avec un angle
     *
     * @param v
     * @param angle
     * @return
     */
    public static final Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    /**
     * @param v
     * @param angleX
     * @param angleY
     * @param angleZ
     * @return
     */
    public static final Vector rotateVector(Vector v, double angleX, double angleY, double angleZ) {
        rotateAroundAxisX(v, angleX);
        rotateAroundAxisY(v, angleY);
        rotateAroundAxisZ(v, angleZ);
        return v;
    }

    public static Vector rotate(Vector v, Location l) {
        double yaw = l.getYaw() / 180 * Math.PI;
        double pitch = l.getPitch() / 180 * Math.PI;

        v = rotateAroundAxisX(v, pitch);
        v = rotateAroundAxisY(v, -yaw);
        return v;
    }

    /**
     * Util convert
     *
     * @return f
     */
    public static byte toPackedByte(float f) {
        return (byte) ((int) (f * 256.0F / 360.0F));
    }





}
