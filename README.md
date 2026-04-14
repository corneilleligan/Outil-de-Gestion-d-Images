<div align="center">

# Gestionnaire de Bibliothèque d'Images

Application desktop JavaFX avec architecture MVC. Transformations d'images, système de tags, chiffrement SHA-256 et persistance JSON.

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=flat&logo=java&logoColor=white)
![Jackson](https://img.shields.io/badge/Jackson-JSON-brightgreen?style=flat)
![MIT](https://img.shields.io/badge/Licence-MIT-4f8ef7?style=flat)

<br/>

![screenshot](screenshot.png)

</div>

---

## Fonctionnalités

**Transformations**
- Rotation et symétrie (horizontale / verticale)
- Filtre noir et blanc — moyenne des composantes RGB
- Filtre sépia
- Filtre Prewitt — détection de contours
- Échange des composantes RGB → GBR

**Organisation**
- Système de tags par image pour recherche intuitive
- Persistance des filtres appliqués et des tags au format JSON (Jackson)
- Réapplication automatique des transformations au rechargement

**Sécurité**
- Chiffrement par mélange de pixels via `SecureRandom` seedé avec SHA-256
- Le mot de passe n'est jamais conservé

---

## Architecture

Le projet suit le patron **MVC** — interfaces décrites en FXML, contrôleurs dédiés aux interactions, logique métier isolée dans des classes séparées.

```
src/
├── controller/      # Contrôleurs JavaFX
├── model/
│   ├── filter/      # Filtres (héritage + interfaces)
│   ├── transform/   # Rotations, symétries
│   ├── tag/         # Gestion des tags
│   └── security/    # Chiffrement SHA-256
├── view/            # Fichiers FXML
├── persistence/     # Sérialisation JSON (Jackson)
└── resources/       # Images
```

---

## Lancement

```bash
# Compiler
javac --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -d out src/**/*.java

# Exécuter
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp out Main
```

---

## Dépendances

- [JavaFX](https://openjfx.io/) — interface graphique
- [Jackson](https://github.com/FasterXML/jackson) — sérialisation JSON
