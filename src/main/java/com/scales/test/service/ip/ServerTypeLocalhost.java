package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

import static com.scales.test.util.Constants.serverTypeLocalhost;

@Service
public class ServerTypeLocalhost implements IServerType {

    @Override
    public String getIp() {
        return serverTypeLocalhost;
    }
}
