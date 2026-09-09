package com.bookbridge.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public String action;
    public Object payload;
    public boolean success;
    public Object responseData;
    public String errorMessage;

    public NetworkMessage() {}

    public NetworkMessage(String action, Object payload) {
        this.action = action;
        this.payload = payload;
        this.success = false;
    }

    public static NetworkMessage success(String action, Object responseData) {
        NetworkMessage msg = new NetworkMessage(action, null);
        msg.success = true;
        msg.responseData = responseData;
        return msg;
    }

    public static NetworkMessage error(String action, String errorMessage) {
        NetworkMessage msg = new NetworkMessage(action, null);
        msg.success = false;
        msg.errorMessage = errorMessage;
        return msg;
    }

    @Override
    public String toString() {
        return String.format("NetworkMessage[action=%s, success=%b, error=%s]", action, success, errorMessage);
    }
}
