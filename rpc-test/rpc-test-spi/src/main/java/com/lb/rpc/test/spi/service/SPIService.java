package com.lb.rpc.test.spi.service;

import com.lb.rpc.spi.annotation.SPI;

@SPI("spiService")
public interface SPIService {
    String hello(String name);
}
