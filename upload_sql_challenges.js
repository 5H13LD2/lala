/**
 * Firebase SQL Challenges Upload Script
 *
 * This script uploads SQL challenge data to Firestore.
 *
 * Prerequisites:
 * 1. Install Node.js
 * 2. Run: npm install firebase-admin
 * 3. Download your Firebase service account key JSON from Firebase Console
 * 4. Update the path to your service account key below
 * 5. Run: node upload_sql_challenges.js
 */

const admin = require('firebase-admin');
const fs = require('fs');

// TODO: Update this path to your Firebase service account key
const serviceAccount = require('./path/to/your-service-account-key.json');

// Initialize Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Read the SQL challenges JSON file
const challengesData = JSON.parse(fs.readFileSync('./sql_challenges.json', 'utf8'));

async function uploadChallenges() {
  console.log('🚀 Starting SQL Challenges Upload...\n');

  const batch = db.batch();
  let uploadCount = 0;

  for (const challenge of challengesData.challenges) {
    const { id, ...challengeData } = challenge;

    const docRef = db.collection('sql_challenges').doc(id);
    batch.set(docRef, challengeData);
    uploadCount++;

    console.log(`✅ Queued: ${id} - ${challengeData.title}`);
  }

  try {
    await batch.commit();
    console.log(`\n✅ Successfully uploaded ${uploadCount} SQL challenges to Firestore!`);
    console.log('🎉 Upload complete!\n');

    // Verify upload
    const snapshot = await db.collection('sql_challenges').get();
    console.log(`📊 Total challenges in Firestore: ${snapshot.size}`);

  } catch (error) {
    console.error('❌ Error uploading challenges:', error);
  }

  process.exit(0);
}

// Run the upload
uploadChallenges();
