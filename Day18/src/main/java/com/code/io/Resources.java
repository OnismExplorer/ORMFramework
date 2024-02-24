package com.code.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 通过类加载器获得 resource 的辅助类
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class Resources {


    /**
     * 获取资源作为读取者
     *
     * @param resource 资源
     * @return {@link Reader}
     * @throws IOException ioexception
     */
    public static Reader getResourceAsReader(String resource) {
        try {
            return new InputStreamReader(getResourceAsStream(resource));
        } catch (IOException e) {
            throw new RuntimeException("获取资源时发生错误："+e,e);
        }
    }

    /**
     * 将资源作为流获取
     *
     * @param resource 资源
     * @return {@link InputStream}
     * @throws IOException ioexception
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader[] classLoaders = getClassLoaders();
        for (ClassLoader loader : classLoaders) {
            InputStream inputStream = loader.getResourceAsStream(resource);
            if (inputStream != null) {
                return inputStream;
            }
        }
        throw new IOException("没有找到资源：" + resource);
    }

    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };
    }

    public static Class<?> classForName(String namespace) throws ClassNotFoundException {
        return Class.forName(namespace);
    }
}
