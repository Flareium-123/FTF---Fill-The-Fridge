# Fill The Fridge — Roguelike Sorting & Shelf-Stacking Puzzle

A standalone Java 17+ and libGDX (LWJGL3 desktop backend) roguelike puzzle game inspired by *Fill The Fridge*. Pack groceries from shopping baskets into a procedurally rendered tile-based refrigerator before they spoil on the counter, manage weight and support physics, dispose of trap items in a slot-limited trash bin, and collect unique roguelike powerups along a branching stage map. Sort liquids into the lower chiller, general groceries into the middle chiller, and meat into the top freezer; the final stage is a three-round fridge boss.

The desktop game targets **1920×1080**, starts maximized to fit the current display, and uses a 1280×720 logical canvas with a 16:9 fit viewport. This keeps menus, gameplay, and pointer input aligned at 1080p while letterboxing safely at other aspect ratios. Food and power-up illustrations use 512px Noto Emoji artwork; the full-screen fridge photograph is 1920×1080. Asset sources and licenses are listed in [`assets/credits.txt`](assets/credits.txt).

---

## 1. How to Run

### Prerequisites
- **Java 17+** (compatible with JDK 17 through JDK 25, including JetBrains Runtime bundled with IntelliJ IDEA)
- **Gradle 8.x / 9.x** (included via `./gradlew` and `gradlew.bat`)

### Run from IntelliJ IDEA
1. Open IntelliJ IDEA and select **File -> Open...**, then choose this project folder (`Final OOP`).
2. Allow IntelliJ IDEA to import the Gradle project (`build.gradle`).
3. Open the **Gradle** tool window on the right (`fill-the-fridge -> Tasks -> application -> run`) and double-click **`run`**, OR open [`src/main/java/com/fillthefridge/DesktopLauncher.java`](src/main/java/com/fillthefridge/DesktopLauncher.java) and click the green **Run** arrow next to `main`.

### Run from the Command Line (Gradle)
On Windows (PowerShell or Command Prompt):
```powershell
.\gradlew.bat run
```
On macOS / Linux:
```bash
./gradlew run
```

---

## 2. Controls

| Input | Action |
| :--- | :--- |
| **Left Click (Tray Item)** | Select or deselect an item in the outside shopping tray |
| **Hover (Fridge Grid)** | Display a live placement preview (`LEGAL` green, `UNSTABLE` red) and ghost sprite |
| **Left / Right (No Item Selected)** | Move the keyboard focus between tray items |
| **Space (No Item Selected)** | Select the focused tray item |
| **Arrow Keys (Item Selected)** | Move the placement preview across the fridge grid |
| **Space (Item Selected)** | Place the selected item at the keyboard cursor |
| **V** | Confirm and permanently lock legal placements |
| **X (Trap Selected)** | Send the selected trap item to the trash bin |
| **Left Click (Fridge Tile)** | Place the currently selected tray item at the hovered grid tile, or pick up an unconfirmed / trap item from the fridge |
| **Undo Button / U / Right Click** | Undo the last unconfirmed fridge placement (returns the item to the tray and resumes its expiry timer from its remaining time); every 3 successful undos cost 1 life. Rebind the keyboard shortcut in Settings. |
| **Left Click (Trash Bin)** | Trash the currently selected trap item (consumes 1 trash slot) |
| **Tutorial & Safe Storage (Main Menu)** | Play a step-by-step sample sort with storage and food-safety guidance |
| **F Key (or Freeze Button)** | Activate a **Deep Freeze** charge to pause the selected (or most urgent) tray item's timer for 3 seconds |
| **C Key** | Use one **FIFO Caddy** charge to pause the most urgent grocery for 4 seconds |
| **R Key** | Rotate one selected rectangular grocery per stage with **Reusable Containers** |
| **Shelf Rails (Power-Up)** | Click one locked fridge item per stage to return it to the tray and reposition it |
| **1 / 2 / 3 (or Left Click)** | Choose a reward route on the branching map after a stage |
| **Save & Return to Menu (stage-clear screen)** | Save the run checkpoint and return to the menu; the checkpoint includes the next route/reward choices |
| **Continue Saved Run (Main Menu, or C)** | Resume the saved route choice or the next boss phase |
| **Mouse Wheel / Up & Down** | Scroll the Credits screen |
| **Escape** | Return to the Main Menu |
| **Settings (Main Menu)** | Change resolution, toggle fullscreen and interface audio, and choose standard or high-quality texture filtering |

