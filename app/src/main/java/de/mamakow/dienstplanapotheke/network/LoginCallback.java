package de.mamakow.dienstplanapotheke.network;

public interface LoginCallback {
    void onSuccess(String jwtLoginToken);

    void onFailure(Exception exception);
}