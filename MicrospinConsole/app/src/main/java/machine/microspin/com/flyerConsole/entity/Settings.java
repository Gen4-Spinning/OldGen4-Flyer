package machine.microspin.com.flyerConsole.entity;

import android.bluetooth.BluetoothDevice;

/**
 * Settings Repo for storing storing throughout the application lifetime
 */

public class Settings {

    public static BluetoothDevice device;
    //===== Settings Parameters =======
    private static int spindleSpeed;
    private static float tensionDraft;
    private static float twistPerInch;
    private static float RTF;
    private static int maxLengthLimit;
    private static int maxHeightOfContent;
    private static float rovingWidth;
    private static float deltaBobbinDia;
    private static int bareBobbinDia;
    private static int boostFactor;
    private static int buckFactor;

    private static String machineId = "01"; //default
    //==== END: Settings Parameters ====

    final private static String ATTR_COUNT = "01";
    final private static String ATTR_MACHINE_TYPE = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.FLYER.name());;
    final private static String ATTR_MSG_TYPE_BACKGROUND = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
    final private static String ATTR_SCREEN_SUB_STATE_NONE = "00";
    final private static String ATTR_TYPE_FLYER_PATTERN = "60";
    final private static String ATTR_SCREEN_SETTING = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.SETTING.name());
    final private static String ATTR_PACKET_LENGTH = "18"; // HexCode
    final private static int ATTR_LENGTH = 01;
 //Default settings for factory settings
    private static int defaultspindleSpeed = 650;
    private static float defaulttensionDraft = 8.0f;
    private static float defaulttwistPerInch = 1.0f;
    private static float defaultRTF = 1.0f;
    private static int defaultLengthLimit= 1200; //50 layers
    private static int defaultmaxHeightOfContent = 250;
    private static float defaultrovingWidth = 2.0f;
    private static float defaultdeltaBobbinDia = 1.6f;
    private static int defaultbareBobbinDia = 48;

 //====Settings for REQUEST PID =============//
    //only those different from the setting s above is  defined
    final private static String PID_ATTR_PACKET_LENGTH = "0B";  // IN HEX
    final private static String PID_ATTR_PID_PATTERN = "06";
    final private static String PID_REQUEST_ATTR_CODE= "01";
    final private static String PID_NEWSETTINGS_ATTR_CODE= "02";
    final private static int PID_ATTR_LENGTH = 20;

    private static float Kp_f = 0;
    private static float Ki_f = 0;
    private static int startPwmOffset = 0;
    private static float ff_mutiplier = 0;
    //default vals.
    private static float defaultKp_f = 0;
    private static float defaultKi_f = 0;
    private static int defaultstartPwmOffset = 0;
    private static float defaultFFmultiplier = 0;

    // these are all the pid request msgs that you can get from the machine
    public static int pid_req_attr1 = 0;
    public static int pid_req_attr2 = 0;
    public static int pid_req_attr3 = 0;
    public static int pid_req_attr4 = 0;

    public static String getMachineId (){
        return machineId;
    }
    public static Boolean processSettingsPacket(String payload) {
        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);

        // Mapping Setting Parameters.
        spindleSpeed = Utility.convertHexToInt(payload.substring(22, 26));
        tensionDraft = Utility.convertHexToFloat(payload.substring(26, 34));
        twistPerInch = Utility.convertHexToFloat(payload.substring(34, 42));
        RTF = Utility.convertHexToFloat(payload.substring(42, 50));
        maxLengthLimit = Utility.convertHexToInt(payload.substring(50, 54));
        maxHeightOfContent = Utility.convertHexToInt(payload.substring(54, 58));
        rovingWidth = Utility.convertHexToFloat(payload.substring(58, 66));
        deltaBobbinDia = Utility.convertHexToFloat(payload.substring(66, 74));
        bareBobbinDia = Utility.convertHexToInt(payload.substring(74, 78));
        boostFactor = Utility.convertHexToInt(payload.substring(78, 82));
        buckFactor = Utility.convertHexToInt(payload.substring(82, 86));
        return true;
    }

    public static String updateNewSetting(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9 ) {
        // Update new values in Repo.

        spindleSpeed = Integer.parseInt(s1);
        tensionDraft = Float.parseFloat(s2);
        twistPerInch = Float.parseFloat(s3);
        RTF = Float.parseFloat(s4);
        maxLengthLimit = Integer.parseInt(s5);
        maxHeightOfContent = Integer.parseInt(s6);
        rovingWidth = Float.parseFloat(s7);
        deltaBobbinDia = Float.parseFloat(s8);
        bareBobbinDia = Integer.parseInt(s9);
        boostFactor = 10;//Integer.parseInt(s10);
        buckFactor = 10;//Integer.parseInt(s11);

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(ATTR_TYPE_FLYER_PATTERN).
                append(String.format("%02d", ATTR_LENGTH));

        String attr = Utility.convertIntToHexString(spindleSpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(tensionDraft);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertFloatToHex(twistPerInch);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertFloatToHex(RTF);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertIntToHexString(maxLengthLimit);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(maxHeightOfContent);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(rovingWidth);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertFloatToHex(deltaBobbinDia);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertIntToHexString(bareBobbinDia);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(boostFactor);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(buckFactor);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(ATTR_SCREEN_SETTING).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    public static String updateNewPIDSetting(String motorIndex,int s1, int s2, int s3 , int s4) {

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;
        String sender = Packet.SENDER_HMI;

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(PID_NEWSETTINGS_ATTR_CODE).
                append(String.format("%02d", PID_ATTR_LENGTH));


        attrPayload.append(Utility.formatValueByPadding(motorIndex,2));
        String attr = Utility.convertIntToHexString(s1);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(s2);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(s3);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(s4);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(PID_ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(PID_ATTR_PID_PATTERN).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    //==========GETTERS============
    public static int getSpindleSpeed() {
        return spindleSpeed;
    }

    public static String getSpindleSpeedString() {
        return String.format("%d", spindleSpeed);
    }

    public static float getTensionDraft() {
        return tensionDraft;
    }

    public static String getTensionDraftString() {
        String s = String.format("%f", tensionDraft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getMaxLengthLimit() {
        return maxLengthLimit;
    }

    public static String getLengthLimitString() {
        return String.format("%d", maxLengthLimit);
    }

    public static float getTwistPerInch() {
        return twistPerInch;
    }

    public static String getTwistPerInchString() {
        String s = String.format("%f", twistPerInch);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getMaxHeightOfContent() {
        return maxHeightOfContent;
    }

    public static String getMaxHeightOfContentString() {
        return String.format("%d", maxHeightOfContent);
    }

    public static float getRovingWidth() {
        return rovingWidth;
    }

    public static String getRovingWidthString() {
        String s = String.format("%f", rovingWidth);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static float getDeltaBobbinDia() {
        return deltaBobbinDia;
    }

    public static String getDeltaBobbinDiaString() {
        String s = String.format("%f", deltaBobbinDia);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getBareBobbinDia() {
        return bareBobbinDia;
    }

    public static String getBareBobbinString() {
        return String.format("%d", bareBobbinDia);
    }

    public static int getBoostFactor() {
        return boostFactor;
    }

    public static String getBoostFactorString() {
        return String.format("%d", boostFactor);
    }
    public static int getBuckFactor() {
        return buckFactor;
    }

    public static String getBuckFactorString() {
        return String.format("%d", buckFactor);
    }

    public static float getRTF() {
        return RTF;
    }

    public static String getRTFString() {
        String s = String.format("%f", RTF);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    // default strings
    public static String getDefaultSpindleSpeedString() {
        return String.format("%d", defaultspindleSpeed);
    }

    public static String getDefaultTensionDraftString() {
        String s = String.format("%f", defaulttensionDraft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultLengthLimitString() {
        return String.format("%d", defaultLengthLimit);
    }

    public static String getDefaultTwistPerInchString() {
        String s = String.format("%f", defaulttwistPerInch);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultMaxHeightOfContentString() {
        return String.format("%d", defaultmaxHeightOfContent);
    }

    public static String getDefaultRovingWidthString() {
        String s = String.format("%f", defaultrovingWidth);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }
    public static String getDefaultDeltaBobbinDiaString() {
        String s = String.format("%f", defaultdeltaBobbinDia);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultBareBobbinString() {
        return String.format("%d", defaultbareBobbinDia);
    }

    public static String getDefaultRTFString() {
        String s = String.format("%f", defaultRTF);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    /********************PID SETTINGS STUFF ***********************/

    public static String RequestPIDSettings(String motorValue) {

       // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //the TLV
        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();
        attrPayload.append(PID_REQUEST_ATTR_CODE).
                append( Pattern.ATTR_LENGTH_02);
        attrPayload.append(motorValue);

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(PID_ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(PID_ATTR_PID_PATTERN).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    public static Boolean processPIDSettingsPacket(String payload) {

        int motorID ;
        int attr1;
        int attr2;
        int attr3;
        int attr4;

        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);

        // Mapping Setting Parameters.
        motorID = Utility.convertHexToInt(payload.substring(22, 26));
        //float fl = Utility.convertHexToFloat(payload.substring(22, 26));
        pid_req_attr1 = Utility.convertHexToInt(payload.substring(26, 30));
        pid_req_attr2 = Utility.convertHexToInt(payload.substring(30, 34));
        pid_req_attr3 = Utility.convertHexToInt(payload.substring(34, 38));
        pid_req_attr4 = Utility.convertHexToInt(payload.substring(38, 42));
        return true;
    }

    public static String MakeIntString(int input) {
        return String.format("%d", input);
    }

    public static String MakeFloatString(float input,int decimals) {
        String s = String.format("%."+ String.valueOf(decimals) + "f", input);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


}

