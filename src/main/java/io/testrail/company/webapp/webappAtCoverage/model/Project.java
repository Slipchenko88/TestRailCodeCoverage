package io.testrail.company.webapp.webappAtCoverage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Short projectId;
    @JsonIgnore
    private Short suiteId;
    private String projectName;
    private Integer total;
    private Short covered;
    private Short mustBeCovered;
    private Short manual;
    private Short updated;
    private Double projectCoverageByAt;
    private Double atCoverage;
    private String date;

    public Project(Short projectId, String projectName, Short suiteId, Integer total, Short covered, Short mustBeCovered, Short manual, Short updated, Double projectCoverageByAt, Double atCoverage, String date) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.suiteId = suiteId;
        this.total = total;
        this.covered = covered;
        this.mustBeCovered = mustBeCovered;
        this.manual = manual;
        this.updated = updated;
        this.projectCoverageByAt = projectCoverageByAt;
        this.atCoverage = atCoverage;
        this.date = date;
    }
}


