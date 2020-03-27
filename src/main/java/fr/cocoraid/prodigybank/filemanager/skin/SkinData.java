package fr.cocoraid.prodigybank.filemanager.skin;

public class SkinData {


    private String uuid, texture, signature;
    public SkinData(String uuid,String texture, String signature) {
        this.signature = signature;
        this.texture = texture;
        this.uuid = uuid;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public String getUuid() {
        return uuid;
    }
}
