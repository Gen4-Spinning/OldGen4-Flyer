package machine.microspin.com.flyerConsole.entity;

/**
 * TLV - Type Length Value
 * Format used for representing attribute type, length and its value.
 */

public class TLV implements Pattern {
    private String type;
    private String length;
    private String value;
    private String screen;

    final public static String TYPE_FLOAT_LENGTH = "04";
    final public static String TYPE_INT_LENGTH = "02";


    /*
    * TLV Constructor for Incoming Packet creation
    */
    public TLV(String type, String length, String value, String screen) {
        this.type = type;
        this.length = length;
        //noinspection IfCanBeSwitch
        if (this.length.equals(TYPE_FLOAT_LENGTH)) {
            Float fl = Utility.convertHexToFloat(value);
            this.value = fl.toString();
        } else if (this.length.equals(TYPE_INT_LENGTH)) {
            this.value = Utility.convertHexToIntString(value);
        } else {
            this.value = value;
        }
        this.screen = screen;
    }

    /*
    * TLV Constructor for Outgoing Packet creation
    */
    public TLV(String type, String length, String value) {
        this.type = type;
        this.length = length;
        //noinspection IfCanBeSwitch
        if (this.length.equals(Pattern.ATTR_LENGTH_02)) {
            this.value = Utility.formatValueByPadding(value,2);
        } else if (this.length.equals(Pattern.ATTR_LENGTH_04)) {
            this.value = Utility.formatValueByPadding(value,4);
        } else {
            this.value = value;
        }
        this.screen = "";
    }

    public String getType() {
        if (this.screen.equals(Screen.RUN.name())) {
            return Pattern.operationParameterMap.get(this.type);
        }

        if (this.screen.equals(Screen.STOP.name())) {
            return Pattern.stopMessageTypeMap.get(this.type);
        }

        if (this.screen.equals(Screen.SETTING.name())) {
            return Pattern.stopMessageTypeMap.get(this.type); // CHANGE THIS
        }

        return this.type;
    }

    public String getLength() {
        return this.length;
    }

    public String getValue() {
        /*if (this.screen.equals(Screen.RUN.name())) {
            return this.value;
        }*/
        /*if (this.screen.equals(Screen.STOP.name())) {
            if (getType().equals(StopMessageType.REASON.name())) {
                return stopReasonValueMap.get(this.value);
            }
        }*/
        /*if (this.screen.equals(Screen.SETTING.name())) {
            return this.value;
        }*/

        return this.value;
    }

}
