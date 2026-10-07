# CoE: Chiko AI CFO & Financial Assistant 🚀

A next-generation wealth, expense, sales, and financial management Android app built with modern Jetpack Compose, Kotlin, Room Database, and Firebase Firestore.

---

## 🌟 Key Features

- **Chiko AI CFO Assistant**: Interactive financial intelligence supporting structured tables, emotion-driven audio feedback, and visual breakdowns for budget & wealth planning.
- **Privacy-First Net Worth & Passbook**:
  - Net worth section hidden by default with zero/masked balance for complete privacy.
  - Quick eye toggle button to reveal consolidated balances across bank accounts, investments, EPFO/ESI, and liabilities.
- **Clean Slate Architecture**: Production-ready without hardcoded sample data or demo balances—starts clean with ₹0.
- **Dual Persistence Architecture**:
  - **Local-First**: Fast Room SQLite persistence for offline operations.
  - **Cloud Sync**: Firebase Firestore integration for cross-device synchronization with zero-trust security rules.
- **Backup & Portability**: Standard CSV export and import support for backing up transactions.
- **Recurring Transactions**: Daily, weekly, and monthly automated transaction schedules.
- **Shake-to-Log**: Quick gesture-based transaction logger for rapid expense and sale capture.
- **Market & Financial Telemetry**: Real-time ticker tracking (Nifty 50, Sensex, etc.) with automated updates.
- **Anumati Account Aggregator**: Seamless in-app portal integration for financial data sharing.

---

## 🛠️ Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose & Material Design 3 (Edge-to-Edge)
- **Architecture**: MVVM with Kotlin Coroutines & `StateFlow`
- **Local Database**: Android Jetpack Room with KSP
- **Cloud Database**: Google Cloud Firestore & Firebase Auth
- **Security**: Jetpack Credential Manager (Google Sign-In) & Zero-Trust Firestore Security Rules

---

## 📦 Building the APK

To build the APK locally:

```bash
# Debug APK
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
