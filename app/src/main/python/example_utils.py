"""
Example Python utility module for Chaquopy
This demonstrates how to create reusable Python modules
"""

def calculate_sum(a, b):
    """Calculate sum of two numbers"""
    return a + b

def calculate_product(a, b):
    """Calculate product of two numbers"""
    return a * b

def process_list(numbers):
    """Double each number in a list"""
    return [x * 2 for x in numbers]

def get_greeting(name):
    """Return a greeting message"""
    return f"Hello from Python, {name}!"

def fibonacci(n):
    """Calculate nth Fibonacci number"""
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
