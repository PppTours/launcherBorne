This is the launcher utility for running P++ games on an arcade machine.

## Installation

Build the java project under `Server/`, with `lib` and `res` as resource folders.

Build the `wintool/` visual studio project in `x64|release` and place the resulting
`wintool.exe` next to the server jar.

Create a `games/` folder and add your game files, they are described in a latter section.

## Running

Run the following command in the directory with the 2 files and the games folder:
```
java -jar server.jar
```

## Dev notes

### Background

A first version of this launcher was created in javascript with a node server and a
web front-end. It had humongus performance issues so I quickly ported it to java, I
kinda rushed the project so the code is not the cleanest, with my personnal librairies
and code I scrapped from my older projects.

### Regarding joystick support

Joystick buttons have been remapped:
- Player 1 uses ZQSD and RTY/FGH
- Player 2 uses the arrow keys and UIO/JKL
- The 3 left buttons map to W/esc/X.

### game file format

A game is registered with this file structure:
```
<game-name>/
├── game/
│   ├── <executable>
│   └── ...
└── meta/
    ├── game.json
    ├── <vignette.png>
    ├── <cartridge.png>
    └── ...
```

The `game.json` file must respect this format:

```json
{
    "title":           string,
    "creation_date":   string,
    "description":     multiline string,
    "authors":         string[],
    "run_command":     string[],
    "vignette":        file name in /meta,
    "cartridge_image": file name in /meta,
    "tags":            tag[]
}

tags ::= [
	versus
	coop
	solo
	platformer
	shoot them up
	beat them up
	rpg
	gestion
	strategy
	puzzle
	fighting game
]
```

The description string can be long, it must be broken to multiple lines with `\n` manually.

## Machine setup

The arcade was restored in december 2022, you'll find a list of the installed software
next to this file. On boot the machine will run both the joystick-to-keystrokes
program and the launcher.

> **Note:** The machine is currently not connected to the internet

> **Note:** To future restorers, windows 10 is running on the wrong drive, it should be
> on the RAID disk. You may try to fix this to improve performances but be prepared to
> reinstall *everything*. You have been warned.

