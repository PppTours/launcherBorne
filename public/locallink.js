const localLink = {
    _ws: null,

    openConnection(host, port) {
        if(this._ws != null)
            throw Error('LocalLink is already connected');
        let ws = this._ws = new WebSocket(`ws://${host}:${port}`);
        // ws.addEventListener('open', () => {
        //     console.log('websocket connected');
        // });
        ws.addEventListener('message', (ev) => {
            this._onMessage(ev);
        });
        ws.addEventListener('close', () => {
            console.error('websocket closed');
            promptInfo('LocalLink connection closed, please refresh the page', true);
            this._ws = null;
        });
    },

    _onMessage(ev) {
        let data = JSON.parse(ev.data);
        switch(data.action) {
        case 'game-exit':
            let { code } = data.response;
            console.log('game exited with code ' + code);
            break;
        default:
            console.error('unhandled message: ' + data);
            break;
        }
    },
};

localLink.openConnection(location.hostname, location.port);
