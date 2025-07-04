package com.lb.rpc.spi.loader;

import com.lb.rpc.spi.annotation.SPI;
import com.lb.rpc.spi.annotation.SPIClass;
import com.lb.rpc.spi.factory.ExtensionFactory;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SPI扩展点加载器 - 核心类
 * <p>
 * 这个类实现了Java SPI机制的增强版本，主要功能包括：
 * 1. 动态加载配置文件中定义的服务实现类
 * 2. 管理服务实例的生命周期（单例模式）
 * 3. 支持默认实现和按名称获取实现
 * 4. 线程安全的实例缓存机制
 *
 * @param <T> 服务接口的泛型类型
 * @see <a href="https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/common/extension/ExtensionLoader.java">ExtensionLoader</a>
 */
public final class ExtensionLoader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionLoader.class);

    // SPI配置文件存放的目录路径常量
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String ZHIYU_DIRECTORY = "META-INF/zhiyu/";
    private static final String ZHIYU_DIRECTORY_EXTERNAL = "META-INF/zhiyu/external/";
    private static final String ZHIYU_DIRECTORY_INTERNAL = "META-INF/zhiyu/internal/";

    // 所有需要扫描的SPI目录数组
    private static final String[] SPI_DIRECTORIES = new String[]{
            SERVICES_DIRECTORY,
            ZHIYU_DIRECTORY,
            ZHIYU_DIRECTORY_EXTERNAL,
            ZHIYU_DIRECTORY_INTERNAL
    };

    // 全局缓存：存储每个接口类型对应的ExtensionLoader实例
    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    // 当前ExtensionLoader负责加载的接口类型
    private final Class<T> clazz;

    // 类加载器，用于加载扩展实现类
    private final ClassLoader classLoader;

    // 缓存已加载的扩展类信息：名称 -> Class对象的映射
    // 使用Holder包装以支持延迟初始化和线程安全
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // 缓存已创建的扩展实例：名称 -> 实例对象的映射
    // 每个名称对应的实例用Holder包装，实现单例模式
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    // 缓存已创建的SPI类实例：Class -> 实例对象的映射
    // 避免同一个类被重复实例化
    private final Map<Class<?>, Object> spiClassInstances = new ConcurrentHashMap<>();

    // 缓存默认SPI实现的名称（从@SPI注解的value属性获取）
    private String cachedDefaultName;

    /**
     * 私有构造函数，防止外部直接实例化
     * 只能通过getExtensionLoader方法获取实例
     *
     * @param clazz 要加载的SPI接口类
     * @param cl    类加载器
     */
    private ExtensionLoader(final Class<T> clazz, final ClassLoader cl) {
        this.clazz = clazz;
        this.classLoader = cl;
        // 如果不是ExtensionFactory类本身，则预加载ExtensionFactory的扩展类
        // 这是为了避免循环依赖问题
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClasses();
        }
    }

    /**
     * 获取指定类型和类加载器的ExtensionLoader实例
     * <p>
     * 这是一个工厂方法，实现了单例模式：
     * - 每个接口类型只会创建一个ExtensionLoader实例
     * - 使用双重检查锁定模式保证线程安全
     *
     * @param <T>   服务接口的泛型类型
     * @param clazz 服务接口的Class对象
     * @param cl    类加载器
     * @return ExtensionLoader实例
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz, final ClassLoader cl) {
        // 参数校验：接口类不能为null
        Objects.requireNonNull(clazz, "extension clazz is null");

        // 参数校验：必须是接口类型
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") is not interface!");
        }
        // 参数校验：必须标注@SPI注解
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") without @" + SPI.class + " Annotation");
        }

        // 先从缓存中获取，如果存在则直接返回
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (Objects.nonNull(extensionLoader)) {
            return extensionLoader;
        }

        // 缓存中不存在，则创建新实例并放入缓存
        // 使用putIfAbsent避免并发情况下重复创建
        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz, cl));
        return (ExtensionLoader<T>) LOADERS.get(clazz);
    }

    /**
     * 便捷方法：直接获取指定名称的扩展实例
     * <p>
     * 这是一个静态工具方法，简化了获取扩展实例的流程：
     * 1. 如果name为空，返回默认实现
     * 2. 如果name不为空，返回指定名称的实现
     *
     * @param clazz 接口的Class实例
     * @param name  SPI名称，可以为空
     * @param <T>   泛型类型
     * @return 扩展实例
     */
    public static <T> T getExtension(final Class<T> clazz, String name) {
        return StringUtils.isEmpty(name) ? getExtensionLoader(clazz).getDefaultSpiClassInstance() : getExtensionLoader(clazz).getSpiClassInstance(name);
    }

    /**
     * 获取ExtensionLoader实例（使用默认类加载器）
     *
     * @param <T>   泛型类型
     * @param clazz 接口Class对象
     * @return ExtensionLoader实例
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    /**
     * 获取默认的SPI实现实例
     * <p>
     * 默认实现是通过@SPI注解的value属性指定的：
     * 例如：@SPI("defaultImpl") 表示默认使用名为"defaultImpl"的实现
     *
     * @return 默认SPI实现实例，如果没有配置默认实现则返回null
     */
    public T getDefaultSpiClassInstance() {
        // 确保扩展类已经加载，这会同时解析默认实现名称
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName)) {
            return null;
        }
        // 获取默认实现的实例
        return getSpiClassInstance(cachedDefaultName);
    }

    /**
     * 根据名称获取SPI实现实例
     * <p>
     * 这个方法实现了单例模式和延迟初始化：
     * 1. 首先检查实例缓存
     * 2. 如果缓存中没有，则创建新实例
     * 3. 使用双重检查锁定保证线程安全
     *
     * @param name SPI实现的名称
     * @return SPI实现实例
     */
    public T getSpiClassInstance(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullPointerException("get spi class name is null");
        }
        Holder<Object> objectHolder = cachedInstances.get(name);
        if (Objects.isNull(objectHolder)) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstances.get(name);
        }
        Object value = objectHolder.getValue();

        // 如果实例不存在，需要创建新实例（双重检查锁定模式）
        if (Objects.isNull(value)) {
            synchronized (cachedInstances) {
                value = objectHolder.getValue();
                if (Objects.isNull(value)) {
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T) value;
    }

    /**
     * 获取所有SPI实现的实例列表
     * <p>
     * 这个方法会返回当前接口的所有可用实现实例：
     * 1. 如果所有实例都已缓存，直接从缓存返回
     * 2. 否则逐个创建缺失的实例
     *
     * @return SPI实例列表
     */
    public List<T> getSpiClassInstances() {
        Map<String, Class<?>> extensionClasses = this.getExtensionClasses();
        if (extensionClasses.isEmpty()) {
            return Collections.emptyList();
        }
        if (Objects.equals(extensionClasses.size(), cachedInstances.size())) {
            return (List<T>) this.cachedInstances.values().stream().map(e -> {
                return e.getValue();
            }).collect(Collectors.toList());
        }
        List<T> instances = new ArrayList<>();
        extensionClasses.forEach((name, v) -> {
            T instance = this.getSpiClassInstance(name);
            instances.add(instance);
        });
        return instances;
    }


    /**
     * 创建指定名称的扩展实例
     * <p>
     * 这个方法负责实际的实例创建工作：
     * 1. 根据名称找到对应的Class对象
     * 2. 检查是否已经有该Class的实例
     * 3. 如果没有，通过反射创建新实例
     *
     * @param name 扩展名称
     * @return 扩展实例
     */
    @SuppressWarnings("unchecked")
    private T createExtension(final String name) {
        Class<?> aClass = getExtensionClasses().get(name);
        if (Objects.isNull(aClass)) {
            throw new IllegalArgumentException("name is error");
        }

        // 检查是否已经有该Class的实例（避免同一个Class被多次实例化）
        Object o = spiClassInstances.get(aClass);
        if (Objects.isNull(o)) {
            try {
                spiClassInstances.putIfAbsent(aClass, aClass.newInstance());
                o = spiClassInstances.get(aClass);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: "
                        + aClass + ")  could not be instantiated: " + e.getMessage(), e);

            }
        }
        return (T) o;
    }

    /**
     * Gets extension classes.
     *
     * @return the extension classes
     */
    public Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if (Objects.isNull(classes)) {
            synchronized (cachedClasses) {
                classes = cachedClasses.getValue();
                if (Objects.isNull(classes)) {
                    classes = loadExtensionClass();
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClass() {
        SPI annotation = clazz.getAnnotation(SPI.class);
        if (Objects.nonNull(annotation)) {
            String value = annotation.value();
            if (StringUtils.isNotBlank(value)) {
                cachedDefaultName = value;
            }
        }
        Map<String, Class<?>> classes = new HashMap<>(16);
        loadDirectory(classes);
        return classes;
    }

    /**
     * 扫描所有SPI目录，加载配置文件
     * <p>
     * 这个方法会遍历所有预定义的SPI目录：
     * 1. 构造配置文件路径（目录 + 接口全限定名）
     * 2. 使用类加载器查找所有匹配的资源文件
     * 3. 逐个解析每个配置文件
     *
     * @param classes 用于存储加载结果的Map
     */
    private void loadDirectory(final Map<String, Class<?>> classes) {
        for (String directory : SPI_DIRECTORIES) {
            String fileName = directory + clazz.getName();
            try {
                // 使用类加载器查找所有同名资源文件
                Enumeration<URL> urls = Objects.nonNull(this.classLoader) ? classLoader.getResources(fileName)
                        : ClassLoader.getSystemResources(fileName);
                if (Objects.nonNull(urls)) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        loadResources(classes, url);
                    }
                }
            } catch (IOException t) {
                LOG.error("load extension class error {}", fileName, t);
            }
        }
    }

    /**
     * 加载单个配置文件的内容
     * <p>
     * 配置文件格式为Properties格式：
     * key=value
     * 其中key是扩展名称，value是实现类的全限定名
     *
     * @param classes 用于存储加载结果的Map
     * @param url     配置文件的URL
     * @throws IOException 文件读取异常
     */
    private void loadResources(final Map<String, Class<?>> classes, final URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                String classPath = (String) v;
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(classPath)) {
                    try {
                        loadClass(classes, name, classPath);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("load extension resources error", e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    /**
     * 加载并验证单个扩展实现类
     * <p>
     * 这个方法执行严格的验证：
     * 1. 使用类加载器加载指定的类
     * 2. 验证该类是否实现了SPI接口
     * 3. 验证该类是否标注了@SPIClass注解
     * 4. 检查是否有重复的名称映射
     *
     * @param classes   用于存储结果的Map
     * @param name      扩展名称
     * @param classPath 实现类的全限定名
     * @throws ClassNotFoundException 类加载异常
     */
    private void loadClass(final Map<String, Class<?>> classes,
                           final String name, final String classPath) throws ClassNotFoundException {
        Class<?> subClass = Objects.nonNull(this.classLoader) ? Class.forName(classPath, true, this.classLoader) : Class.forName(classPath);
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error," + subClass + " subtype is not of " + clazz);
        }
        if (!subClass.isAnnotationPresent(SPIClass.class)) {
            throw new IllegalStateException("load extension resources error," + subClass + " without @" + SPIClass.class + " annotation");
        }
        Class<?> oldClass = classes.get(name);
        if (Objects.isNull(oldClass)) {
            classes.put(name, subClass);
        } else if (!Objects.equals(oldClass, subClass)) {
            throw new IllegalStateException("load extension resources error,Duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
    }


    /**
     * 通用持有者类，用于实现延迟初始化和线程安全
     * <p>
     * 这个内部类提供了一个简单的容器：
     * 1. 使用volatile关键字保证可见性
     * 2. 支持null值的存储
     * 3. 用于配合双重检查锁定模式
     *
     * @param <T> 持有对象的类型
     */
    public static class Holder<T> {

        private volatile T value;

        /**
         * Gets value.
         *
         * @return the value
         */
        public T getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(final T value) {
            this.value = value;
        }
    }
}
