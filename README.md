# Zik Bluetooth 🎵

Écoutez de la musique ensemble via Bluetooth - Une application Android pour partager votre musique préférée avec vos amis.

**Listen to music together via Bluetooth - An Android app for sharing your favorite music with friends.**

## 📱 Description

Zik Bluetooth est une application Android native qui permet à plusieurs utilisateurs de se connecter via Bluetooth et d'écouter de la musique ensemble en synchronisation.

## ✨ Fonctionnalités Principales

- 🔌 Connexion Bluetooth multi-appareils
- 🎵 Lecteur de musique synchronisé
- 📱 Interface utilisateur intuitive
- 🔊 Contrôle du volume partagé
- 📋 Playlist personnalisées
- ⚡ Optimisé pour les performances

## 🛠️ Pile Technologique

- **Langage:** Kotlin/Java
- **API minimum:** Android API 21+
- **Architecture:** MVVM
- **Build System:** Gradle

## 📋 Prérequis

- Android Studio Hedgehog ou plus récent
- JDK 11+
- SDK Android (API 21+)
- Git

## 🚀 Installation et Configuration

### 1. Cloner le repository
```bash
git clone https://github.com/yaoubadiguir-dot/Zik-bluetooth-.git
cd Zik-bluetooth-
```

### 2. Ouvrir dans Android Studio
```bash
# Depuis Android Studio: File > Open > Sélectionner le dossier du projet
```

### 3. Synchroniser les dépendances
- Android Studio téléchargera automatiquement les dépendances Gradle

## 🔨 Construire l'APK

### Debug APK
```bash
./gradlew assembleDebug
```
Le fichier APK se trouvera dans: `app/build/outputs/apk/debug/`

### Release APK (Production)
```bash
./gradlew assembleRelease
```
Le fichier APK se trouvera dans: `app/build/outputs/apk/release/`

## 📦 Installation sur un appareil

### Via ADB (Android Debug Bridge)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Via USB
1. Connecter votre appareil Android au PC via USB
2. Activer le mode développeur sur l'appareil
3. Faire confiance au PC
4. Exécuter: `adb install -r [chemin-vers-apk]`

## 💻 Structure du Projet

```
Zik-bluetooth-/
├── app/                          # Module application principale
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/            # Code source Kotlin/Java
│   │   │   ├── res/             # Ressources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/               # Tests unitaires
│   │   └── androidTest/        # Tests instrumentés
│   └── build.gradle.kts        # Configuration Gradle
├── gradle/                      # Configuration Gradle
├── build.gradle.kts            # Configuration Gradle racine
├── settings.gradle.kts         # Paramètres du projet
└── README.md                   # Ce fichier
```

## 🎯 Utilisation

1. **Lancer l'application** sur votre appareil Android
2. **Activer Bluetooth** sur tous les appareils
3. **Appairer les appareils** (première connexion)
4. **Sélectionner ou importer** une musique
5. **Partager** avec les amis connectés
6. **Écouter ensemble** en synchronisation

## 🔐 Permissions Requises

L'application demande les permissions suivantes:
- `BLUETOOTH` - Accès au Bluetooth
- `BLUETOOTH_ADMIN` - Gestion des appareils Bluetooth
- `ACCESS_FINE_LOCATION` - Localisation pour découverte Bluetooth
- `READ_EXTERNAL_STORAGE` - Lecture des fichiers audio
- `READ_MEDIA_AUDIO` - Accès aux fichiers musicaux (Android 13+)

## 🐛 Troubleshooting

### L'application ne détecte pas les appareils Bluetooth
- Vérifier que Bluetooth est activé
- S'assurer que les appareils sont en mode appairage
- Vérifier les permissions de localisation

### L'APK ne s'installe pas
- Vérifier la version Android minimale requise
- Désinstaller les versions précédentes: `adb uninstall [nom-package]`
- Utiliser le flag `-r`: `adb install -r [apk]`

### Problèmes de synchronisation audio
- Vérifier la qualité de la connexion Bluetooth
- Réduire la distance entre les appareils
- Redémarrer l'application

## 🤝 Contribuer

Les contributions sont les bienvenues! Pour contribuer:

1. Fork le projet
2. Créer une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Commiter vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Pousser vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 Licence

Ce projet est sous licence [Ajouter votre licence ici - MIT, Apache 2.0, etc.]

## 👤 Auteur

**yaoubadiguir-dot**
- GitHub: [@yaoubadiguir-dot](https://github.com/yaoubadiguir-dot)

## 📞 Support

Pour toute question ou problème, ouvrir une [Issue](https://github.com/yaoubadiguir-dot/Zik-bluetooth-/issues) sur GitHub.

## 📚 Ressources Utiles

- [Documentation Android Officielle](https://developer.android.com/docs)
- [Guide Bluetooth Android](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [Android Studio Setup Guide](https://developer.android.com/studio/install)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)

---

**Bon développement! 🎉**