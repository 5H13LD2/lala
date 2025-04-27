# In myscript.py
import sys
from io import StringIO
import os
import threading
import time

# Global variables to handle input
input_requested = False
input_prompt = ""
input_value = None
input_ready = threading.Event()
execution_active = False

# Custom input function that doesn't block the main thread
def custom_input(prompt=""):
    global input_requested, input_prompt, input_value, input_ready

    # Set prompt and flag
    input_prompt = prompt
    input_requested = True

    # Print the prompt to stdout (will be captured)
    print(prompt, end="")
    sys.stdout.flush()

    # Wait for input to be provided by the Android app (with timeout)
    input_ready.clear()
    input_received = input_ready.wait(60.0)  # 60-second timeout

    if not input_received:
        print("\nInput timed out")
        return ""

    # Reset flag
    input_requested = False

    # Print the input value to make it appear in the output
    print(input_value)

    # Return the value provided by the Android app
    result = input_value
    return result

# Don't replace the built-in input function immediately
# We'll do this inside execute_code to avoid blocking at import time

def execute_code(user_code):
    """
    Execute Python code and save output to a file
    """
    global input_value, input_ready, execution_active

    # Set execution flag
    execution_active = True

    # Replace the built-in input function
    sys.modules['builtins'].input = custom_input

    # Capture stdout
    original_stdout = sys.stdout
    capture = StringIO()
    sys.stdout = capture

    try:
        # Write initial message to output file
        output_file = get_output_file_path()
        with open(output_file, "w") as f:
            f.write("Running Python code...\n")
    except Exception as e:
        print(f"Error writing initial output: {str(e)}")

    # Execute in a separate thread to prevent blocking
    def run_code():
        global execution_active
        try:
            # Execute the user's code
            exec(user_code, globals())
        except Exception as e:
            print(f"Error executing code: {e}")
        finally:
            # Restore stdout
            sys.stdout = original_stdout
            execution_active = False

    # Start the thread
    code_thread = threading.Thread(target=run_code)
    code_thread.daemon = True  # Make thread a daemon so it doesn't block app exit
    code_thread.start()

    # Start a separate thread to monitor and update output
    def update_output():
        while execution_active:
            try:
                with open(get_output_file_path(), "w") as f:
                    f.write(capture.getvalue())
            except Exception as e:
                print(f"Error updating output file: {e}")
            time.sleep(0.2)

        # Final write
        try:
            with open(get_output_file_path(), "w") as f:
                f.write(capture.getvalue())
        except Exception as e:
            print(f"Error writing final output: {e}")

    update_thread = threading.Thread(target=update_output)
    update_thread.daemon = True
    update_thread.start()

    # Let the threads run independently (non-blocking)
    return True

def is_execution_active():
    """
    Check if Python code execution is still active
    """
    global execution_active
    return execution_active

def get_output_file_path():
    """
    Get the path to the output file
    """
    from com.chaquo.python import Python
    app_context = Python.getPlatform().getApplication()
    files_dir = app_context.getFilesDir().getAbsolutePath()
    return os.path.join(files_dir, "code_output.txt")

def is_input_requested():
    """
    Check if input is being requested by the Python script
    """
    global input_requested
    return input_requested

def get_input_prompt():
    """
    Get the current input prompt
    """
    global input_prompt
    return input_prompt

def provide_input(value):
    """
    Provide input to the Python script
    """
    global input_value, input_ready
    input_value = value
    input_ready.set()