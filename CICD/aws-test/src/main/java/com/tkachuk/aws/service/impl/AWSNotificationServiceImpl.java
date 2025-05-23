package com.tkachuk.aws.service.impl;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.tkachuk.aws.configuration.SNSClientProperties;
import com.tkachuk.aws.configuration.SQSClientProperties;
import com.tkachuk.aws.controller.AWSImageController;
import com.tkachuk.aws.dto.ImageInfoDTO;
import com.tkachuk.aws.service.AWSNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AWSNotificationServiceImpl implements AWSNotificationService {

    private static final String EMAIL_PROTOCOL = "email";

    private final AmazonSNS amazonSNS;
    private final AmazonSQS amazonSQS;

    private final SNSClientProperties snsClientProperties;
    private final SQSClientProperties sqsClientProperties;

    @Override
    public ResponseEntity<?> subscribe(String email) {
        try {
            var subscribeRequest = new SubscribeRequest();
            subscribeRequest.withProtocol(EMAIL_PROTOCOL);
            subscribeRequest.withEndpoint(email);
            subscribeRequest.withTopicArn(snsClientProperties.getTopicArn());

            amazonSNS.subscribe(subscribeRequest);
            return new ResponseEntity<>(format("Subscribed -> %s", email), OK);
        } catch (Exception ex) {
            log.error("Error while subscribing", ex);
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> unsubscribe(String email) {
        if (email.isBlank()) {
            return new ResponseEntity<>("Email is blank!", BAD_GATEWAY);
        }

        try {
            ListSubscriptionsByTopicResult listSubscriptionsByTopicResult =
                amazonSNS.listSubscriptionsByTopic(snsClientProperties.getTopicArn());

            ofNullable(listSubscriptionsByTopicResult)
                .map(ListSubscriptionsByTopicResult::getSubscriptions)
                .orElse(emptyList())
                .stream()
                .filter(subscription -> email.equalsIgnoreCase(subscription.getEndpoint()))
                .findFirst()
                .ifPresent((subscription) -> amazonSNS.unsubscribe(subscription.getSubscriptionArn()));

            return new ResponseEntity<>(format("Unsubscribed -> %s", email), OK);
        } catch (Exception ex) {
            log.error("Error while unsubscribing", ex);
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void sendMessageToSQS(ImageInfoDTO imageInfoDTO) {
        var downloadLink = linkTo(methodOn(AWSImageController.class).getImage(imageInfoDTO.getName()));
        String body = format("The image has been uploaded, %s, %s", imageInfoDTO, downloadLink);

        try {
            var sendMessageRequest = new SendMessageRequest()
                .withMessageBody(body)
                .withDelaySeconds(5)
                .withQueueUrl(sqsClientProperties.getQueueUrl());

            log.debug("Send message: {}", body);
            amazonSQS.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            log.error("Error while sending message to SQS", ex);
        }

    }

    @Override
    public List<Message> readMessages() {
        try {
            var receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(sqsClientProperties.getQueueUrl())
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(10);

            ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest);
            List<Message> messages = receiveMessageResult.getMessages();
            log.debug("newMessage -> {}", messages);

            messages.stream()
                .map(Message::getReceiptHandle)
                .forEach(receipt -> amazonSQS.deleteMessage(sqsClientProperties.getQueueUrl(), receipt));

            return messages;
        } catch (Exception ex) {
            log.error("Error while reading messages from SQS", ex);
            return emptyList();
        }

    }

    public void sendMessageToTopic(List<Message> messages) {
        try {
            List<PublishRequest> publishRequests = messages.stream()
                .map(message -> new PublishRequest()
                    .withMessage(message.getBody())
                    .withTopicArn(snsClientProperties.getTopicArn())
                )
                .toList();

            log.debug("Push messages to SNS Topic -> {}", messages);
            publishRequests.forEach(amazonSNS::publish);
        } catch (Exception ex) {
            log.error("Error while publishing to SNS", ex);
        }
    }
}
