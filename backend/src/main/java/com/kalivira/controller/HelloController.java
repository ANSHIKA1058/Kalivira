package com.kalivira.controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {
    @GetMapping("/hello")
    public String hello(){
        return "Welcome to Kalivira API";
    }
    @PostMapping("/upload")
    public String upload(){
        return "File Uploaded Successfully";
    }
    @PutMapping("/update")
    public String update(){
        return "File Updated Successfully";
    }
    @DeleteMapping("/delete")
    public String delete(){
        return "File Deleted Successully";
    }

}
