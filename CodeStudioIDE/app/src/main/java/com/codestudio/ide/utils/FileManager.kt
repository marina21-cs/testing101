package com.codestudio.ide.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.codestudio.ide.model.FileItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

class FileManager(private val context: Context) {

    private val workspaceDir: File
        get() = File(context.filesDir, "workspace")

    private val projectsDir: File
        get() = File(workspaceDir, "projects")

    fun createDefaultWorkspace() {
        if (!workspaceDir.exists()) {
            workspaceDir.mkdirs()
        }
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
            createSampleProject()
        }
    }

    private fun createSampleProject() {
        val sampleProject = File(projectsDir, "MyProject")
        sampleProject.mkdirs()

        // Create sample files
        File(sampleProject, "index.html").writeText("""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Project</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <h1>Welcome to CodeStudio IDE</h1>
    <p>Start coding your amazing project!</p>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent())

        File(sampleProject, "styles.css").writeText("""
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
    background-color: #1e1e1e;
    color: #d4d4d4;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
}

h1 {
    color: #569cd6;
    margin-bottom: 1rem;
}

p {
    color: #9cdcfe;
}
        """.trimIndent())

        File(sampleProject, "script.js").writeText("""
// Welcome to CodeStudio IDE
console.log('Hello from CodeStudio!');

document.addEventListener('DOMContentLoaded', () => {
    console.log('Document loaded');
    
    // Add your JavaScript code here
    const heading = document.querySelector('h1');
    heading.addEventListener('click', () => {
        heading.style.color = getRandomColor();
    });
});

function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}
        """.trimIndent())

        File(sampleProject, "main.py").writeText("""
#!/usr/bin/env python3
\"\"\"
Sample Python file for CodeStudio IDE
\"\"\"

def greet(name: str) -> str:
    \"\"\"Return a greeting message.\"\"\"
    return f"Hello, {name}! Welcome to CodeStudio IDE."

def fibonacci(n: int) -> list[int]:
    \"\"\"Generate Fibonacci sequence up to n numbers.\"\"\"
    if n <= 0:
        return []
    elif n == 1:
        return [0]
    
    sequence = [0, 1]
    while len(sequence) < n:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence

class Calculator:
    \"\"\"A simple calculator class.\"\"\"
    
    @staticmethod
    def add(a: float, b: float) -> float:
        return a + b
    
    @staticmethod
    def subtract(a: float, b: float) -> float:
        return a - b
    
    @staticmethod
    def multiply(a: float, b: float) -> float:
        return a * b
    
    @staticmethod
    def divide(a: float, b: float) -> float:
        if b == 0:
            raise ValueError("Cannot divide by zero")
        return a / b

if __name__ == "__main__":
    print(greet("Developer"))
    print(f"Fibonacci(10): {fibonacci(10)}")
    
    calc = Calculator()
    print(f"10 + 5 = {calc.add(10, 5)}")
        """.trimIndent())

        File(sampleProject, "main.rs").writeText("""
// Sample Rust file for CodeStudio IDE

fn main() {
    println!("Hello from CodeStudio IDE!");
    
    let numbers = vec![1, 2, 3, 4, 5];
    let sum: i32 = numbers.iter().sum();
    println!("Sum: {}", sum);
    
    let message = greet("Rustacean");
    println!("{}", message);
}

fn greet(name: &str) -> String {
    format!("Hello, {}! Welcome to Rust.", name)
}

struct Rectangle {
    width: u32,
    height: u32,
}

impl Rectangle {
    fn new(width: u32, height: u32) -> Self {
        Rectangle { width, height }
    }
    
    fn area(&self) -> u32 {
        self.width * self.height
    }
    
    fn perimeter(&self) -> u32 {
        2 * (self.width + self.height)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_rectangle_area() {
        let rect = Rectangle::new(10, 5);
        assert_eq!(rect.area(), 50);
    }
}
        """.trimIndent())

        File(sampleProject, "script.lua").writeText("""
-- Sample Lua file for CodeStudio IDE

-- Print welcome message
print("Hello from CodeStudio IDE!")

-- Function to calculate factorial
function factorial(n)
    if n <= 1 then
        return 1
    else
        return n * factorial(n - 1)
    end
end

-- Function to generate Fibonacci sequence
function fibonacci(n)
    local seq = {}
    local a, b = 0, 1
    for i = 1, n do
        seq[i] = a
        a, b = b, a + b
    end
    return seq
end

-- Simple class-like table
local Calculator = {}
Calculator.__index = Calculator

function Calculator:new()
    local self = setmetatable({}, Calculator)
    return self
end

function Calculator:add(a, b)
    return a + b
end

function Calculator:multiply(a, b)
    return a * b
end

-- Main execution
print("Factorial of 5:", factorial(5))

local fib = fibonacci(10)
print("Fibonacci sequence:")
for i, v in ipairs(fib) do
    io.write(v .. " ")
end
print()

local calc = Calculator:new()
print("10 + 5 =", calc:add(10, 5))
        """.trimIndent())

        File(sampleProject, "analysis.R").writeText("""
# Sample R file for CodeStudio IDE

# Load required libraries (install if needed)
# install.packages(c("ggplot2", "dplyr"))

# Generate sample data
set.seed(42)
data <- data.frame(
    x = rnorm(100, mean = 50, sd = 10),
    y = rnorm(100, mean = 100, sd = 20),
    category = sample(c("A", "B", "C"), 100, replace = TRUE)
)

# Basic statistics
print("Summary Statistics:")
print(summary(data))

# Custom function for mean calculation
calculate_stats <- function(values) {
    list(
        mean = mean(values),
        median = median(values),
        sd = sd(values),
        min = min(values),
        max = max(values)
    )
}

# Apply function
x_stats <- calculate_stats(data${'$'}x)
print(paste("Mean of X:", round(x_stats${'$'}mean, 2)))

# Group by analysis
category_means <- aggregate(x ~ category, data = data, FUN = mean)
print("Category Means:")
print(category_means)

# Linear model
model <- lm(y ~ x, data = data)
print("Linear Model Summary:")
print(summary(model))
        """.trimIndent())

        // Create src subdirectory
        val srcDir = File(sampleProject, "src")
        srcDir.mkdirs()

        File(srcDir, "utils.js").writeText("""
// Utility functions

export function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

export function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

export function formatDate(date) {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    }).format(date);
}

