package io.testrail.company.webapp.webappAtCoverage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "testcase")
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Short suiteId;
    @JsonIgnore
    private Short projectId;
    private String projectName;
    private Long testCaseId;
    private String status;
    private Byte priority;
    private String title;
    private Date date;
    @ElementCollection
    @CollectionTable(name = "history", joinColumns = @JoinColumn(name = "test_case_id"))
    @MapKeyColumn()
    private Map<Date, History> history;

    @JsonIgnore
    public boolean isDeleted() {
        return this.getHistory().values().stream().anyMatch(history -> history.getIsDeleted().equals(true));
    }

    public TestCase(Short suiteId, Short projectId, String projectName, Long testCaseId, Byte priority, String title, Map<Date, History> history) {
        this.suiteId = suiteId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.testCaseId = testCaseId;
        this.priority = priority;
        this.title = title;
        this.history = history;
        this.date = history.keySet().stream().max(Comparator.comparingLong(Date::getTime)).get();
        this.status = history.get(date).getStatus();
    }
}
