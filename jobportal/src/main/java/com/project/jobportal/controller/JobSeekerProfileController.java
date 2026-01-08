package com.project.jobportal.controller;

import com.project.jobportal.entity.JobSeekerProfile;
import com.project.jobportal.entity.Skills;
import com.project.jobportal.entity.Users;
import com.project.jobportal.repository.UsersRepository;
import com.project.jobportal.services.JobSeekerProfileService;
import com.project.jobportal.util.FileDownloadUtil;
import com.project.jobportal.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
@Slf4j
public class JobSeekerProfileController {
    private JobSeekerProfileService jobSeekerProfileService;
    private UsersRepository usersRepository;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }

    @Operation(summary = "Display job seeker profile page",
            description = "Retrieves the current user's profile and skills. If no skills exist, initializes an empty list for the form.")
    @ApiResponse(responseCode = "200", description = "Profile page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/")
    public String JobSeekerProfile(Model model) {
        log.info("Accessing job seeker profile page");

        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(()
                    -> new UsernameNotFoundException("User not found"));

            String currentUserName = authentication.getName();
            log.debug("Loading profile for user: {}", currentUserName);

            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();
                if (jobSeekerProfile.getSkills().isEmpty()) {
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);
                }
                log.debug("Profile found for user: {}, skills count: {}", currentUserName, jobSeekerProfile.getSkills().size());
            } else {
                log.warn("No profile record found in DB for user: {}", currentUserName);
            }
            model.addAttribute("skills", skills);
            model.addAttribute("profile", jobSeekerProfile);
        }
        return "job-seeker-profile";
    }

    @Operation(summary = "Update job seeker profile",
            description = "Saves profile details including skills and uploads profile photo and resume PDF to the server."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Profile updated successfully. Redirecting to dashboard."),
            @ApiResponse(responseCode = "500", description = "Error occurred while saving files")
    })
    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(()
                    -> new UsernameNotFoundException("User not found"));
            jobSeekerProfile.setUsersId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());

            log.info("Updating profile for user: {}", user.getEmail());
        }
        List<Skills> skillsList = new ArrayList<>();
        model.addAttribute("profile", jobSeekerProfile);
        model.addAttribute("skills", skillsList);

        for (Skills skills : jobSeekerProfile.getSkills()) {
            skills.setJobSeekerProfile(jobSeekerProfile);
        }

        String imageName = "";
        String resumeName = "";

        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);

            log.debug("New profile image detected: {}", imageName);
        }
        if (!Objects.equals(pdf.getOriginalFilename(), "")) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);

            log.debug("New resume file detected: {}", resumeName);
        }
        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);
        log.info("Profile data saved to database for user ID: {}", jobSeekerProfile.getUserAccountId());

        try {
            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, imageName, image);

                log.info("Image file uploaded successfully to: {}", uploadDir);
            }
            if (!Objects.equals(pdf.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);

                log.info("Resume file uploaded successfully to: {}", uploadDir);
            }
        } catch (Exception e) {
            log.error("Error occurred while saving files for user {}: {}", jobSeekerProfile.getUserAccountId(), e.getMessage());
            throw new RuntimeException(e);
        }

        return "redirect:/dashboard/";
    }

    @Operation(summary = "View specific candidate profile",
            description = "Retrieves a public or recruiter-facing view of a candidate's profile by their ID.")
    @ApiResponse(responseCode = "200", description = "Candidate profile found and displayed", content = @Content(mediaType = "text/html"))
    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        log.info("Viewing candidate profile with ID: {}", id);

        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        model.addAttribute("profile", seekerProfile.get());
        return "job-seeker-profile";
    }

    @Operation(summary = "Download candidate resume",
            description = "Downloads the stored PDF resume for a specific user from the server storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume file retrieved successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName") String fileName,
                                            @RequestParam(value = "userID") String userId) {
        log.info("Resume download request: file={}, userId={}", fileName, userId);

        FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
        Resource resource = null;
        try {
            resource = fileDownloadUtil.getFileAsResourse("photos/candidate/" + userId, fileName);
        } catch (IOException e) {
            log.error("IO Exception during resume download for user {}: {}", userId, e.getMessage());

            return ResponseEntity.badRequest().build();
        }
        if (resource == null) {
            log.warn("Resume file not found: photos/candidate/{}/{}", userId, fileName);

            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
        log.info("File {} successfully found and starting download", fileName);

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}