export function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}
        """.trimIndent())

        File(sampleProject, "README.md").writeText("""
# MyProject

A sample project created with CodeStudio IDE.

## Features

- HTML/CSS/JavaScript web development
- Python scripting
- Rust programming
- Lua scripting
- R statistical analysis

## Getting Started

1. Open any file in the editor
2. Make your changes
3. Use the WebView to preview HTML files
4. Use the terminal for command-line operations

## Project Structure

```
MyProject/
├── index.html      # Main HTML file
├── styles.css      # Stylesheet
├── script.js       # JavaScript
├── main.py         # Python script
├── main.rs         # Rust program
├── script.lua      # Lua script
├── analysis.R      # R analysis script
├── src/
│   └── utils.js    # Utility functions
└── README.md       # This file
```

## License

MIT License
        """.trimIndent())
    }

    fun getWorkspaceRoot(): File = workspaceDir

    fun getProjectsDirectory(): File = projectsDir

    fun getFileTree(rootPath: String, showHidden: Boolean = false): List<FileItem> {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            return emptyList()
        }
        return getFileTreeRecursive(root, 0, showHidden)
    }

    private fun getFileTreeRecursive(
        directory: File,
        depth: Int,
        showHidden: Boolean
    ): List<FileItem> {
        val items = mutableListOf<FileItem>()
        
        val files = directory.listFiles()?.filter { 
            showHidden || !it.name.startsWith(".") 
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        
        files?.forEach { file ->
            val item = FileItem.fromFile(file, depth)
            items.add(item)
            
            if (file.isDirectory) {
                item.children.addAll(getFileTreeRecursive(file, depth + 1, showHidden))
            }
        }
        
        return items
    }

    fun readFile(path: String, charset: Charset = Charsets.UTF_8): Result<String> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Result.failure(IOException("File not found: $path"))
            } else {
                Result.success(file.readText(charset))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeFile(path: String, content: String, charset: Charset = Charsets.UTF_8): Result<Unit> {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content, charset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createFile(path: String): Result<File> {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            if (file.createNewFile()) {
                Result.success(file)
            } else {
                Result.failure(IOException("File already exists: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createDirectory(path: String): Result<File> {
        return try {
            val dir = File(path)
            if (dir.mkdirs() || dir.exists()) {
                Result.success(dir)
            } else {
                Result.failure(IOException("Failed to create directory: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFile(path: String): Result<Unit> {
        return try {
            val file = File(path)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun renameFile(oldPath: String, newPath: String): Result<File> {
        return try {
            val oldFile = File(oldPath)
            val newFile = File(newPath)
            if (oldFile.renameTo(newFile)) {
                Result.success(newFile)
            } else {
                Result.failure(IOException("Failed to rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun copyFile(sourcePath: String, destPath: String): Result<File> {
        return try {
            val source = File(sourcePath)
            val dest = File(destPath)
            dest.parentFile?.mkdirs()
            source.copyTo(dest, overwrite = true)
            Result.success(dest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun moveFile(sourcePath: String, destPath: String): Result<File> {
        return try {
            val result = copyFile(sourcePath, destPath)
            if (result.isSuccess) {
                deleteFile(sourcePath)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFileSize(path: String): Long {
        return File(path).length()
    }

    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    fun isDirectory(path: String): Boolean {
        return File(path).isDirectory
    }

    fun getParentPath(path: String): String? {
        return File(path).parent
    }

    fun searchFiles(
        directory: String,
        query: String,
        caseSensitive: Boolean = false,
        includeContent: Boolean = false
    ): List<FileItem> {
        val results = mutableListOf<FileItem>()
        val root = File(directory)
        
        fun search(dir: File) {
            dir.listFiles()?.forEach { file ->
                val matches = if (caseSensitive) {
                    file.name.contains(query)
                } else {
                    file.name.lowercase().contains(query.lowercase())
                }
                
                if (matches) {
                    results.add(FileItem.fromFile(file))
                }
                
                if (file.isDirectory) {
                    search(file)
                } else if (includeContent && file.isFile && file.length() < 1024 * 1024) {
                    try {
                        val content = file.readText()
                        val contentMatches = if (caseSensitive) {
                            content.contains(query)
                        } else {
                            content.lowercase().contains(query.lowercase())
                        }
                        if (contentMatches && !matches) {
                            results.add(FileItem.fromFile(file))
                        }
                    } catch (e: Exception) {
                        // Skip files that can't be read
                    }
                }
            }
        }
        
        search(root)
        return results
    }

    fun readFileFromUri(uri: Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Result.success(inputStream.bufferedReader().readText())
            } ?: Result.failure(IOException("Cannot open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeFileToUri(uri: Uri, content: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                Result.success(Unit)
            } ?: Result.failure(IOException("Cannot open output stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
