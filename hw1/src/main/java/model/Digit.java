package model;

/**
 * Represents a digit on a seven-segment display clock with encodings for each.
 *
 * https://en.wikipedia.org/wiki/Seven-segment_display
 */
public enum Digit {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE;

    /**
     * Returns the seven-segment encoding for this digit.
     *
     * @return encoding for this digit.
     */
    public Encoding[] getEncoding() {
        Encoding[] encoding;

        switch (this) {
            case ZERO:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.C, Encoding.D, Encoding.E, Encoding.F};
                return encoding;
            case ONE:
                encoding = new Encoding[] {Encoding.B, Encoding.C};
                return encoding;
            case TWO:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.D, Encoding.E, Encoding.G};
                return encoding;
            case THREE:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.C, Encoding.D, Encoding.G};
                return encoding;
            case FOUR:
                encoding = new Encoding[] {Encoding.B, Encoding.C, Encoding.F, Encoding.G};
                return encoding;
            case FIVE:
                encoding = new Encoding[] {Encoding.A, Encoding.C, Encoding.D, Encoding.F, Encoding.G};
                return encoding;
            case SIX:
                encoding = new Encoding[] {Encoding.A, Encoding.C, Encoding.D, Encoding.E, Encoding.F, Encoding.G};
                return encoding;
            case SEVEN:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.C};
                return encoding;
            case EIGHT:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.C, Encoding.D, Encoding.E, Encoding.F,
                        Encoding.G};
                return encoding;
            case NINE:
                encoding = new Encoding[] {Encoding.A, Encoding.B, Encoding.C, Encoding.D, Encoding.F, Encoding.G};
                return encoding;
            default:
                throw new AssertionError("Unrecognized digit: " + this);
        }
    }

    /**
     * Returns the seven-segment digit for given integer.
     *
     * @return seven-segment digit for integer.
     */
    public static Digit getDigit(int digit) {
        switch (digit) {
            case 0:
                return Digit.ZERO;
            case 1:
                return Digit.ONE;
            case 2:
                return Digit.TWO;
            case 3:
                return Digit.THREE;
            case 4:
                return Digit.FOUR;
            case 5:
                return Digit.FIVE;
            case 6:
                return Digit.SIX;
            case 7:
                return Digit.SEVEN;
            case 8:
                return Digit.EIGHT;
            case 9:
                return Digit.NINE;
            default:
                throw new AssertionError("Unrecognized integer digit: " + digit);
        }
    }
}
