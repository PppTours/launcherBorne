This is the launcher utility for running P++ games on an arcade machine.

## Installation

Run the following command in the `server/` directory:
```
npm i
```
Build the `wintool/` visual studio project in `x64|release` and place the resulting
`wintool.exe` in the `server/wintool/` directory, it may need to be created.

## Running

Run the following command in the `server/` directory:
```
npm start
```

## Dev notes

### QOL notes

When developing, set your `DEBUG_ENV` environment variable to `true` to
prevent the web page from popping up.

----

Add the following json snippet to your VSCode's workspace's `settings.json` file
to get autocompletion in `game.json` files:

```json
"json.schemas": [
    {
        "fileMatch": [
            "game.json"
        ],
        "url": "./server/games/schema.json"
    }
]
```

### To generate the logo animation
First export the blender animation in `doc/` as an ffmpeg video *using rgba* (google
to find out how). Then export using ffmpeg:
```
ffmpeg -i blender-output.mov -c:v libvpx-vp9 logo-anim.webm
```

### Regarding the launcher
A tiny api is used for the client to tell the server to launch games. A web socket
is used by the server to tell the client when games exit.

If a games does not stop or is still running when the launcher starts for some reason
the launcher will tell the server to kill it. This also means that no two launchers
must run at the same time.

### Regarding joystick support
There is an external program that is used to read joystick input and translate theim
to key presses. `wintool` was supposed to be used for this, but it is not currently
working. Instead `wintool` is required to send keystrokes at specific times, like
when a game is launched to make sure it has input focus.

## Machine setup

The arcade was restored in december 2022, you'll find a list of the installed software
under `download/installation_files/`. On boot the machine will run both the joystick-to-keystrokes
program and the launcher. The launcher will then open its web page.
The BIOS has been setup to boot directly whenever power is turned on, this only works
if the machine **has not been shutdown** when power was turned off.

> **Note:** The machine is currently not connected to the internet

> **Note:** To future restorers, windows 10 is running on the wrong drive, it should be
> on the RAID disk. You may try to fix this to improve performances but be prepared to
> reinstall *everything*. You have been warned.

