package com.code;

import com.code.dao.UserDao;
import com.code.io.Resources;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * xmlconfig 测试
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class XMLConfigTest {

    @Test
    public void SqlSessionFactoryTest() throws IOException {
        // 从SqlSessionFactory 中获取 SqlSession
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取 Mapper 映射器对象
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        // 验证测试
        String result = userDao.getNameById(1L);
        System.out.println("测试结果："+result);
    }
}
