# vispar - Movie & TV Series Streaming App

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/splash_logo.png" alt="vispar Logo" width="150"/>
</p>

vispar is a modern Android streaming application built with Jetpack Compose and Kotlin. The app provides a sleek user interface for browsing movies and TV series, playing videos, and customizing the viewing experience. With a focus on user experience and performance, vispar delivers high-quality streaming content in an intuitive and visually appealing interface.

## 🚀 Key Features

### 🎬 Content Discovery
- **Browse Movies & Series**: Attractive grid layout for discovering content
- **Powerful Search**: Search across both movies and TV series with real-time results
- **Detailed Information**: Comprehensive details for each movie and series including ratings, genres, and descriptions

### ▶️ Media Playback
- **Custom Video Player**: Built with ExoPlayer for smooth video playback
- **Multiple Quality Options**: Choose from various quality options for playback and download
- **Download Options**: Multiple download methods including browser, ADM, and VLC
- **Fullscreen Experience**: Optimized landscape mode for immersive viewing

### ❤️ Favorites Management
- **Save Content**: Add movies and series to your favorites list
- **Quick Access**: Easily access your saved content from the Favorites screen
- **Organize**: Remove items from favorites with individual delete options
- **Bulk Actions**: Clear all favorites with a single action

### 🎨 Personalization
- **Theme Customization**: Light, dark, and system themes with color options
- **Subtitle Settings**: Customize subtitle appearance (background, text color, border, size)
- **Liquid Glass UI**: A refractive, iOS-style glass interface built with [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) that adapts to all screen sizes

### 📱 User Experience
- **Intuitive Navigation**: Bottom navigation for mobile/tablet and sidebar navigation for TV
- **Error Handling**: Retry mechanisms for failed requests
- **Loading States**: Shimmer loading animations for smooth user experience
- **Season & Episode Management**: Easy navigation through TV series seasons and episodes

### 📺 Android TV Support
- **Optimized Layout**: Sidebar navigation for better TV experience
- **Full Screen Support**: Content fills the entire TV screen
- **Remote Control Navigation**: D-pad optimized navigation
- **Cross-Platform Compatibility**: Works on mobile, tablet, and TV with adaptive UI

## 🛠️ Tech Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern toolkit for building native UI
- **ExoPlayer** - Media playback library
- **Kotlin Serialization** - JSON serialization
- **Coil** - Image loading library
- **Liquid Glass (Backdrop)** - Real-time blur, lens refraction and specular highlights via [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)
- **Navigation Component** - For seamless screen transitions
- **ViewModel & LiveData** - For state management
- **Coroutines** - For asynchronous operations
- **OkHttp** - For network requests
- **Leanback** - Android TV support library

## 💧 Liquid Glass Design

The UI is built on a small design system in `app/src/main/java/com/fazli/vispar/ui/glass`:

- **AmbientBackground** – Apple-style neutral canvas: pure black in dark mode, white to system grey in light mode.
- **GlassSurface / glass()** – blurred, saturated and lens-refracted panels; nested controls refract their parent panel.
- **LiquidButton / LiquidIconButton** – capsule buttons that swell and follow the finger like a drop of water.
- **LiquidTabs** – the floating tab bar and segmented controls, with a draggable lens that stretches with velocity.
- **LiquidSlider** – a glass thumb that magnifies the track with chromatic aberration while dragged.
- **GlassScaffold** – floating glass top bar plus a progressive "scroll edge" blur under the status bar.
- **GlassDialog** – dialogs rendered in an in-window overlay so they can refract the whole screen.

The video player renders into a `TextureView`, so its glass controls refract the playing video.

### Options
- **Glass material**: Clear / Glass / Frosted / No transparency (fastest; default on Android TV)
- **Compact posters**, **haptic feedback**, monochrome or Apple system accent colours
- **Player**: autoplay, resume from last position, default playback speed (0.75x–2x), seek step
- **Continue watching** shelf on the Movies and Series tabs
- Clear image cache and watch history

### Languages
- Persian (right-to-left) and English (left-to-right), switchable in Settings; Persian is the default

### Android TV
- Launcher banner, glass sidebar, focus rings and lift on every control
- Remote-controlled player: OK = play/pause, ←/→ = seek, ↑/↓ = controls, media keys supported

Full blur needs Android 12+, refraction needs Android 13+. Older devices fall back to a frosted tint.

## 🏗️ Architecture

The app follows a clean architecture pattern with the following components:

- **UI Layer** - Jetpack Compose screens and components
- **Data Layer** - Models and repositories for data management
- **Domain Layer** - Business logic and use cases
- **Utils** - Helper classes and extensions
- **Navigation** - Single-activity architecture with Compose Navigation
- **State Management** - ViewModel for UI state handling

## Getting Started

### Prerequisites

