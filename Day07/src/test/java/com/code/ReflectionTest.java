package com.code;

import com.alibaba.fastjson.JSON;
import com.code.dao.UserDao;
import com.code.entity.Teacher;
import com.code.entity.User;
import com.code.io.Resources;
import com.code.reflection.MetaObject;
import com.code.reflection.SystemMetaObject;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 池测试
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class ReflectionTest {

    @Test
    public void testReflection(){
        Teacher teacher = new Teacher();
        List<Teacher.Student> list = new ArrayList<>();
        list.add(new Teacher.Student());
        teacher.setName("HeXin");
        teacher.setStudents(list);

        MetaObject metaObject = SystemMetaObject.forObject(teacher);

        System.out.println("getGetterNames："+ JSON.toJSONString(metaObject.getGetterNames()));
        System.out.println("getSetterNames："+JSON.toJSONString(metaObject.getSetterNames()));
        System.out.println("name的get方法返回值："+ JSON.toJSONString(metaObject.getGetterType("name")));
        System.out.println("students的set方法参数值："+JSON.toJSONString(metaObject.getGetterType("students")));
        System.out.println("name的hasGetter："+metaObject.hasGetter("name"));
        System.out.println("student.id（属性为对象）的hasGetter："+metaObject.hasGetter("student.id"));
        System.out.println("获取name的属性值："+metaObject.getValue("name"));
        // 重新设置属性值
        metaObject.setValue("name", "小白");
        System.out.println("设置name的属性值："+metaObject.getValue("name"));
        // 设置属性（集合）的元素值
        metaObject.setValue("students[0].id", "001");
        System.out.println("获取students集合的第一个元素的属性值："+ JSON.toJSONString(metaObject.getValue("students[0].id")));
        System.out.println("对象的序列化："+JSON.toJSONString(teacher));

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
        System.out.println("测试结果：" + JSON.toJSONString(user));
    }
}
