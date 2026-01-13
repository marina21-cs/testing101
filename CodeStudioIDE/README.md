# CodeStudio IDE

<p align="center">
  <img src="app/src/main/res/drawable/ic_app_logo.xml" width="120" height="120" alt="CodeStudio IDE Logo">
</p>

<p align="center">
  <strong>A powerful, VS Code-inspired IDE for Android</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#supported-languages">Languages</a> •
  <a href="#installation">Installation</a> •
  <a href="#building">Building</a> •
  <a href="#usage">Usage</a>
</p>

---

## 🚀 Features

### Code Editor
- **Syntax Highlighting** for 25+ programming languages
- **Line Numbers** with current line highlighting
- **Auto-Completion** with intelligent code suggestions
- **Find & Replace** with regex support
- **Undo/Redo** with unlimited history
- **Multiple Themes** (Dark, Light, Monokai, Dracula, Nord, Solarized)
- **Customizable Font Size** and tab settings
- **Code Folding** for better navigation
- **Bracket Matching** and auto-closing

### File Management
- **File Explorer** with tree view navigation
- **Create, Rename, Delete** files and folders
- **Multiple Open Files** with tab management
- **Modified File Indicators** (dot indicator on unsaved files)
- **Virtual File System** for in-app projects
- **Local Storage Access** for device files

### Integrated Terminal
- **Linux-like Shell** experience
- **Built-in Commands**: cd, pwd, ls, echo, cat, mkdir, rm, touch, clear, help
- **Command History** navigation
- **Scrollable Output** with clear option

### Web Preview
- **Built-in WebView** for HTML/CSS/JS preview
- **Local Development Server** (NanoHTTPD)
- **URL Navigation** with back/forward controls
- **Developer Tools** access
- **Refresh** and reload functionality

### Project Support
- **Multiple Project Types**: Web, Python, R, Rust, Lua, Generic
- **Project Templates** with starter files
- **Recent Files** tracking
- **Code Snippets** storage

## 📱 Supported Languages

| Category | Languages |
|----------|-----------|
| **Web Development** | HTML, CSS, JavaScript, TypeScript, JSON, XML |
| **Data Science** | Python, R |
| **Systems** | Rust, C, C++, Go |
| **Scripting** | Lua, Shell/Bash, Ruby, PHP |
| **Mobile** | Kotlin, Java, Swift, Dart |
| **Database** | SQL |
| **Config** | YAML, TOML, Markdown |

## 📋 Requirements

- **Android**: 7.0 (API 24) or higher
- **Storage**: ~50MB for installation
- **RAM**: 2GB+ recommended

## 📥 Installation

### From APK (Release)

1. Download the latest `CodeStudioIDE.apk` from [Releases](releases/)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the APK file to install
4. Launch CodeStudio IDE from your app drawer

### From Source

Follow the [Building](#building) instructions below.

## 🔧 Building

### Prerequisites

1. **Android Studio** Arctic Fox (2020.3.1) or newer
   - Download: https://developer.android.com/studio

2. **JDK 17** or higher
   - Included with Android Studio, or install separately

3. **Android SDK**
   - SDK Platform: Android 14 (API 34)
   - Build Tools: 34.0.0
   - (Android Studio will prompt to install these)

### Clone the Repository

```bash
git clone https://github.com/yourusername/CodeStudioIDE.git
cd CodeStudioIDE
```

### Build with Android Studio

1. **Open Project**
   - Launch Android Studio
   - Click "Open" and select the `CodeStudioIDE` folder
   - Wait for Gradle sync to complete

2. **Build Debug APK**
   - Go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK**
   - Go to `Build` → `Generate Signed Bundle / APK`
   - Select "APK"
   - Create or use existing keystore
   - Select "release" build variant
   - APK will be at: `app/build/outputs/apk/release/app-release.apk`

### Build with Command Line

#### Debug Build

```bash
# On Linux/macOS
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build

First, create a `keystore.properties` file in the project root:

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=/path/to/your/keystore.jks
```

Then add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other config
        }
    }
}
```

Then build:

```bash
# On Linux/macOS
./gradlew assembleRelease

# On Windows
gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Create Signing Key (First Time Only)

```bash
keytool -genkey -v -keystore codestudio-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias codestudio
```

Follow the prompts to set passwords and certificate info.

## 📖 Usage

### Getting Started

1. **Launch the App** - Opens to the default workspace
2. **Create a Project** - Tap the folder icon to browse or create files
3. **Open Files** - Tap any file in the explorer to open in editor
4. **Switch Files** - Use tabs at the top to switch between open files
5. **Save Files** - Tap the save icon or use auto-save

### Editor Shortcuts

| Action | Method |
|--------|--------|
| Save | Menu → Save |
| Undo | Menu → Undo |
| Redo | Menu → Redo |
| Find | Menu → Find |
| Settings | Menu → Settings |

### Terminal Commands

```bash
# Navigation
cd <path>     # Change directory
pwd           # Print working directory
ls            # List files

# File Operations
touch <file>  # Create empty file
mkdir <dir>   # Create directory
rm <file>     # Delete file
cat <file>    # Display file contents

# Utility
clear         # Clear terminal
echo <text>   # Print text
help          # Show help
```

### Web Preview

1. Open an HTML file
2. Tap "Preview" in bottom navigation
3. The page will load in the built-in browser
4. Use the URL bar to navigate to external sites

## ⚙️ Configuration

### Editor Settings

Access via **Settings** in the menu:

- **Font Size**: 10-32pt
- **Tab Size**: 2, 4, or 8 spaces
- **Theme**: Dark, Light, Monokai, Dracula, Nord, Solarized
- **Auto-Save**: Enable/disable automatic saving
- **Line Numbers**: Show/hide line numbers
- **Word Wrap**: Enable/disable text wrapping

### File Associations

Files are automatically associated with languages based on extension:

- `.py` → Python
- `.rs` → Rust
- `.lua` → Lua
- `.r`, `.R` → R
- `.html`, `.htm` → HTML
- `.css` → CSS
- `.js` → JavaScript
- `.ts` → TypeScript
- And many more...

## 🏗️ Project Structure

```
CodeStudioIDE/
├── app/
│   ├── src/main/
│   │   ├── java/com/codestudio/ide/
│   │   │   ├── CodeStudioApp.kt          # Application class
│   │   │   ├── data/                     # Database & preferences
│   │   │   ├── model/                    # Data models
│   │   │   ├── ui/                       # Activities & UI
│   │   │   └── utils/                    # Utilities
│   │   ├── res/
│   │   │   ├── drawable/                 # Icons & graphics
│   │   │   ├── layout/                   # XML layouts
│   │   │   ├── menu/                     # Menu definitions
│   │   │   ├── values/                   # Colors, strings, themes
│   │   │   └── xml/                      # Config files
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.20
- **UI**: Material Design 3, ViewBinding
- **Editor**: Sora Editor (io.github.Rosemoe.sora-editor)
- **Database**: Room 2.6.1
- **Web Server**: NanoHTTPD
- **Architecture**: MVVM with StateFlow
- **Async**: Kotlin Coroutines

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Sora Editor](https://github.com/Rosemoe/sora-editor) - Powerful code editor for Android
- [Material Design](https://material.io/) - Design system
- [VS Code](https://code.visualstudio.com/) - Design inspiration
- [Positron](https://github.com/posit-dev/positron) - UI inspiration

## 📞 Support

- **Issues**: [GitHub Issues](issues/)
- **Discussions**: [GitHub Discussions](discussions/)

---

<p align="center">
  Made with ❤️ for mobile developers
</p>
