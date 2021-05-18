package io.testrail.company.webapp.webappAtCoverage.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRailCase {
    private Long id;
    @JsonAlias("custom_webappautoteststatus")
    private Byte autotestStatus;
    @JsonAlias("priority_id")
    private Byte priorityId;
    private String title;
    @JsonAlias("suite_id")
    private Short suiteId;
}