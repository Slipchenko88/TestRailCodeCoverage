package io.testrail.company.webapp.webappAtCoverage.controller;

import io.testrail.company.webapp.webappAtCoverage.service.ProjectProcessor;
import io.testrail.company.webapp.webappAtCoverage.model.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.validator.GenericValidator.isDate;

@RestController
@RequestMapping("/api")
class ProjectController {
    private final static Logger log = LogManager.getLogger(ProjectController.class);
    private final String datePattern = "dd.MM.yyyy";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
    @Value("#{${projects.map}}")
    private Map<Short, List<Short>> projectsAndSuits;
    @Value("#{${projectNames.map}}")
    private Map<Short, String> projectsNamesMap;
    @Autowired
    private ProjectProcessor projectProcessor;

    @GetMapping("/projects")
    @ResponseBody
    ResponseEntity<?> getProjectInfoById(@RequestParam Short id,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = datePattern) String date) throws ParseException {
        //Check if such project is present in config
        String currentDate = simpleDateFormat.format(new Date());
        Date dateReceived = null;
        if (!projectsAndSuits.containsKey(id)) {
            log.error("Invalid projectID=" + id + " was requested!");
            String error = "No such project with projectID = " + id;
            String message = "Available projects: " +
                    projectsNamesMap.entrySet()
                            .stream()
                            .map(e -> e.getKey() + " => " + e.getValue() + "")
                            .collect(Collectors.joining(", "));
            return new ResponseEntity<>(new Error(error, message), HttpStatus.NOT_FOUND);
        }
        if (date != null) {
            //Check if date pattern is valid
            if (!isDate(date, datePattern, true)) {
                log.error("Invalid date " + date + " was requested!");
                return new ResponseEntity<>(new Error("Invalid date format! Valid date pattern : " + currentDate), HttpStatus.BAD_REQUEST);
            } else {
                dateReceived = simpleDateFormat.parse(date);
            }
        }
        log.info("Create response about " + projectsNamesMap.get(id) + " project for " + (date != null ? date : currentDate));
        return new ResponseEntity<>(projectProcessor.getProjectById(id, dateReceived), HttpStatus.OK);
    }
}
