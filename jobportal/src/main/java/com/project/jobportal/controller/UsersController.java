package com.project.jobportal.controller;

import com.project.jobportal.entity.Users;
import com.project.jobportal.entity.UsersType;
import com.project.jobportal.services.UsersService;
import com.project.jobportal.services.UsersTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
public class UsersController {
    private final UsersTypeService usersTypeService;
    private final UsersService usersService;

    @Autowired
    public UsersController(UsersTypeService usersTypeService, UsersService usersService) {
        this.usersTypeService = usersTypeService;
        this.usersService = usersService;
    }

    @Operation(summary = "Display registration page",
            description = "Loads the registration form and populates it with available user types (roles) retrieved from the database.")
    @ApiResponse(responseCode = "200", description = "Registration page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/register")
    public String register(@Parameter(hidden = true) Model model) {
        log.info("Request to display registration page"); // Лог запроса страницы

        List<UsersType> usersTypes = usersTypeService.getAll();
        model.addAttribute("getAllTypes", usersTypes);
        model.addAttribute("user", new Users());

        log.debug("Loaded {} user types for registration", usersTypes.size());

        return "register";
    }
//          ???
//    @PostMapping("/register/new")
//    public String userRegistration(Users users, Model model){
//        //---- check if email is already existed ----
//        Optional<Users> optionalUsers = usersService.getUserByEmail(users.getEmail());
//        if(optionalUsers.isPresent()){
//            model.addAttribute("error", "Email already in use");
//            List<UsersType> usersTypes = usersTypeService.getAll();
//            model.addAttribute("getAllTypes", usersTypes);
//            model.addAttribute("user", new Users());
//            return "register";
//        }
//        //----------------
//        usersService.addNew(users);
//        return "dashboard";
//    }

    @Operation(summary = "Register a new user",
            description = "Processes the registration form, validates user input, and saves the new user to the database before redirecting to the dashboard.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "User successfully registered. Redirecting to dashboard.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation Error: Provided user data is invalid")
    })
    @PostMapping("/register/new")
    public String userRegistration(@Valid Users users){
        log.info("Attempting to register new user with email: {}", users.getEmail()); // Лог начала регистрации

        usersService.addNew(users);

        log.info("User with email: {} successfully registered", users.getEmail()); // Лог успеха

        return "redirect:/dashboard/";
    }

    @Operation(summary = "Display login page", description = "Returns the standard login view for existing users.")
    @ApiResponse(responseCode = "200", description = "Login page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/login")
    public String login(){
        log.info("Request to display login page");
        return "login";
    }

    @Operation(summary = "Logout user",
            description = "Invalidates the current session, clears the security context, and redirects the user to the home page.")
    @ApiResponse(responseCode = "302", description = "Successfully logged out. Redirecting to home page.", content = @Content)
    @GetMapping("/logout")
    public String logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if(authentication != null){
            log.info("User {} is logging out", authentication.getName()); // лог выхода
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }

}
