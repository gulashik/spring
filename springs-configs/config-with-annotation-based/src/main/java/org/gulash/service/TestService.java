package org.gulash.service;

import org.gulash.domain.Student;
import org.gulash.domain.TestResult;

public interface TestService {
    TestResult executeTestFor(Student student);
}
