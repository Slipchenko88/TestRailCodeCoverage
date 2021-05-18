package io.testrail.company.webapp.webappAtCoverage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {
    private String status;
    private Boolean isDeleted;
}