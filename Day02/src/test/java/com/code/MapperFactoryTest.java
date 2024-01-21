package com.code;

import com.code.binding.MapperRegistry;
import com.code.dao.StudentDao;
import com.code.dao.UserDao;
import com.code.session.SqlSession;
import com.code.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;

/**
 * Mapper 工厂测试
 *
 * @author HeXin
 * @date 2024/01/21
 */
public class MapperFactoryTest {
    @Test
    public void MapperProxyFactoryTest() {
        // 注册 Mapper
        MapperRegistry registry = new MapperRegistry();
        registry.addMappers("com.code.dao");
        // 从 SqlSession 工厂中获取 Session
        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取映射器对象
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        StudentDao studentDao = sqlSession.getMapper(StudentDao.class);

        // 测试验证
        System.out.println(userDao.getNameById(1L));
        System.out.println("--------------------------------");
        System.out.println(studentDao.getNameByUid(1L));
    }
}
