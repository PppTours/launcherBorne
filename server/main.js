const express = require('express');
const fs = require('fs');
const path = require('path');
const { runGame, forciblyKillGame, gameEvents } = require('./games-manager.js');
const { exec } = require('child_process');
const { Logger } = require('./loggers.js');
const WebSocket = require('ws');
const wintool = require('./wintool.js');

const GAMES_DIR = path.join(__dirname, 'games');

const port = process.env.PORT || 12345;
const isDebugEnv = process.env.DEBUG_ENV === 'true';

const apiLogger = new Logger('api');

function api_getGames(req, res) {
    apiLogger.log('getting games');
    let games = [];
    for(let gameId of fs.readdirSync(GAMES_DIR)) {
        let schemaFile = path.join(GAMES_DIR, gameId, 'meta', 'game.json');
        if(fs.lstatSync(path.join(GAMES_DIR, gameId)).isDirectory() && fs.existsSync(schemaFile)) {
            let gameInfo = JSON.parse(fs.readFileSync(schemaFile));
            gameInfo.id = gameId;
            games.push(gameInfo);
        }
    }
    res.status(200).send(games);
}

function api_launchGame(req, res) {
    apiLogger.log('launching game', req.params.game);
    try {
        let gameId = req.params.game;
        let schemaFile = path.join(GAMES_DIR, gameId, 'meta', 'game.json');
        if(!fs.existsSync(schemaFile))
            throw Error('Unknown game "' + gameId + '"');
        let gameInfo = JSON.parse(fs.readFileSync(schemaFile));
        let command = gameInfo['launch_file'];
        let args = gameInfo['launch_args'];
        let executionPath = path.join(GAMES_DIR, gameId, 'game');
        runGame(command, args, executionPath); // actually execute the file, asynchronously
        res.status(200).send('Launched "' + gameId + '"');
    } catch (e) {
        res.status(500).send('Cannot launch a game: ' + e);
    }
}

function api_killGame(req, res) {
    apiLogger.log('killing game');
    let killed = forciblyKillGame();
    res.status(200).send({ killed });
}

function createServer() {
    const app = express();
    const wsServer = new WebSocket.Server({ noServer: true });
    const wsClients = new Map();

    app.use(express.json());
    app.use(express.urlencoded({ extended: true }));
    // api/getgames returns a list of available games 
    app.get('/api/getgames', api_getGames);
    app.get('/api/launchgame/:game', api_launchGame);
    app.get('/api/killgame', api_killGame);
    // expose the /public directory
    app.use(express.static('public'));
    // make the meta folder of each game accessible (but not the game folder)
    app.get('/games/:game/meta/:file.:ext', (req, res) => {
        let p = path.join(GAMES_DIR, req.params.game, 'meta', req.params.file+'.'+req.params.ext);
        if(fs.existsSync(p))
            res.sendFile(p);
        else
            res.status(404).send("No such file");
    });
    // redirect when the url is invalid (=404 page)
    app.use((_req, res) => {
        res.redirect('/');
    });
    // handle websocket connections
    wsServer.on('connection', (ws) => {
        let clientId = Math.random().toString(36).substring(2, 9);
        let logger = new Logger('ws-'+clientId.substring(0,2));
        logger.log('socket connected');
        wsClients.set(clientId, { socket: ws, logger: logger });
        // the client is not supposed to send anything, do not add a 'message' event listener
        ws.on('close', () => {
            wsClients.delete(clientId);
            logger.log('socket disconnected');
        });
    });
    // broadcast a message when a game event occurs
    gameEvents.on('game-exit', ({code}) => {
        for(let client of wsClients.values()) {
            client.socket.send(JSON.stringify({ action: 'game-exit', response: { code } }));
        }
    });

    // actually create the server
    const server = app.listen(port, () => {
        console.log(`Running on http://localhost:${port} ...`);
    });
    // handle websocket connections (to send data from the server to the client)
    server.on('upgrade', (req, socket, head) => {
        wsServer.handleUpgrade(req, socket, head, (ws) => {
            wsServer.emit('connection', ws, req);
        });
    });
}

function openBrowser() {
    // 'start' is win32 only
    exec('start http://localhost:' + port);
}

function main() {
    let command = (process.argv.length > 2 && process.argv[2]) || 'server';

    switch(command) {
    case 'server':
        createServer();
        if(!isDebugEnv) {
            openBrowser();
            setTimeout(() => wintool.focus_launcher(), 2000);
        }
        break;
    default:
        console.error('Unknown command "' + command + '", use one of ["server"]');
        break;
    }
}

main();
