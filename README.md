
# TAD — Terminal ASCII Display 3D Engine

A lightweight Java 3D engine that renders OBJ models directly in your terminal using Unicode Braille characters and ANSI colors.  
Navigate in real time with keyboard controls, animate your scene graph, and extend with custom models and behaviors.

---

## 🚀 Features

- **Braille-based rendering**: Efficiently packs 2×4 pixel blocks into one Braille character for high-resolution terminal output.
- **ANSI color support**: Per-vertex and per-object coloring via ANSI escape codes.
- **Interactive camera**: Move and rotate the camera with WASD and arrow keys.
- **Scene graph**: Hierarchical `Node`-based structure with parent/child transforms.
- **Auto-update**: Per-node background threads for smooth, programmatic animations.
- **Cross-platform**: Works on Unix and Windows terminals (including Windows Terminal).

---

## 🛠 Requirements

- **Java 17** or higher
- **Maven** (for building and running via `mvn`)
- A terminal with Braille and ANSI support:
  - **Windows**: Windows Terminal with “Segoe UI Emoji” or similar font  
  - **Unix**: Most modern Linux/macOS terminals

---

## 🎯 Getting Started

### 1. Clone & Build

```bash
git clone https://github.com/your-user/tad-engine.git
cd tad-engine
mvn clean package
````

### 2. Run

#### Via Maven

```bash
mvn exec:java \
  -Dexec.mainClass="ch.carlopezzotti.Main"
```

#### Via JAR

```bash
java -cp target/tad-1.0-SNAPSHOT.jar ch.carlopezzotti.Main
```

---

## 🎮 Controls

| Key       | Action                                   |
| --------- | ---------------------------------------- |
| **W / S** | Move camera forward / backward           |
| **A / D** | Strafe camera left / right               |
| **← / →** | Rotate camera yaw (pan left / right)     |
| **ESC**   | Exit application                         |
| **SPACE** | (Custom) Teleport nearest enemy to right |

---

## 📂 Project Structure

```
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── ch.carlopezzotti
│   │   │       ├── Main.java            # Entry point & input handling
│   │   │       └── engine
│   │   │           ├── Engine.java       # Core loop, projection, input
│   │   │           ├── Display.java      # Display interface
│   │   │           ├── BrailleDisplay.java # Terminal Braille renderer
│   │   │           ├── Camera.java       # Camera node subclass
│   │   │           ├── Node.java         # Scene graph node
│   │   │           ├── TreeScene.java    # Recursive scene renderer
│   │   │           └── KeyCaptureWindow.java # Keyboard listener window
│   │   └── resources
│   │       └── scene
│   │           ├── cow.obj
│   │           └── teapot.obj
└──
```

---

## ⚙️ Configuration & Extension

* **Adding models**

  1. Place `.obj` files in `src/main/resources/scene/`.
  2. Edit the `OBJ_FILES` array in `Main.java`’s `loadSimpleScene(...)`.

* **Custom animations**
  Use `node.startAutoUpdate(intervalMillis, node -> { ... })` to drive per-node logic.

* **Changing controls**
  Modify `Main.onKeyDown(...)` and `updateCamera(...)` for bespoke input mappings.

---

> Crafted with ❤️ by Carlo Pezzotti