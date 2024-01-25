package com.code.io;

import com.code.builder.BaseBuilder;
import com.code.session.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Resources {


    /**
     * 获取资源作为读取者
     *
     * @param resource 资源
     * @return {@link Reader}
     * @throws IOException ioexception
     */
    public static Reader getResourceAsReader(String resource) throws IOException{
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * 将资源作为流获取
     *
     * @param resource 资源
     * @return {@link InputStream}
     * @throws IOException ioexception
     */
    private static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader[] classLoaders = getClassLoaders();
        for (ClassLoader loader : classLoaders) {
            InputStream inputStream = loader.getResourceAsStream(resource);
            if(inputStream != null){
                return inputStream;
            }
        }
        throw new IOException("没有找到资源："+resource);
    }

    private static ClassLoader[] getClassLoaders(){
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };
    }

    public static Class<?> classForName(String namespace) {
        try {
            return Class.forName(namespace);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("未找到该类："+e);
        }
    }
}
