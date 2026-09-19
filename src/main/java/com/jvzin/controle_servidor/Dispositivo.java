package com.jvzin.controle_servidor;

public class Dispositivo {

    private final String deviceId;

    private final String deviceTokenHash;

    private String username;


    public Dispositivo(
            String deviceId,
            String deviceTokenHash
    ) {

        this.deviceId = deviceId;

        this.deviceTokenHash = deviceTokenHash;
    }


    public String getDeviceId() {
        return deviceId;
    }


    public String getDeviceTokenHash() {
        return deviceTokenHash;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(
            String username
    ) {

        this.username = username;
    }
}