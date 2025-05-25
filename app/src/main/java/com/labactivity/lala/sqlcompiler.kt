package com.labactivity.lala

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.DatabaseHelper
import com.labactivity.lala.TestCase
import com.labactivity.lala.QueryResult
import com.labactivity.lala.QueryValidator
import com.labactivity.lala.QueryEvaluator

class sqlcompiler : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var queryEvaluator: QueryEvaluator
    private lateinit var queryValidator: QueryValidator

    // UI Components
    private lateinit var queryEditText: EditText
    private lateinit var runButton: Button
    private lateinit var testCaseSpinner: Spinner
    private lateinit var expectedTableLayout: TableLayout
    private lateinit var resultTableLayout: TableLayout
    private lateinit var resultMessage: TextView
    private lateinit var resetButton: Button
    private lateinit var viewSolutionButton: Button
    private lateinit var sampleTableTitle: TextView

    // Data
    private lateinit var testCases: List<TestCase>
    private var currentTestCase: TestCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sqlcompiler)

        initializeComponents()
        setupDatabase()
        setupTestCases()
        setupListeners()
    }

    private fun initializeComponents() {
        queryEditText = findViewById(R.id.queryEditText)
        runButton = findViewById(R.id.runButton)
        testCaseSpinner = findViewById(R.id.testCaseSpinner)
        expectedTableLayout = findViewById(R.id.expectedTableLayout)
        resultTableLayout = findViewById(R.id.resultTableLayout)
        resultMessage = findViewById(R.id.resultMessage)
        resetButton = findViewById(R.id.resetButton)
        viewSolutionButton = findViewById(R.id.viewSolutionButton)
        sampleTableTitle = findViewById(R.id.sampleTableTitle)

        queryValidator = QueryValidator()
    }

    private fun setupDatabase() {
        databaseHelper = DatabaseHelper(this)
        val database = databaseHelper.writableDatabase
        queryEvaluator = QueryEvaluator(database)
    }

    private fun setupTestCases() {
        testCases = databaseHelper.getTestCases()

        // Setup spinner
        val testCaseNames = testCases.map { "${it.id}. ${it.title}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, testCaseNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        testCaseSpinner.adapter = adapter

        // Set initial test case
        if (testCases.isNotEmpty()) {
            currentTestCase = testCases[0]
            displayExpectedOutput(currentTestCase!!)
        }
    }

    private fun setupListeners() {
        runButton.setOnClickListener {
            executeQuery()
        }

        testCaseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentTestCase = testCases[position]
                displayExpectedOutput(currentTestCase!!)
                clearResults()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        resetButton.setOnClickListener {
            resetQuery()
        }

        viewSolutionButton.setOnClickListener {
            showSolution()
        }
    }

    private fun executeQuery() {
        val query = queryEditText.text.toString().trim()

        // Validate query
        val validationResult = queryValidator.validateQuery(query)
        if (!validationResult.isValid) {
            showError(validationResult.errorMessage ?: "Invalid query")
            return
        }

        // Execute and evaluate query
        currentTestCase?.let { testCase ->
            val expectedResult = QueryResult(
                success = true,
                columns = testCase.expectedOutput.firstOrNull()?.keys?.toList() ?: emptyList(),
                rows = testCase.expectedOutput.map { row ->
                    testCase.expectedOutput.firstOrNull()?.keys?.map { key -> row[key] ?: "" } ?: emptyList()
                }
            )

            val evaluation = queryEvaluator.evaluateQuery(query, expectedResult)
            displayResults(evaluation)
        }
    }

    private fun displayExpectedOutput(testCase: TestCase) {
        sampleTableTitle.text = "Sample Table: `${testCase.sampleTables.keys.first()}`"

        expectedTableLayout.removeAllViews()

        if (testCase.expectedOutput.isNotEmpty()) {
            // Create header row
            val headerRow = TableRow(this)
            val columns = testCase.expectedOutput.first().keys

            for (column in columns) {
                val headerCell = TextView(this).apply {
                    text = column
                    setBackgroundColor(getColor(R.color.primary_blue))
                    setTextColor(Color.WHITE)
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                headerRow.addView(headerCell)
            }
            expectedTableLayout.addView(headerRow)

            // Create data rows
            for (row in testCase.expectedOutput) {
                val dataRow = TableRow(this)
                for (column in columns) {
                    val dataCell = TextView(this).apply {
                        text = row[column]?.toString() ?: ""
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
    }

    private fun displayResults(evaluation: com.labactivity.lala.EvaluationResult) {
        // Clear previous results
        resultTableLayout.removeAllViews()

        val result = evaluation.actualResult

        if (result.success && result.columns.isNotEmpty()) {
            // Create header row
            val headerRow = TableRow(this)
            for (column in result.columns) {
                val headerCell = TextView(this).apply {
                    text = column
                    setBackgroundColor(getColor(R.color.primary_blue))
                    setTextColor(Color.WHITE)
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                headerRow.addView(headerCell)
            }
            resultTableLayout.addView(headerRow)

            // Create data rows
            for (row in result.rows) {
                val dataRow = TableRow(this)
                for (cell in row) {
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
            setTextColor(if (evaluation.isCorrect) getColor(R.color.success_green) else getColor(R.color.error_red))
            visibility = android.view.View.VISIBLE
        }
    }

    private fun showError(message: String) {
        resultMessage.apply {
            text = "❌ Error: $message"
            setTextColor(getColor(R.color.error_red))
            visibility = android.view.View.VISIBLE
        }

        // Clear result table
        resultTableLayout.removeAllViews()
    }

    private fun clearResults() {
        resultTableLayout.removeAllViews()
        resultMessage.visibility = android.view.View.GONE
    }

    private fun resetQuery() {
        queryEditText.setText("")
        clearResults()
    }

    private fun showSolution() {
        currentTestCase?.let { testCase ->
            queryEditText.setText(testCase.expectedQuery)

            // Show solution dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Solution")
                .setMessage("Query: ${testCase.expectedQuery}\n\nDescription: ${testCase.description}")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
    }
}

