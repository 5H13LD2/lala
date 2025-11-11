package com.labactivity.lala.SQLCOMPILER

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.labactivity.lala.R
import com.labactivity.lala.UTILS.DialogUtils
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge
import com.labactivity.lala.SQLCOMPILER.models.TableData
import com.labactivity.lala.SQLCOMPILER.services.FirestoreSQLHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for executing SQL challenges fetched from Firebase Firestore
 * Integrates with QueryEvaluator to validate user queries
 */
class SQLChallengeActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var queryEvaluator: QueryEvaluator
    private lateinit var queryValidator: QueryValidator
    private val firestoreHelper = FirestoreSQLHelper.getInstance()

    // UI Components
    private lateinit var challengeTitle: TextView
    private lateinit var challengeDescription: TextView
    private lateinit var challengeDifficulty: TextView
    private lateinit var challengeTopic: TextView
    private lateinit var queryEditText: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var runButton: Button
    private lateinit var hintButton: Button
    private lateinit var viewSolutionButton: Button
    private lateinit var resetButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var expectedTableLayout: TableLayout
    private lateinit var resultTableLayout: TableLayout
    private lateinit var resultMessage: TextView
    private lateinit var sampleTablesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    // Data
    private var currentChallenge: SQLChallenge? = null
    private var challengeId: String? = null
    private var currentHintIndex = 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sql_challenge)

        // Get challenge ID from intent
        challengeId = intent.getStringExtra(EXTRA_CHALLENGE_ID)

        if (challengeId == null) {
            DialogUtils.showErrorDialog(this, "Error", "No challenge ID provided")
            finish()
            return
        }

        initializeComponents()
        setupDatabase()
        loadChallenge()
        setupListeners()
    }

    private fun initializeComponents() {
        challengeTitle = findViewById(R.id.challengeTitle)
        challengeDescription = findViewById(R.id.challengeDescription)
        challengeDifficulty = findViewById(R.id.challengeDifficulty)
        challengeTopic = findViewById(R.id.challengeTopic)
        queryEditText = findViewById(R.id.queryEditText)
        lineNumbers = findViewById(R.id.lineNumbers)
        runButton = findViewById(R.id.runButton)
        hintButton = findViewById(R.id.hintButton)
        viewSolutionButton = findViewById(R.id.viewSolutionButton)
        resetButton = findViewById(R.id.resetButton)
        backButton = findViewById(R.id.backButton)
        expectedTableLayout = findViewById(R.id.expectedTableLayout)
        resultTableLayout = findViewById(R.id.resultTableLayout)
        resultMessage = findViewById(R.id.resultMessage)
        sampleTablesContainer = findViewById(R.id.sampleTablesContainer)
        progressBar = findViewById(R.id.progressBar)

        queryValidator = QueryValidator()

        // Setup line numbers to update dynamically
        setupLineNumbers()
    }

    private fun setupLineNumbers() {
        queryEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateLineNumbers()
            }
        })
        updateLineNumbers()
    }

    private fun updateLineNumbers() {
        val text = queryEditText.text.toString()
        val lineCount = text.split("\n").size.coerceAtLeast(5)
        val numbers = (1..lineCount).joinToString("\n")
        lineNumbers.text = numbers
    }

    private fun setupDatabase() {
        databaseHelper = DatabaseHelper(this)
    }

    private fun loadChallenge() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val challenge = withContext(Dispatchers.IO) {
                    firestoreHelper.getChallengeById(challengeId!!)
                }

                if (challenge != null) {
                    currentChallenge = challenge
                    displayChallenge(challenge)
                    setupChallengeDatabase(challenge)
                    startTime = System.currentTimeMillis()
                } else {
                    DialogUtils.showErrorDialog(
                        this@SQLChallengeActivity,
                        "Error",
                        "Challenge not found"
                    )
                    finish()
                }

            } catch (e: Exception) {
                DialogUtils.showErrorDialog(
                    this@SQLChallengeActivity,
                    "Error",
                    "Error loading challenge: ${e.message}"
                )
                finish()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayChallenge(challenge: SQLChallenge) {
        challengeTitle.text = challenge.title
        challengeDescription.text = challenge.description
        challengeTopic.text = challenge.topic

        // Set difficulty with color
        challengeDifficulty.apply {
            text = challenge.difficulty
            setTextColor(Color.parseColor(challenge.difficultyColor))
        }

        // Display sample tables
        displaySampleTables(challenge.getAllTables())

        // Display expected output
        displayExpectedOutput(challenge.expectedResult.columns, challenge.expectedResult.rows)

        // Show/hide hint button based on availability
        hintButton.visibility = if (challenge.hints.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupChallengeDatabase(challenge: SQLChallenge) {
        try {
            val database = databaseHelper.writableDatabase

            // Clear any existing data
            databaseHelper.resetDatabase()

            // Create and populate all tables (primary + additional)
            challenge.getAllTables().forEach { tableData ->
                createAndPopulateTable(database, tableData)
            }

            // Initialize query evaluator with the database
            queryEvaluator = QueryEvaluator(database)

        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Database Error", "Error setting up database: ${e.message}")
        }
    }

    private fun createAndPopulateTable(
        database: android.database.sqlite.SQLiteDatabase,
        tableData: TableData
    ) {
        try {
            // Drop table if exists
            database.execSQL("DROP TABLE IF EXISTS ${tableData.name}")

            // Create table
            val createTableSQL = tableData.generateCreateTableSQL()
            database.execSQL(createTableSQL)

            // Insert data
            val insertStatements = tableData.generateInsertSQL()
            insertStatements.forEach { insertSQL ->
                database.execSQL(insertSQL)
            }

        } catch (e: Exception) {
            throw Exception("Error creating table ${tableData.name}: ${e.message}")
        }
    }

    private fun displaySampleTables(tables: List<TableData>) {
        sampleTablesContainer.removeAllViews()

        tables.forEach { tableData ->
            // Add table title
            val titleView = TextView(this).apply {
                text = "Table: ${tableData.name}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 16, 0, 8)
            }
            sampleTablesContainer.addView(titleView)

            // Add table layout
            val tableLayout = TableLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Create header row
            val headerRow = TableRow(this)
            tableData.columns.forEach { column ->
                val headerCell = TextView(this).apply {
                    text = column
                    setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
                    setTextColor(Color.WHITE)
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                headerRow.addView(headerCell)
            }
            tableLayout.addView(headerRow)

            // Create data rows (show max 5 rows)
            val maxRows = 5
            tableData.rows.take(maxRows).forEach { row ->
                val dataRow = TableRow(this)
                row.forEach { cell ->
                    val dataCell = TextView(this).apply {
                        text = cell.toString()
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        setPadding(16, 16, 16, 16)
                        gravity = android.view.Gravity.CENTER
                    }
                    dataRow.addView(dataCell)
                }
                tableLayout.addView(dataRow)
            }

            // Add "..." row if there are more rows
            if (tableData.rows.size > maxRows) {
                val moreRow = TableRow(this)
                val moreCell = TextView(this).apply {
                    text = "... (${tableData.rows.size - maxRows} more rows)"
                    setTextColor(Color.GRAY)
                    setPadding(16, 8, 16, 8)
                    gravity = android.view.Gravity.CENTER
                }
                moreRow.addView(moreCell)
                tableLayout.addView(moreRow)
            }

            sampleTablesContainer.addView(tableLayout)

            // Add spacing
            val spacer = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    16
                )
            }
            sampleTablesContainer.addView(spacer)
        }
    }

    private fun displayExpectedOutput(columns: List<String>, rows: List<List<Any>>) {
        expectedTableLayout.removeAllViews()

        if (columns.isEmpty()) return

        // Create header row
        val headerRow = TableRow(this)
        columns.forEach { column ->
            val headerCell = TextView(this).apply {
                text = column
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
                setTextColor(Color.WHITE)
                setPadding(16, 16, 16, 16)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            headerRow.addView(headerCell)
        }
        expectedTableLayout.addView(headerRow)

        // Create data rows
        rows.forEach { row ->
            val dataRow = TableRow(this)
            row.forEach { cell ->
                val dataCell = TextView(this).apply {
                    text = cell.toString()
                    setBackgroundColor(Color.WHITE)
                    setTextColor(Color.BLACK)
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                }
                dataRow.addView(dataCell)
            }
            expectedTableLayout.addView(dataRow)
        }
    }

    private fun setupListeners() {
        runButton.setOnClickListener {
            executeQuery()
        }

        hintButton.setOnClickListener {
            showHint()
        }

        viewSolutionButton.setOnClickListener {
            showSolution()
        }

        resetButton.setOnClickListener {
            resetQuery()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun executeQuery() {
        val query = queryEditText.text.toString().trim()

        if (query.isEmpty()) {
            DialogUtils.showWarningDialog(this, "Empty Query", "Please enter a query")
            return
        }

        // Check if queryEvaluator is initialized
        if (!::queryEvaluator.isInitialized) {
            DialogUtils.showWarningDialog(this, "Not Ready", "Database not ready. Please wait...")
            return
        }

        // Validate query
        val validationResult = queryValidator.validateQuery(query)
        if (!validationResult.isValid) {
            showError(validationResult.errorMessage ?: "Invalid query")
            return
        }

        // Execute and evaluate query
        currentChallenge?.let { challenge ->
            try {
                val expectedResult = challenge.expectedResult.toQueryResult()
                val evaluation = queryEvaluator.evaluateQuery(query, expectedResult)

                displayResults(evaluation)

                // Save progress to Firestore
                if (evaluation.isCorrect) {
                    saveProgress(true, evaluation.score, query)
                    showSuccessDialog()
                } else {
                    saveProgress(false, evaluation.score, query)
                }

            } catch (e: Exception) {
                showError("Error executing query: ${e.message}")
            }
        }
    }

    private fun displayResults(evaluation: EvaluationResult) {
        // Clear previous results
        resultTableLayout.removeAllViews()

        val result = evaluation.actualResult

        if (result.success && result.columns.isNotEmpty()) {
            // Create header row
            val headerRow = TableRow(this)
            result.columns.forEach { column ->
                val headerCell = TextView(this).apply {
                    text = column
                    setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
                    setTextColor(Color.WHITE)
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                headerRow.addView(headerCell)
            }
            resultTableLayout.addView(headerRow)

            // Create data rows
            result.rows.forEach { row ->
                val dataRow = TableRow(this)
                row.forEach { cell ->
                    val dataCell = TextView(this).apply {
                        text = cell.toString()
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        setPadding(16, 16, 16, 16)
                        gravity = android.view.Gravity.CENTER
                    }
                    dataRow.addView(dataCell)
                }
                resultTableLayout.addView(dataRow)
            }
        }

        // Show result message
        resultMessage.apply {
            text = evaluation.feedback
            setTextColor(
                if (evaluation.isCorrect)
                    ContextCompat.getColor(context, R.color.success_green)
                else
                    ContextCompat.getColor(context, R.color.error_red)
            )
            visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        resultMessage.apply {
            text = "❌ Error: $message"
            setTextColor(ContextCompat.getColor(context, R.color.error_red))
            visibility = View.VISIBLE
        }

        // Clear result table
        resultTableLayout.removeAllViews()
    }

    private fun showHint() {
        currentChallenge?.let { challenge ->
            if (challenge.hints.isEmpty()) {
                DialogUtils.showInfoDialog(this, "No Hints", "No hints available")
                return
            }

            val hint = challenge.hints[currentHintIndex % challenge.hints.size]
            currentHintIndex++

            DialogUtils.showHintDialog(this, "Hint ${currentHintIndex}/${challenge.hints.size}", hint)
        }
    }

    private fun showSolution() {
        AlertDialog.Builder(this)
            .setTitle("View Solution")
            .setMessage("Are you sure you want to view the solution? This will reveal the answer.")
            .setPositiveButton("Yes") { dialog, _ ->
                currentChallenge?.let { challenge ->
                    queryEditText.setText(challenge.expectedQuery)

                    AlertDialog.Builder(this)
                        .setTitle("Solution")
                        .setMessage(
                            "Query:\n${challenge.expectedQuery}\n\n" +
                                    "Explanation:\n${challenge.description}"
                        )
                        .setPositiveButton("OK") { d, _ -> d.dismiss() }
                        .show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun resetQuery() {
        queryEditText.setText("")
        resultTableLayout.removeAllViews()
        resultMessage.visibility = View.GONE
        currentHintIndex = 0
    }

    private fun showSuccessDialog() {
        val timeTaken = (System.currentTimeMillis() - startTime) / 1000 // in seconds

        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage(
                "You've successfully completed this challenge!\n\n" +
                        "Time taken: ${timeTaken}s\n" +
                        "Difficulty: ${currentChallenge?.difficulty ?: "Unknown"}"
            )
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Back to Challenges") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun saveProgress(passed: Boolean, score: Int, userQuery: String) {
        val timeTaken = (System.currentTimeMillis() - startTime) / 1000

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreHelper.updateProgressAfterAttempt(
                    challengeId = challengeId!!,
                    passed = passed,
                    score = score,
                    userQuery = userQuery,
                    timeTaken = timeTaken
                )
            } catch (e: Exception) {
                // Log error but don't show to user
                android.util.Log.e(TAG, "Error saving progress: ${e.message}")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }
    }

    companion object {
        private const val TAG = "SQLChallengeActivity"
        const val EXTRA_CHALLENGE_ID = "challenge_id"
    }
}
