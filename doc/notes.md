Add the following json snippet to your VSCode's workspace's `settings.json file` to get autocompletion in game files

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