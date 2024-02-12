package com.code;

import com.alibaba.fastjson.JSON;
import com.code.dao.UserDao;
import com.code.entity.User;
import com.code.io.Resources;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * JDBC 池测试
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class ReflectionTest {

    private SqlSession sqlSession;
    @Before
    public void init() throws IOException {
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        // 从 SqlSessionFactory 中获取 SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void GetUserByIdTest(){
        // 获取映射器对象
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        // 测试
        System.out.println(JSON.toJSONString(userDao.getNameById(1L)));
    }

    @Test
    public void GetUserTest() {
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        System.out.println(JSON.toJSONString(userDao.getUser(new User(1L,"hexin"))));
    }
}
