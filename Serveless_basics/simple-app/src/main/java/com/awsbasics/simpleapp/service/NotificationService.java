package com.awsbasics.simpleapp.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.awsbasics.simpleapp.configuration.SNSClientProperties;
import com.awsbasics.simpleapp.configuration.SQSClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String SNS_PROTOCOL = "email";

    private final SNSClientProperties snsClientProperties;
    private final SQSClientProperties sqsClientProperties;
    private final AmazonSNS snsClient;
    private final AmazonSQS sqsClient;

    public ResponseEntity<?> subscribeEmail(String email) {
        try {
            var request = new SubscribeRequest()
                    .withProtocol(SNS_PROTOCOL)
                    .withEndpoint(email)
                    .withTopicArn(snsClientProperties.getTopicArn());
            snsClient.subscribe(request);
            return new ResponseEntity<>(OK);
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public ResponseEntity<?> unsubscribeEmail(String email) {
        try {
            var listResult = snsClient.listSubscriptionsByTopic(snsClientProperties.getTopicArn());
            var subscriptions = listResult.getSubscriptions();
            subscriptions.stream()
                    .filter(subscription -> email.equals(subscription.getEndpoint()))
                    .findAny()
                    .ifPresent(subscription -> unsubscribe(subscription.getSubscriptionArn()));
            return new ResponseEntity<>(NO_CONTENT);
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public void sendMessageToQueue(String message) {
        try {
            var request = new SendMessageRequest()
                    .withQueueUrl(sqsClientProperties.getQueueUrl())
                    .withMessageBody(message)
                    .withDelaySeconds(5);
            sqsClient.sendMessage(request);
        } catch (AmazonSQSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public void sendMessageToTopic(String message) {
        try {
            var publishRequest = new PublishRequest()
                    .withMessage(message)
                    .withTopicArn(snsClientProperties.getTopicArn());
            snsClient.publish(publishRequest);
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

     public List<Message> readMessages() {
        try {
            var queueUrl = sqsClientProperties.getQueueUrl();
            var request = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withWaitTimeSeconds(10)
                    .withMaxNumberOfMessages(10);
            var messages = sqsClient.receiveMessage(request).getMessages();
            messages.stream()
                    .map(Message::getReceiptHandle)
                    .forEach(receipt -> sqsClient.deleteMessage(queueUrl, receipt));
            return messages;
        } catch (AmazonSQSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private void unsubscribe(String subscriptionArn) {
        try {
            var unsubscribeRequest = new UnsubscribeRequest()
                    .withSubscriptionArn(subscriptionArn);
            snsClient.unsubscribe(unsubscribeRequest);
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
