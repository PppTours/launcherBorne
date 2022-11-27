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

function key_logger() {
    runWinTool('key_logger');
}

function simple_click() {
    runWinTool('simple_click');
}

function substitution() {
    runWinTool('substitution', true);
}

module.exports = {
    key_logger,
    simple_click,
    substitution,
};