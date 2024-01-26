package com.code;

import com.alibaba.fastjson2.JSON;
import com.code.builder.xml.XMLConfigBuilder;
import com.code.dao.UserDao;
import com.code.entity.User;
import com.code.io.Resources;
import com.code.session.Configuration;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import com.code.session.defaults.DefaultSqlSession;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * xmlconfig 测试
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class XMLJdbcTest {

    @Test
    public void SelectOneTest() throws IOException{
        // 解析 XML
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        XMLConfigBuilder builder = new XMLConfigBuilder(reader);
        Configuration configuration = builder.parse();

        // 获取 DefaultSqlSession
        SqlSession sqlSession = new DefaultSqlSession(configuration);

        // 执行查询(默认为一个集合参数)
        Object[] request = {1L};
        Object result = sqlSession.selectOne("com.code.dao.UserDao.getNameById", request);
        System.out.println("测试结果："+JSON.toJSONString(result));
    }
    @Test
    public void SqlSessionFactoryTest() throws IOException {
        // 从SqlSessionFactory 中获取 SqlSession
        Reader reader = Resources.getResourceAsReader("datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取 Mapper 映射器对象
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        // 验证测试
        User user = userDao.getNameById(1L);
        System.out.println("测试结果："+ JSON.toJSONString(user));
    }
}
