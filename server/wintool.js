const { spawn } = require('child_process');
const path = require('path');

const { Logger } = require('./loggers.js');

const WINTOOL_DIR = path.join(__dirname, 'wintool');
const WINTOOL_PATH = path.join(WINTOOL_DIR, 'wintool.exe');

function runWinTool(command, logDetails=false) {
    const logger = new Logger(`wintool-${command}`);
    let instance = spawn(WINTOOL_PATH, [ command ], { cwd: WINTOOL_DIR });
    instance.stdout.on('data', (data) => {
        logger.log(`stdout: \x1b[32m${`${data}`.trimEnd()}\x1b[0m`);
    });
    instance.stderr.on('data', (data) => {
        logger.error(`stderr: \x1b[31m${`${data}`.trimEnd()}\x1b[0m`);
    });
    instance.on('close', (code) => {
        if(code != 0 || logDetails)
            logger.log(`Exited with code ${code}`);
    });
    instance.on('error', (err) => {
        logger.error('Unexpected error: ' + err);
    });
}

function focus_launcher() {
    runWinTool('focus_launcher');
}
function focus_game() {
    runWinTool('focus_game');
}

module.exports = {
    focus_launcher,
    focus_game,
};