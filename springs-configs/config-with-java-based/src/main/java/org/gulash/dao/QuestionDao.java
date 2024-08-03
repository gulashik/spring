package org.gulash.dao;

import org.gulash.domain.Question;

import java.util.List;

public interface QuestionDao {
    List<Question> findAll();
}
