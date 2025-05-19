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
const dbConnection = mysql.createConnection({
    host: DB_HOST,
    user: DB_USER,
    password: DB_PASSWORD,
    database: DB_NAME // Use the environment variable
});

export const handler = async (event, context) => {
    console.log("Event: ", event);
    
    let inconsistencies = [];

    // Wrap the existing logic in a Promise and await its resolution
    const res = await new Promise((resolve, reject) => {
        dbConnection.query("SELECT * FROM " + TABLE_NAME, async function(err, rows, fields) {
            if (err) {
                console.log('Error occured', err);
                return reject(err);
            }
                 
            for (let i = 0; i < rows.length; i++) {
                try {
                    const data = await s3.send(new HeadObjectCommand({
                        Bucket: BUCKET,
                        Key: rows[i].name
                    }));
                    
                    if (data.ContentLength != rows[i].size) {
                        inconsistencies.push({
                            'object': rows[i].name,
                            'bucket': BUCKET,
                            'Error': 'Image sizes dont match'
                        });
                    }
                } catch (headErr) {
                    if (headErr.name === 'NotFound') {
                        console.log(`Inconsistency found for object: ${rows[i].name} in bucket: ${BUCKET}`);
                        
                        inconsistencies.push({
                            'object': rows[i].name,
                            'bucket': BUCKET,
                            'Error': 'Object not found'
                        });
                    } else {
                        console.log('Error occured', headErr);              
                        return reject(headErr);
                    }
                }
            }
            

            resolve({
                isBase64Encoded:false,
                headers: { },
                statusCode: inconsistencies.length > 0 ? 400 : 200,
                body: JSON.stringify(inconsistencies.length > 0 ? inconsistencies : "No inconsistencies found")
            });
        });
    });

    // Log the resolved value
    console.log('Resolved to', res);

    // Return the resolved value
    return res;
};