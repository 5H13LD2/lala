import sys
from io import StringI
import myscript

def func(user_code):
    original_stdout = sys.stdout
    capture = StringIO()
    sys.stdout = capture

    try:
        exec(user_code)
    except Exception as e:
        print(f"Error: {e}")

    sys.stdout = original_stdout

    # Kunin yung folder path na gusto mo
    file_dir = str(getApplicationContext().getFilesDir())
    folder_path = os.path.join(os.path.dirname(file_dir), 'com.andro01.chaquopylatestversion')
    os.makedirs(folder_path, exist_ok=True)
    filename = os.path.join(folder_path, 'file.txt')

    # Isulat yung output sa file.txt
    with open(filename, "w") as f:
        f.write(capture.getvalue())

def getApplicationContext():
    from com.chaquo.python import Python
    return Python.getPlatform().getApplication()
