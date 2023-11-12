package com.adnantech.chatapp_free_version;

import android.app.Application;

import org.webrtc.PeerConnection;

public class RTCClients {
    private Application application;
    private String username;
    private SocketRepository socketRepository;
    private PeerConnection.Observer observer;

    public RTCClients(Application application, String username, SocketRepository socketRepository, PeerConnection.Observer observer) {
        this.application = application;
        this.username = username;
        this.socketRepository = socketRepository;
        this.observer = observer;

        initPeerConnectionFactory(application);
    }

    private void initPeerConnectionFactory(Application application) {
        // Implement the code to initialize PeerConnectionFactory
    }
}
