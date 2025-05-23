package com.tkachuk.aws.listener;

import com.amazonaws.services.sqs.model.Message;
import com.tkachuk.aws.service.AWSNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AWSQueueListener {

    private static final String YES = "Yes";

    @Value("${aws.app.queue-listener}")
    private String enable;

    private final AWSNotificationService awsNotificationService;

    @Scheduled(fixedRate = 5000)
    public void readBatchFromSQSQueueAndPushToSNSTopic() {
        if (YES.equalsIgnoreCase(enable)) {
            log.info("Start readBatchFromSQSQueueAndPushToSNSTopic()");
            List<Message> newMessages = awsNotificationService.readMessages();

            if (!newMessages.isEmpty()) {
                awsNotificationService.sendMessageToTopic(newMessages);
            }
        } else {
            log.info("Skip readBatchFromSQSQueueAndPushToSNSTopic()");
        }
    }

}
