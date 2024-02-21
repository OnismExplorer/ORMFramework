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
    private SqlSession sqlSession;
    @Before
    public void init() throws IOException {
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        // 从 SqlSessionFactory 中获取 SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void GetTaskTest() {
        TaskDao taskDao = sqlSession.getMapper(TaskDao.class);
        System.out.println(JSON.toJSONString(taskDao.getById(100001L)));
    }
}
