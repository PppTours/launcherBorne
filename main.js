const express = require('express');
const fs = require('fs');
const path = require('path');
const { runGame, forciblyKillGame } = require('./games-manager.js');
const { Logger } = require('./loggers.js');

const GAMES_DIR = path.join(__dirname, 'games');

const port = 12345;

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
    if(forciblyKillGame()) {
        res.status(200).send('Game killed');
    } else {
        res.status(500).send('No game running');
    }
}

function createServer() {
    const app = express();
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
    app.listen(port, () => {
        console.log(`Running on http://localhost:${port} ...`);
    });
}

createServer();