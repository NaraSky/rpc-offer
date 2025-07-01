package com.lb.rpc.test.spi;

import com.lb.rpc.spi.loader.ExtensionLoader;
import com.lb.rpc.test.spi.service.SPIService;
import org.junit.Test;

public class SPITest {
    @Test
    public void testSpiLoader(){
        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        String result = spiService.hello("zhiyu");
        System.out.println(result);
    }
}
