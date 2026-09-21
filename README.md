# MealMate 🍴

> **Plan. Eat. Save.**

![Android CI](https://github.com/Oratile122/MealMate/actions/workflows/workflow.yml/badge.svg)

A student meal planning app built for OPSC6312 Part 2.

---

## 🎯 Purpose of the App

MealMate helps students manage their food budget and plan meals. It addresses the common problem of poor budgeting and unhealthy eating among university students by providing:

- **Weekly meal planning** with cost and calorie tracking
- **Budget management** with real-time spending calculations
- **Persistent shopping lists** tied to the user's account
- **Offline capability** via RoomDB so meals work without internet

The app targets South African students managing tight food budgets (default: R 500/week).

---

## 🎨 Design Considerations

### Architecture
- **MVVM-inspired** structure with Activity-driven logic
- **Offline-first**: RoomDB stores user data and meals locally
- **Online-sync**: Retrofit syncs with the .NET API and SQL Server

### Technical Decisions

| Decision | Reason |
|----------|--------|
| **RoomDB** | Offline persistence across app restarts and logout cycles |
| **Retrofit** | Type-safe HTTP client for the REST API |
| **KSP** | Modern replacement for KAPT when using Room |
| **Coroutines** | Clean asynchronous database and network operations |
| **Material Components** | Consistent UI matching the Figma prototype |

### UI/UX Design
- All 8 screens match the Figma prototypes from Part 1
- **Rands (R)** used for all currency (South African context)
- Bottom navigation for quick access to Home, Planner, Shop, and Profile
- Persistent state via SharedPreferences for session, RoomDB for data

### Authentication
- Passwords hashed with **BCrypt** on the .NET backend
- Session stored in SharedPreferences (`email`, `displayName`)
- Data survives logout — only the `isLoggedIn` flag is reset

---

## 🔧 GitHub Utilisation

**Repository:** [https://github.com/Oratile122/MealMate](https://github.com/Oratile122/MealMate)

- All source code committed with descriptive messages
- `.gitignore` excludes build artefacts
- Project structured for clean collaboration

---
## 🎥 Demo Video

📹 **[Watch the MealMate demo video](https://youtu.be/c22LDSKUfMs?si=tRd3mSZ2IGgIef4n)**

---

## ⚙️ GitHub Actions Utilisation

![Android CI](https://github.com/Oratile122/MealMate/actions/workflows/workflow.yml/badge.svg)

This project uses **GitHub Actions** to automatically:

- ✅ Build the Android app on every push to `main`
- ✅ Run all **10 unit tests** on a clean build environment
- ✅ Verify the project compiles without local IDE dependencies

**Status:** [View all workflow runs →](https://github.com/Oratile122/MealMate/actions)

Workflow file: `.github/workflows/workflow.yml`

This ensures the app is portable — it doesn't just work on the developer's machine.

### What the Workflow Does

| Step | Action |
|------|--------|
| 1. Checkout | Downloads the latest code |
| 2. Setup Java 17 | Installs the correct JDK |
| 3. Grant permission | Makes `gradlew` executable |
| 4. Create local.properties | Points to the Android SDK |
| 5. Run tests | Executes `./gradlew test --no-daemon` |

All steps run on GitHub's clean Ubuntu machines — not on the developer's computer.

---

## 🧪 Unit Tests

The project includes **10 unit tests** covering:

| Test File | Tests |
|-----------|-------|
| `AuthResponseTest` | API response model |
| `LoginRequestTest` | Login request model |
| `RegisterRequestTest` | Registration request model |
| `UserEntityTest` | User database entity |
| `MealEntityTest` | Meal database entity |

**Run tests locally:**

```bash
./gradlew test