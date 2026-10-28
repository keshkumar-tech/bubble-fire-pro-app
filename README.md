<div align="center">

# 🫧 Bubble Fire Pro

**A fast, colorful bubble shooter for Android — built 100% in Java with a custom Canvas game engine.**

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen)
![Gradle](https://img.shields.io/badge/Gradle-9.2.1-02303A?logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

</div>

---

## 📖 About

**Bubble Fire Pro** is a classic bubble shooter reimagined with power-ups, glossy animated bubbles and a 50-level progression system. Aim, shoot and match **3 or more bubbles of the same color** to pop them — clear the whole board before you run out of moves.

The entire game runs on a **custom `Canvas` renderer (`GameView`)** — no game engine, no third-party graphics library. Just pure Android `View` drawing, physics and game logic in Java.

> Fun fact: the whole game loop lives in one file — `GameView.java` — which handles aiming, shooting, collision, matching, gravity/disconnection detection, power-ups and effects.

---

## ✨ Features

### 🎯 Core Gameplay
- **Touch aiming & shooting** — drag to aim (dashed guide line), release to fire.
- **Smart collision** — bubbles bounce off the left/right walls and stick to the first bubble they touch.
- **Match-3 popping** — flood-fill search removes every connected bubble of the same color (3+).
- **Disconnection drop** — bubbles that lose connection to the top of the board automatically fall and pop, scoring bonus points.
- **Next-bubble preview** — see which color is coming next.
- **Swap button** — swap the current bubble with the next one when the color doesn't fit.

### 💥 Power-Ups
Power-ups are earned by making big pops, and they turn your next shot into a bomb:

| Power-up | Color | Effect | Earned by |
| :--- | :--- | :--- | :--- |
| 🖤 **Bomb** | Black | Clears a 5× bubble radius area | Popping 6+ bubbles (`GREAT!`) |
| 🟠 **Fire** | Orange | Clears a 5× bubble radius area | Popping 6+ bubbles (`GREAT!`) |
| 🟢 **Nuclear** | Green | Clears a **9× radius** area — board-wide blast | Popping 10+ bubbles (`EXCELLENT!!`) |
| 🟡 **Electric** | Yellow | Clears a 5× bubble radius area | Popping 10+ bubbles (`EXCELLENT!!`) |

Each power-up has a **count badge** on its button and is disabled (dimmed) when you have none left.

### 🗺️ Progression
- **50 levels** on a scrollable level map.
- Levels unlock one by one and your progress is **saved with `SharedPreferences`** — close the app and continue where you left off.
- **4 rotating board patterns** (looping by level): solid rows, massive diamond, V-shape, and a dense heart/shield.
- **Rising difficulty** — each level adds moves and increases the shot speed.

### 🎨 Polish
- Glossy, highlight-shaded bubbles drawn with `RadialGradient` shaders.
- Animated **pop effects** (expanding, fading circles).
- Floating **"GOOD" / "GREAT!" / "EXCELLENT!!"** score messages.
- Glowing aura around the shooter when a power-up is armed.
- Custom start screen, level-start dialog, pause dialog and win/lose dialogs.
- Lightweight sound effects via `ToneGenerator` (no audio assets required).

---

## 📸 Screenshots

> Add your own screenshots to a `docs/screenshots/` folder and reference them here:
>
> ```markdown
> | Start Screen | Level Map | Gameplay | Power-Up |
> | :---: | :---: | :---: | :---: |
> | ![Start](docs/screenshots/start.png) | ![Map](docs/screenshots/map.png) | ![Game](docs/screenshots/game.png) | ![Power](docs/screenshots/powerup.png) |
> ```

---

## 🎮 How to Play

1. **Tap Play** on the start screen and pick an unlocked level from the map.
2. **Drag** anywhere above the shooter to aim — a dashed line shows your trajectory.
3. **Release** to fire the bubble.
4. Match **3 or more same-colored bubbles** to pop them.
5. **Big pops give power-ups** — use them on tough clusters.
6. **Clear the entire board** to win the level. Run out of moves and it's game over.

### Scoring

| Action | Points |
| :--- | :--- |
| Popping a connected group (3+) | **25** per bubble |
| Bubbles dropped by disconnection | **15** per bubble |
| Bubbles destroyed by a power-up | **30** per bubble |

### Performance ranks
| Popped in one shot | Message | Reward |
| :--- | :--- | :--- |
| 3 – 5 | `GOOD` | — |
| 6 – 9 | `GREAT!` | 🖤 Bomb + 🟠 Fire |
| 10+ | `EXCELLENT!!` | 🟢 Nuclear + 🟡 Electric |

---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | **Java 11** |
| Game rendering | Android `Canvas` / `Paint` / `RadialGradient` (custom `View`) |
| UI | XML layouts, `AppCompatActivity`, `ConstraintLayout`, `GridLayout`, `Dialog` |
| Persistence | `SharedPreferences` (`BubbleFirePrefs`) |
| Audio | `android.media.ToneGenerator` |
| Build | Gradle **9.2.1** + Android Gradle Plugin **9.0.1** (Kotlin DSL + Version Catalog) |
| Libraries | AndroidX AppCompat, Material Components, ConstraintLayout, Activity |

---

## 📂 Project Structure

```
BubbleFirePro/
├── app/
│   ├── build.gradle.kts                  # App module config (namespace, SDKs, deps)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/bubblefirepro/
│       │   │   ├── MainActivity.java     # UI, dialogs, level map, power-up buttons, sounds
│       │   │   └── GameView.java         # The game engine: aim, fire, collide, match, draw
│       │   └── res/
│       │       ├── drawable/             # Bubbles, buttons, badges, progress bars
│       │       ├── layout/               # activity_main, level_map, dialogs
│       │       ├── mipmap-*/             # Launcher icons
│       │       ├── values/               # strings, colors, themes
│       │       └── xml/                  # backup & data-extraction rules
│       ├── test/                         # JUnit unit tests
│       └── androidTest/                  # Espresso instrumented tests
├── gradle/
│   ├── libs.versions.toml                # Centralized dependency versions
│   └── wrapper/
├── build.gradle.kts                      # Root build file
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

### Key classes

| Class | Responsibility |
| :--- | :--- |
| `MainActivity` | Binds UI, drives screens (start → level map → game), shows dialogs, stores progress, plays sounds. Implements `GameView.GameListener`. |
| `GameView` | The heart of the game. Owns the game loop (`onDraw` → `update` → `invalidate`), physics, matching (`checkMatches`), gravity (`dropDisconnected`), power-ups (`applyPowerUp`) and rendering. |
| `GameView.GameListener` | Callback interface: `onScoreChanged`, `onLevelChanged`, `onMovesChanged`, `onLevelWin`, `onGameOver`, `onPerformanceAchieved`, `playSound`. |
| `Bubble` / `PoppingBubble` / `Message` | Lightweight data classes for the board, pop animations and floating text. |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (latest stable — Narwhal or newer recommended)
- **JDK 17+** (required by AGP 9.x)
- **Android SDK Platform 36**
- An emulator or a physical device running **Android 7.0 (API 24)** or higher

### Clone the repository

```bash
git clone https://github.com/keshkumar-tech/bubble-fire-pro-app.git
cd bubble-fire-pro-app
```

### Run the app

**Option 1 — Android Studio**
1. `File → Open…` and select the project folder.
2. Let Gradle sync finish.
3. Press **Run ▶** (`Shift + F10`).

**Option 2 — Command line**

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build a release APK

```bash
./gradlew assembleRelease
```

> ⚠️ Remember to configure your own signing config in `app/build.gradle.kts` before publishing to the Play Store.

---

## 🧩 Customizing the Game

Most of the fun is one constant away. Open `GameView.java`:

| What to change | Where |
| :--- | :--- |
| Bubble colors | `colors[]` array (line ~43) |
| Bubble size | `BUBBLE_RADIUS` (default `42f`) |
| Base shot speed | `currentSpeed` in `initLevel()` (default `120f + level * 2`) |
| Moves per level | `this.moves = 20 + (level / 2)` in `initLevel()` |
| Number of board rows | `int rows = 18` in `initLevel()` |
| Board patterns | `patternType = level % 4` block in `initLevel()` |
| Power-up blast radius | `range` in `applyPowerUp()` (`5×` normal, `9×` nuclear) |
| Match threshold | `matches.size() >= 3` in `checkMatches()` |
| Points per pop / drop / blast | `25` / `15` / `30` |
| Number of levels | loop `for (int i = 1; i <= 50; i++)` in `MainActivity.showLevelMap()` |
| Sound effects | `playSound()` switch in `MainActivity` |

---

## 🧠 How the Engine Works

```
        ┌─────────────── onDraw() ───────────────┐
        │  1. Paint background & divider         │
        │  2. update()  → move flying bubble     │
        │  3. Draw aim line (while aiming)       │
        │  4. Draw static board                  │
        │  5. Animate popping bubbles & messages │
        │  6. Draw current + next bubble         │
        │  7. invalidate() → next frame          │
        └────────────────────────────────────────┘
```

1. **Input** — `onTouchEvent` records the aim point on `ACTION_DOWN/MOVE`; on `ACTION_UP` it computes a normalized velocity vector and sets `isFiring = true`.
2. **Movement** — `update()` advances the flying bubble each frame and mirrors `vx` on wall contact.
3. **Sticking** — collision is a simple distance check against every static bubble (or the top wall). On contact, `stickBubble()` locks it into the board.
4. **Resolution** — either `applyPowerUp()` (if one was armed) or `checkMatches()` runs a BFS flood fill over same-colored neighbours.
5. **Gravity** — `dropDisconnected()` BFS-searches from the top row; anything unreachable falls.
6. **Feedback** — pop animations, floating messages, score callbacks, then a move is consumed. Empty board → **win**, zero moves → **game over**.

---

## 🤝 Contributing

Contributions, ideas and bug reports are welcome!

1. Fork the repo
2. Create your branch: `git checkout -b feature/my-awesome-idea`
3. Commit your changes: `git commit -m "Add my awesome idea"`
4. Push: `git push origin feature/my-awesome-idea`
5. Open a **Pull Request**

Please keep the code style consistent with the existing Java sources.

---

## 📄 License

This project is open source and available under the **MIT License**.

---

<div align="center">

Made with ❤️ and a lot of ☕ by [Kesh Kumar](https://github.com/keshkumar-tech)

⭐ **If you like this game, give the repo a star!** ⭐

</div>
