import {
    SNSClient,
    PublishCommand
} from "@aws-sdk/client-sns";

import {
    SQSClient,
    DeleteMessageCommand
} from "@aws-sdk/client-sqs";

const TOPIC_ARN_NAME = process.env.SNS_TOPIC_ARN;
const QUEUE_URL = process.env.QUEUE_URL; // Use the queue URL instead of the ARN
const REGION = "us-east-1";

const sns = new SNSClient({ region: REGION });
const sqs = new SQSClient({ region: REGION });

export const handler = async (event, context) => {
    if (!event || !event.Records || event.Records.length === 0) {
        return {
            statusCode: 200,
            body: JSON.stringify('No messages to process. Lambda function completed')
        };
    }

    const processed = await processRecords(event.Records);

    console.log(`SNS TOPIC ARN = ${TOPIC_ARN_NAME};
                Function Name = ${context.functionName};
                Processed Messages count = ${processed};
                `);

    return {
        statusCode: 200,
        body: JSON.stringify('Lambda function completed')
    };
};

const processRecords = async (records) => {
    if (!records) {
        console.log("No records to process.");
        return 0;
    }

    let processedCount = 0;

    for (const record of records) {
        try {
            if (!record.body) {
                throw new Error('No body in SQS record.');
            }

            // Publish the message to the SNS topic
            await sns.send(new PublishCommand({
                TopicArn: TOPIC_ARN_NAME,
                Subject: "Processed SQS Queue Messages",
                Message: record.body
            }));

            console.log(`Message ${record.body} processed successfully.`);

            // Delete the message from the SQS queue
            await sqs.send(new DeleteMessageCommand({
                QueueUrl: QUEUE_URL, // Use the queue URL instead of the ARN
                ReceiptHandle: record.receiptHandle
            }));

            console.log(`Message ${record.body} deleted successfully from SQS.`);
            processedCount++;
        } catch (error) {
            console.error(`Failed to process message: ${record.body}. Error: ${error.message}`);
        }
    }

    return processedCount;
};