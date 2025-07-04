package com.lb.rpc.provider.common.scanner;

import com.lb.rpc.annotation.RpcService;
import com.lb.rpc.common.helper.RpcServiceHelper;
import com.lb.rpc.common.scanner.ClassScanner;
import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RpcServiceScanner——扫描指定包下所有类，
 * 筛选出被 @RpcService 标注的服务实现，
 * 并注册到注册中心，构建 handlerMap。
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描并注册 RPC 服务
     *
     * @param host            本机地址
     * @param port            本机端口
     * @param scanPackage     要扫描的包名
     * @param registryService 注册中心客户端
     * @return key->服务实例 的映射表
     * @throws Exception 扫描或注册异常
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(
            String host, int port, String scanPackage, RegistryService registryService) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();

        // 获取包下全部类名
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handlerMap;
        }

        // 遍历所有类，筛选出带注解的
        classNameList.stream().forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);

                // 判断是否标注了 @RpcService
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    // 构建服务元数据，包含 name/version/group/host/port
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcService), rpcService.version(), rpcService.group(), host, port);

                    // 注册到注册中心
                    registryService.register(serviceMeta);
                    // 生成用于映射的唯一 key
                    String key = RpcServiceHelper.buildServiceKey(
                            serviceMeta.getServiceName(),
                            serviceMeta.getServiceVersion(),
                            serviceMeta.getServiceGroup());

                    // 实例化服务实现，放入 handlerMap
                    handlerMap.put(key, clazz.newInstance());
                    LOGGER.info("注册服务: key={}, impl={}", key, clazz.getName());
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });
        return handlerMap;
    }

    /**
     * 获取服务接口名：
     * 优先使用 interfaceClass，如果未指定，则使用 interfaceClassName
     */
    private static String getServiceName(RpcService rpcService) {
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == void.class) {
            // 如果接口Class未设置，则从名称属性获取
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}