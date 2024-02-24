package com.code;

import com.alibaba.fastjson.JSON;
import com.code.dao.TaskDao;
import com.code.io.Resources;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class ORMFrameworkTest {
    private SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("datasource.xml"));

    @Test
    public void GetTaskTest() {
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        TaskDao taskDao1 = sqlSession1.getMapper(TaskDao.class);
        System.out.println(JSON.toJSONString(taskDao1.getById(100001L)));
        sqlSession1.close();

        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        TaskDao taskDao2 = sqlSession2.getMapper(TaskDao.class);
        // 再次查询则先从缓存中获取
        System.out.println(JSON.toJSONString(taskDao2.getById(100001L)));
        sqlSession2.close();
    }
}
