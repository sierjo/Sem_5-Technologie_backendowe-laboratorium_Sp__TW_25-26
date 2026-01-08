package com.project.jobportal.services;

import com.project.jobportal.entity.JobPostActivity;
import com.project.jobportal.entity.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

//Сервис для отправки уведомлений
//Инжектируем SimpMessagingTemplate для отправки сообщений через брокер.
@Service
@Slf4j
public class NotificationService {
//    private static final Logger log = LogManager.getLogger(MyService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final UsersService usersService;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate, UsersService usersService) {
        this.messagingTemplate = messagingTemplate;
        this.usersService = usersService;
    }

//     Отправляет личное уведомление конкретному пользователю.
//     @param targetUserEmail Email пользователя, которому отправляется уведомление.
//     @param notificationDTO Объект уведомления.
    public void sendPrivateNotification(String targetUserEmail, NotificationDTO notificationDTO) {
        log.debug("WS: Attempting to send private notification to user: {}", targetUserEmail);
        System.out.println("WS: Attempting to send private notification to user: "+ targetUserEmail);
        // Отправка в приватный канал пользователя: /user/{targetUserEmail}/queue/notifications
        // Spring Security автоматически маппит аутентифицированного пользователя на этот канал.
        messagingTemplate.convertAndSendToUser(
                targetUserEmail,
                "/queue/notifications",// <-- Конечный адрес, на который подписывается клиент
                notificationDTO
        );
        log.info(">>> WS: Sending notification in broker for: " + targetUserEmail);
        System.out.println(">>> WS: Sending notification in broker for: " + targetUserEmail);
    }

    // --- Логика Уведомления Рекрутера ---
    public void notifyRecruiterOfNewApplication(JobPostActivity job, String candidateName) {
        log.info("Starting notification process for recruiter regarding new application on job ID: {}", job.getJobPostId());

        Users recruiter = job.getPostedById();
        //Проверяем владельца вакансии
        if (recruiter == null) {
            log.info("Starting notification process for recruiter regarding new application on job ID: {}", job.getJobPostId());
            System.out.println(">>> WS ERROR: The vacancy has an ID=" + job.getJobPostId() + " there is no author (recruiter is null)!");
            return;
        }
        String recruiterEmail = recruiter.getEmail();

        log.debug("WS: Job ID={} belongs to recruiter: {}", job.getJobPostId(), recruiterEmail);
        System.out.println(">>> WS: Vacancy ID=" + job.getJobPostId() + " belongs: " + recruiterEmail);

        String content = "New apply from " + candidateName + " to the vacancy: " + job.getJobTitle();
        NotificationDTO notification = new NotificationDTO(
                content,
                "NEW_APPLY",
                job.getJobPostId()
        );

        // Отправка уведомления рекрутеру
        sendPrivateNotification(recruiterEmail, notification);
    }
}