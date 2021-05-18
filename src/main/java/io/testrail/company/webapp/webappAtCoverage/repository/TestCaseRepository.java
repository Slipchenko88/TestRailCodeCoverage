package io.testrail.company.webapp.webappAtCoverage.repository;

import io.testrail.company.webapp.webappAtCoverage.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    TestCase findByTestCaseId(Long testCaseId);

    List<TestCase> findByProjectId(Short projectId);

    List<TestCase> findByStatus(String status);

    List<TestCase> findByPriority(Byte priority);

    List<TestCase> findBySuiteId(Short suiteId);

}
