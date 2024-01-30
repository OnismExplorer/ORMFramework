package com.code;

import com.alibaba.fastjson.JSON;
import com.code.dao.UserDao;
import com.code.datasource.pool.PoolDataSource;
import com.code.entity.User;
import com.code.io.Resources;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 池测试
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class JdbcPoolTest {

    @Test
    public void PoolTest() throws SQLException, InterruptedException {
        PoolDataSource dataSource = new PoolDataSource();
        dataSource.setDriver("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis?useUnicode=true&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        // 通过循环持续的获取连接
        while(true){
            Connection connection = dataSource.getConnection();
            // 打印 Connection 连接的hashcode
            System.out.println(connection);
            // 线程休眠500毫秒
            Thread.sleep(500);
            // 关闭连接
            connection.close();
        }
    }
    @Test
    public void SqlSessionFactoryTest() throws IOException {
        // 从SqlSessionFactory 中获取 SqlSession
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取 Mapper 映射器对象
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        // 验证测试(查询100次id为1的用户)
        for (int i = 0; i < 100; i++) {
            User user = userDao.getNameById(1L);
            System.out.println("测试结果：" + JSON.toJSONString(user));
        }
    }
}
