package com.task.demo.task;

import com.task.demo.task.model.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


@Controller
@RequestMapping()
public class fileController {
    private final fileService fileService;


    @Autowired
    public fileController(fileServiceImpl fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/upload")
    public String uploadFile(Model model, @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "There is no file!");
                return "index.html";
            }
            model.addAttribute("selected",new Pair());
            model.addAttribute("options",fileService.getAllEmplIds(file));

            return "table.html";
        } catch (Exception e) {
            return "index.html";
        }
    }
    @GetMapping("/show")
    public String showTable(Model model,@ModelAttribute("selected")Pair pair) {
        try {
            Map<String[],Long> listOfRecords=fileService.findAllSelectedPairs(pair);
            if(listOfRecords.isEmpty()) {
                model.addAttribute("emptyList", "There are no records of these employees.");
            }
            model.addAttribute("listOfRecords",listOfRecords);
            return "table.html";
        } catch (Exception e) {
            return "table.html";
        }
    }
    @GetMapping()
    public String homepage() {
        return "index.html";
    }
}
