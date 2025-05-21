# TAD - Terminal ASCII Display 3D Engine

A simple Java 3D engine that renders OBJ models in the terminal using Unicode Braille characters and ANSI colors. Supports camera movement via keyboard (WASD + arrow keys).

## Features

- Renders 3D OBJ models in the terminal using Braille characters.
- Supports colored output with ANSI escape codes.
- Camera movement and rotation via keyboard (WASD + arrow keys).
- Scene graph with hierarchical nodes and auto-update for animation.
- Cross-platform (Windows/Unix).

## Requirements

- Java 17 or higher
- Maven

## Usage

### 1. Build

```sh
mvn clean package
```

### 2. Run

```sh
mvn exec:java -Dexec.mainClass="ch.carlopezzotti.Main"
```

Or, if you prefer to run the compiled JAR:

```sh
java -cp target/tad-1.0-SNAPSHOT.jar ch.carlopezzotti.Main
```

### 3. Controls

- **W/S**: Zoom in/out (move camera forward/backward)
- **A/D**: Move camera left/right
- **Arrow Left/Right**: Rotate camera yaw
- **Arrow Up/Down**: Rotate camera pitch
- **ESC**: Exit

## Example

By default, the engine loads two OBJ files from `src/main/resources/scene/`:

- `cow.obj`
- `teapot.obj`

You should see a colored 3D rendering of these models in your terminal. The first model cycles through rainbow colors, and the second rotates automatically.

## Adding More Models

Place additional `.obj` files in `src/main/resources/scene/` and add their filenames to the `OBJ_FILES` array in [`Main.java`](src/main/java/ch/carlopezzotti/Main.java).

## Notes

- For best results on Windows, use Windows Terminal with the "Segoe UI Emoji" font.
- On Unix, the terminal is set to raw mode for better key capture; it will restore settings on exit.

## Project Structure

- [`Main.java`](src/main/java/ch/carlopezzotti/Main.java): Entry point, scene setup, and input handling.
- [`engine/`](src/main/java/ch/carlopezzotti/engine/): Core engine classes (rendering, display, scene graph).
- [`engine/helper/`](src/main/java/ch/carlopezzotti/engine/helper/): Math utilities (vectors, transforms).
- [`src/main/resources/scene/`](src/main/resources/scene/): OBJ models.

## License

