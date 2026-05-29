# 🏢 Employee Profile Management App
### Kotlin Multiplatform · Compose Multiplatform · Android & iOS

---

## 🚀 Tech Stack (2026)

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.21 (K2 compiler) |
| UI | Compose Multiplatform 1.8.1 (iOS Stable) |
| Database | Room KMP 2.7.1 + BundledSQLiteDriver |
| DI | Koin 4.1.0 |
| Navigation | Navigation Compose 2.9.0 |
| Async | Kotlin Coroutines 1.10.2 + StateFlow |
| Image Loading | Coil3 KMP 3.2.0 |
| Date/Time | kotlinx-datetime 0.6.2 |
| DataStore | Jetpack DataStore 1.1.7 (Preferences) |
| Build System | Gradle 9 + Version Catalog (libs.versions.toml) |
| Min SDK | Android 26 · iOS 16+ |

---

## 📐 Architecture

```
composeApp/
├── commonMain/           # 90% of codebase
│   ├── data/
│   │   ├── db/           # Room entities, DAO, mapper, DatabaseFactory (expect)
│   │   └── repository/   # Repository interface + impl
│   ├── domain/
│   │   ├── model/        # Employee, Department, Skill, enums, FilterState
│   │   └── usecase/      # DSA: DuplicateDetector, topNBySalary, UndoDeleteStack, FormValidation
│   ├── presentation/
│   │   ├── components/   # Shimmer, EmployeeCard, States (Empty/Error)
│   │   ├── screens/      # List, Form, Detail, TopEarners
│   │   ├── theme/        # Material3 light/dark ColorScheme, Typography, Shapes
│   │   ├── viewmodel/    # EmployeeListViewModel, EmployeeFormViewModel
│   │   └── AppNavigation.kt
│   └── di/              # Koin modules (repository, domain, viewmodel)
├── androidMain/         # Android actual: DatabaseFactory, ImagePicker, DI, MainActivity
└── iosMain/             # iOS actual: DatabaseFactory, MainViewController
```

**Pattern:** MVVM + Repository with unidirectional data flow via `StateFlow` / `combine()`.

---

## ⚙️ Algorithms & Data Structures

### 6.1 Duplicate Detection — `HashSet` O(1)
- Two in-memory `HashSet<String>` (emails + normalised phones)
- Populated from DB on startup · O(n) once
- All subsequent checks: **O(1) average** via `HashSet.contains()`
- Auto-updated on every insert / update / delete

### 6.2 Top-N Earners — Min-Heap O(n log k)
```kotlin
// Min-heap of size k — dramatically faster than full sort when k << n
// Time: O(n log k)  |  Space: O(k)
fun topNBySalary(employees: List<Employee>, n: Int = 5): List<Employee>
```
- Configurable N via slider (1–10), reactively recomputed
- Why min-heap? Full sort = O(n log n). With n=5, m=500 → heap ~100× fewer comparisons.

### 6.3 Undo Delete — Stack O(1) push/pop
- `ArrayDeque<Employee>` used as LIFO stack
- Max depth: 10 · Snackbar shown for 5 s
- Both push and pop: **O(1) amortised**

---

## ✅ Features Checklist

### Section 1 – Employee Form
- [x] Full Name (min 3 chars, no duplicate names, whitespace trimmed)
- [x] Email (regex validated, lowercased before store)
- [x] Phone (10 digits only, normalised: strip +/spaces/dashes)
- [x] Address (min 6 chars, 4-line multi-line)
- [x] Gender (Radio Button Group – Male / Female / Prefer not to say)
- [x] Department (ExposedDropdown – enum-driven, no hardcoded strings)
- [x] Skills (multi-select FilterChip group – at least 1 required)
- [x] Employment Type (ExposedDropdown)
- [x] Is Active (Switch, default ON)
- [x] Joining Date (DatePickerDialog, not future date, format DD MMM YYYY)
- [x] Salary (numeric, positive, currency prefix ₹)
- [x] Blur validation on every field
- [x] Submit disabled until form is valid

