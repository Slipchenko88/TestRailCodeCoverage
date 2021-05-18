package io.testrail.company.webapp.webappAtCoverage.service;

import io.testrail.company.webapp.webappAtCoverage.model.Project;
import io.testrail.company.webapp.webappAtCoverage.model.TestCase;
import io.testrail.company.webapp.webappAtCoverage.repository.TestCaseRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.time.DateUtils.isSameDay;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ProjectProcessor {
    @Value("#{${projectNames.map}}")
    private Map<Short, String> projectsNamesMap;
    @Value("#{${projects.map}}")
    private Map<Short, Short> projectsAndSuits;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    @Autowired
    private TestCaseRepository testCaseRepository;

    private Project project;

    public Project getProjectById(Short projectId, Date dateReceived) {
        Date projectDate = null;
        List<TestCase> testCases = testCaseRepository.findByProjectId(projectId);
        String projectName = projectsNamesMap.get(projectId);
        short suiteId = projectsAndSuits.get(projectId);
        int total = 0;
        short covered = 0;
        short mustBeCovered = 0;
        short manual = 0;
        short updated = 0;
        Date now = new Date();
        if (dateReceived == null || dateReceived.after(now) || isSameDay(dateReceived, now)) {
            //filter deleted testcases
            testCases = testCases.stream()
                    .filter(testCase -> testCase
                            .getHistory()
                            .values()
                            .stream()
                            .noneMatch(history -> history.getIsDeleted().equals(true)))
                    .collect(toList());
            total = testCases.size();
            for (TestCase testcase : testCases) {
                if (testcase.getStatus().equalsIgnoreCase("must be covered")) {
                    mustBeCovered++;
                } else if (testcase.getStatus().equalsIgnoreCase("manual")) {
                    manual++;
                } else if (testcase.getStatus().equalsIgnoreCase("covered")) {
                    covered++;
                } else if ((testcase.getStatus().equalsIgnoreCase("updated"))) {
                    updated++;
                }
            }
            projectDate = testCases.stream().map(TestCase::getDate).max(Comparator.comparingLong(Date::getTime)).get();
        } else {
            List<TestCase> filtered = testCases.stream()
                    .filter(testCase -> testCase
                            .getHistory()
                            .keySet()
                            .stream()
                            .anyMatch(date -> isSameDay(date, dateReceived)))
                    .collect(toList());
            if (filtered.isEmpty()) {
                return new Project(
                        projectId,
                        projectName,
                        suiteId,
                        0,
                        (short) 0,
                        (short) 0,
                        (short) 0,
                        (short) 0,
                        0.0,
                        0.0,
                        formatter.format(dateReceived)
                );
            }
            total = filtered.size();
            for (TestCase testcase : filtered) {
                Date testCaseDate = testcase
                        .getHistory()
                        .keySet()
                        .stream()
                        .filter(date -> isSameDay(date, dateReceived))
                        .max(Comparator.naturalOrder())
                        .get();
                if (testcase.getHistory().get(testCaseDate).getStatus().equalsIgnoreCase("must be covered")) {
                    mustBeCovered++;
                } else if (testcase.getHistory().get(testCaseDate).getStatus().equalsIgnoreCase("manual")) {
                    manual++;
                } else if (testcase.getHistory().get(testCaseDate).getStatus().equalsIgnoreCase("covered")) {
                    covered++;
                } else if (testcase.getHistory().get(testCaseDate).getStatus().equalsIgnoreCase("updated")) {
                    updated++;
                }
                projectDate = testCaseDate;
            }
        }
        double projectCoverageByAt = Math.round(((covered / (double) total)) * 100 * 100.0) / 100.0;
        double atCoverage = Math.round((covered) / (double) (covered + mustBeCovered + updated) * 100 * 100.0) / 100.0;
        project = new Project(
                projectId,
                projectName,
                suiteId,
                total,
                covered,
                mustBeCovered,
                manual,
                updated,
                projectCoverageByAt,
                atCoverage,
                formatter.format(projectDate)
        );
        return project;
    }
}
