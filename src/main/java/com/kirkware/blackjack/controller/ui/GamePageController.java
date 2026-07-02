package com.kirkware.blackjack.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that serves the web UI for playing Blackjack.
 */
@Controller
public class GamePageController {

    /**
     * Serves the main Blackjack game page at /.
     */
    @GetMapping("/")
    public String gamePage() {
        return "index";
    }
}
