package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

import static com.scales.test.util.Constants.*;

@Service
public class ServerTypeFactory {

    public IServerType getServerType(String type) {
        return switch (type.toLowerCase()) {
            case serverTypeEc2 -> new ServerTypeEc2();
            case serverTypeEcs -> new ServerTypeEcs();
            case serverTypeLambda -> new ServerTypeLambda();
            case serverTypeLocalhost -> new ServerTypeLocalhost();
            default -> throw new IllegalArgumentException("Unsupported server type: " + type);
        };
    }
}