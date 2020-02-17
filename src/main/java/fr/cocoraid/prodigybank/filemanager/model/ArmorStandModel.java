package fr.cocoraid.prodigybank.filemanager.model;

import fr.cocoraid.prodigybank.ProdigyBank;
import fr.cocoraid.prodigybank.bank.Driller;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ArmorStandModel {

    private Driller driller;

    private Map<ModelType, List<Model>> models = new HashMap<>();

    private ProdigyBank instance;
    public ArmorStandModel(ProdigyBank instance) {
        this.instance = instance;
    }
    public void loadModels() {
        try {

            for (ModelType model : ModelType.values()) {

                List<Model> tempList = new ArrayList<>();

                InputStream in = instance.getResource("models/" + model.getPathName() + ".txt");

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] arr = line.split(" ", 12);
                    List<String> elements = new ArrayList<>(Arrays.asList(arr));
                    double vecX, vecY, vecZ = 0;
                    vecX = Double.valueOf(arr[0]);
                    vecY = Double.valueOf(arr[1]);
                    vecZ = Double.valueOf(arr[2]);
                    EulerAngle angle = new EulerAngle(0, 0, 0);
                    Material material = null;
                    if (arr[3].contains("Head")) {
                        String posecrypted = arr[3].replace("Head:[", "").replace("]", "").replace("f", "");
                        String[] pose = posecrypted.split(",", 10);
                        angle = new EulerAngle(Double.valueOf(pose[0]),Double.valueOf(pose[1]),Double.valueOf(pose[2]));
                    }

                    try {
                        material = Material.valueOf(arr[arr.length - 1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        //silent
                        e.printStackTrace();
                    }
                    if (material == null) {
                        continue;
                    }
                    Model m = new Model(new ItemStack(material), new Vector(vecX, vecY, vecZ), angle, elements.contains("small"));
                    tempList.add(m);
                }
                models.put(model, tempList);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!models.isEmpty()) {
            System.out.println("[ProdigyBank] Models loaded:");
            models.keySet().forEach(m -> {
                System.out.println("   - " + m.name().toLowerCase());
            });
        }
    }

    public Map<ModelType, List<Model>> getModels() {
        return models;
    }

    public void setDriller(Driller driller) {
        this.driller = driller;
    }

    public Driller getDriller() {
        return driller;
    }
}
