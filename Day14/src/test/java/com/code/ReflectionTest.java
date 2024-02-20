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
    public void saveTest() {
        TaskDao taskDao = sqlSession.getMapper(TaskDao.class);

        Task task = new Task(100003L, "任务三", "这是第三个任务", new Date(), new Date());
        taskDao.save(task);
        sqlSession.commit();
    }

    @Test
    public void SaveSelectTest() throws IOException {
        // 解析 XML
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        // 获取 DefaultSqlSession
        final Environment environment = configuration.getEnvironment();
        TransactionFactory transactionFactory = environment.transactionFactory();
        Transaction tx = transactionFactory.newTransaction(configuration.getEnvironment().dataSource(), TransactionIsolationLevel.READ_COMMITTED, false);

        // 创建执行器
        final Executor executor = configuration.newExecutor(tx);
        SqlSession sqlSession = new DefaultSqlSession(configuration, executor);

        Task task = new Task(100004L, "任务四", "这是第四个任务", new Date(), new Date());
        int count = sqlSession.insert("com.code.dao.TaskDao.save", task);

        Object o = sqlSession.selectOne("com.code.dao.TaskDao.save!selectKey");
        System.out.println("count："+count);
        System.out.println("index:"+JSON.toJSONString(o));
        sqlSession.commit();
    }
}
