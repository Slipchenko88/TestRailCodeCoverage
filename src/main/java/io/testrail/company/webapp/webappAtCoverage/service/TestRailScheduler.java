package io.testrail.company.webapp.webappAtCoverage.service;

import io.testrail.company.webapp.webappAtCoverage.model.TestCase;
import io.testrail.company.webapp.webappAtCoverage.model.History;
import io.testrail.company.webapp.webappAtCoverage.model.TestRailCase;
import io.testrail.company.webapp.webappAtCoverage.repository.TestCaseRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Component
class TestRailScheduler {
    private static final Logger log = LogManager.getLogger(TestRailScheduler.class);

    @Value("${test.rail.user.name}")
    private String username;
    @Value("${test.rail.user.password}")
    private String password;
    @Value("${test.rail.url}")
    private String url;
    @Value("${test.rail.host}")
    private String host;
    @Value("#{${projects.map}}")
    private Map<Short, List<Short>> projectsAndSuits;
    @Value("#{${projectNames.map}}")
    private Map<Short, String> projectsNamesMap;
    @Value("#{${priorities.map}}")
    private Map<Byte, Byte> prioritiesMap;
    @Value("#{${statuses.map}}")
    private Map<Byte, String> statusesMap;

    @Autowired
    private TestCaseRepository testCaseRepository;

//        @Scheduled(fixedRateString = "${rate}")// every 30 seconds
    @Scheduled(cron = "${cron}")
    protected void handleProjects() {
        for (Short projectId : projectsAndSuits.keySet()) {
            for (Short suiteId : projectsAndSuits.get(projectId)) {
                getProjectCoverage(projectId, suiteId);
            }
        }
    }

    /**
     * Method accept projectId and suiteId and save testcases to DB
     *
     * @param projectId id of project in TestRail
     * @param suiteId   id of test suit in projectId
     */
    private void getProjectCoverage(Short projectId, Short suiteId) {
        //getting set of testcases from Test Rail
        List<TestRailCase> testRailCases = getTestRailCases(projectId, suiteId);
        //count number of each testcase statuses and log the result
        projectProcessing(testRailCases, projectId);
        //process testcases and save result
        testCasesProcessing(testRailCases, projectId, suiteId);
    }

    /**
     * Method accepts list of TestRail testcases, processes them and saves result to DB
     *
     * @param testRailCases set of TestRail testcases
     * @see TestRailCase
     * @see TestCase
     */
    private void testCasesProcessing(List<TestRailCase> testRailCases, Short projectId, Short suiteId) {
        Date now = new Date();
        /*In case TestRail is not available this list will be empty.
        Therefore just put to history new entry with current time
        and new History entry with Test Rail status and isDeleted=false(test case still presents in TestRail)*/
        if (testRailCases.isEmpty()) {
            List<TestCase> oldCases = testCaseRepository.findBySuiteId(suiteId);
            for (TestCase testCase : oldCases) {
                testCase.setDate(now);
                testCase.getHistory().put(now, new History(testCase.getStatus(), false));
                testCaseRepository.save(testCase);
            }
        } else {
            List<TestCase> newTestCases = new ArrayList<>();
            List<TestCase> dbTestCases = testCaseRepository.findBySuiteId(suiteId);
            //Check if some testcase were removed from TestRail and remove them from DB.
            int numberOfCasesInDb = dbTestCases.size();
            int numberOfCasesInTestRail = testRailCases.size();
            if ((numberOfCasesInDb > numberOfCasesInTestRail)) {
                log.info("Number of cases in DB: " + numberOfCasesInDb);
                log.info("Number of cases in TestRail: " + numberOfCasesInTestRail);
                List<Long> dbIdList = dbTestCases.stream().map(TestCase::getTestCaseId).collect(Collectors.toList());
                List<Long> testrailIdList = testRailCases.stream().map(TestRailCase::getId).collect(Collectors.toList());
                dbIdList.removeAll(testrailIdList);
                for (Long testCaseId : dbIdList) {
                    TestCase testCase = testCaseRepository.findByTestCaseId(testCaseId);
                    if (!testCase.isDeleted()) {
                        log.debug("Test case with testcase ID: " + testCase.getTestCaseId() + " was removed.");
                        testCase.getHistory().put(now, new History(testCase.getStatus(), true));
                        testCaseRepository.save(testCase);
                    }
                }
            }
            log.info("========================================");
            for (TestRailCase testRailCase : testRailCases) {
                String status = statusesMap.get(testRailCase.getAutotestStatus());
            /* If there is no such test case with TestRail id in DB
            create and fill the list of Test Cases and save them to DB */
                if (!dbTestCases.stream().map(TestCase::getTestCaseId).collect(Collectors.toList()).contains(testRailCase.getId())) {
                    newTestCases.add(new TestCase(
                            testRailCase.getSuiteId(),
                            projectId,
                            projectsNamesMap.get(projectId),
                            testRailCase.getId(),
                            prioritiesMap.get(testRailCase.getPriorityId()),
                            testRailCase.getTitle(),
                            new HashMap<Date, History>() {{
                                put(now, new History(status, false));
                            }}
                    ));
                } else {
                /* Otherwise, for all existing Test Cases if TestRail id and Test Case id in DB are equal
                and there are no isDeleted==true in history (test case still presents in TestRail)
                put to the map new entry with current time and new History entry
                with Test Rail status  and isDeleted=false(test case still presents in TestRail)
                update date, status, title and priority and save Test Case to DB */
                    for (TestCase testCase : dbTestCases) {
                        if (testCase.getTestCaseId().equals(testRailCase.getId()) && (!testCase.isDeleted())) {
                            testCase.setDate(now);
                            testCase.setStatus(status);
                            testCase.setTitle(testRailCase.getTitle());
                            testCase.setPriority(prioritiesMap.get(testRailCase.getPriorityId()));
                            testCase.getHistory().put(now, new History(status, false));
                            testCaseRepository.save(testCase);
                        }
                    }
                }
            }
            testCaseRepository.saveAll(newTestCases);
        }
    }

