package com.code;

import com.code.binding.MapperProxyFactory;
import com.code.dao.UserDao;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 映射器代理测试
 * Unit test for simple App.
 *
 * @author HeXin
 * @date 2024/01/21
 */

public class MapperProxyTest {

   @Test
   public void MapperProxyFactortyTest(){
      MapperProxyFactory<UserDao> factory = new MapperProxyFactory<>(UserDao.class);
      // 模拟sqlSession
      Map<String,String> sqlSession = new HashMap<>();
      // 模拟执行Mapper.xml中的SQL语句，执行根据用户id查询用户名称(key：方法名，value：SQL语句)
      sqlSession.put("com.code.dao.UserDao.getNameById","select * from user where id = ?");
      UserDao userDao = factory.newInstance(sqlSession);
      System.out.println(userDao.getNameById(1L));
   }

   @Test
   public void ProxyClassTest(){
      UserDao userDao = (UserDao) Proxy.newProxyInstance(
              Thread.currentThread().getContextClassLoader(),
              new Class[]{UserDao.class},
              ((proxy, method, args) -> "UserDao.getUser")
      );
      String result = userDao.getNameById(1L);
      System.out.println("测试结果："+ result);
   }
}
