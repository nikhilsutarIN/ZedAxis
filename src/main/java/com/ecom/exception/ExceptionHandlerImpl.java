package com.ecom.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateInputException;

@ControllerAdvice
public class ExceptionHandlerImpl {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingServletRequestParameterException(Model model){
        model.addAttribute("error", "Reset link is invalid or expired");
        return "message";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(Model model){
        model.addAttribute("error", "Error 404! Page Not Found");
        return "message";
    }


}
