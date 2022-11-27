const { spawn } = require('child_process');
const EventEmitter = require('events');
const { Logger } = require('./loggers.js');
const wintool = require('./wintool.js');

const logger = new Logger('game');
const gameEvents = new EventEmitter();

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
            logger.log(`game stdout: \x1b[32m${`${data}`.trimEnd()}\x1b[0m`);
        });
        instance.stderr.on('data', (data) => {
            logger.error(`game stderr: \x1b[31m${`${data}`.trimEnd()}\x1b[0m`);
        });
        instance.on('close', (code) => {
            if(code == 0) {
                logger.log('Game exited gracefully');
            } else {
                logger.log(`Game exited with code ${code}`);
            }
            runningInstance = null;
            gameEvents.emit('game-exit', { code });
        });
        instance.on('error', (err) => {
            logger.error('Game error: ' + err);
        });
        // click in the middle of the screen to make sure the game has focus
        // this is a shady workaround, but it works as long as the game starts
        // before the timeout
        // TODO hide the cursor once the game exited
        setTimeout(() => {
            wintool.simple_click();
        }, 1000);
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
    gameEvents,
};