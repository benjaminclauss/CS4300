package model;

/**
 * Represents encodings for seven-segment display and geometric translations.
 *
 * https://en.wikipedia.org/wiki/Seven-segment_display
 */
public enum Encoding {
    A(false, 0.0f, 7.0f),
    B(true, 3.5f, 3.5f),
    C(true, 3.5f, -3.5f),
    D(false, 0.0f, -7.0f),
    E(true, -3.5f, -3.5f),
    F(true, -3.5f, 3.5f),
    G(false, 0, 0);

    private boolean vertical;
    private float dx;
    private float dy;

    /**
     * Constructs an encoding.
     *
     * @param vertical whether or not this encoding is vertical.
     * @param dx x-transition for encoding.
     * @param dy y-transition for encoding.
     */
    Encoding(boolean vertical, float dx, float dy) {
        this.vertical = vertical;
        this.dx = dx;
        this.dy = dy;
    }

    public boolean isVertical() {
        return vertical;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }
}
