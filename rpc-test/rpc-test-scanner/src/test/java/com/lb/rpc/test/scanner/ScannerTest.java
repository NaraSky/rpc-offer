package com.lb.rpc.test.scanner;

import com.lb.rpc.common.scanner.ClassScanner;
import com.lb.rpc.common.scanner.reference.RpcReferenceScanner;
import com.lb.rpc.provider.common.scanner.RpcServiceScanner;
import org.junit.Test;

import java.util.List;

public class ScannerTest {

    /**
     * 扫描com.lb.rpc.test.scanner包下所有的类
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("com.lb.rpc.test.scanner");
        classNameList.forEach(System.out::println);
    }

    /**
     * 扫描com.lb.rpc.test.scanner包下所有标注了@RpcService注解的类
     */
    @Test
    public void testScannerClassNameListByRpcService() throws Exception {
        // RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService("com.lb.rpc.test.scanner");
    }

    /**
     * 扫描com.lb.rpc.test.scanner包下所有标注了@RpcReference注解的类
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("com.lb.rpc.test.scanner");
    }
}
