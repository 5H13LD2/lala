# Android Studio Built-in Compilers Guide: Janino & Chaquopy

## Table of Contents
- [Overview](#overview)
- [Janino - Embedded Java Compiler](#janino---embedded-java-compiler)
  - [Setup](#janino-setup)
  - [Basic Usage](#janino-basic-usage)
  - [Advanced Examples](#janino-advanced-examples)
- [Chaquopy - Python API](#chaquopy---python-api)
  - [Setup](#chaquopy-setup)
  - [Basic Usage](#chaquopy-basic-usage)
  - [Advanced Examples](#chaquopy-advanced-examples)
- [Comparison & Use Cases](#comparison--use-cases)
- [Troubleshooting](#troubleshooting)

---

## Overview

This guide covers the integration and usage of two powerful runtime compilation tools in Android Studio:

- **Janino**: A lightweight Java compiler that allows you to compile and execute Java code at runtime
- **Chaquopy**: A Python SDK that enables Python code execution within Android applications

Both tools enable dynamic code execution, making your Android apps more flexible and powerful.

---

## Janino - Embedded Java Compiler

### Janino Setup

#### Step 1: Add Dependency

Add Janino to your app's `build.gradle` file:

```gradle
dependencies {
    implementation 'org.codehaus.janino:janino:3.1.12'
    implementation 'org.codehaus.janino:commons-compiler:3.1.12'
}
```

#### Step 2: Sync Project

Click "Sync Now" in Android Studio after adding the dependency.

#### Step 3: Add Permissions (if needed)

No special permissions required for basic Janino usage.

### Janino Basic Usage

#### Example 1: Simple Expression Evaluation

```java
import org.codehaus.janino.ExpressionEvaluator;

public class JaninoExample {
    public void evaluateExpression() {
        try {
            // Create an expression evaluator
            ExpressionEvaluator ee = new ExpressionEvaluator();
            
            // Define the expression
            ee.cook("3 + 4 * 5");
            
            // Evaluate and get result
            Object result = ee.evaluate(null);
            System.out.println("Result: " + result); // Output: 23
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### Example 2: Dynamic Method Compilation

```java
import org.codehaus.janino.SimpleCompiler;

public class DynamicCompilation {
    public void compileDynamicClass() {
        try {
            String javaCode = 
                "public class DynamicClass {" +
                "    public String getMessage() {" +
                "        return \"Hello from dynamic code!\";" +
                "    }" +
                "}";
            
            SimpleCompiler compiler = new SimpleCompiler();
            compiler.cook(javaCode);
            
            // Load and instantiate the compiled class
            Class<?> clazz = compiler.getClassLoader()
                .loadClass("DynamicClass");
            Object instance = clazz.newInstance();
            
            // Call the method
            Method method = clazz.getMethod("getMessage");
            String result = (String) method.invoke(instance);
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Janino Advanced Examples

#### Script Evaluator with Variables

```java
import org.codehaus.janino.ScriptEvaluator;

public class ScriptExample {
    public void runScript() {
        try {
            ScriptEvaluator se = new ScriptEvaluator();
            
            // Define parameters
            se.setParameters(
                new String[] { "a", "b" },
                new Class[] { int.class, int.class }
            );
            
            // Define return type
            se.setReturnType(int.class);
            
            // Cook the script
            se.cook(
                "int sum = a + b;" +
                "return sum * 2;"
            );
            
            // Evaluate with arguments
            Object result = se.evaluate(new Object[] { 5, 3 });
            System.out.println("Result: " + result); // Output: 16
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### Android Activity Integration

```java
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.codehaus.janino.ExpressionEvaluator;

public class MainActivity extends AppCompatActivity {
    private TextView resultTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        resultTextView = findViewById(R.id.resultTextView);
        
        // Execute dynamic Java code
        executeDynamicCode("Math.sqrt(16) + Math.pow(2, 3)");
    }
    
    private void executeDynamicCode(String expression) {
        try {
            ExpressionEvaluator ee = new ExpressionEvaluator();
            ee.cook(expression);
            Object result = ee.evaluate(null);
            resultTextView.setText("Result: " + result);
        } catch (Exception e) {
            resultTextView.setText("Error: " + e.getMessage());
        }
    }
}
```

---

## Chaquopy - Python API

### Chaquopy Setup

#### Step 1: Configure Project-level build.gradle

Add the Chaquopy plugin to your project's `build.gradle`:

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:8.1.0"
        classpath "com.chaquo.python:gradle:16.1.0"
    }
}
```

#### Step 2: Configure App-level build.gradle

Apply the plugin and configure Python in your app's `build.gradle`:

```gradle
plugins {
    id 'com.android.application'
    id 'com.chaquo.python'
}

android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.pythonapp"
        minSdk 21
        targetSdk 34
        
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
        }
        
        python {
            version "3.11"
            
            pip {
                // Add Python packages
                install "numpy"
                install "pandas"
            }
        }
    }
}
```

#### Step 3: Initialize Python in Application Class

Create an Application class:

```java
import android.app.Application;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}
```

Add to AndroidManifest.xml:

```xml
<application
    android:name=".MyApplication"
    ... >
```

### Chaquopy Basic Usage

#### Example 1: Simple Python Execution

```java
import com.chaquo.python.Python;
import com.chaquo.python.PyObject;

public class PythonExample {
    public void runPythonCode() {
        Python py = Python.getInstance();
        
        // Execute simple Python code
        PyObject pyObj = py.getModule("builtins");
        
        // Call Python's eval function
        PyObject result = pyObj.callAttr("eval", "2 + 3 * 4");
        
        // Convert to Java
        int javaResult = result.toJava(Integer.class);
        System.out.println("Result: " + javaResult); // Output: 14
    }
}
```

#### Example 2: Python Script File

Create `python_script.py` in `src/main/python/`:

```python
def calculate_sum(a, b):
    return a + b

def process_list(numbers):
    return [x * 2 for x in numbers]

def get_message():
    return "Hello from Python!"
```

Use in Java:

```java
public class UsePythonScript {
    public void callPythonFunction() {
        Python py = Python.getInstance();
        PyObject module = py.getModule("python_script");
        
        // Call function with arguments
        PyObject result = module.callAttr("calculate_sum", 10, 20);
        int sum = result.toJava(Integer.class);
        System.out.println("Sum: " + sum);
        
        // Call function returning string
        PyObject message = module.callAttr("get_message");
        String msg = message.toString();
        System.out.println(msg);
    }
}
```

### Chaquopy Advanced Examples

#### Working with NumPy

```java
import com.chaquo.python.Python;
import com.chaquo.python.PyObject;

public class NumPyExample {
    public void useNumPy() {
        Python py = Python.getInstance();
        
        // Import numpy
        PyObject np = py.getModule("numpy");
        
        // Create array
        PyObject array = np.callAttr("array", 
            new int[]{1, 2, 3, 4, 5});
        
        // Calculate mean
        PyObject mean = np.callAttr("mean", array);
        double meanValue = mean.toJava(Double.class);
        
        System.out.println("Mean: " + meanValue);
        
        // Matrix operations
        PyObject matrix1 = np.callAttr("array", 
            new int[][]{{1, 2}, {3, 4}});
        PyObject matrix2 = np.callAttr("array", 
            new int[][]{{5, 6}, {7, 8}});
        
        PyObject result = np.callAttr("dot", matrix1, matrix2);
        System.out.println("Matrix multiplication: " + result);
    }
}
```

#### Android Activity with Python Integration

```java
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.chaquo.python.Python;
import com.chaquo.python.PyObject;

public class PythonActivity extends AppCompatActivity {
    private EditText codeInput;
    private Button executeButton;
    private TextView outputText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python);
        
        codeInput = findViewById(R.id.codeInput);
        executeButton = findViewById(R.id.executeButton);
        outputText = findViewById(R.id.outputText);
        
        executeButton.setOnClickListener(v -> executePython());
    }
    
    private void executePython() {
        try {
            Python py = Python.getInstance();
            PyObject builtins = py.getModule("builtins");
            
            String code = codeInput.getText().toString();
            PyObject result = builtins.callAttr("eval", code);
            
            outputText.setText("Result: " + result.toString());
        } catch (Exception e) {
            outputText.setText("Error: " + e.getMessage());
        }
    }
}
```

---

## Comparison & Use Cases

### When to Use Janino

**Pros:**
- Lightweight (small library size)
- Fast compilation for Java code
- No additional runtime required
- Perfect for mathematical expressions
- Good for runtime configuration

**Use Cases:**
- Dynamic business rules
- Mathematical formula evaluation
- Runtime code generation
- Plugin systems
- Configuration scripts

**Example Use Case:**
```java
// Dynamic tax calculation based on user-defined rules
String taxFormula = "income * 0.2 + (income > 50000 ? 1000 : 0)";
ExpressionEvaluator ee = new ExpressionEvaluator();
ee.setParameters(new String[]{"income"}, new Class[]{double.class});
ee.cook(taxFormula);
double tax = (Double) ee.evaluate(new Object[]{75000.0});
```

### When to Use Chaquopy

**Pros:**
- Access to Python's vast ecosystem
- Scientific computing libraries (NumPy, SciPy)
- Machine learning capabilities
- Data analysis tools
- Easy syntax for complex operations

**Use Cases:**
- Data science applications
- Machine learning on mobile
- Scientific calculations
- Image processing
- Natural language processing

**Example Use Case:**
```java
// Data analysis with pandas
PyObject pd = py.getModule("pandas");
PyObject df = pd.callAttr("DataFrame", data);
PyObject stats = df.callAttr("describe");
```

---

## Troubleshooting

### Common Janino Issues

**Issue 1: ClassNotFoundException**
```
Solution: Ensure all required classes are in the classpath
```

**Issue 2: CompileException**
```
Solution: Check Java syntax in your dynamic code
Enable debug mode: compiler.setDebuggingInformation(true, true, true);
```

### Common Chaquopy Issues

**Issue 1: Python not started**
```
Solution: Initialize Python in Application class
Ensure AndroidPlatform is properly configured
```

**Issue 2: Module not found**
```
Solution: Place Python files in src/main/python/
Check pip installations in build.gradle
```

**Issue 3: Build failures**
```
Solution: Check NDK configuration
Verify Python version compatibility
Clean and rebuild project
```

### Performance Tips

#### Janino Optimization:
- Cache compiled expressions
- Reuse ExpressionEvaluator instances
- Minimize compilation frequency

#### Chaquopy Optimization:
- Keep Python instances alive
- Use batch operations
- Minimize data transfer between Java and Python

---

## Security Considerations

### For Both Compilers:
1. **Never execute untrusted code**
2. **Validate all user inputs**
3. **Implement sandboxing if needed**
4. **Use try-catch blocks extensively**
5. **Log all dynamic code execution**

### Example Security Implementation:
```java
public boolean isCodeSafe(String code) {
    // Check for dangerous patterns
    String[] dangerous = {"System.exit", "Runtime.exec", 
                         "ProcessBuilder", "File", "delete"};
    for (String pattern : dangerous) {
        if (code.contains(pattern)) {
            return false;
        }
    }
    return true;
}
```

---

## Conclusion

Both Janino and Chaquopy provide powerful runtime code execution capabilities for Android applications. Choose based on your specific needs:

- **Use Janino** for Java-based dynamic logic, expressions, and lightweight scripting
- **Use Chaquopy** for data science, machine learning, and when you need Python's ecosystem

Remember to always prioritize security when executing dynamic code and thoroughly test your implementations.

---

## Resources

- [Janino Official Documentation](http://janino-compiler.github.io/janino/)
- [Chaquopy Official Documentation](https://chaquo.com/chaquopy/doc/current/)
- [Android Studio Documentation](https://developer.android.com/studio)