package machine.microspin.com.flyerConsole.entity;

import android.widget.EditText;

/**
 * Input Min-Max filter [Float types] for EditText fields.
 */

public class DoubleInputFilter {

    private Double min, max;
    private String label;

    private final static String ERROR_MESSAGE = "&LABEL should be between &MIN & &MAX";

    public DoubleInputFilter(String label,double min, double max) {
        this.min = min;
        this.max = max;
        this.label = label;
    }

    public String filter(EditText editText) {
        try {
            double input = Double.parseDouble(editText.getText().toString());
            if (isInRange(min, max, input))
                return null;
            else
                return ERROR_MESSAGE.replace("&LABEL", Utility.formatString(label))
                        .replace("&MIN", min.toString())
                        .replace("&MAX", max.toString());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private boolean isInRange(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
