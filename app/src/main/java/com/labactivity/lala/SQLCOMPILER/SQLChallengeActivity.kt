package com.labactivity.lala.SQLCOMPILER

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.R
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge
import com.labactivity.lala.SQLCOMPILER.services.FirestoreSQLHelper
import com.labactivity.lala.SQLCOMPILER.services.SQLiteHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for displaying and executing SQL challenges
 */
class SQLChallengeActivity : BaseActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var challengeTitle: TextView
    private lateinit var challengeDifficulty: TextView
    private lateinit var challengeTopic: TextView
    private lateinit var challengeDescription: TextView
    private lateinit var sampleTablesContainer: LinearLayout
    private lateinit var expectedTableLayout: TableLayout
    private lateinit var queryEditText: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var runButton: Button
    private lateinit var hintButton: Button
    private lateinit var resetButton: Button
    private lateinit var viewSolutionButton: Button
    private lateinit var resultMessage: TextView
    private lateinit var resultTableLayout: TableLayout

    private val sqlHelper = FirestoreSQLHelper.getInstance()
    private val sqliteHelper = SQLiteHelper(this)
    private var currentChallenge: SQLChallenge? = null
    private var challengeId: String? = null

    companion object {
        private const val TAG = "SQLChallengeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sql_challenge)

        initializeViews()
        setupListeners()

        // Get challenge ID from intent
        challengeId = intent.getStringExtra("CHALLENGE_ID")

        if (challengeId != null) {
            loadChallengeFromFirebase(challengeId!!)
        } else {
            Log.e(TAG, "No challenge ID provided")
            finish()
        }
    }

    /**
     * Initialize all views
     */
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        challengeTitle = findViewById(R.id.challengeTitle)
        challengeDifficulty = findViewById(R.id.challengeDifficulty)
        challengeTopic = findViewById(R.id.challengeTopic)
        challengeDescription = findViewById(R.id.challengeDescription)
        sampleTablesContainer = findViewById(R.id.sampleTablesContainer)
        expectedTableLayout = findViewById(R.id.expectedTableLayout)
        queryEditText = findViewById(R.id.queryEditText)
        lineNumbers = findViewById(R.id.lineNumbers)
        runButton = findViewById(R.id.runButton)
        hintButton = findViewById(R.id.hintButton)
        resetButton = findViewById(R.id.resetButton)
        viewSolutionButton = findViewById(R.id.viewSolutionButton)
        resultMessage = findViewById(R.id.resultMessage)
        resultTableLayout = findViewById(R.id.resultTableLayout)
    }

    /**
     * Setup click listeners
     */
    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        runButton.setOnClickListener {
            val query = queryEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                executeQuery(query)
            } else {
                showResultMessage("Please enter a SQL query", isSuccess = false)
            }
        }

        hintButton.setOnClickListener {
            currentChallenge?.hints?.let { hints ->
                if (hints.isNotEmpty()) {
                    showHintDialog(hints.first())
                }
            }
        }

        resetButton.setOnClickListener {
            queryEditText.text.clear()
            resultMessage.visibility = View.GONE
            resultTableLayout.removeAllViews()
        }

        viewSolutionButton.setOnClickListener {
            currentChallenge?.expectedQuery?.let { solution ->
                showSolutionDialog(solution)
            }
        }

        // Update line numbers as user types
        queryEditText.setOnKeyListener { _, _, _ ->
            updateLineNumbers()
            false
        }
    }

    /**
     * Load challenge data from Firebase
     */
    private fun loadChallengeFromFirebase(challengeId: String) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading challenge: $challengeId from Firebase...")

                val challenge = withContext(Dispatchers.IO) {
                    sqlHelper.getChallengeById(challengeId)
                }

                if (challenge != null) {
                    Log.d(TAG, "✅ Challenge loaded: ${challenge.title}")
                    currentChallenge = challenge
                    displayChallenge(challenge)
                } else {
                    Log.e(TAG, "❌ Challenge not found")
                    showResultMessage("Challenge not found", isSuccess = false)
                }

                progressBar.visibility = View.GONE

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading challenge", e)
                progressBar.visibility = View.GONE
                showResultMessage("Error loading challenge: ${e.message}", isSuccess = false)
            }
        }
    }

    /**
     * Display challenge data in the UI
     */
    private fun displayChallenge(challenge: SQLChallenge) {
        // Set basic info
        challengeTitle.text = challenge.title
        challengeDifficulty.text = challenge.difficulty
        challengeTopic.text = challenge.topic
        challengeDescription.text = challenge.description

        // Display sample tables
        displaySampleTables(challenge)

        // Display expected output
        displayExpectedOutput(challenge)

        // Initialize database with sample data
        initializeDatabase(challenge)
    }

    /**
     * Display sample tables
     */
    private fun displaySampleTables(challenge: SQLChallenge) {
        sampleTablesContainer.removeAllViews()

        challenge.getAllTables().forEach { table ->
            val tableView = createTableView(table.name, table.columns, table.rows)
            sampleTablesContainer.addView(tableView)
        }
    }

    /**
     * Display expected output
     */
    private fun displayExpectedOutput(challenge: SQLChallenge) {
        expectedTableLayout.removeAllViews()

        val expectedResult = challenge.expectedResult
        if (expectedResult.columns.isNotEmpty() && expectedResult.rows.isNotEmpty()) {
            // Add header row
            val headerRow = TableRow(this)
            expectedResult.columns.forEach { header ->
                val textView = TextView(this).apply {
                    text = header
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_header_background)
                    setTextColor(resources.getColor(R.color.white, null))
                }
                headerRow.addView(textView)
            }
            expectedTableLayout.addView(headerRow)

            // Add data rows
            expectedResult.rows.forEach { rowData ->
                val dataRow = TableRow(this)
                rowData.forEach { cellValue ->
                    val textView = TextView(this).apply {
                        text = cellValue?.toString() ?: ""
                        setPadding(16, 16, 16, 16)
                        setBackgroundResource(R.drawable.table_cell_background)
                    }
                    dataRow.addView(textView)
                }
                expectedTableLayout.addView(dataRow)
            }
        }
    }

    /**
     * Create a visual representation of a table
     */
    private fun createTableView(
        tableName: String,
        columns: List<String>,
        rows: List<List<Any>>
    ): View {
        val tableContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 32, 16)
        }

        // Table name
        val nameTextView = TextView(this).apply {
            text = tableName
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        tableContainer.addView(nameTextView)

        // Table layout
        val tableLayout = TableLayout(this)

        // Add header
        val headerRow = TableRow(this)
        columns.forEach { column ->
            val textView = TextView(this).apply {
                text = column
                setPadding(12, 12, 12, 12)
                setBackgroundResource(R.drawable.table_header_background)
                setTextColor(resources.getColor(R.color.white, null))
                textSize = 12f
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Add data rows
        rows.forEach { rowData ->
            val dataRow = TableRow(this)
            rowData.forEach { cellValue ->
                val textView = TextView(this).apply {
                    text = cellValue?.toString() ?: ""
                    setPadding(12, 12, 12, 12)
                    setBackgroundResource(R.drawable.table_cell_background)
                    textSize = 12f
                }
                dataRow.addView(textView)
            }
            tableLayout.addView(dataRow)
        }

        tableContainer.addView(tableLayout)
        return tableContainer
    }

    /**
     * Initialize SQLite database with sample data
     */
    private fun initializeDatabase(challenge: SQLChallenge) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sqliteHelper.createTablesFromChallenge(challenge)
                Log.d(TAG, "✅ Database initialized with sample data")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing database", e)
            }
        }
    }

    /**
     * Execute user's SQL query
     */
    private fun executeQuery(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    sqliteHelper.executeQuery(query)
                }

                displayQueryResult(result)
                validateQuery(result)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Query execution error", e)
                showResultMessage("Error: ${e.message}", isSuccess = false)
            }
        }
    }

    /**
     * Display query results
     */
    private fun displayQueryResult(result: List<Map<String, Any?>>) {
        resultTableLayout.removeAllViews()

        if (result.isEmpty()) {
            showResultMessage("Query executed successfully (0 rows returned)", isSuccess = true)
            return
        }

        val headers = result[0].keys.toList()

        // Add header row
        val headerRow = TableRow(this)
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.table_header_background)
                setTextColor(resources.getColor(R.color.white, null))
            }
            headerRow.addView(textView)
        }
        resultTableLayout.addView(headerRow)

        // Add data rows
        result.forEach { row ->
            val dataRow = TableRow(this)
            headers.forEach { header ->
                val textView = TextView(this).apply {
                    text = row[header]?.toString() ?: ""
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_background)
                }
                dataRow.addView(textView)
            }
            resultTableLayout.addView(dataRow)
        }
    }

    /**
     * Validate query result against expected output
     */
    private fun validateQuery(result: List<Map<String, Any?>>) {
        val expectedResult = currentChallenge?.expectedResult ?: return

        // Check row count
        if (result.size != expectedResult.rowCount) {
            showResultMessage(
                "❌ Incorrect: Expected ${expectedResult.rowCount} rows, got ${result.size} rows",
                isSuccess = false
            )
            return
        }

        // Check column count
        if (result.isNotEmpty() && result[0].size != expectedResult.columnCount) {
            showResultMessage(
                "❌ Incorrect: Expected ${expectedResult.columnCount} columns, got ${result[0].size} columns",
                isSuccess = false
            )
            return
        }

        // Compare results row by row
        val isCorrect = result.indices.all { i ->
            val resultRow = result[i]
            val expectedRow = expectedResult.rows[i]
            val resultValues = expectedResult.columns.map { col -> resultRow[col]?.toString() ?: "" }
            val expectedValues = expectedRow.map { it?.toString() ?: "" }
            resultValues == expectedValues
        }

        if (isCorrect) {
            showResultMessage("✅ Correct! Well done!", isSuccess = true)
            saveProgress()
        } else {
            showResultMessage("❌ Incorrect result. Try again!", isSuccess = false)
        }
    }

    /**
     * Save user progress to Firebase
     */
    private fun saveProgress() {
        challengeId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userQuery = queryEditText.text.toString()
                    sqlHelper.updateProgressAfterAttempt(
                        challengeId = id,
                        passed = true,
                        score = 100,
                        userQuery = userQuery,
                        timeTaken = 0L
                    )
                    Log.d(TAG, "✅ Progress saved")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error saving progress", e)
                }
            }
        }
    }

    /**
     * Show result message
     */
    private fun showResultMessage(message: String, isSuccess: Boolean) {
        resultMessage.text = message
        resultMessage.setBackgroundColor(
            if (isSuccess) {
                resources.getColor(R.color.success_green, null)
            } else {
                resources.getColor(R.color.error_red, null)
            }
        )
        resultMessage.setTextColor(resources.getColor(R.color.white, null))
        resultMessage.visibility = View.VISIBLE
    }

    /**
     * Show hint dialog
     */
    private fun showHintDialog(hint: String) {
        AlertDialog.Builder(this)
            .setTitle("💡 Hint")
            .setMessage(hint)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Show solution dialog
     */
    private fun showSolutionDialog(solution: String) {
        AlertDialog.Builder(this)
            .setTitle("Solution")
            .setMessage(solution)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Update line numbers based on query text
     */
    private fun updateLineNumbers() {
        val lines = queryEditText.text.toString().split("\n")
        val lineNumberText = (1..maxOf(lines.size, 5)).joinToString("\n")
        lineNumbers.text = lineNumberText
    }

    override fun onDestroy() {
        super.onDestroy()
        sqliteHelper.close()
    }
}
