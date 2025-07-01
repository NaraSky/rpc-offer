package com.lb.rpc.test.spi.service;

import com.lb.rpc.spi.annotation.SPIClass;

@SPIClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
