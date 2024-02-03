package com.code.entity;

import java.util.List;

/**
 * 教师类
 *
 * @author HeXin
 * @date 2024/02/03
 */
public class Teacher {

    private String name;

    private double price;

    private List<Student> students;

    private Student student;

    public static class Student {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
