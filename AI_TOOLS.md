# AI Tools Disclosure

**Module:** OPSC6312POE
**Part:** Part 2 — App Prototype Development
**Group Members:** Oratile Morudi, Mbalenhle Mngomezulu, Busisiwe nyembe
**17:** September 2025

---

## Declaration

We declare that the code, design, and documentation submitted in this portfolio of evidence are our own work. AI tools were used **only as learning aids and debugging assistants** to support our understanding of concepts and to help identify and fix errors. No AI tool generated any part of the submitted code directly.

---

## AI Tools Used

### ChatGPT / Claude (Anthropic)

**Purpose:** Conceptual learning, debugging assistance, and error resolution.

**How it was used:**

- **Debugging errors** — AI helped interpret error messages from Android Studio (e.g., Gradle sync errors, Kotlin type inference errors, unresolved references, KSP/kapt configuration issues)
- **Explaining error causes** — When the app crashed, AI was used to understand *why* a crash occurred by interpreting Logcat output
- **Concept clarification** — Asking conceptual questions about Android architecture (e.g., "How does Room's `fallbackToDestructiveMigration()` work?", "What is the difference between KAPT and KSP?")
- **Configuration assistance** — Understanding how to correctly set up `build.gradle.kts` for RoomDB, Retrofit, and KSP
- **Understanding Kotlin syntax** — Clarifying coroutines, lifecycle scopes, and Kotlin features used in the app

**How it was NOT used:**

-  No code was copied directly from AI output into the final submission
-  No AI tool was asked to write complete features, Activities, or modules
-  AI was not used to design the app's architecture, database schema, or UI
- AI was not used to generate any final submitted file

### Google Search / Stack Overflow

**Purpose:** Looking up official API documentation, troubleshooting runtime errors, and finding solutions to common Android development problems.

This is standard practice in software development and was used alongside official Android Developer documentation.

---

## How AI-Assisted Learning Was Verified

Every concept, fix, or solution suggested by AI was:

1. **Understood** — the reasoning behind each suggestion was reviewed and confirmed
2. **Cross-referenced** — checked against official Android Developer documentation
3. **Applied manually** — the code was written by us, in our own structure
4. **Tested** — verified on a physical Android device
5. **Validated** — confirmed through 10 passing unit tests

---

## Examples of AI-Assisted Debugging

| Issue Encountered | How AI Helped | What We Did |
|-------------------|---------------|-------------|
| Gradle sync error (`HasConvention`) | Explained it was a version mismatch between Gradle and AGP | We changed the Gradle version to 8.7 in `gradle-wrapper.properties` |
| `Unresolved reference 'ksp'` | Explained that KSP plugin needs correct plugin block order | We corrected the `build.gradle.kts` configuration |
| Room database not compiling | Explained KSP annotation processor setup | We added the correct `add("ksp", ...)` dependency line |
| Black screen after login | Suggested checking `onResume()` for infinite `recreate()` loop | We removed the recursive call and used a proper data reload method |
| Email mismatch in database | Suggested logging the email at save and load time | We added debug logging and verified the flow |
| `ActivityNotFoundException` | Pointed to missing `AndroidManifest.xml` declaration | We registered the missing activity in the manifest |

---

## Team Contribution

| Member | Role | AI Usage |
|------|------|----------|
| **Oratile Morudi** | Lead Developer | Used AI for conceptual learning and debugging support during Android development |
| **Mbalenhle Mngomezulu** | UI/UX Assistant | Did not use AI tools |
| **Busisiwe nyembe** | Documentation & Testing | Did not use AI tools |

---

## Academic Integrity Statement

- All submitted code is our own work.
- AI tools served the same role as a textbook, tutorial, or Stack Overflow discussion — a learning and debugging resource.
- No AI tool generated any submitted file directly.
- All AI-assisted learning was applied critically and verified independently through testing.
- This disclosure is provided in full transparency to meet the assessment's academic integrity requirements.

---

## AI Tools NOT Used For

-  Writing the Kotlin source code
-  Designing the UI or the 8 screens
-  Creating the database schema or Room entities
-  Writing the README content
-  Recording or editing the demo video

---

**Signed:** Oratile Morudi, Mbalenhle Mngomezulu, Busisiwe nyembe

**17:** September 2025