The interactive tutorial uses the live gameplay board, pauses for each short explanation, and advances when you click **CLICK TO CONTINUE**. Tutorial items have extended timers so you can practice without rushing.

---

## 3. Safe Storage & Roguelike Goal

The fridge has three color-coded zones: the top freezer for meat, the middle chiller for produce, dairy, bakery and leftovers, and the lower chiller for liquids. Items must fit entirely inside the matching compartment. Compartmentalizing helps players find food and reinforces separating raw from ready-to-eat items. The game's top meat zone is a simplified puzzle rule: in a real refrigerator, keep raw meat sealed on the lowest shelf to prevent drips reaching other food. Fresh food cannot be trashed; only trap items belong in the bin. Throwing away edible food wastes food and resources, while putting trash in the fridge risks contamination and takes up clean storage.

The last stage is a boss encounter with three health points. Each point is removed only after completing a separate full fridge sort.

The tutorial is a self-contained practice round: select sample foods, place them in the matching color-coded compartment, and discard a trap in the bin. It explains how categories help keep storage organized, why raw meat must be kept separate, and why fresh food should not be wasted or trash stored beside groceries. Before stage 8, a short burger-monster cutscene introduces the three-sort boss.

The grocery pool includes varied raw meats (chicken, steak, fish, shrimp, pork, and salmon), plus avocado, banana, cherries, grapes, strawberries, peaches, corn, mushrooms, watermelon, juice boxes, iced fruit drinks, and bubble tea. Food choices are randomized for each run, with duplicate item types avoided within a stage whenever a legal alternative fits. Catalog food, trap, power-up, and boss icons use Noto Emoji illustrations (Apache 2.0); attribution and license are in [`assets/credits.txt`](assets/credits.txt) and [`assets/noto-emoji-LICENSE.txt`](assets/noto-emoji-LICENSE.txt). High-resolution textures use mipmaps and linear filtering for cleaner scaling.

After each stage, an educational feedback screen explains the contract outcome and reinforces why safe storage matters. Contracts reward clean planning: prioritize the most urgent grocery, avoid invalid placements, or prevent spoilage. Completing a contract restores a life; **Efficient Packer** and **Meal Prep Plan** can add recovery and a category-synergy bonus.

Later stages add puzzle rules: from stage 4, one confirmed grocery starts as a fixed obstacle; from stage 5, a gold-marked shelf accepts only groceries up to its displayed weight limit. Stages also mark a specific grocery and slot that must be used. The current stage contract and active rules are displayed in the HUD; the “First to Safety” contract rewards placing the most urgent grocery before other food.

The branching map offers color-coded **Grocery Run**, **Market**, **Rest Stop**, and **Inspection** rooms. Each route gives a power-up: grocery runs add time, markets expand the next tray, and rest stops restore a life. Inspections are riskier (all groceries start with 1 second less) but completing the cold-chain contract grants an extra life. Power-ups are unique per run, with upgrade tiers available for selected builds.

Runs also unlock more power-ups permanently as you progress: Efficient Packer, Trap Scout, and Cold Snap II unlock after Floor 2; FIFO Caddy, Shelf Rails, and Deep Freeze II after Floor 4; Reusable Containers and Produce Saver II after Floor 6. Unlocks are saved locally and available in future runs. Several pairs grant additional effects, such as Cold Snap + Smart Thermostat, Produce Saver + Crisper Drawer, Deep Freeze + FIFO Caddy, and Efficient Packer + Meal Prep Plan.

The final route ends at the junk-food boss. Its three full-fridge sorts telegraph a different rule before each round: shorter grocery timers, a tighter visible tray, then an extra trap. The feedback screen teaches the food-safety lesson after each round and previews the next threat.

---

## 4. Text-Form Class Diagram

