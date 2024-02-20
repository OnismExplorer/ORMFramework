package com.code.dao;

import com.code.annotations.Select;
import com.code.entity.Task;

import java.util.List;

public interface TaskDao {

    Task getById(Long taskId);

    List<Task> getByIdAndName(Task task);
}
