const { spawn } = require('child_process');
const { Logger } = require('./loggers.js');

const logger = new Logger('game');

var runningInstance = null;

function runGame(command, args, executionPath) {
    if(runningInstance != null)
        throw new Error('A game is already running');
    runInstance(command, args, executionPath);
}

function runInstance(command, args, executionPath) {
    try {
        let controller = new AbortController();
        let { signal } = controller;
        logger.log('Running command: "' + command + ' ' + args.join(' ') + '" in ' + executionPath);
        let instance = spawn(command, args, { signal, cwd: executionPath });
        runningInstance = { instance, controller };
        instance.stdout.on('data', (data) => {
            logger.log(`game stdout: \x1b[32m${data}\x1b[0m`);
        });
        instance.stderr.on('data', (data) => {
            console.error(`game stderr: \x1b[31m${data}\x1b[0m`);
        });
        instance.on('close', (code) => {
            if(code == 0) {
                logger.log('Game exited gracefully');
            } else {
                logger.log(`Game exited with code ${code}`);
            }
            runningInstance = null;
        });
        instance.on('error', (err) => {
            logger.error('Game error: ' + err);
        });
    } catch (e) {
        logger.error('Unhandled game error: ' + e);
    }
}

function forciblyKillGame() {
    if(runningInstance == null)
        return false;
    runningInstance.controller.abort();
    return true;
}

module.exports = {
    runGame,
    forciblyKillGame,
};