### Section 2 – Profile Image & Documents
- [x] Bottom sheet: Take Photo / Choose from Gallery
- [x] Runtime permissions (Camera + READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE)
- [x] Circular avatar 80dp with camera icon overlay
- [x] Image stored as local file path (not Base64)
- [x] Document upload (PDF/DOC/DOCX)
- [x] File type icon, name, size, remove (×) button
- [x] 5 MB size validation with snackbar error
- [x] Document metadata (name, size, MIME type) stored in DB

### Section 3 – Room KMP
- [x] Room 2.7.1 KMP with BundledSQLiteDriver
- [x] `@TypeConverter` List<String> ↔ comma-separated String (skills)
- [x] `normalizedPhone` stored column for duplicate detection
- [x] `createdAt` / `updatedAt` epoch millis
- [x] All DB ops on background threads via coroutines
- [x] `Flow` from DAO → StateFlow in ViewModel

### Section 4 – Search, Filter & Sort
- [x] Always-visible SearchBar
- [x] Searches Full Name + Email + Department simultaneously
- [x] Text highlight in results (yellow background via AnnotatedString)
- [x] Filter bottom sheet: Department (multi), Status, Employment Type
- [x] AND logic for multiple filters
- [x] Active filter badge count
- [x] Clear All Filters
- [x] Sort: Name A-Z/Z-A, Date Newest/Oldest, Salary High/Low
- [x] Sort persists for session (ViewModel state)
- [x] Reactive `combine()` on search + filter + sort Flows
- [x] Debounce 300ms on search

### Section 6 – Algorithms
- [x] 6.1 HashMap duplicate detection (email + phone HashSets)
- [x] 6.2 Min-Heap top-N salaries with configurable N (slider)
- [x] 6.3 Undo delete via ArrayDeque Stack with Snackbar

### Bonus Features
- [x] Unit Tests (26 tests – DSA + validation + phone normalisation)
- [x] Koin DI (commonMain modules + platform modules)
- [x] Dark Mode (Material3 light/dark ColorScheme)
- [x] Shimmer loading states
- [x] Empty state, no-results state, error state
- [x] Animated list item enter/exit (AnimatedVisibility + AnimatedContent)
- [x] Long-press context menu (Edit / View Details / Delete)
- [x] Delete confirmation dialog
- [x] Employee Detail screen with full profile view

---

## 🏗️ Setup & Build

### Prerequisites
- Android Studio Narwhal (2025.1.1+) or IntelliJ IDEA 2026.1+
- KMP Plugin installed
- Xcode 16+ (for iOS builds)
- JDK 17

### Android
```bash
./gradlew :composeApp:assembleDebug
# APK at composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and build for simulator.

### Tests
```bash
./gradlew :composeApp:allTests
```

---

## 📁 Project Structure

```
EmployeeApp/
├── composeApp/
│   ├── build.gradle.kts
│   ├── schemas/                     # Room schema exports
│   └── src/
│       ├── commonMain/kotlin/…      # Shared code (90%)
│       ├── androidMain/kotlin/…     # Android actuals
│       ├── iosMain/kotlin/…         # iOS actuals
│       └── commonTest/kotlin/…      # 26 unit tests
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift
│       ├── ContentView.swift
│       └── Info.plist
├── gradle/
│   └── libs.versions.toml           # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🎨 UI Design Decisions

- **Material 3** with custom brand colour palette (Primary Blue `#1E6FFF`)
- **Shimmer skeleton** loading via animated `LinearGradient`
- **Colour-coded** department badges per department
- **Search text highlighting** using `AnnotatedString` + `SpanStyle`
- **Min-heap N slider** in Top Earners screen with reactive recompute
- **Rank medals** (gold/silver/bronze) for top-3 earners
- Smooth `AnimatedContent` transitions between list states

---

## 📝 Commit Convention

```
feat: add employee list with search/filter/sort
feat: implement Room KMP database with type converters
feat: add HashSet duplicate detection (O(1) lookup)
feat: implement min-heap top-N salary algorithm
feat: add undo delete with ArrayDeque stack
feat: add comprehensive unit tests (26 tests)
fix: normalise phone before duplicate check
chore: configure Koin DI modules for Android + iOS
```

---

*Built with ❤️ using Kotlin Multiplatform · May 2026*
