----
Add the following json snippet to your VSCode's workspace's `settings.json file`
to get autocompletion in game files:

```json
"json.schemas": [
    {
        "fileMatch": [
            "game.json"
        ],
        "url": "./games/schema.json"
    }
]
```

----
To generate the logo animation; first export the blender animation as an ffmpeg
video *using rgba* (google to know how). Then export using ffmpeg:
```
ffmpeg -i blender-output.mov -c:v libvpx-vp9 logo-anim.webm
```

----
A tiny "api" is used for the client to tell the server to launch games. A web socket
is used by the server to tell the client when games exit.

If a games does not stop or is still running when the launcher starts for some reason
the launcher will tell the server to kill it. This also means that no two launchers
must run at the same time.
