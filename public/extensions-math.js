const Mathf = {
    lerp(a, b, x) {
        return a + (b-a) * x;
    },

    damp(a, b, lambda, dt) {
        return Mathf.lerp(a, b, 1 - Math.exp(-lambda * dt));
    },

    positiveModulo(a, m) {
        a %= m;
        return a < 0 ? a+m : a;
    },
}