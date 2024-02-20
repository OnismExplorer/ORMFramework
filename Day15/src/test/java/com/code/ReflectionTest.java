package com.code;

import com.alibaba.fastjson.JSON;
import com.code.builder.xml.XMLConfigBuilder;
import com.code.dao.TaskDao;
import com.code.dao.UserDao;
import com.code.entity.Task;
import com.code.entity.User;
import com.code.executor.Executor;
import com.code.io.Resources;
import com.code.mapping.Environment;
import com.code.session.*;
import com.code.session.defaults.DefaultSqlSession;
import com.code.transaction.Transaction;
import com.code.transaction.TransactionFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;

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
    public void GetTaskTest() {
        TaskDao taskDao = sqlSession.getMapper(TaskDao.class);
        System.out.println(JSON.toJSONString(taskDao.getById(100001L)));
    }

    @Test
    public void GetByIdAndNameTest() {
        TaskDao taskDao = sqlSession.getMapper(TaskDao.class);
        // 模糊查询
        Task t1 = new Task(null, "任务%");
        List<Task> list = taskDao.getByIdAndName(t1);
        for (Task task : list) {
            System.out.println(JSON.toJSONString(task));
        }

        System.out.println("-----------------------------------------------");

        // id查询
        Task t2 = new Task(1L, "任务%");
        List<Task> list1 = taskDao.getByIdAndName(t2);
        for (Task task : list1) {
            System.out.println(JSON.toJSONString(task));
        }
    }
}
