Place `wintool.exe` in this folder, along with a `keys.txt` file that
contains key substitutions.
Substitutions work on virtual key codes, not codepoints or characters.
for example a line with `39 37` replaces the right arrow key with the
left arrow key.
Substitutions must be placed one per line, with the virtual key code
to replace first, and the virtual key code to replace it with second.
Do not add a trailing newline to the file.