class Logger {
    constructor(name) {
        this.name = name;
    }

    log(...args) {
        console.log(`\x1b[33m[${this.name}]\x1b[0m`, ...args);
    }

    error(...args) {
        console.error(`\x1b[33m[${this.name}]\x1b[0m`, ...args);
    }
}

module.exports = {
    Logger,
}