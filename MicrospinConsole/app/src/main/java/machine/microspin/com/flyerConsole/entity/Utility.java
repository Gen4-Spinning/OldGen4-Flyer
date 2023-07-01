package machine.microspin.com.flyerConsole.entity;

import java.util.Map;

/**
 * Utility class for common conversion functionality
 */

public class Utility {

    public static float convertHexToFloat(String value) {
        Long i = Long.parseLong(value, 16);
        return Float.intBitsToFloat(i.intValue());
    }

    public static String convertIntToHexString(Integer i) {
        return Integer.toHexString(i);
    }

    public static String convertFloatToHex(Float fl){
        String s = hex(fl);
        if(s.split("0x").length == 2){
            return s.split("0x")[1];
        }
        return "";
    }

    //Used for float -> Hex
    private static String hex(int n) {
        return String.format("0x%8s", Integer.toHexString(n)).replace(' ', '0');
    }

    //Used for float -> Hex
    private static String hex(float f) {
        return hex(Float.floatToRawIntBits(f));
    }

    public static String formatValueByPadding(String value, int length){
        switch (length){
            case 4:
                return ("00000000" + value).substring(value.length());
            case 2:
                return ("0000" + value).substring(value.length());
            case 1:
                return ("00" + value).substring(value.length());
        }
        return "";
    }

    public static String convertHexToIntString(String val) {
        Integer temp = Integer.parseInt(val, 16);
        return temp.toString();
    }

    public static Integer convertHexToInt(String val) {
        return Integer.parseInt(val, 16);
    }

    public static String formatString(String str) {
        String label, unit;
        String[] strArr;
        strArr = str.split("\\(");
        label = strArr[0];
        label = label.replace("_", " ");
        //****** Release Change v2
        //label = capitalizeString(label);
        label = label.toUpperCase();
        if (strArr.length > 1) {
            unit = strArr[1];
            unit = unit.toLowerCase();
            unit = "(" + unit;
            return label + " " + unit;
        }
        return label;
    }

    public static String formatStringCode(String str){
        return str.toUpperCase().replace(" ","_");
    }

    private static String capitalizeString(String val) {
        String[] words = val.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0))).append(words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }


    public static <K, V> K ReverseLookUp(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


}
