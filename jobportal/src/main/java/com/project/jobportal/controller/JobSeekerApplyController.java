package com.project.jobportal.controller;

import com.project.jobportal.entity.*;
import com.project.jobportal.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class JobSeekerApplyController {
    private final JobPostActivityService jobPostActivityService;
    private final UsersService usersService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;
    private final RecruiterProfileService recruiterProfileService;
    private final JobSeekerProfileService jobSeekerProfileService;

    private final NotificationService notificationService;

    @Autowired
    public JobSeekerApplyController(JobPostActivityService jobPostActivityService,
                                    UsersService usersService,
                                    JobSeekerApplyService jobSeekerApplyService,
                                    JobSeekerSaveService jobSeekerSaveService,
                                    RecruiterProfileService recruiterProfileService,
                                    JobSeekerProfileService jobSeekerProfileService,
                                    NotificationService notificationService) {
        this.jobPostActivityService = jobPostActivityService;
        this.usersService = usersService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
        this.recruiterProfileService = recruiterProfileService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Display job details page",
            description = "Retrieves detailed information about a specific job post. " +
                    "If a Recruiter is logged in, it shows the list of applicants. " +
                    "If a Job Seeker is logged in, it checks if they have already applied for or saved this job."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job details page loaded successfully",
                    content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "404", description = "Job post not found")
    })
    @GetMapping("/job-details-apply/{id}")
    public String display(@PathVariable("id") int id, Model model) {
        log.info("Displaying job details for job ID: {}", id);

        JobPostActivity jobDetails = jobPostActivityService.getOne(id);
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getJobCandidates(jobDetails);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getJobCandidates(jobDetails);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            log.debug("Authenticated user {} accessing job details {}", currentUsername, id);

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                RecruiterProfile user = recruiterProfileService.getCurrentRecruiterProfile();
                if (user != null) {
                    model.addAttribute("applyList", jobSeekerApplyList);

                    log.info("Recruiter {} is viewing applicants for job ID: {}", currentUsername, id);
                }
            } else {
                JobSeekerProfile user = jobSeekerProfileService.getCurrentSeekerProfile();
                if (user != null) {
                    boolean exists = false;
                    boolean saved = false;
                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                        if (jobSeekerApply.getUserId().getUserAccountId() == user.getUserAccountId()) {
                            exists = true;
                            break;
                        }
                    }
                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (jobSeekerSave.getUserId().getUserAccountId() == user.getUserAccountId()) {
                            saved = true;
                            break;
                        }
                    }
                    model.addAttribute("alreadyApplied", exists);
                    model.addAttribute("alreadySaved", saved);

                    log.debug("JobSeeker {} status for job {}: applied={}, saved={}", currentUsername, id, exists, saved);
                }
            }
        }
        JobSeekerApply jobSeekerApply = new JobSeekerApply();
        model.addAttribute("applyJob", jobSeekerApply);

        model.addAttribute("jobDetails", jobDetails);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "job-details";
    }

    //    @PostMapping("job-details/apply/{id}")
//    public String apply(@PathVariable("id") int id, JobSeekerApply jobSeekerApply){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(!(authentication instanceof AnonymousAuthenticationToken)){
//            String currentUsername = authentication.getName();
//            Users user = usersService.findByEmail(currentUsername);
//            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
//            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
//            if(seekerProfile.isPresent() && jobPostActivity != null){
//                jobSeekerApply.setUserId(seekerProfile.get());
//                jobSeekerApply.setJob(jobPostActivity);
//                jobSeekerApply.setApplyDate(new Date());
//                jobSeekerApply.setId(null);
//            } else {
//                throw new RuntimeException("User not found");
//            }
//            jobSeekerApplyService.addNew(jobSeekerApply);
//        }
//        return "redirect:/dashboard/";
//    }

    @Operation(summary = "Apply for a job",
            description = "Submits a job application for the authenticated Job Seeker. It links the user's profile to the specific job post, records the application date, and sends a notification to the responsible recruiter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302",
                    description = "Application submitted successfully. Redirecting to the dashboard.",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized: User must be logged in to apply",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Not Found: Job post or Seeker profile does not exist",
                    content = @Content)
    })
    @PostMapping("job-details/apply/{id}")
    public String apply(@PathVariable("id") int id, JobSeekerApply jobSeekerApply) {
        log.info("Job application attempt for Job ID: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

            if (seekerProfile.isPresent() && jobPostActivity != null) {
                // существующая логика сохранения JobSeekerApply
                jobSeekerApply.setUserId(seekerProfile.get());
                jobSeekerApply.setJob(jobPostActivity);
                jobSeekerApply.setApplyDate(new Date());
                jobSeekerApply.setId(null);
                jobSeekerApplyService.addNew(jobSeekerApply); //сохранение

                log.info("Application successfully saved for user: {} on job ID: {}", currentUsername, id);

                // уведомление рекрутера ---
                String candidateName = seekerProfile.get().getFirstName() + " " + seekerProfile.get().getLastName();
                notificationService.notifyRecruiterOfNewApplication(jobPostActivity, candidateName);
                log.debug("Notification sent to recruiter for job ID: {} from candidate: {}", id, candidateName);
                // ----------------------------------------

            } else {
                log.error("Failed to process application: User profile or Job post not found. User ID: {}, Job ID: {}", user.getUserId(), id);
                throw new RuntimeException("User not found");
            }
        }
        return "redirect:/dashboard/";
    }
}
