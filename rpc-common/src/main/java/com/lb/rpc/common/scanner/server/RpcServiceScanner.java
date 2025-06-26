package com.lb.rpc.common.scanner.server;

import com.lb.rpc.annotation.RpcReference;
import com.lb.rpc.annotation.RpcService;
import com.lb.rpc.common.scanner.ClassScanner;
import com.lb.rpc.common.scanner.reference.RpcReferenceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * RPC服务提供者扫描器
 * <p>
 * 该类负责扫描指定包下的所有类，识别并处理标注了@RpcService注解的类，
 * 将这些类注册为RPC服务提供者。这是RPC服务端初始化的核心组件。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>扫描指定包下的所有类文件</li>
 *   <li>识别标注了@RpcService注解的服务实现类</li>
 *   <li>解析服务注解配置信息</li>
 *   <li>构建服务元数据并注册到注册中心</li>
 *   <li>将服务实例添加到处理器映射表中</li>
 * </ul>
 *
 * @author lb
 * @since 1.0.0
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选处理使用@RpcService注解标注的类
     * <p>
     * 这是RPC服务端初始化的核心方法，负责发现和处理所有需要暴露为远程服务的实现类。
     * 方法会递归扫描指定包及其子包下的所有类，通过反射技术检查每个类的@RpcService注解，
     * 解析注解配置信息，注册服务。
     * </p>
     *
     * @param scanPackage 要扫描的包名，支持通配符，如"com.lb.rpc"
     * @return 处理结果的映射表，目前返回空Map，后续会包含服务实例信息
     * @throws Exception 扫描过程中可能抛出的异常，包括类加载异常、反射异常等
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilter(String scanPackage) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            LOGGER.warn("未在包 [{}] 中找到任何类文件", scanPackage);
            return handlerMap;
        }
        LOGGER.info("开始扫描包 [{}]，共找到 {} 个类", scanPackage, classNameList.size());
        classNameList.stream().forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    LOGGER.info("发现标注了@RpcService注解的类: {}", clazz.getName());
                    LOGGER.info("@RpcService注解配置详情:");
                    LOGGER.info("  interfaceClass: {}", rpcService.interfaceClass());
                    LOGGER.info("  interfaceClassName: {}", rpcService.interfaceClassName());
                    LOGGER.info("  版本号(version): {}", rpcService.version());
                    LOGGER.info("  服务分组(group): {}", rpcService.group());
                    // TODO: 后续实现服务注册、元数据构建、handlerMap填充等
                    LOGGER.warn("服务 [{}] 的注册与元数据构建尚未实现", clazz.getName());
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("无法加载类 [{}]：{}", className, e.getMessage());
            } catch (Exception e) {
                LOGGER.error("扫描类 [{}] 时发生异常：{}", className, e.getMessage(), e);
            }
        });
        LOGGER.info("包 [{}] 服务扫描完成", scanPackage);
        return handlerMap;
    }

    /**
     * 获取serviceName
     */
    private static String getServiceName(RpcService rpcService) {
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == void.class) {
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}