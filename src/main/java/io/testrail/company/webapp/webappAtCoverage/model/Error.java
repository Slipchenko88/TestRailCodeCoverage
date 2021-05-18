package io.testrail.company.webapp.webappAtCoverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Error {
    private String error;
    private String msg;

    public Error(String error) {
        this.error = error;
    }
}