package fr.cocoraid.prodigybank.filemanager.skin;

import java.util.UUID;

public class SkinData {


    private String texture, signature, uuid;

    public SkinData(String texture, String signature, String uuid) {
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
