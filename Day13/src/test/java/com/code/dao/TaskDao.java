package com.code.dao;

import com.code.annotations.Select;
import com.code.entity.Task;

public interface TaskDao {

    Task getById(Long taskId);
}
