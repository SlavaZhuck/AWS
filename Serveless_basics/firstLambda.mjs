import {
    SNSClient,
    PublishCommand
} from "@aws-sdk/client-sns";

const TOPIC_ARN_NAME = "arn:aws:sns:us-east-1:537124968674:Slava-UploadsNotificationTopic";
const REGION = "us-east-1";

const sns = new SNSClient({
    region: REGION
});

export const handler = async (event, context) => {
    if(!event) {
        return {
            'statusCode': 200,
            'body': JSON.stringify('No messages to process. Lambda function completed')
        };
    }

    const processed = await processRecords(event.Records);

    console.log(`SNS TOPIC ARN = ${TOPIC_ARN_NAME};
                Function Name = ${context.functionName};
                Processed Messages count = ${processed};
                Remaining Time in millis = ${context.getRemainingTimeMillis()}
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

    for (const record of records) {
        if (!record.body) {
            throw new Error('no body in SQS record.');
        }
        // Publish the message to the SNS topic
        await sns.send(new PublishCommand({
            TopicArn: TOPIC_ARN_NAME,
            Subject: "Processed SQS Queue Messages",
            Message: record.body
        }));
        
        console.log('Message ${record.body} processed successfully.');
    }
    
    return records.length;
}