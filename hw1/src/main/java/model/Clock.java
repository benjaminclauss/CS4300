package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a model for a digital clock.
 */
public class Clock {

    /**
     * Returns the seven-segment digits for the current time.
     *
     * @return list of digits for the current time.
     */
    public List<Digit> getDigitsForTime() {
        Calendar calendar = Calendar.getInstance();

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        List<Digit> digits = new ArrayList<>();
        digits.add(Digit.getDigit(hours / 10));
        digits.add(Digit.getDigit(hours % 10));
        digits.add(Digit.getDigit(minutes / 10));
        digits.add(Digit.getDigit(minutes % 10));
        digits.add(Digit.getDigit(seconds / 10));
        digits.add(Digit.getDigit(seconds % 10));

        return digits;
    }
}