```text
+--------------------------------------------------------------------------------------------------+
|                                      com.fillthefridge.util                                      |
+--------------------------------------------------------------------------------------------------+
| <<interface>> Updatable          : + update(float deltaTime)                                     |
| <<interface>> Renderable         : + render(SpriteBatch, ShapeRenderer, BitmapFont)              |
| <<interface>> Placeable          : + getGridPosition(), + setGridPosition(GridPosition),         |
|                                    + getSize(), + isConfirmed(), + setConfirmed(boolean)         |
| <<interface>> Spoilable          : + getRemainingTime(), + getMaxShelfLife(), + updateTimer(),   |
|                                    + isWarningThresholdReached(), + isCriticalThresholdReached(),|
|                                    + isSpoiledAfterGrace(), + onTimerWarning()                   |
| <<interface>> StageGenerator     : + generateStage(StageConfig, ActivePowerUps) : GeneratedStage |
|                                    + verifySolvability(GeneratedStage, StageConfig) : boolean    |
| <<interface>> GameEventListener  : + onLivesChanged(...), + onStageCleared(...),                 |
|                                    + onItemSpoiled(...),                                        |
|                                    + onWarningMessage(...)                                       |
| GridPosition (final, immutable)  : - col: int, - row: int                                        |
| GridSize (final, immutable)      : - width: int, - height: int                                   |
| UndoStack<T>                     : - stack: Deque<T>                                             |
| Registry<K, V>                   : - entries: Map<K, V>                                          |
| InvalidPlacementException        : extends Exception                                             |
| AssetLoadException               : extends RuntimeException                                      |
+--------------------------------------------------------------------------------------------------+
                                                 ^
                                                 | implements / uses
+--------------------------------------------------------------------------------------------------+
|                                     com.fillthefridge.model                                      |
+--------------------------------------------------------------------------------------------------+
| <<abstract>> GameObject          : implements Updatable, Renderable                              |
|   |-- Fridge                     : *-- List<Shelf>, *-- Tile[][] tiles, o-- List<Item>           |
|   |-- Tray                       : o-- List<Item> activeItems, o-- Deque<Item> pendingQueue      |
|   |-- TrashBin                   : o-- List<Item> trashedItems, - maxSlots: int                  |
|   +-- <<abstract>> Item          : (in com.fillthefridge.items)                                  |
|                                                                                                  |
| GameState                        : *-- Fridge, *-- Tray, *-- TrashBin, *-- PlayerLives,          |
|                                    *-- ActivePowerUps, *-- UndoStack<PlacementAction>,           |
|                                    o-- List<GameEventListener>                                   |
| Shelf                            : - rowIndex: int, - columnSpan: int, - baseShelf: boolean      |
| Tile                             : - position: GridPosition, - state: TileState,                 |
|                                    - occupyingItem: Item                                         |
| PlayerLives                      : - currentLives: int, - maxLives: int,                         |
|                                    o-- List<GameEventListener>                                   |
| ActivePowerUps                   : - coldSnapBonusSeconds: float, - deepFreezeCharges: int,      |
|                                    - ghostFitEnabled: boolean, - sniffTestEnabled: boolean,      |
|                                    - extraTrashSlots: int, - extraTrayCapacity: int               |
| StageConfig (final, immutable)   : stage scaling parameters & seed                               |
| PlacementAction (final, immut.)  : - item: Item, - position: GridPosition                        |
| <<enum>> ItemCategory            : VEGETABLE, MEAT, DAIRY, MILK, BAKERY, PANTRY, LEFTOVER, TRAP  |
| <<enum>> TileState               : EMPTY, OCCUPIED_UNCONFIRMED, OCCUPIED_CONFIRMED,              |
|                                    TRAP_OCCUPIED                                                 |
| <<enum>> PlacementResult         : LEGAL, UNSTABLE                                               |
| <<enum>> GamePhase               : MAIN_MENU, PLAYING, STAGE_CLEAR, MAP_SELECTION,               |
|                                    EDUCATIONAL_FEEDBACK, BOSS_INTRO, GAME_OVER, RUN_COMPLETE     |
+--------------------------------------------------------------------------------------------------+
                                                 ^
                                                 | extends / implements
+--------------------------------------------------------------------------------------------------+
|                                     com.fillthefridge.items                                      |
+--------------------------------------------------------------------------------------------------+
| <<abstract>> Item extends GameObject implements Placeable, Spoilable                             |
|   + {abstract} onTimerWarning() : String                                                         |
|   + {abstract} canSupport(Item aboveItem) : boolean                                              |
|   + {abstract} onStackedUpon(Item aboveItem, Fridge fridge) : PlacementResult                    |
|   + {abstract} createFreshCopy() : Item                                                          |
|   |                                                                                              |
|   |-- Vegetable                  : lettuce, cabbage, carrot, tomato,                             |
|   |                                broccoli, apple                                               |
|   |-- Meat                       : whole_chicken, steak, fish, sausage                           |
|   |-- Dairy                      : yogurt_cup, cheese, butter, egg_carton                        |
|   |-- Milk                       : milk carton                                                  |
|   |-- BakeryItem                 : bread, pizza_box, jam_jar, juice_bottle, soda_can, juice_box, |
|   |                                cup_with_straw, bubble_tea                                    |
|   |-- Leftover                   : leftovers_container, soup_container, takeout_box              |
|   +-- TrapItem                   : empty_milk_carton, empty_can, banana_peel, plastic_bag        |
|                                                                                                  |
| ItemCatalog                      : factory & template registry for all 27 catalog items          |
+--------------------------------------------------------------------------------------------------+
                                                 ^
                                                 |
+--------------------------------------------------------------------------------------------------+
|                                    com.fillthefridge.powerups                                    |
+--------------------------------------------------------------------------------------------------+
| <<abstract>> PowerUp             : + {abstract} apply(GameState gameState)                       |
|   |-- ColdSnap / DeepFreeze / GhostFit / SniffTest / TrashCompactor / SecondWind                 |
|   |-- SmartThermostat / MealPrepPlan / EfficientPacker / TrapScout / FifoCaddy                   |
|   +-- ShelfRails / ReusableContainers / CrisperDrawer / ProduceSaver / ReinforcedShelves        |
| PowerUpPool                      : Unique run offers with selected upgrade tiers                 |
+--------------------------------------------------------------------------------------------------+
                                                 ^
                                                 |
+--------------------------------------------------------------------------------------------------+
|                        com.fillthefridge.logic / assets / ui / root                              |
+--------------------------------------------------------------------------------------------------+
| <<interface>> DifficultyScalingStrategy : + createStageConfig(int stage, long seed)              |
|   +-- ProgressiveDifficultyStrategy     : Priority-ordered stage difficulty scaling              |
| SolvableStageGenerator                  : implements StageGenerator (bottom-up gravity tiling)   |
| GeneratedStage                          : holds verified solutionPlacements + stageItems         |
| GameController                          : coordinates placement, undo, lock, and powerups       |
| AssetManager (assets)                   : centralized Registry<String, Texture> + credits reader |
| ProceduralFridgeRenderer (ui)           : procedural cabinet, shelves, door, light gradient      |
| HudRenderer (ui)                        : top HUD, lives, baskets, timer bars, stage-clear toast |
| BaseScreen (ui)                         : abstract base screen with smooth fade transitions      |
|   |-- MainMenuScreen                    |-- PowerUpSelectionScreen      |-- CreditsScreen        |
|   |-- GameplayScreen (GameEventListener)|-- GameOverScreen                                       |
| FillTheFridgeGame & DesktopLauncher     : LWJGL3 application entry point                         |
+--------------------------------------------------------------------------------------------------+
```

