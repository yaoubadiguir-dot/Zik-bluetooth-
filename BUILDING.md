# Guide de Construction de l'APK - Zik Bluetooth

Ce guide explique comment construire l'APK de Zik Bluetooth.

## 📋 Prérequis

- Android Studio 2023.1 ou plus récent
- JDK 11 ou plus récent
- Gradle 8.0 ou plus récent
- SDK Android (API 21+)

## 🔨 Construction via Android Studio

### 1. Ouvrir le projet
```bash
# Clone le repository
git clone https://github.com/yaoubadiguir-dot/Zik-bluetooth-.git
cd Zik-bluetooth-

# Ouvrir dans Android Studio
# File > Open > Sélectionner le dossier
```

### 2. Build APK Debug
Dans Android Studio:
1. Aller à `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. L'APK sera généré dans: `app/build/outputs/apk/debug/app-debug.apk`

Ou via terminal:
```bash
./gradlew assembleDebug
```

### 3. Build APK Release
Pour une version de production:
```bash
./gradlew assembleRelease
```

L'APK sera dans: `app/build/outputs/apk/release/app-release.apk`

## 🖥️ Construction via Terminal (CLI)

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

### Clean et Build
```bash
./gradlew clean assembleDebug
```

## 📱 Installation de l'APK

### Via ADB
```bash
# Debug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Via Drag & Drop
1. Copier l'APK sur le téléphone
2. Ouvrir le gestionnaire de fichiers
3. Cliquer sur l'APK et installer

### Via Android Studio
1. Générer l'APK
2. Dans le notifiction popup, cliquer sur "Install"
3. Sélectionner l'appareil cible

## 🔧 Troubleshooting

### Erreur: "SDK not found"
```bash
# Définir le chemin du SDK
# File > Project Structure > SDK Location
# Ou dans local.properties:
echo "sdk.dir=/path/to/android/sdk" > local.properties
```

### Erreur: "Gradle sync failed"
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

### APK trop volumineux
Vérifier la configuration ProGuard dans `app/build.gradle.kts`:
```kotlin
isMinifyEnabled = true
isShrinkResources = true
```

### Problèmes de permissions
S'assurer que toutes les permissions sont déclarées dans `AndroidManifest.xml`

## 📊 Vérifier la taille de l'APK
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

## 🚀 Optimisations de Release

Pour un meilleur APK release:

1. **ProGuard/R8** - Active automatiquement pour release
2. **Shrink Resources** - Supprime les ressources inutilisées
3. **Min SDK 21** - Taille plus petite que les versions antérieures
4. **Split APK** - Créer des APK par architecture (si nécessaire)

## ✅ Vérification avant la publication

- [ ] Tests unitaires passent: `./gradlew test`
- [ ] Tests instrumentés passent: `./gradlew connectedAndroidTest`
- [ ] Lint sans erreurs critiques: `./gradlew lint`
- [ ] Taille APK acceptable
- [ ] Permissions correctes dans le manifest
- [ ] Versioncode et versionname à jour

---

Pour plus d'infos: https://developer.android.com/studio/build