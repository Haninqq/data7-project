package com.data7.instdesign.controller;

import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.service.ToolsService;
import com.data7.instdesign.util.JSFunc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/tools")
public class ToolController {

    private final ToolsService toolsService;

    @GetMapping("/list")
    public String list(Model model) {

        List<ToolsDTO> toolsList = toolsService.getToolsList();

        model.addAttribute("toolsList", toolsList);
        return "tools/list";
    }
    @GetMapping("/view/{toolId}")
    public String view(@PathVariable Integer toolId, Model model) {
        ToolsDTO tool = toolsService.getToolById(toolId);
        model.addAttribute("tool", tool);
        return "tools/view";
    }


}