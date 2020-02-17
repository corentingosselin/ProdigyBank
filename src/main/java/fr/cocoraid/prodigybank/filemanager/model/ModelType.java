package fr.cocoraid.prodigybank.filemanager.model;

public enum ModelType {

    DRILLER("driller");
    //VAULT_DOOR_UNLOCKER("vaultdoor-unlocker");

    private String pathName;
    ModelType(String pathName) {
        this.pathName = pathName;
    }

    public String getPathName() {
        return pathName;
    }
}
