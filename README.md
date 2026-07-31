# 🎙️ VoxText - Convertisseur Universel d'Audio & Vidéo en Texte (Android)

**VoxText** est une application Android native écrite en **Kotlin** et **Jetpack Compose** qui transforme immédiatement n'importe quel contenu sonore (messages vocaux WhatsApp, fichiers MP3/MP4, liens YouTube, TikTok, Instagram) en un texte écrit fluide et lisible.

L'application s'appuie sur le moteur d'IA **Groq (Whisper Large V3)** pour une précision de la langue française, sans nécessiter de clé API codée en dur. Chaque utilisateur peut renseigner sa propre clé gratuite Groq directement dans les paramètres.

---

## ✨ Piliers et Fonctionnalités

* **⚡ Partage Direct (Share Sheet)** : Appui long sur un vocal WhatsApp ou partage d'un fichier/lien -> Sélectionner **VoxText** -> Le texte s'affiche en quelques secondes.
* **🧠 Moteur d'IA Whisper Large V3 (Groq API)** : Transcription ultra-rapide et gratuite en français via l'API REST de Groq.
* **🔒 100% Respectueux de la vie privée & Open Source** : Aucune clé API n'est incluse dans le code source. Les fichiers temporaires téléchargés dans le cache sont immédiatement supprimés après transcription.
* **💾 Historique Local (Room BDD)** : Vos transcriptions passées sont sauvegardées localement dans une base de données Room avec possibilité de copier ou partager le texte à tout moment.
* **🎨 Design Moderne & Dynamic Theme** : Interface développée sous Jetpack Compose (Material 3) s'adaptant automatiquement au mode clair/sombre du téléphone.

---

## 🏗️ Architecture du projet (Clean Architecture)

L'application respecte les principes de la **Clean Architecture** Android :

```
com.voctext.app/
├── data/
│   ├── local/            # Base de données Room (AppDatabase, TranscriptionDao, Entity)
│   └── repository/       # HistoryRepository & SettingsRepository (SharedPreferences Clé API)
├── domain/
│   ├── engine/           # GroqTranscriptionEngine, CobaltMediaExtractor, AudioExtractorImpl
│   └── model/            # Modèles métier (Transcription, TranscriptionSource, Status)
├── ui/
│   ├── components/       # Composants UI (SettingsDialog, VoctextCard, VoctextButton, Toast, etc.)
│   ├── screens/          # Écrans Compose (HomeScreen, OnboardingScreen, ResultScreen)
│   └── theme/            # Theme, Typography, Color, Spacing, Radius
└── MainActivity.kt       # Gestion de la navigation, Intents de partage et ViewModel
```

---

## 🛠️ Stack Technique

* **Langage** : Kotlin 2.0+
* **UI Framework** : Jetpack Compose (Material 3)
* **Architecture** : MVVM (Model-View-ViewModel) + Coroutines & Flow
* **Base de données locale** : Room Database
* **Réseau HTTP** : OkHttp 4
* **APIs tierces** :
  * API Groq (`https://api.groq.com/openai/v1/audio/transcriptions`)
  * API Cobalt (`https://api.cobalt.tools/`)
* **Audio Pipeline** : Android `MediaExtractor` + `MediaCodec` (Conversion PCM & en-tête WAV 16kHz mono)

---

## 🚀 Guide de lancement & de compilation

### 1. Prérequis
* **Android Studio** : Version 2024.1+ (Ladybug, Koala ou Jellyfish)
* **JDK** : Java 17
* **Android SDK** : Compile SDK 35 (minSdk 29 - Android 10+)
* **Appareils supportés** : Android 10 à Android 15+ (Prise en charge native des architectures 16 KB Page Size).

### 2. Obtenir une clé API Groq gratuite
1. Rendez-vous sur la console gratuite de Groq : [console.groq.com/keys](https://console.groq.com/keys).
2. Créez un compte gratuit et générez une clé API (du type `gsk_...`).

### 3. Cloner et compiler le projet
```bash
# Cloner le dépôt GitHub
git clone https://github.com/dr-spook/voxtext.git
cd voxtext

# Compiler l'application en mode Debug avec Gradle
./gradlew assembleDebug   # Sur Linux / macOS
.\gradlew.bat assembleDebug # Sur Windows
```

L'APK compilé se trouvera dans :
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Lancer l'application
1. Ouvrez le projet dans **Android Studio**.
2. Synchronisez Gradle (`File` -> `Sync Project with Gradle Files`).
3. Lancez l'application sur votre émulateur ou appareil physique (`Shift + F10`).
4. À l'ouverture de l'application, cliquez sur l'icône **Paramètres (engrenage)** en haut à droite et collez votre clé API Groq (`gsk_...`).

---

## 📱 Utilisation au quotidien

### Scénario A : Message vocal WhatsApp
1. Dans WhatsApp, faites un appui long sur un vocal.
2. Cliquez sur les 3 petits points -> **Partager**.
3. Sélectionnez **VoxText**.
4. La fenêtre de résultat s'affiche avec la transcription exacte. Cliquez sur **Copier** ou **Partager**.

### Scénario B : Fichier Audio ou Vidéo local
1. Ouvrez VoxText.
2. Cliquez sur **Importer un fichier** (sélectionnez un fichier MP3, MP4, MOV, WAV, M4A, OGG).
3. Le texte est immédiatement extrait et sauvegardé dans l'historique.

---

## 📄 Licence

Ce projet est sous licence **MIT**. Vous êtes libre de le réutiliser, de le modifier et de le distribuer pour vos projets personnels ou open-source.
