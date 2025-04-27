import sys
from io import StringIO
import os

def execute_code(user_code):
    """
    Execute Python code and save output to a file
    """
    # Capture stdout
    original_stdout = sys.stdout
    capture = StringIO()
    sys.stdout = capture

    try:
        # Execute the user's code
        exec(user_code)
    except Exception as e:
        print(f"Error: {e}")
    finally:
        # Restore stdout
        sys.stdout = original_stdout

    try:
        # Get Android app context for file operations
        from com.chaquo.python import Python
        app_context = Python.getPlatform().getApplication()

        # Get files directory
        files_dir = app_context.getFilesDir().getAbsolutePath()

        # Create output directory if needed
        output_dir = os.path.join(files_dir, "files")
        os.makedirs(output_dir, exist_ok=True)

        # Define output file path
        output_file = os.path.join(files_dir, "code_output.txt")

        # Write output to file
        with open(output_file, "w") as f:
            f.write(capture.getvalue())

        # Also try alternate location for compatibility
        alt_output_file = os.path.join(output_dir, "code_output.txt")
        with open(alt_output_file, "w") as f:
            f.write(capture.getvalue())

    except Exception as e:
        # If file operations fail, write to a different location
        with open(files_dir + "/error_log.txt", "w") as f:
            f.write(f"File operation error: {str(e)}")

        # Try to print to Logcat for debugging
        print(f"File operation error: {str(e)}")

def get_file_info():
    """
    Get diagnostic information about file paths and permissions
    """
    info = []

    try:
        from com.chaquo.python import Python
        app_context = Python.getPlatform().getApplication()

        files_dir = app_context.getFilesDir().getAbsolutePath()
        info.append(f"Files dir: {files_dir}")

        output_dir = os.path.join(files_dir, "files")
        info.append(f"Output dir exists: {os.path.exists(output_dir)}")

        output_file = os.path.join(files_dir, "code_output.txt")
        info.append(f"Output file exists: {os.path.exists(output_file)}")

        # Check for write permission
        try:
            with open(files_dir + "/test_write.txt", "w") as f:
                f.write("Test")
            info.append("Write permission: OK")
            os.remove(files_dir + "/test_write.txt")
        except Exception as e:
            info.append(f"Write permission error: {str(e)}")

        # List files in directory
        info.append("Files in directory:")
        for f in os.listdir(files_dir):
            info.append(f"- {f}")

    except Exception as e:
        info.append(f"Error getting file info: {str(e)}")

    return "\n".join(info)