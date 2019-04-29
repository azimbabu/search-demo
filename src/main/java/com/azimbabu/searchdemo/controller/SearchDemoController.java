package com.azimbabu.searchdemo.controller;

import com.azimbabu.searchdemo.dto.SearchDemoResponse;
import com.azimbabu.searchdemo.service.SearchDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = {"application/json"}, path ="/api")
public class SearchDemoController {

    private SearchDemoService searchDemoService;

    @Autowired
    public SearchDemoController(SearchDemoService searchDemoService) {
        this.searchDemoService = searchDemoService;
    }

    @GetMapping(path = {"/search"}, produces = {"application/json"})
    public ResponseEntity<SearchDemoResponse> search(
            @RequestParam(value = "plan-name", defaultValue = "") String planName,
            @RequestParam(value = "sponsor-name", defaultValue = "") String sponsorName,
            @RequestParam(value = "sponsor-state", defaultValue = "") String sponsorState,
            @RequestParam(value = "limit", defaultValue = "-1") int limit) {
        return ResponseEntity.ok(searchDemoService.search(planName, sponsorName, sponsorState, limit));
    }
}
