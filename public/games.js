const pages = {
    front: document.getElementById("front-page"),
    games: document.getElementById("games-page"),
};

const gameTemplate = document.getElementById("game-display-template");
const gameTagTemplate = document.getElementById("game-tag-template");
const gamesList = document.getElementById("games-list");
const infoPopup = document.getElementById("info-popup");
const activeGameTitle = document.querySelector("#games-page .game-title");

const games = [];
var selectedGameIdx = 0;

// after 30 seconds of inactivity, return to the front page
const idlingMenuTransition = {
    _timeout: undefined,
    
    delay() {
        clearTimeout(this._timeout);
        this._timeout = setTimeout(() => {
            this._timeout = null;
            pages.front.classList.add('active');
            pages.games.classList.remove('active');
        }, 3000);
    },
};

function createGameVignette(gameInfo) {
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
            games[i].vignette.style.opacity = Math.min(1, 3*(.75-dist*dist));
            games[i].vignette.style.left = (50 + dist * 40) + '%';
            games[i].vignette.style.zIndex = Math.floor((games.length - Math.abs(cyclicDist))*1000);
            games[i].vignette.style.transform = `translateX(-50%) scale(${1-Math.pow(Math.abs(dist), 3)})`;
        }
    },
}

// fetch all available games
window.addEventListener('load', async () => {
    let availableGames = await fetchAvailableGames();
    for(let game of availableGames) {
        let vignette = createGameVignette(game);
        gamesList.appendChild(vignette);
        games.push({
            vignette: vignette,
            info: game,
        });
    }
    activeGameTitle.innerHTML = games[selectedGameIdx].info.name;
});

// kickstart the carousel animation
window.addEventListener('load', () => {
    let animate = (timestamp) => {
        gamesCarousel._animateScroll(timestamp);
        requestAnimationFrame(animate);
    };
    requestAnimationFrame(animate);
});

// handle game selection and launch
window.addEventListener('keydown', async (ev) => {
    if(pages.front.classList.contains('active')) {
        Sounds.playOnTopOfMainMusic(Sounds.menu_transition);
        pages.front.classList.add('fading');
        pages.front.addEventListener('transitionend', (ev) => {
            pages.front.classList.remove('active', 'fading');
            pages.games.classList.add('active');
            idlingMenuTransition.delay();
        });
        return;
    }

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
    case 'ArrowUp':
        break;
    case 'ArrowDown':
        break;
    case 'Enter':
        try {
            await launchGame(games[selectedGameIdx]);
            Sounds.game_start.play();
        } catch (e) {
            console.error(e);
            promptInfo(e);
        }
        break;
    }
});