---

## 5. OOP Pillars & Design Patterns Mapping Table

| OOP Pillar / Requirement | Exact Classes & Interfaces Demonstrating It | How It Is Demonstrated |
| :--- | :--- | :--- |
| **Abstraction** | [`GameObject`](src/main/java/com/fillthefridge/model/GameObject.java), [`Item`](src/main/java/com/fillthefridge/items/Item.java), [`PowerUp`](src/main/java/com/fillthefridge/powerups/PowerUp.java), [`BaseScreen`](src/main/java/com/fillthefridge/ui/BaseScreen.java) | Abstract base classes define shared state and contracts (`update`, `render`, `onTimerWarning`, `canSupport`, `onStackedUpon`, `apply`) while deferring concrete behavior to subclasses. |
| **Inheritance** | [`Vegetable`](src/main/java/com/fillthefridge/items/Vegetable.java), [`Meat`](src/main/java/com/fillthefridge/items/Meat.java), [`Dairy`](src/main/java/com/fillthefridge/items/Dairy.java), [`Milk`](src/main/java/com/fillthefridge/items/Milk.java), [`BakeryItem`](src/main/java/com/fillthefridge/items/BakeryItem.java), [`Leftover`](src/main/java/com/fillthefridge/items/Leftover.java), [`TrapItem`](src/main/java/com/fillthefridge/items/TrapItem.java) extend [`Item`](src/main/java/com/fillthefridge/items/Item.java); [`ColdSnapPowerUp`](src/main/java/com/fillthefridge/powerups/ColdSnapPowerUp.java), [`DeepFreezePowerUp`](src/main/java/com/fillthefridge/powerups/DeepFreezePowerUp.java), [`GhostFitPowerUp`](src/main/java/com/fillthefridge/powerups/GhostFitPowerUp.java), [`SniffTestPowerUp`](src/main/java/com/fillthefridge/powerups/SniffTestPowerUp.java), [`SecondWindPowerUp`](src/main/java/com/fillthefridge/powerups/SecondWindPowerUp.java), and the new route, utility, and upgrade power-ups extend [`PowerUp`](src/main/java/com/fillthefridge/powerups/PowerUp.java) | Specialized grocery categories and powerup types inherit common spatial, timer, and rendering state while customizing domain rules. |
| **Polymorphism** | [`Item.onTimerWarning()`](src/main/java/com/fillthefridge/items/Item.java), [`Item.canSupport(Item)`](src/main/java/com/fillthefridge/items/Item.java), [`Item.onStackedUpon(Item, Fridge)`](src/main/java/com/fillthefridge/items/Item.java), [`PowerUp.apply(GameState)`](src/main/java/com/fillthefridge/powerups/PowerUp.java) | `Fridge.evaluatePlacement` invokes `belowItem.onStackedUpon(item, this)` and `belowItem.canSupport(item)` through the `Item` base type; `GameController` invokes `item.onTimerWarning()` and `chosenPowerUp.apply(gameState)` polymorphically. |
| **Encapsulation** | [`GridPosition`](src/main/java/com/fillthefridge/util/GridPosition.java), [`GridSize`](src/main/java/com/fillthefridge/util/GridSize.java), [`StageConfig`](src/main/java/com/fillthefridge/model/StageConfig.java), [`PlacementAction`](src/main/java/com/fillthefridge/model/PlacementAction.java), [`GeneratedStage`](src/main/java/com/fillthefridge/logic/GeneratedStage.java), [`PlayerLives`](src/main/java/com/fillthefridge/model/PlayerLives.java) | All fields across all classes are `private`; value objects (`GridPosition`, `GridSize`, `StageConfig`, `PlacementAction`) are `final` and immutable; collections are exposed via `Collections.unmodifiableList(...)` / `List.copyOf(...)`. |
| **Interfaces** | [`Updatable`](src/main/java/com/fillthefridge/util/Updatable.java), [`Renderable`](src/main/java/com/fillthefridge/util/Renderable.java), [`Placeable`](src/main/java/com/fillthefridge/util/Placeable.java), [`Spoilable`](src/main/java/com/fillthefridge/util/Spoilable.java), [`StageGenerator`](src/main/java/com/fillthefridge/util/StageGenerator.java), [`DifficultyScalingStrategy`](src/main/java/com/fillthefridge/logic/DifficultyScalingStrategy.java), [`GameEventListener`](src/main/java/com/fillthefridge/util/GameEventListener.java) | Capability interfaces decouple update/render loops, grid placement, spoilage timers, stage generation, difficulty scaling, and UI event notifications. |
| **Composition** | [`Fridge`](src/main/java/com/fillthefridge/model/Fridge.java) (`List<Shelf>` + `Tile[][]`), [`GameState`](src/main/java/com/fillthefridge/model/GameState.java) (`Fridge`, `Tray`, `TrashBin`, `PlayerLives`, `ActivePowerUps`, `UndoStack<PlacementAction>`) | Complex domain aggregates are composed of focused collaborating objects rather than monolithic classes. |
| **Enums** | [`ItemCategory`](src/main/java/com/fillthefridge/model/ItemCategory.java), [`TileState`](src/main/java/com/fillthefridge/model/TileState.java), [`PlacementResult`](src/main/java/com/fillthefridge/model/PlacementResult.java), [`GamePhase`](src/main/java/com/fillthefridge/model/GamePhase.java) | Type-safe enumerations represent item categories, tile occupancy states, 3-state placement previews, and game flow phases. |
| **Custom Exceptions** | [`InvalidPlacementException`](src/main/java/com/fillthefridge/util/InvalidPlacementException.java), [`AssetLoadException`](src/main/java/com/fillthefridge/util/AssetLoadException.java) | Checked `InvalidPlacementException` handles illegal fridge placements and invalid trash bin operations; `AssetLoadException` handles missing/corrupt resource files. |
| **Collections & Generics** | [`UndoStack<T>`](src/main/java/com/fillthefridge/util/UndoStack.java), [`Registry<K, V>`](src/main/java/com/fillthefridge/util/Registry.java), `Deque<Item>`, `Set<Item>`, `List<PowerUp>` | Custom generic classes `UndoStack<T>` and `Registry<K, V>` and standard generic collections manage textures, undo history, and tray queues. |
| **Strategy Pattern** | [`DifficultyScalingStrategy`](src/main/java/com/fillthefridge/logic/DifficultyScalingStrategy.java), [`ProgressiveDifficultyStrategy`](src/main/java/com/fillthefridge/logic/ProgressiveDifficultyStrategy.java) | `GameController` delegates per-stage parameter calculation to an interchangeable `DifficultyScalingStrategy`. |
| **Observer Pattern** | [`GameEventListener`](src/main/java/com/fillthefridge/util/GameEventListener.java), [`PlayerLives`](src/main/java/com/fillthefridge/model/PlayerLives.java), [`GameState`](src/main/java/com/fillthefridge/model/GameState.java), [`GameplayScreen`](src/main/java/com/fillthefridge/ui/GameplayScreen.java) | `PlayerLives` and `GameState` broadcast life changes, spoilage, warnings, and stage clears to registered `GameEventListener` observers (`GameplayScreen`). |

