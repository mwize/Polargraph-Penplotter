# Polargraph Pen Plotter

A Raspberry Pi–controlled polargraph (wall plotter) that reads SVG files and reproduces them on a vertical drawing surface using two stepper motors and a hanging pen gondola.

## How it works

A polargraph suspends a pen gondola from two strings that are wound around pulleys driven by stepper motors mounted at the top-left and top-right corners of the drawing area. By adjusting the length of each string independently the gondola can be moved to any point within the reachable area. The software converts an SVG image into a sequence of (x, y) coordinates and then translates each movement into the precise number of steps each motor must turn.

```
  [Motor L] ────────────────── [Motor R]
       \                           /
        \                         /
         \                       /
          \                     /
           ●  ← pen gondola
```

## Hardware

| Component | Details |
|---|---|
| Controller | Raspberry Pi (any model with GPIO) |
| Stepper motors | 2× (left + right), driven via step/dir/enable pins |
| Left motor pins | ENABLE: GPIO 4 · STEP: GPIO 18 · DIR: GPIO 24 |
| Right motor pins | ENABLE: GPIO 12 · STEP: GPIO 19 · DIR: GPIO 13 |
| Steps per mm | 25 (adjustable in `config.properties`) |
| Drawing area | 540 mm × 780 mm |
| Motor spacing | 540 mm (width of the drawing surface) |

### 3D-printed parts

The `3DPrintedParts/` directory contains STL files for printing the mechanical components:

| File | Description |
|---|---|
| `pen-holder.stl` | Main body of the pen gondola |
| `penholder-top.stl` | Top cap of the pen gondola |
| `top-left-corner.stl` | Corner bracket for the left motor mount |
| `top-right-corner.stl` | Corner bracket for the right motor mount |

## Software

The project is written in **Java 17** and built with **Maven**.

### Dependencies

| Library | Purpose |
|---|---|
| [Pi4J 2.6.1](https://pi4j.com/) | GPIO control (stepper motor step/dir/enable signals) |
| [jsoup 1.17.2](https://jsoup.org/) | SVG file parsing |

### Project structure

```
src/main/
├── java/de/mwize/
│   ├── Main.java               # Entry point: motor control, movement logic
│   ├── SVGParser.java          # Parses SVG elements into lists of (x,y) points
│   ├── SVGPathParser.java      # Handles SVG <path> d-attribute commands
│   ├── elements/
│   │   ├── Circle.java
│   │   ├── Ellipse.java
│   │   ├── Line.java
│   │   ├── Polygon.java
│   │   ├── Rectangle.java
│   │   ├── CubicBezierCurve.java
│   │   └── QuadraticBezierCurve.java
│   └── utils/
│       └── BezierUtils.java    # De Casteljau / arc-length helpers
└── resources/
    └── config.properties       # Hardware configuration (pins, dimensions, …)
```

### Supported SVG elements

`<path>`, `<rect>`, `<circle>`, `<ellipse>`, `<line>`, `<polygon>`, `<g>` (groups)

Supported path commands: `M/m`, `L/l`, `H/h`, `V/v`, `C/c`, `S/s`, `Q/q`, `T/t`, `Z/z`

## Getting started

### Prerequisites

- Raspberry Pi with Raspberry Pi OS
- Java 17 JDK (`sudo apt install openjdk-17-jdk`)
- Maven (`sudo apt install maven`)
- Hardware wired according to the pin table above

### Build

```bash
mvn package
```

This produces two JARs in `target/`:
- `PenPlotterSoftware-1.0-SNAPSHOT.jar` – library JAR (no dependencies)
- `PenPlotterSoftware-1.0-SNAPSHOT-jar-with-dependencies.jar` – **self-contained runnable JAR** (copy this to the Raspberry Pi)

### Configuration

All hardware parameters are in `src/main/resources/config.properties`. Edit this file before building – no source code changes are needed.

```properties
# Drawing area (mm)
width=540
height=780

# Home / start position (mm)
startX=270
startY=70

# Steps the motor must turn to move the belt 1 mm
stepsPerMM=25

# Left stepper motor GPIO pins (BCM numbering)
pin.left.enable=4
pin.left.step=18
pin.left.dir=24

# Right stepper motor GPIO pins (BCM numbering)
pin.right.enable=12
pin.right.step=19
pin.right.dir=13

# Default SVG file (used when no argument is given on the command line)
svg.file=/home/pi/Desktop/svg3.svg
```

### Run

Copy the fat JAR and your SVG file to the Raspberry Pi, then:

```bash
# Use the path from config.properties
java -jar PenPlotterSoftware-1.0-SNAPSHOT-jar-with-dependencies.jar

# Or pass the SVG path directly
java -jar PenPlotterSoftware-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/drawing.svg
```

The plotter will:
1. Parse the SVG and scale it to fit the drawing area.
2. Move the gondola to the start of the first path.
3. Draw all paths in sequence.
4. Return the gondola to the home position when finished.

## License

See [LICENSE](LICENSE).
