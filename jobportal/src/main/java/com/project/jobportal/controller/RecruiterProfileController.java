package com.project.jobportal.controller;

import com.project.jobportal.entity.RecruiterProfile;
import com.project.jobportal.entity.Users;
import com.project.jobportal.repository.UsersRepository;
import com.project.jobportal.services.RecruiterProfileService;
import com.project.jobportal.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/recruiter-profile")
@Slf4j
public class RecruiterProfileController {
    private final UsersRepository usersRepository;
    private final RecruiterProfileService recruiterProfileService;

    public RecruiterProfileController(UsersRepository usersRepository, RecruiterProfileService recruiterProfileService) {
        this.usersRepository = usersRepository;
        this.recruiterProfileService = recruiterProfileService;
    }

    @Operation(summary = "Display recruiter profile page",
            description = "Retrieves the profile data for the currently authenticated recruiter and prepares the 'recruiter_profile' view.")
    @ApiResponse(responseCode = "200", description = "Recruiter profile page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/")
    public String recruiterProfile(@Parameter(hidden = true) Model model) {
        log.info("Request to access recruiter profile page"); // лог входа

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            log.debug("Fetching profile for recruiter: {}", currentUsername);

            Users users = usersRepository.findByEmail(currentUsername).orElseThrow(() -> {
                log.error("User with email {} not found", currentUsername);
                return new UsernameNotFoundException("Couldn't found user");
            });
            Optional<RecruiterProfile> recruiterProfile = recruiterProfileService.getOne(users.getUserId());
            if (!recruiterProfile.isEmpty()) {
                log.info("Profile data found for recruiter: {}", currentUsername);
                model.addAttribute("profile", recruiterProfile.get());
            } else {
                log.warn("No recruiter profile record found for user: {}", currentUsername);
            }
        }
        return "recruiter_profile";
    }

    @Operation(summary = "Update recruiter profile info",
            description = "Saves recruiter profile details and uploads a profile photo. Links the profile to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Profile successfully updated. Redirecting to dashboard.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Failed to save the profile photo")
    })
    @PostMapping(value = "/addNew", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addNew(RecruiterProfile recruiterProfile,
                         @Parameter(description = "Recruiter's profile photo (image file)")
                         @RequestParam("image") MultipartFile multipartFile,
                         @Parameter(hidden = true) Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            log.info("Starting profile update for recruiter: {}", currentUsername);

            Users users = usersRepository.findByEmail(currentUsername).orElseThrow(() -> {
                log.error("Profile update failed: User {} not found", currentUsername);
                return new UsernameNotFoundException("Couldn't found user");
            });

            recruiterProfile.setUsersId(users);
            recruiterProfile.setUserAccountId(users.getUserId());
        }
        model.addAttribute("profile", recruiterProfile);
        String fileName = "";
        if (!multipartFile.getOriginalFilename().equals("")) {
            fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            recruiterProfile.setProfilePhoto(fileName);

            log.debug("Profile photo update detected: {}", fileName);

        }
        RecruiterProfile savedUser = recruiterProfileService.addNew(recruiterProfile);
        log.info("Recruiter profile saved to DB for account ID: {}", savedUser.getUserAccountId());

        String uploadDir = "photos/recruiter/" + savedUser.getUserAccountId();
        try {
            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

            log.info("Profile photo successfully saved to: {}", uploadDir);

        } catch (Exception ex) {

            log.error("Failed to save profile photo for user {}: {}", savedUser.getUserAccountId(), ex.getMessage());

            ex.printStackTrace();
        }
        return "redirect:/dashboard/";
    }
}