---

## 6. Asset Inspection & Missing Assets List

### Inspected Files in `assets/items/`
All 38 image files discovered in `assets/items/` were inspected and indexed by [`AssetManager`](src/main/java/com/fillthefridge/assets/AssetManager.java):
- **Catalog Grocery Items (23/23 present)**: `whole_chicken.png`, `steak.png`, `fish.png`, `sausage.png`, `milk.png`, `yogurt_cup.png`, `cheese.png`, `butter.png`, `egg_carton.png`, `lettuce.png`, `carrot.png`, `tomato.png`, `broccoli.png`, `cabbage.png`, `apple.png`, `bread.png`, `pizza_box.png`, `jam_jar.png`, `juice_bottle.png`, `soda_can.png`, `leftovers_container.png`, `soup_container.png`, `takeout_box.png`
- **Catalog Trap Items (4/4 present)**: `empty_milk_carton.png`, `empty_can.png`, `banana_peel.png`, `plastic_bag.png`
- **Catalog Interface Icons (4/4 present)**: `trash_bin.png`, `heart.png`, `warning_icon.png`, `snowflake.png`
- **Fridge Illustration**: `fridge_bg.png` is used on the main menu; the playable fridge and compartment tiles are rendered procedurally by [`ProceduralFridgeRenderer`](src/main/java/com/fillthefridge/ui/ProceduralFridgeRenderer.java)
- **Additional Variant Files Present**: `cheese.jpg`, `leftovers.jpg`, `rawchicken.jpg`, `rawmeat.jpg`, `Raw_Fish_icon.jpg`, `yogurt.jpg`

