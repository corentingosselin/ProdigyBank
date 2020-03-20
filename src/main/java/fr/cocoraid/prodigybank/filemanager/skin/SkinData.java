package fr.cocoraid.prodigybank.filemanager.skin;

public class SkinData {


    private String texture, signature;
    public SkinData(String texture, String signature) {
        this.signature = signature;
        this.texture = texture;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }
}
