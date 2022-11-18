/* ----------------------------------------------------------------- */
/* ---------------------------- UI elements ------------------------ */
/* ----------------------------------------------------------------- */

function promptInfo(message, permanent=false) {
    let info = document.createElement('li');
    info.innerHTML = message;
    infoPopup.appendChild(info);
    if(!permanent)
        setTimeout(() => info.remove(), 4000);
}

const Background = {
    canvas: null,
    context: null,
    frameFunc: null,

    HEADER_HEIGHT: NaN,
    BODY_HEIGHT: NaN,
    FOOTER_HEIGHT: NaN,

    init() {
        const canvas = document.getElementById("background-canvas");
        const context = canvas.getContext("2d");
        this.canvas = canvas;
        this.context = context;
        this.frameFunc = this.frame.bind(this);
        
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        context.strokeStyle = getComputedStyle(document.body).getPropertyValue('--c-background');
        context.lineWidth = 5;

        this.HEADER_HEIGHT = .2 * canvas.height;
        this.BODY_HEIGHT = .5 * canvas.height;
        this.FOOTER_HEIGHT = .3 * canvas.height;

        requestAnimationFrame(this.frameFunc);
    },

    frame(time) {
        const { canvas, context, HEADER_HEIGHT, BODY_HEIGHT, FOOTER_HEIGHT } = this;
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.beginPath();
        time /= 1000;
    
        // draw vertical lines
        {
            let xstep = 100;

            for(let x = (time*2)%1*xstep; x < canvas.width; x += xstep) {
                context.moveTo((x-canvas.width*.5) * 3 + canvas.width*.5, 0);
                // header
                context.lineTo(x, HEADER_HEIGHT);
                // body
                context.lineTo(x, HEADER_HEIGHT + BODY_HEIGHT);
                // footer
                context.lineTo((x-canvas.width*.5) * 3 + canvas.width*.5, canvas.height);
            }
        }
    
        // draw horizontal lines
        {
            let ystep = 100;
    
            for(let y = (time*2)%ystep; y < canvas.height; y += ystep) {
                let yy;
                // very approximate way of faking perspective
                if(y < HEADER_HEIGHT) {
                    yy = Mathf.lerp(-HEADER_HEIGHT*.2, HEADER_HEIGHT, Math.sqrt(y/HEADER_HEIGHT));
                } else if(y < HEADER_HEIGHT + BODY_HEIGHT) {
                    yy = y;
                } else {
                    let a = (y-HEADER_HEIGHT-BODY_HEIGHT)/FOOTER_HEIGHT;
                    yy = Mathf.lerp(HEADER_HEIGHT + BODY_HEIGHT, canvas.height + FOOTER_HEIGHT*.2, a*(a+1)/2);
                }
                context.moveTo(0, yy);
                context.lineTo(canvas.width, yy);
            }
        }
        context.stroke();
        requestAnimationFrame(this.frameFunc);
    },
};

window.addEventListener('load', () => {
    Background.init();
});

/* ----------------------------------------------------------------- */
/* -------------------------- Sound library ------------------------ */
/* ----------------------------------------------------------------- */

function loadAudio(path, options={}) {
    let bufferCount = options.bufferCount || 1;
    let volume = options.volume || 1;

    let genAudio = () => {
        let audio = new Audio(`/audio/${path}`);
        audio.volume = volume;
        return audio;
    };

    if(bufferCount == 1)
        return genAudio();
    
    let buffers = Array(bufferCount).fill(null).map(() => genAudio());
    return {
        _buffers: buffers,
        _currentBuffer: 0,
        play() {
            let buffer = buffers[this._currentBuffer];
            if(!buffer.paused)
                return;
            buffer.currentTime = 0;
            buffer.play();
            this._currentBuffer = (this._currentBuffer+1) % buffers.length;
        },
    };
}

const Sounds = {
    "menu_transition": loadAudio("nessfx/03_start4.wav", { volume: .4 }),
    "game_start":      loadAudio("nessfx/04_start5.wav", { volume: .4 }),
    "carousel_move":   loadAudio("nessfx/31_text.wav" /*"nessfx/48_skip.wav"*/, { bufferCount: 5, volume: .5 }),
    "main_music":      loadAudio("stereotypical-90s-space-shooter-music/boss.ogg", { volume: .6 }),

    playOnTopOfMainMusic(sound) {
        let mainMusic = this.main_music;
        let dampSpeed = .1;
        let decCount = 1/dampSpeed, incCount = decCount;
        let dampDelay = .5 /*.5s*/ * 1000/decCount;
        let masterVolume = mainMusic.volume;
        let decInterval = setInterval(() => {
            mainMusic.volume -= dampSpeed * masterVolume;
            if(--decCount <= 0)
                clearInterval(decInterval);
        }, dampDelay);
        sound.play();
        sound.addEventListener('ended', () => {
            let incInterval = setInterval(() => {
                mainMusic.volume += dampSpeed * masterVolume;
                if(--incCount <= 0)
                    clearInterval(incInterval);
            }, dampDelay);
        }, { once: true });
    }
};

window.addEventListener('load', () => {
    Sounds.main_music.loop = true;
    Sounds.main_music.play();
});