    /**
     * Method accept list of TestRail testcases and projectId for calculating and logging
     * number of each test case statuses
     *
     * @param testRailCases set of TestRail testcases
     * @param projectId     id of project in TestRail for displaying human-readable name of the project
     * @see TestRailCase
     */

    private void projectProcessing(List<TestRailCase> testRailCases, Short projectId) {
        int total = testRailCases.size();
        short covered = 0;
        short mustBeCovered = 0;
        short manual = 0;
        short updated = 0;
        for (TestRailCase testcase : testRailCases) {
            if (statusesMap.get(testcase.getAutotestStatus()).equalsIgnoreCase("must be covered")) {
                mustBeCovered++;
            } else if (statusesMap.get(testcase.getAutotestStatus()).equalsIgnoreCase("manual")) {
                manual++;
            } else if (statusesMap.get(testcase.getAutotestStatus()).equalsIgnoreCase("covered")) {
                covered++;
            } else if (statusesMap.get(testcase.getAutotestStatus()).equalsIgnoreCase("updated")) {
                updated++;
            }
        }
        double projectCoverageByAt = Math.round(((covered / (double) total)) * 100 * 100.0) / 100.0;
        double atCoverage = Math.round((covered) / (double) (covered + mustBeCovered + updated) * 100 * 100.0) / 100.0;
        log.warn("Project : " + projectsNamesMap.get(projectId));
        log.info("Total: " + total);
        log.trace("Must be covered: " + mustBeCovered);
        log.warn("Covered: " + covered);
        log.error("Manual: " + manual);
        log.error("Updated: " + updated);
        log.info("Coverage by AT: " + projectCoverageByAt + "%");
        log.info("AT Coverage: " + atCoverage + "%");
    }

    /**
     * Method returns list of testcases that relates to specified projectId and suiteId in TestRail via HTTP request
     *
     * @param projectId id of project in TestRail
     * @param suiteId   id of test suit in projectId
     * @return set of TestRail testcases
     * @see TestRailCase
     **/
    private List<TestRailCase> getTestRailCases(Short projectId, Short suiteId) {
        List<TestRailCase> testRailCaseList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Host", host);
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        try {
            testRailCaseList = restTemplate
                    .exchange(url + "/index.php?/api/v2/get_cases/" + projectId + "&suite_id=" + suiteId,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<TestRailCase>>() {
                            }).getBody();
        } catch (RestClientException e) {
            log.error("Can't reach TestRail and get test cases!");
            log.error(e.getMessage());
        }
        return testRailCaseList;
    }
}