### Missing Assets
- **None** (`0` missing assets). Every expected catalog filename in `assets/items/` is present.
- If any image file is ever removed from `assets/items/`, [`AssetManager`](src/main/java/com/fillthefridge/assets/AssetManager.java) first attempts name-similarity matching against remaining files and otherwise generates a clean colored rectangle placeholder with the item's display name rendered on top.

---

## 7. Credits (`assets/credits.txt`)

The Credits screen reads [`assets/credits.txt`](assets/credits.txt) dynamically at runtime:

```text
============================================================
               FILL THE FRIDGE - CREDITS
============================================================

Game Design & OOP Implementation:
- Built with Java 17+ and libGDX (LWJGL3 Desktop Backend)
- Procedural Fridge Renderer & Solvable Stage Generator

Icon Attributions (Flaticon - https://www.flaticon.com):
- fridge_bg.png : Fridge illustration from www.flaticon.com
- whole_chicken.png : Icon made by [Author Name] from www.flaticon.com
- steak.png : Icon made by [Author Name] from www.flaticon.com
- fish.png : Icon made by [Author Name] from www.flaticon.com
- sausage.png : Icon made by [Author Name] from www.flaticon.com
- milk.png : Icon made by [Author Name] from www.flaticon.com
- yogurt_cup.png : Icon made by [Author Name] from www.flaticon.com
- cheese.png : Icon made by [Author Name] from www.flaticon.com
- butter.png : Icon made by [Author Name] from www.flaticon.com
- egg_carton.png : Icon made by [Author Name] from www.flaticon.com
- lettuce.png : Icon made by [Author Name] from www.flaticon.com
- carrot.png : Icon made by [Author Name] from www.flaticon.com
- tomato.png : Icon made by [Author Name] from www.flaticon.com
- broccoli.png : Icon made by [Author Name] from www.flaticon.com
- cabbage.png : Icon made by [Author Name] from www.flaticon.com
- apple.png : Icon made by [Author Name] from www.flaticon.com
- bread.png : Icon made by [Author Name] from www.flaticon.com
- pizza_box.png : Icon made by [Author Name] from www.flaticon.com
- jam_jar.png : Icon made by [Author Name] from www.flaticon.com
- juice_bottle.png : Icon made by [Author Name] from www.flaticon.com
- soda_can.png : Icon made by [Author Name] from www.flaticon.com
- leftovers_container.png : Icon made by [Author Name] from www.flaticon.com
- soup_container.png : Icon made by [Author Name] from www.flaticon.com
- takeout_box.png : Icon made by [Author Name] from www.flaticon.com
- empty_milk_carton.png : Icon made by [Author Name] from www.flaticon.com
- empty_can.png : Icon made by [Author Name] from www.flaticon.com
- banana_peel.png : Icon made by [Author Name] from www.flaticon.com
- plastic_bag.png : Icon made by [Author Name] from www.flaticon.com
- trash_bin.png : Icon made by [Author Name] from www.flaticon.com
- heart.png : Icon made by [Author Name] from www.flaticon.com
- warning_icon.png : Icon made by [Author Name] from www.flaticon.com
- snowflake.png : Icon made by [Author Name] from www.flaticon.com
```

---

## 8. Assumptions & Design Decisions

1. **Asset Organization**: The provided images were found in the project root and copied into `assets/items/` (and `assets/`) so that standard `assets/items/` lookup works out of the box.
2. **Tray Capacity & Cart Queue**: Because short-life items like raw meat/fish have a 4-6s shelf life while a stage has 8-18 total items, the `Tray` displays up to `trayCapacity` (3-4) active items at a time, and remaining items wait in the Cart Queue until a slot opens in the tray. Timers tick only for items currently in the active tray.
3. **Solvability Preservation on Spoilage**: Winning a stage requires every fridge tile to be occupied by a legal non-trap item. When a required grocery spoils in the tray, a fresh replacement is added so the stage remains solvable if the player still has lives left.
4. **Protecting Fresh Groceries from Accidental Trashing**: Only trap items can be placed in the Trash Bin. Attempting to trash fresh groceries is rejected with a helpful HUD prompt so players cannot accidentally soft-lock a stage or waste food.
5. **Picking Up Traps from the Fridge**: Left-clicking a trap or unconfirmed item inside the fridge (when no tray item is selected) picks it up so the player can reposition it or discard a trap. Any unconfirmed items resting directly above a removed item automatically return to the tray so nothing floats in mid-air.
