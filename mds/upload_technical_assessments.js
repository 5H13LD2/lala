/**
 * Firebase Technical Assessment Upload Script
 *
 * This script uploads technical assessment challenges to Firestore.
 *
 * Prerequisites:
 * 1. Install Node.js
 * 2. Run: npm install firebase-admin
 * 3. Download your Firebase service account key JSON from Firebase Console
 * 4. Update the path to your service account key below
 * 5. Run: node upload_technical_assessments.js
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

// Read the technical assessments JSON file
const assessmentData = JSON.parse(fs.readFileSync('./technical_assessments.json', 'utf8'));

async function uploadAssessments() {
  console.log('🚀 Starting Technical Assessment Upload...\n');

  const batch = db.batch();
  let uploadCount = 0;

  // Group challenges by difficulty for summary
  const byDifficulty = { Easy: 0, Medium: 0, Hard: 0 };
  const byLanguage = {};

  for (const challenge of assessmentData.challenges) {
    const { id, ...challengeData } = challenge;

    // Add timestamp if not present
    if (!challengeData.createdAt) {
      challengeData.createdAt = admin.firestore.Timestamp.now();
    }

    const docRef = db.collection('technical_assesment').doc(id);
    batch.set(docRef, challengeData);
    uploadCount++;

    // Count by difficulty
    if (byDifficulty[challengeData.difficulty] !== undefined) {
      byDifficulty[challengeData.difficulty]++;
    }

    // Count by language
    const lang = challengeData.compilerType || 'unknown';
    byLanguage[lang] = (byLanguage[lang] || 0) + 1;

    console.log(`✅ Queued: ${id}`);
    console.log(`   Title: ${challengeData.title}`);
    console.log(`   Language: ${challengeData.compilerType} | Difficulty: ${challengeData.difficulty}`);
    console.log('');
  }

  try {
    await batch.commit();
    console.log(`✅ Successfully uploaded ${uploadCount} technical assessments to Firestore!\n`);

    // Show statistics
    console.log('📊 Upload Statistics:');
    console.log('═══════════════════════════════════════');
    console.log(`Total Challenges: ${uploadCount}`);
    console.log('');
    console.log('By Difficulty:');
    Object.entries(byDifficulty).forEach(([difficulty, count]) => {
      console.log(`  ${difficulty}: ${count}`);
    });
    console.log('');
    console.log('By Programming Language:');
    Object.entries(byLanguage).forEach(([lang, count]) => {
      console.log(`  ${lang}: ${count}`);
    });
    console.log('═══════════════════════════════════════\n');

    // Verify upload
    const snapshot = await db.collection('technical_assesment').get();
    console.log(`✅ Total challenges in Firestore: ${snapshot.size}`);
    console.log('🎉 Upload complete!\n');

  } catch (error) {
    console.error('❌ Error uploading assessments:', error);
  }

  process.exit(0);
}

// Run the upload
uploadAssessments();
