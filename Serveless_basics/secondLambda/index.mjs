import {
    S3Client,
    HeadObjectCommand
} from "@aws-sdk/client-s3";

import mysql from 'mysql';

const REGION = "us-east-1";
const BUCKET = "demobucket-test-for-rds";

const {
    DB_HOST,
    DB_USER,
    DB_PASSWORD,
    DB_NAME,
    TABLE_NAME,
} = process.env;

const s3 = new S3Client({
    region: REGION
});

export const handler = async (event, context) => {
    console.log("Event received: ", event);

    let inconsistencies = [];

    // Wrap the entire logic in a Promise
    const res = await new Promise((resolve, reject) => {
        console.log("Step 1: Creating MySQL connection...");
        const dbConnection = mysql.createConnection({
            host: DB_HOST,
            user: DB_USER,
            password: DB_PASSWORD,
            database: DB_NAME
        });

        console.log("Step 2: Connecting to the database...");
        dbConnection.connect((err) => {
            if (err) {
                console.error("Error connecting to the database:", err);
                return reject(err);
            }
            console.log("Successfully connected to the database.");
        });

        console.log(`Step 3: Querying the database for table: ${TABLE_NAME}...`);
        dbConnection.query("SELECT * FROM " + TABLE_NAME, async function (err, rows, fields) {
            if (err) {
                console.error("Error occurred while querying the database:", err);
                dbConnection.end(); // Close the connection
                return reject(err);
            }

            console.log(`Step 4: Fetched ${rows.length} rows from the database.`);
            for (let i = 0; i < rows.length; i++) {
                console.log(`Step 5: Checking S3 object for key: ${rows[i].name}...`);
                try {
                    const data = await s3.send(new HeadObjectCommand({
                        Bucket: BUCKET,
                        Key: rows[i].name
                    }));

                    console.log(`Step 6: S3 object found for key: ${rows[i].name}. Validating size...`);
                    if (data.ContentLength != rows[i].image_size) {
                        console.warn(`Inconsistency found: Size mismatch for object ${rows[i].name}.`);
						console.log(`S3 object size: ${data.ContentLength}, Database size: ${rows[i].image_size}`);
                        inconsistencies.push({
                            'object': rows[i].name,
                            'bucket': BUCKET,
                            'Error': 'Image sizes don\'t match'
                        });
                    }
                } catch (headErr) {
                    if (headErr.name === 'NotFound') {
                        console.warn(`Inconsistency found: Object not found for key ${rows[i].name}.`);
                        inconsistencies.push({
                            'object': rows[i].name,
                            'bucket': BUCKET,
                            'Error': 'Object not found'
                        });
                    } else {
                        console.error("Error occurred while querying S3:", headErr);
                        dbConnection.end(); // Close the connection
                        return reject(headErr);
                    }
                }
            }

            console.log("Step 7: Closing the database connection...");
            dbConnection.end();

            console.log("Step 8: Resolving the response...");
            resolve({
                isBase64Encoded: false,
                headers: {},
                statusCode: inconsistencies.length > 0 ? 400 : 200,
                body: JSON.stringify(inconsistencies.length > 0 ? inconsistencies : "No inconsistencies found")
            });
        });
    });

    // Log the resolved value
    console.log("Resolved to:", res);

    // Return the resolved value
    return res;
};