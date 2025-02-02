package com.shadow.dashboard.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    @GetMapping("Analytics")
    public String analytics(Model model) {
        return "analytics";
    }
}
