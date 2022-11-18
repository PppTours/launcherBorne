const pages = {
    front: document.getElementById("front-page"),
    games: document.getElementById("games-page"),
    playingGame: document.getElementById("playing-game-page"),

    activePage: document.querySelector(".main-page.active"), // one of the above
};

const gameTemplate = document.getElementById("game-cartridge-template");
const gameTagTemplate = document.getElementById("game-tag-template");
const gamesList = document.getElementById("games-list");
const infoPopup = document.getElementById("info-popup");
const activeGameTitle = document.querySelector("#games-page .game-title");
const currentPlaytimeText = document.getElementById("current-playtime");
const currentPlayingGameContainer = document.getElementById("currently-playing-cartridge-container");

const games = [];
var selectedGameIdx = 0;

// after 10 seconds of inactivity, return to the front page
const idlingMenuTransition = {
    _timeout: undefined,
    
    delay() {
        clearTimeout(this._timeout);
        this._timeout = setTimeout(() => {
            this._timeout = null;
            pages.front.classList.add('active');
            pages.games.classList.remove('active');
            pages.activePage = pages.front;
        }, 10000);
    },

    pause() {
        clearTimeout(this._timeout);
        this._timeout = null;
    },

    resume() {
        this.delay();
    }
};

function createGameCartridge(gameInfo) {
    let tags = [];
    for(let t of gameInfo.tags) {
        tags.push(HTMLUtils.elementFromTemplate(gameTagTemplate, {
            "t-tag-name": '#'+t
        }));
    }
    return HTMLUtils.elementFromTemplate(gameTemplate, {
        "t-name": gameInfo.name,
        "t-vignette": (e => e.src = `games/${gameInfo.id}/meta/${gameInfo.vignette}`),
        "t-description": gameInfo.description,
        "t-info": ""+gameInfo.creators,
        "t-tags": tags,
    });
}

async function fetchAvailableGames() {
    return await HTMLUtils.sendRequest('/api/getgames');
}

async function launchGame(game) {
    return await HTMLUtils.sendRequest(`/api/launchgame/${game.info.id}`, {}, { receiveJSON: false });
}

const gamesCarousel = {
    _currentScroll: 0, // 0,N
    _targetScroll: 0,  // 0..N
    _previousFrame: null,

    scrollToNext() {
        if(this._currentScroll > this._targetScroll - 1)
            this._targetScroll++;
    },

    scrollToPrevious() {
        if(this._currentScroll < this._targetScroll + 1)
            this._targetScroll--;
    },

    _animateScroll(timestamp) {
        // maybe too much math, the loop lags when there are more than ~500 games
        // TODO fix: when currentScroll is <0 the vignettes are offseted by 1

        if(this._previousFrame == null)
            this._previousFrame = timestamp;
        let delta = (timestamp - this._previousFrame) / 1000;
        this._previousFrame = timestamp;
        // if(this._currentScroll == this._targetScroll) return; this._currentScroll = this._targetScroll;
        this._currentScroll = Mathf.damp(this._currentScroll, this._targetScroll, 6, delta);
        let absScroll = Mathf.positiveModulo(this._currentScroll, games.length);
        let dx = Math.max(Math.pow(.5, games.length/5-1), .4); // 1 when there are ~5 games, halved every time the number of games double
        for(let i = 0; i < games.length; i++) {
            let cyclicDist = Mathf.positiveModulo(i - absScroll + games.length/2, games.length) - games.length/2;
            let dist = 2/(1+Math.exp(-cyclicDist * dx))-1;
            games[i].cartridge.style.opacity = Math.min(1, 3*(.75-dist*dist));
            games[i].cartridge.style.left = (50 + dist * 40) + '%';
            games[i].cartridge.style.zIndex = Math.floor((games.length - Math.abs(cyclicDist))*1000);
            games[i].cartridge.style.transform = `translateX(-50%) scale(${1-Math.pow(Math.abs(dist), 3)})`;
        }
    },
};

const playingGamePage = {
    _currentPlaytime: 0,

    resetPlaytime() {
        this._currentPlaytime = 0;
        this._refreshPlaytimeText();
    },

    _refreshPlaytimeText() {
        let hours = Math.floor(this._currentPlaytime / 3600);
        let minutes = Math.floor(this._currentPlaytime / 60) % 60;
        let seconds = Math.floor(this._currentPlaytime) % 60;
        let text = "";
        if(hours > 0) text += hours + "h ";
        if(minutes > 0) text += minutes + "m ";
        text += seconds + "s";
        text += '.'.repeat(1 + seconds % 3);
        currentPlaytimeText.innerText = `Playing since ${text}`;
    },

    updatePlaytime() {
        this._currentPlaytime += 1;
        this._refreshPlaytimeText();
    }
};

// fetch all available games
window.addEventListener('load', async () => {
    let availableGames = await fetchAvailableGames();
    for(let game of availableGames) {
        let cartridge = createGameCartridge(game);
        gamesList.appendChild(cartridge);
        games.push({
            cartridge: cartridge,
            info: game,
        });
    }
    activeGameTitle.innerHTML = games[selectedGameIdx].info.name;
});

// kickstart animations
window.addEventListener('load', () => {
    let animateCarousel = (timestamp) => {
        gamesCarousel._animateScroll(timestamp);
        requestAnimationFrame(animateCarousel);
    };
    requestAnimationFrame(animateCarousel);

    setInterval(() => {
        playingGamePage.updatePlaytime();
    }, 1000);
});

// kill game if one is runing when page is loaded
window.addEventListener('load', async () => {
    if((await HTMLUtils.sendRequest('/api/killgame')).killed)
        promptInfo("Killed the still running game");
});

// handle game selection and launch
window.addEventListener('keydown', async (ev) => {
    if(document.querySelector('.main-page.fading') != null) return;

    if(pages.activePage == pages.front) {
        Sounds.playOnTopOfMainMusic(Sounds.menu_transition);
        pages.front.classList.add('fading');
        pages.front.addEventListener('transitionend', (ev) => {
            pages.front.classList.remove('active', 'fading');
            pages.games.classList.add('active');
            idlingMenuTransition.resume();
        });
        pages.activePage = pages.games;
        
    } else if(pages.activePage == pages.playingGame) {
        // no user interaction

    } else if(pages.activePage == pages.games) {
        idlingMenuTransition.delay();

        switch(ev.key) {
        case 'ArrowRight':
            gamesCarousel.scrollToNext();
            selectedGameIdx++;
            selectedGameIdx %= games.length;
            activeGameTitle.innerHTML = games[selectedGameIdx].info.name;
            Sounds.carousel_move.play();
            break;
        case 'ArrowLeft':
            gamesCarousel.scrollToPrevious();
            selectedGameIdx--;
            if(selectedGameIdx < 0) selectedGameIdx += games.length;
            activeGameTitle.innerHTML = games[selectedGameIdx].info.name;
            Sounds.carousel_move.play();
            break;
        case 'Enter':
            try {
                await launchGame(games[selectedGameIdx]);
                Sounds.game_start.play();
                pages.activePage = pages.playingGame;
                pages.games.classList.remove('active');
                pages.playingGame.classList.add('active');
                playingGamePage.resetPlaytime();
                idlingMenuTransition.pause();
                Sounds.fadeMainMusicVolume(0);
                currentPlayingGameContainer.innerHTML = "";
                currentPlayingGameContainer.appendChild(createGameCartridge(games[selectedGameIdx].info));
            } catch (e) {
                console.error(e);
                promptInfo(e);
            }
            break;
        }
    }
});

