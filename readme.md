## Borne d'arcade - fiche technique

**EN** This is the launcher utility for P++'s arcade.\\
**FR** Ceci est l'utilitaire de lancement pour les jeux de la borne d'arcade.

### Dev. des jeux

#### Contrôles

Layout des touches utilisées :

![](doc/controls-to-keys.png)

Par convention le bouton blanc du milieu sert à lancer *et quitter* les jeux.
Pour publier un jeu faites une notices des contrôles, il y a la police à utiliser et les images nécessaires dans `doc/`.
Les images sont en svg, il faut les exporter en png.

#### Versions installées

| software | version | details |
| --- | -- | --- |
| Python | 3.10 | - |
| Java | JDK 19 | OpenJDK (en plus du JRE), Eclipse est installé |
| nodejs | à vérifier | pas de package npm |
| web | firefox/chrome | pour les jeux webs |
| c++ | c++20 | Cmake et VS sont installés, il faudra sans doute re-compiler sur la borne directement |
| opengl | 3.3 | - |

> **Warning**
> La borne n'est pas connectée à internet, c'est pas évident de faire de nouvelles installations

#### Structure des fichiers

Pour déployer un jeu il faut suivre la structure suivante :

```
<nom-du-jeu>/
├── game/
│   ├── executable.exe  (n'importe quel nom/extension)
|   ├── scores.txt      (optionnel)
│   └── ... fichiers de sauvegarde, de ressources...  
└── meta/
    ├── game.json  
    ├── vignette.png  (optionnel)
    ├── cartridge.png (optionnel)
    ├── controls.png  (optionnel)
    └── ... fichiers de sources, de notes, de contact si besoin...
```

Les deux images pour présenter le jeu sont la vignette et l'image de jaquette. La vignette est en  
ratio 10:17 et la jaquette est en 1:1 Vous pouvez faire des images de 100x170px et 500x500px par exemple.
Pour la vignette une image en 1:2 fait l'afaire, elle sera un tout petit peu étirée.

L'image des contrôles n'a pas de format imposé, elle sera plus lisible si elle est carré.

##### game.json

Format à suivre :
```json
{
    "title":           string,
    "creation_date":   string,
    "description":     multiline string,
    "authors":         string[],
    "run_command":     string[],
    "tags":            tag[]
}
```

La descritption doit contenir les `\n`, une ligne fait environ 20 charactères max.
Tags possibles : `versus`, `coop`, `solo`, `platformer`, `shoot them up`, `beat them up`, `rpg`, `gestion`, `strategy`, `puzzle`, `fighting game`.

Exemple :
```json
{
    "title": "Ping",
    "creation_date": "2023",
    "description": "Pong.\n\nCopie du jeu retro\nVictoire en 10 points",
    "authors": [ "Albin" ],
    "run_command": [ "java", "-jar", "retro-games.jar", "pong" ],
    "tags": [ "versus" ]
}
```

Pour `run_command`, il faut spécifier les arguments un-à-un, par exemple :
- (java) `[ "java", "-jar", "game.jar" ]`
- (python) `[ "python", "game.py" ]`
- (.exe) `[ "game.exe" ]`
Le jeu est exécuté dans son dossier `game/`.

#### Scores

Un jeu peut gérer des highscores en créant un fichier `scores.txt` dans le dossier `game/` (donc dans le dossier d'exécution). 
Syntaxe du fichier :
```
<nom1>;<score1>[;<date1>]
<nom2>;<score2>[;<date2>]
<nom3>;<score3>[;<date3>]
...
```
Par exemple :
```
jean;10
dujardin:53462
```
```
jean;10;2001-01-01
jean;6425340;2010-01-01
```
Pas de score à virgule. Pas besoin de trier les lignes (elles sont triées automatiquement par score puis par date).

### Maintient de la borne

Au lancement de la borne il faut aussi un key-mapper.

Ceci est la deuxième version du launcher, la première était en web. La nouvelle a été faite en
quelques heures donc ne regardez pas trop le code.

**TODO**:

Windows 10 est installé sur le mauvaix disque, il est en virtualisation pour l'instant.
Il faudrait le réinstaller sur le bon mais ca demande de passer par le BIOS et de *tout* réinstaller.
