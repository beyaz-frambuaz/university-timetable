package com.foxminded.timetable.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {

    long count();

    List<T> findAll();

    Optional<T> findById(long id);

    List<T> saveAll(List<T> entities);

}