- Android Studio Ladybug or later
- Android SDK API 37
- JDK 17+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/code3-dev/vispar.git
   ```

2. Open the project in Android Studio

3. Build and run the project

### Building

To build the debug APK:
```bash
./gradlew assembleDebug
```

To build the release APK:
```bash
./gradlew assembleRelease
```

### Fixing Gradle Wrapper Issues

If you encounter Gradle wrapper validation errors (especially in CI/CD environments), you can fix them using the provided scripts:

On Unix/Linux/macOS:
```bash
./scripts/fix-gradle-wrapper.sh
```

On Windows:
```cmd
scripts\fix-gradle-wrapper.bat
```

You can also verify the integrity of the Gradle wrapper:

On Unix/Linux/macOS:
```bash
./scripts/verify-gradle-wrapper.sh
```

On Windows:
```cmd
scripts\verify-gradle-wrapper.bat
```

These scripts will regenerate the Gradle wrapper checksums which are required for validation.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/fazli/vispar/
│   │   │   ├── components/           # Reusable UI components
│   │   │   ├── data/                 # Data layer
│   │   │   │   ├── model/            # Data models (Movie, Series, etc.)
│   │   │   │   └── repository/       # Data repositories
│   │   │   ├── navigation/           # Navigation graph and components
│   │   │   ├── screens/              # Compose screens
│   │   │   ├── ui/                   # UI components and theme
│   │   │   │   ├── movies/           # Movie-specific UI components
│   │   │   │   ├── search/           # Search-specific UI components
│   │   │   │   ├── series/           # Series-specific UI components
│   │   │   │   └── theme/            # Theme definitions and management
│   │   │   ├── utils/                # Utility classes
│   │   │   ├── MainActivity.kt       # Main application activity
│   │   │   └── VideoPlayerActivity.kt # Video player activity
│   │   └── res/                      # Resources
│   │       ├── drawable/             # Drawable resources
│   │       ├── values/               # Default resources
│   │       ├── values-television/    # TV-specific resources
│   │       └── ...                   # Other resources
│   └── test/                         # Unit tests
├── build.gradle.kts                  # App build configuration
└── ...
```

## Key Components

### Video Player

The app features a custom video player built with ExoPlayer that supports:

- Play/Pause controls
- Progress seeking
- Subtitle customization
- Fullscreen landscape mode

### Settings

Users can customize their experience through the settings screen:

- Theme mode (Light/Dark/System)
- Primary and secondary color selection
- Subtitle settings (colors and text size)

## 📱 App Screens

### 🎬 Movies Screen
- Browse latest movies in an attractive grid layout
- Pull-to-refresh functionality
- Infinite scrolling for loading more content
- Error handling with retry option
- Shimmer loading animations

### 📺 Series Screen
- Browse TV series with comprehensive information
- Season and episode organization
- Pull-to-refresh and infinite scrolling
- Error handling with retry option

### ❤️ Favorites Screen
- View all saved movies and series in one place
- Individual item deletion
- Bulk deletion of all favorites
- Direct navigation to content details
- Visual indicators for content type and ratings

### 🔍 Search Screen
- Real-time search across movies and series
- Instant results as you type
- Error handling with retry button
- Clear search functionality

### 📄 Single Movie Screen
- Detailed movie information
- Multiple quality options for playback
- Download options (Browser, ADM, VLC)
- Custom video player integration
- Add/remove from favorites

### 📺 Single Series Screen
- Comprehensive series details
- Season navigation and episode listing
- Multiple quality options per episode
- Download options for each episode
- Error handling with retry functionality
- Add/remove from favorites

### ⚙️ Settings Screen
- Theme customization (Light/Dark/System)
- Primary and secondary color selection
- Subtitle settings (colors and text size)
- Reset to default options

### 🏠 Navigation
The app uses adaptive navigation:
- **Mobile/Tablet**: Bottom navigation bar for switching between screens
- **TV**: Sidebar navigation for better remote control experience

## 📺 Android TV Remote Control Guide

### Navigation Controls
- **D-Pad (Arrow Keys)**: Navigate between UI elements, scroll through lists, and move focus
- **Select (OK) Button**: Confirm selections, open items, play/pause media
- **Back Button**: Navigate to previous screen or exit the app
- **Home Button**: Return to the main launcher

### Sidebar Navigation (TV Only)
- Use the **Up/Down** arrow keys to navigate between menu items in the sidebar
- Press **Select (OK)** to open the selected section (Movies, Series, Search, Settings)

### Content Browsing
- Use **Left/Right** arrow keys to navigate between items in a row
- Use **Up/Down** arrow keys to move between rows
- Press **Select (OK)** to open details for a selected movie or series

### Media Playback Controls
- **Play/Pause**: Press the **Select (OK)** button or **Play/Pause** button when focused on the player
- **Seek Forward/Backward**: Use the **Left/Right** arrow keys to skip 10 seconds
- **Volume Control**: Use the **Up/Down** arrow keys on the directional pad or volume buttons on the remote
- **Exit Fullscreen**: Press the **Back** button to exit fullscreen mode and return to the content details

### Search Functionality (TV)
- Navigate to the Search section using the sidebar
- The on-screen keyboard can be controlled with the D-pad
- **Up/Down/Left/Right**: Move between keyboard keys
- **Select (OK)**: Press the selected key
- **Backspace**: Press the back button to delete characters

### Settings Navigation (TV)
- In the Settings screen, use **Up/Down** to navigate between options
- For toggle switches, press **Select (OK)** to toggle on/off
- For color pickers and other options, press **Select (OK)** to open the selection dialog

### Favorites Management (TV)
- Access the Favorites screen through the Settings menu
- Navigate through saved items using the D-pad
- Press **Select (OK)** to open a saved item
- Use the context menu to remove individual items or clear all favorites

### General Tips for TV Navigation
1. **Focus Highlighting**: The currently selected item is highlighted with a border or background color
2. **Smooth Scrolling**: Content lists support smooth scrolling with the D-pad
3. **Quick Access**: The sidebar is always accessible for quick navigation between main sections
4. **Exit App**: Press the **Back** button repeatedly until you exit the app

## 🤝 Contributing

We welcome contributions to vispar! Here's how you can help:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

Please ensure your code follows the existing style and includes appropriate tests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [ExoPlayer](https://exoplayer.dev/) for media playback
- [Coil](https://coil-kt.github.io/coil/) for image loading
- [Material Design](https://m3.material.io/) for UI components
- [Leanback](https://developer.android.com/training/tv/start/start) for TV support

## 📞 Contact

For support or inquiries, please open an issue on GitHub.