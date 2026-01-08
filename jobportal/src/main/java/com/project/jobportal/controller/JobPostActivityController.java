package com.project.jobportal.controller;

import com.project.jobportal.dto.RecruiterJobsDto;
import com.project.jobportal.entity.*;
import com.project.jobportal.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@Slf4j
public class JobPostActivityController {
    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    public JobPostActivityController(UsersService usersService,
                                     JobPostActivityService jobPostActivityService,
                                     JobSeekerApplyService jobSeekerApplyService,
                                     JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @Operation(summary = "Dashboard page 'dashboard.html'", description = "Return dashboard page 'dashboard.html'")
    @ApiResponse(responseCode = "200", description = "Page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/dashboard/")
    public String searchJobs( @Parameter(hidden = true) Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) boolean today,
                             @RequestParam(value = "days7", required = false) boolean days7,
                             @RequestParam(value = "days30", required = false) boolean days30) {

        log.info("Searching jobs with criteria: job={}, location={}, today={}, days7={}, days30={}", job, location, today, days7, days30);

        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Freelance"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }
        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if (officeOnly == null && remoteOnly == null && partialRemote == null){
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }
        if(!dateSearchFlag && !remote && !type && !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
            log.debug("No filters applied, fetching all jobs. Total: {}", jobPost.size());
        } else {
            jobPost = jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
            log.debug("Filtered search returned {} jobs", jobPost.size());
        }

        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            model.addAttribute("username", currentUserName);
            log.debug("Authenticated user: {}, Role: {}", currentUserName, authentication.getAuthorities());

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(((RecruiterProfile) currentUserProfile).getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
                log.info("Loaded {} jobs for recruiter {}", recruiterJobs.size(), currentUserName);
            } else {
                List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidatesJobs((JobSeekerProfile) currentUserProfile);
                List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

                boolean exist;
                boolean saved;
                for(JobPostActivity jobActivity : jobPost){
                    exist = false;
                    saved = false;
                    for(JobSeekerApply jobSeekerApply : jobSeekerApplyList){
                        if(Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())){
                            jobActivity.setIsActive(true);
                            exist = true;
                            break;
                        }
                    }
                    for(JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId())) {
                            jobActivity.setIsSaved(true);
                            saved = true;
                            break;
                        }
                    }
                    if(!exist){
                        jobActivity.setIsActive(false);
                    }
                    if (!saved){
                        jobActivity.setIsSaved(false);
                    }
                    model.addAttribute("jobPost", jobPost);
                }
                log.info("Dashboard processed for JobSeeker: {}", currentUserName);
            }
        }
        model.addAttribute("user", currentUserProfile);
        return "dashboard";
    }

    @Operation(summary = "Global-search page 'global-search.html'", description = "Return global-search page 'global-search.html'")
    @ApiResponse(responseCode = "200", description = "Page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("global-search/")
    public String globalSearch(Model model,
                               @RequestParam(value = "job", required = false) String job,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "partTime", required = false) String partTime,
                               @RequestParam(value = "fullTime", required = false) String fullTime,
                               @RequestParam(value = "freelance", required = false) String freelance,
                               @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required = false) String officeOnly,
                               @RequestParam(value = "partialRemote", required = false) String partialRemote,
                               @RequestParam(value = "today", required = false) boolean today,
                               @RequestParam(value = "days7", required = false) boolean days7,
                               @RequestParam(value = "days30", required = false) boolean days30){

        log.info("Global search initiated for job: '{}' in location: '{}'", job, location);

        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Freelance"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }
        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if (officeOnly == null && remoteOnly == null && partialRemote == null){
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }
        if(!dateSearchFlag && !remote && !type && !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            jobPost = jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
        }

        log.debug("Global search found {} results", jobPost != null ? jobPost.size() : 0);
        model.addAttribute("jobPost", jobPost);
        return "global-search";
    }

    @Operation(summary = "Add-jobs page 'add-jobs.html'", description = "Return add-jobs page 'add-jobs.html'")
    @ApiResponse(responseCode = "200", description = "Page loaded successfully", content = @Content(mediaType = "text/html"))
    @GetMapping("/dashboard/add")
    public String addJob( @Parameter(hidden = true) Model model) {
        log.info("Opening 'Add Job' page");
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @Operation(summary = "Creating a new job",
               description = "Accepts data from the form, assigns the current user as the author and redirects to the control panel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Successful saving. Redirection to /dashboard/", content = @Content),
            @ApiResponse(responseCode = "400", description = "Input validation error")})
    @PostMapping("/dashboard/addNew")
    public String addNew(@Parameter(hidden = true) JobPostActivity jobPostActivity,
                         @Parameter(hidden = true) Model model) {
        Users users = usersService.getCurrentUser();
        if (users != null) {
            jobPostActivity.setPostedById(users);

            log.info("Adding new job post by user: {}", users.getEmail());
        }
        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("jobPostActivity", jobPostActivity);
        JobPostActivity saved = jobPostActivityService.addNew(jobPostActivity);

        log.info("Job post successfully saved with ID: {}", saved.getJobPostId());
        return "redirect:/dashboard/";
    }

    @Operation(summary = "Going to edit a vacancy",
            description = "Extracts the data of an existing job by ID and displays the edit page 'add-jobs.html '")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Successful saving. Redirection to /dashboard/", content = @Content),
            @ApiResponse(responseCode = "400", description = "Input validation error")})
    @PostMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {
        log.info("Editing job post with ID: {}", id);

        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @Operation(summary = "Export recruiter jobs to Excel",
            description = "Generates and downloads an XLSX file containing all job postings for the currently authenticated recruiter. Access is restricted to users with the 'Recruiter' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file generated successfully", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "403", description = "Access Denied: User does not have the 'Recruiter' role", content = @Content)
    })
    @GetMapping("/dashboard/download-excel")
    public ResponseEntity<InputStreamResource> downloadExcel() {
        // role verification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))){

            log.warn("Unauthorized access attempt to download Excel. User: {}", (authentication != null) ? authentication.getName() : "Anonymous");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Users user = usersService.getCurrentUser();
        log.info("Generating Excel report for recruiter: {}", user.getEmail());

        List<RecruiterJobsDto> jobs = jobPostActivityService.getRecruiterJobs(user.getUserId());
        ByteArrayInputStream in = excelExportService.exportRecruiterJobs(jobs);

        // generating an HTTP response with a file
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=job_posts_report.xlsx");

        log.info("Excel report generated successfully for user: {}", user.getEmail());

        return ResponseEntity
                .ok() // HTTP status 200
                .headers(headers) // attaches instructions
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) // официальный "паспорт" файлов .xlsx
                .body(new InputStreamResource(in));
    }
}
