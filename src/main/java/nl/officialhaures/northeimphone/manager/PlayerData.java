package nl.officialhaures.northeimphone.manager;

import java.util.UUID;

public class PlayerData {
    private UUID playerId;
    private StringBuilder pincodeBuilder;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.pincodeBuilder = new StringBuilder();
    }

    public StringBuilder getPincodeBuilder() {
        return pincodeBuilder;
    }

    public void resetPincodeBuilder() {
        pincodeBuilder.setLength(0);
    }
}
