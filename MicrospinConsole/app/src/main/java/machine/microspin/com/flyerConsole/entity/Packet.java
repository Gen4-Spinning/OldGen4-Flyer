package machine.microspin.com.flyerConsole.entity;

/**
 * Identify Packet in the data for identifying the action required
 * PATTERN FORMAT:
 * <FORMAT>
 * SOF(0x7E)|Sender|Packet Length|Machine Id|Machine Type|Msg Type|Next Screen|Screen SubState|
 * Attribute Count|Attribute Type|Attribute Length|Value|Attribute Type|Attribute Length|Value|
 * Attribute Type|Attribute Length|Value|EOF(0x7E)
 * </FORMAT>
 */

public class Packet implements Pattern {
    @SuppressWarnings("CanBeFinal")
    private int packetType;
    private String SOF;
    private String sender;
    private int packetLength;
    private String machineId;
    private String machineType;
    private String msgType;
    private String nextScreen;
    private String screenSubState;
    private Integer attributeCount;
    private TLV[] attributes;
    private String EOF;

    public final static int INCOMING_PACKET = 1;
    public final static int OUTGOING_PACKET = 0;
    public final static String START_IDENTIFIER = "7E";
    public final static String END_IDENTIFIER = "7E";
    public final static String SENDER_HMI = "02";
    public final static String SENDER_MACHINE = "01";

    public Packet(int packetType) {
        if (packetType == INCOMING_PACKET) {
            this.packetType = INCOMING_PACKET;
        } else {
            this.packetType = OUTGOING_PACKET;
        }
    }

    public Boolean processIncomePayload(String payload) {
        String type, length, value;

        if (this.packetType != INCOMING_PACKET) {
            return false;
        }

        if (payload.length() < 20) {
            return false;
        }

        this.SOF = payload.substring(0, 2);
        int packetLength = payload.length();
        this.EOF = payload.substring(packetLength - 2, packetLength);

        if (!this.SOF.equals(START_IDENTIFIER) || !this.EOF.equals(END_IDENTIFIER)) {
            return false;
        }

        this.sender = payload.substring(2, 4);
        this.packetLength = Integer.parseInt(payload.substring(4, 6));
        this.machineId = payload.substring(6, 8);
        this.machineType = payload.substring(8, 10);
        this.msgType = payload.substring(10, 12);
        this.nextScreen = payload.substring(12, 14);
        this.screenSubState = payload.substring(14, 16);
        this.attributeCount = Integer.parseInt(payload.substring(16, 18));
        this.attributes = new TLV[this.attributeCount];
        int k = 18;                                                                                 // Attributes start index
        for (int i = 0; i < this.attributeCount; i++) {
            type = payload.substring(k, k + 2);
            k = k + 2;
            length = payload.substring(k, k + 2);
            k = k + 2;
            int tempLength = Integer.parseInt(length);
            value = payload.substring(k, k + (tempLength * 2));
            k = k + (tempLength * 2);
            this.attributes[i] = new TLV(type, length, value, getNextScreen());
        }

        return true;
    }

    public String getSender() {
        return Pattern.senderMap.get(this.sender);
    }

    public String getMachineType() {
        return Pattern.machineTypeMap.get(this.machineType);
    }

    public String getMessageType() {
        return Pattern.messageTypeMap.get(this.msgType);
    }

    public String getNextScreen() {
        return screenMap.get(this.nextScreen);
    }

    public String getScreenSubState() {
        return Pattern.screenSubStateMap.get(this.screenSubState);
    }

    public TLV[] getAttributes() {
        return this.attributes;
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String makePacket(String screen, String machineId, String machineType, String msgType, String screenSubState, TLV[] attributes) {
        StringBuilder payload = new StringBuilder();

        //Delimiters
        this.SOF = START_IDENTIFIER;
        this.EOF = END_IDENTIFIER;

        //Identify Sender
        if (this.packetType == Packet.OUTGOING_PACKET) {
            this.sender = SENDER_HMI;
        } else {
            this.sender = SENDER_MACHINE;
        }

        //Constructing attribute string
        String attrPayload = "";
        this.attributeCount = attributes.length;
        this.attributes = attributes;
        for (TLV attr : this.attributes) {
            attrPayload = attrPayload + attr.getType() + attr.getLength() + attr.getValue();
        }

        //Handling header Data
        this.machineId = machineId;
        this.machineType = machineType;
        this.msgType = msgType;
        this.screenSubState = screenSubState;
        this.nextScreen = screen;

        //Getting Packet length
        this.packetLength =
                this.machineId.length() +
                this.machineType.length() +
                this.msgType.length() +
                this.nextScreen.length() +
                this.screenSubState.length() +
                this.attributeCount +
                attrPayload.length();

        String attrCount = Utility.formatValueByPadding(this.attributeCount.toString(),1);
        //Construct payload string
        payload.append(this.SOF).
                append(this.sender).
                append(this.packetLength).
                append(this.machineId).
                append(this.machineType).
                append(this.msgType).
                append(this.nextScreen).
                append(this.screenSubState).
                append(attrCount).
                append(attrPayload).
                append(this.EOF);

        return payload.toString();
    }

    public static String getHeadersScreen(String payload) {
        if (Pattern.screenMap.get(payload.substring(12, 14)) != null) {
            return Pattern.screenMap.get(payload.substring(12, 14));
        }
        return "";
    }

    public static String getHeadersSubScreen(String payload) {
        if (Pattern.screenSubStateMap.get(payload.substring(14, 16)) != null) {
            return Pattern.screenSubStateMap.get(payload.substring(14, 16));
        }
        return "";
    }

    public static Boolean LogPacket(String payload) {
        String SOF = payload.substring(0, 2);
        if (SOF.equals(START_IDENTIFIER)){
            return false;
        }else{
            return true;
        }
    }
}
