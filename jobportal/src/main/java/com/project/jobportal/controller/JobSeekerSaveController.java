package com.project.jobportal.controller;

import com.project.jobportal.entity.JobPostActivity;
import com.project.jobportal.entity.JobSeekerProfile;
import com.project.jobportal.entity.JobSeekerSave;
import com.project.jobportal.entity.Users;
import com.project.jobportal.services.JobPostActivityService;
import com.project.jobportal.services.JobSeekerProfileService;
import com.project.jobportal.services.JobSeekerSaveService;
import com.project.jobportal.services.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
public class JobSeekerSaveController {
    private final UsersService usersService;
    private final JobSeekerProfileService jobSeekerProfileService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobSeekerSaveController(UsersService usersService, JobSeekerProfileService jobSeekerProfileService, JobPostActivityService jobPostActivityService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

//    @Transactional
//    @PostMapping("job-details/save/{id}")
//    public String save(@PathVariable("id") int id, JobSeekerSave jobSeekerSave){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof AnonymousAuthenticationToken)){
//            String currentUsername = authentication.getName();
//            Users user = usersService.findByEmail(currentUsername);
//            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
//            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
//            if(seekerProfile.isPresent() && jobPostActivity != null){
//
//                jobSeekerSave.setId(null); // гарантируем INSERT
//
//                jobSeekerSave.setJob(jobPostActivity);
//                jobSeekerSave.setUserId(seekerProfile.get());
//            } else {
//                throw new RuntimeException("User not found");
//            }
//            jobSeekerSaveService.addNew(jobSeekerSave);
//        }
//        return "redirect:/dashboard";
//    }
//
//    @Transactional
//    @GetMapping("saved-jobs/")
//    public String savedJobs(Model model){
//        List<JobPostActivity> jobPost = new ArrayList<>();
//        Object currentUserProfile = usersService.getCurrentUserProfile();
//
//        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);
//        for(JobSeekerSave jobSeekerSave : jobSeekerSaveList){
//            jobPost.add(jobSeekerSave.getJob());
//        }
//        model.addAttribute("jobPost", jobPost);
//        model.addAttribute("user", currentUserProfile);
//        return "saved-jobs";
//    }

    @Operation(summary = "Save job to favorites",
            description = "Links the specified job post to the authenticated Job Seeker's 'saved jobs' list for later viewing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Job saved successfully. Redirecting to dashboard.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found: Either the Job Post or the User Profile does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User must be logged in to save jobs")
    })
    @Transactional
    @PostMapping("job-details/save/{id}")
    public String save(@Parameter(description = "The unique ID of the job post", example = "10")
                       @PathVariable("id") int id,
                       @Parameter(hidden = true) JobSeekerSave jobSeekerSave) {
        log.info("Request to save job ID: {} to favorites", id); // лог запроса

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);

            log.debug("Authenticating user: {}", currentUsername);

            JobSeekerProfile seekerProfile = jobSeekerProfileService.getOne(user.getUserId())
                    .orElseThrow(() -> {
                        log.error("JobSeekerProfile not found for user: {}", user.getEmail());
                        return new RuntimeException("JobSeekerProfile not found for user " + user.getEmail());
                    });

            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
            if (jobPostActivity == null) {
                log.error("JobPostActivity not found with id: {}", id);

                throw new RuntimeException("JobPostActivity not found with id " + id);
            }

            jobSeekerSave.setId(null); // гарантируем INSERT
            jobSeekerSave.setJob(jobPostActivity);
            jobSeekerSave.setUserId(seekerProfile);

            jobSeekerSaveService.addNew(jobSeekerSave);

            log.info("Job successfully saved for user: {} (Job ID: {})", currentUsername, id);
        }

        return "redirect:/dashboard/";
    }

    @Operation(summary = "View saved jobs list",
            description = "Retrieves and displays all job posts that the current Job Seeker has previously saved.")
    @ApiResponse(responseCode = "200", description = "Saved jobs page loaded successfully", content = @Content(mediaType = "text/html"))
    @Transactional
    @GetMapping("saved-jobs/")
    public String savedJobs(@Parameter(hidden = true) Model model) {
        log.info("Request to view saved jobs list");

        JobSeekerProfile currentUserProfile = (JobSeekerProfile) usersService.getCurrentUserProfile();
        log.debug("Current user profile: {}", currentUserProfile.getUserAccountId());

        List<JobPostActivity> jobPost = jobSeekerSaveService
                .getCandidatesJob(currentUserProfile)
                .stream()
                .map(JobSeekerSave::getJob)
                .toList();

        log.info("Retrieved {} saved jobs for profile ID: {}", jobPost.size(), currentUserProfile.getUserAccountId());

        model.addAttribute("jobPost", jobPost);
        model.addAttribute("user", currentUserProfile);

        return "saved-jobs";
    }
}
