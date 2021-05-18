package io.testrail.company.webapp.webappAtCoverage.controller;

import io.testrail.company.webapp.webappAtCoverage.model.TestCase;
import io.testrail.company.webapp.webappAtCoverage.repository.TestCaseRepository;
import io.testrail.company.webapp.webappAtCoverage.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNumeric;

@RestController
@RequestMapping("/api")
public class TestCaseController {
    @Value("#{${projectNames.map}}")
    private Map<Short, String> projectsNamesMap;

    @Value("#{${projects.map}}")
    private Map<Short, List<Short>> projectsAndSuits;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @GetMapping("/testcases/actual")
    ResponseEntity<?> getTestCasesByProjectId(@RequestParam(required = false) String projectId,
                                              @RequestParam(required = false) String priority,
                                              @RequestParam(required = false) String suiteId,
                                              @RequestParam(required = false) String status) {
        String error;
        String message;
        List<TestCase> testCaseList = new ArrayList<>();
        if (projectId != null) {
            if (isNumeric(projectId)) {
                if (projectsNamesMap.containsKey(Short.valueOf(projectId))) {
                    testCaseList = testCaseRepository.findByProjectId(Short.valueOf(projectId))
                            .stream()
                            .filter(test -> !test.isDeleted())
                            .collect(Collectors.toList());
                } else {
                    error = "No such project with projectID = " + projectId;
                    message = "Available projects: " +
                            projectsNamesMap.entrySet()
                                    .stream()
                                    .map(e -> e.getKey() + " => " + e.getValue() + "")
                                    .collect(Collectors.joining(", "));
                    return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
                }
            } else {
                error = projectId + " is not valid projectId value.";
                message = "Project ID should be a positive number.";
                return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
            }
        } else {
            error = "Project ID should not be null!";
            message = "Available projects: " + Arrays.toString(projectsAndSuits.keySet().toArray());
            return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
        }
        if (priority != null) {
            if (isNumeric(priority)) {
                testCaseList = testCaseList
                        .stream()
                        .filter(testCase -> testCase.getPriority().equals(Byte.valueOf(priority)))
                        .collect(Collectors.toList());

            } else {
                error = priority + " is not valid priority value.";
                message = "Priority should be a positive number.";
                return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
            }
        }
        if (suiteId != null) {
            if (isNumeric(suiteId)) {
                if (projectsAndSuits.get(Short.valueOf(projectId)).contains(Short.valueOf(suiteId))) {
                    testCaseList = testCaseList
                            .stream()
                            .filter(testCase -> testCase.getSuiteId().equals(Short.valueOf(suiteId)))
                            .collect(Collectors.toList());
                } else {
                    error = "No such suite with suite ID = " + suiteId + " in project = " + projectId;
                    message = "Available suites in projects: " + projectsAndSuits
                            .entrySet()
                            .stream()
                            .map(e -> "Project:" + e.getKey() + " => Suites:" + e.getValue())
                            .collect(Collectors.joining(", "));
                    return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
                }
            } else {
                error = suiteId + " is not valid suite ID value.";
                message = "Suite ID should be a positive number.";
                return new ResponseEntity<>(new Error(error, message), HttpStatus.BAD_REQUEST);
            }
        }
        return testCaseList.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(testCaseList, HttpStatus.OK);
    }

    @GetMapping("/testcases/by/id/{testCaseId}")
    ResponseEntity<?> getTestCase(@PathVariable Long testCaseId) {
        Optional<TestCase> testCase = Optional.ofNullable(testCaseRepository.findByTestCaseId(testCaseId));
        return testCase.map(response -> ResponseEntity
                .ok()
                .body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
