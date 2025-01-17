package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

@Service
public interface IServerType {
    public String getIp() throws Exception;
}
