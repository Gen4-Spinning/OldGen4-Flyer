package machine.microspin.com.flyerConsole.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for all constants patterns for the incoming/outgoing packets
 */

public interface Pattern {

    String PID_PASSWORD = "900332"; // first 6 digits of Manis phone no

    String ATTR_LENGTH_01 = "01";
    String ATTR_LENGTH_02 = "02";
    String ATTR_LENGTH_04 = "04";
    String COMMON_NONE_PARAM = "00";

    String DISABLE_MACHINE_START_SETTINGS = "7E020B010102030001010200017E";
    String DISABLE_MACHINE_START_DIAGNOSE = "7E020B010102030001010200027E";
    String STOP_DIAG_RUN = "7E020B010102030001000200037E";     
    String ENABLE_MACHINE_START = "7E020B010102020001000200007E";

    String ENABLE_LOG = "7E020B0101020A00010002007E";
    String DISABLE_LOG = "7E020B0101020B00010002007E";
    String START_STOP_RESTART_SIGNAL = "7E020B010102020001000200007E";

    String UPDATE_RTF_ADD = "7E020B010302070001010200027E";
    String UPDATE_RTF_SUB = "7E020B010302070001010200037E";

    String ERRUSERMSG = "OVERLOAD?";


    /* INCOMING PACKET PATTERNS */
    enum MachineType {
        CARDING_MACHINE, FLYER, DRAW_FRAME, RING_FRAME
    }

    enum Screen {
        IDLE, RUN, STOP, SETTING, DIAGNOSTICS, PID_REQUEST, UPDATE_RTF
    }

    enum ScreenSubState {
        NORMAL, RAMP_UP, PIECING, HOMING, PAUSE, HALT, IDLE, ACK,SAVED,  DIAG_SUCCESS,DIAG_FAIL,DIAG_INCOMPLETE, RTF_CHANGE, RTF_ADD, RTF_SUBTRACT
    }

    enum Sender {
        MACHINE, HMI
    }

    enum MessageType {
        SCREEN_DATA, BACKGROUND_DATA
    }

    enum OperationParameter {
        CYLINDER, BEATER, PRODUCTION, PRODUCTION_SPEED, RTF, LAYERS
    }

    enum StopMessageType {
        REASON, MOTOR_ERROR_CODE, ERROR_VAL
    }

    enum StopReasonValue { // Specific to Flyer Frame. NOT GENERAL
        FLYER, BOBBIN, FRONT_ROLLER, BACK_ROLLER,LIFT, LIFT_RIGHT, LIFT_LEFT, USER_PRESS_PAUSE,SLIVER_CUT, BOBBIN_BED_LIFT, LENGTH_COMPLETE
    }


    enum MotorErrorCode {
        RPM_ERROR, MOTOR_VOLTAGE_ERROR, DRIVER_VOLTAGE_ERROR, SIGNAL_VOLTAGE_ERROR, OVERLOAD_CURRENT_ERROR
    }

    enum MotorTypes { // Specific to Flyer Frame. NOT GENERAL
        LIFT_RIGHT, LIFT_LEFT,BACK_ROLLER ,FRONT_ROLLER, BOBBIN ,FLYER
    }

    enum PID_OPTIONS {
        RAMP_RATES,RTF_MULTIPLIERS,LIFT_RIGHT, LIFT_LEFT,BACK_ROLLER ,FRONT_ROLLER, BOBBIN ,FLYER
    }


    /* OUTGOING PACKET PATTERNS */
    enum Information {
        PAIRED, DISABLE_MACHINE_START, SETTINGS_CHANGE_VALS, HW_CHANGE_VALS, DIAGNOSTICS
    }

    enum InformationSubType {
        NONE
    }

    enum DisableMachineStartType {
        SCREEN_ENTERED_FROM_IDLE
    }

    enum DisableMachineStartValue {
        SETTINGS_CHANGE, DIAGNOSTIC, HW_CHANGE
    }

    enum DiagnosticTestTypes {
        CLOSED_LOOP, OPEN_LOOP
    }

    enum DiagnosticTypes {
        SUBASSEMBLY,MOTOR
    }

    enum LiftTestTypes {
        BOTH
    }

    enum LiftDirectionTypes {
        DOWN,UP
    }


    enum SubAssemblyTypes {
        LIFT,WINDING,DRAFTING
    }
    enum DiagnosticAttrType {
        KIND_OF_TEST, MOTOR_ID, SIGNAL_VOLTAGE, TARGET_RPM, TEST_TIME,LIFT_TESTTYPE,LIFT_DIRECTION,LIFT_DISTANCE
    }

    enum Setting {
        DELIVERY_SPEED, TENSION_DRAFT, CYLINDER_SPEED, CYLINDER_FEED, BEATER_SPEED, BEATER_FEED, CONVEYOR_SPEED, CONVEYOR_DELAY, CONVEYOR_DWELL
    }

    /* INCOMING PACKET PATTERNS MAPPING */
    Map<String, String> machineTypeMap = new HashMap<String, String>() {
        {
            put("01", MachineType.CARDING_MACHINE.name());
            put("02", MachineType.DRAW_FRAME.name());
            put("03", MachineType.FLYER.name());
            put("04", MachineType.RING_FRAME.name());
        }
    };

    Map<String, String> screenMap = new HashMap<String, String>() {
        {
            put("01", Screen.IDLE.name());
            put("02", Screen.RUN.name());
            put("03", Screen.STOP.name());
            put("04", Screen.SETTING.name());
            put("05", Screen.DIAGNOSTICS.name());
            put("06", Screen.PID_REQUEST.name());
            // Flyer parameters:
            put("07", Screen.UPDATE_RTF.name());
        }
    };

    Map<String, String> screenSubStateMap = new HashMap<String, String>() {
        {
            put("00", ScreenSubState.IDLE.name());
            put("01", ScreenSubState.NORMAL.name());
            put("02", ScreenSubState.RAMP_UP.name());
            put("03", ScreenSubState.PIECING.name());
            put("04", ScreenSubState.HOMING.name());
            put("11", ScreenSubState.PAUSE.name());
            put("12", ScreenSubState.HALT.name());
            put("96", ScreenSubState.DIAG_INCOMPLETE.name());
            put("97", ScreenSubState.DIAG_FAIL.name());
            put("98", ScreenSubState.DIAG_SUCCESS.name());
            put("88", ScreenSubState.SAVED.name()); //settings saved
            put("99", ScreenSubState.ACK.name());
            // Flyer parameters:
            //put("13", ScreenSubState.RTF_CHANGE.name()); // Currently in Attr Type in TLV
            //put("14", ScreenSubState.RTF_ADD.name()); // Currently in Attr Value in TLV
            //put("15", ScreenSubState.RTF_SUBTRACT.name()); // Currently in Attr Value in TLV
        }
    };

    Map<String, String> senderMap = new HashMap<String, String>() {
        {
            put("01", Sender.MACHINE.name());
            put("02", Sender.HMI.name());
        }
    };

    Map<String, String> messageTypeMap = new HashMap<String, String>() {
        {
            put("01", MessageType.SCREEN_DATA.name());
            put("02", MessageType.BACKGROUND_DATA.name());
        }
    };

    Map<String, String> operationParameterMap = new HashMap<String, String>() {
        {
            put("00", "");
            put("01", OperationParameter.CYLINDER.name());
            put("02", OperationParameter.BEATER.name());
            put("05", OperationParameter.PRODUCTION.name());
            //--Draw-frame parameters:
            put("06", OperationParameter.PRODUCTION_SPEED.name());
            // Flyer parameters:
            put("06", OperationParameter.RTF.name());
            put("07", OperationParameter.LAYERS.name());
        }
    };

    Map<String, String> stopMessageTypeMap = new HashMap<String, String>() {
        {
            put("01", StopMessageType.REASON.name());
            put("02", StopMessageType.MOTOR_ERROR_CODE.name());
            put("03", StopMessageType.ERROR_VAL.name());
            put("00", "");
        }
    };

    Map<String, String> stopReasonValueMap = new HashMap<String, String>() {
        {
            put("0008", StopReasonValue.USER_PRESS_PAUSE.name());
			put("0009", StopReasonValue.SLIVER_CUT.name());
            put("0010", StopReasonValue.LENGTH_COMPLETE.name());
            put("0011", StopReasonValue.BOBBIN_BED_LIFT.name());
			// Flyer parameters:
            put("0050", StopReasonValue.FLYER.name());
            put("0051", StopReasonValue.BOBBIN.name());
            put("0052", StopReasonValue.FRONT_ROLLER.name());
            put("0053", StopReasonValue.BACK_ROLLER.name());
            put("0054", StopReasonValue.LIFT_RIGHT.name());
            put("0055", StopReasonValue.LIFT_LEFT.name());
            put("0056", StopReasonValue.LIFT.name());
        }
    };

    Map<String, String> motorErrorCodeMap = new HashMap<String, String>() {
        {
            put("0002", ERRUSERMSG);//MotorErrorCode.RPM_ERROR.name());
            put("0003", MotorErrorCode.MOTOR_VOLTAGE_ERROR.name());
            put("0004", MotorErrorCode.DRIVER_VOLTAGE_ERROR.name());
        }
    };

    Map<String, String> motorMap = new HashMap<String, String>() {
        {
            put(MotorTypes.FLYER.name(), "50");
            put(MotorTypes.BOBBIN.name(), "51");
            put(MotorTypes.FRONT_ROLLER.name(), "52");
            put(MotorTypes.BACK_ROLLER.name(), "53");
            put(MotorTypes.LIFT_RIGHT.name(), "54");
            put(MotorTypes.LIFT_LEFT.name(), "55");
            // put(MotorTypes.LIFT.name(), "56"); // lift motor not shown in spinner, but used in LIFT error
        }
    };

    Map<String, String> PIDOptionMap = new HashMap<String, String>() {
        {
            put(MotorTypes.FLYER.name(), "50");
            put(MotorTypes.BOBBIN.name(), "51");
            put(MotorTypes.FRONT_ROLLER.name(), "52");
            put(MotorTypes.BACK_ROLLER.name(), "53");
            put(MotorTypes.LIFT_RIGHT.name(), "54");
            put(MotorTypes.LIFT_LEFT.name(), "55");
            put(PID_OPTIONS.RTF_MULTIPLIERS.name(), "33");
            put(PID_OPTIONS.RAMP_RATES.name(), "34");
        }
    };

    Map<String, String> diagnoseTestTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticTestTypes.CLOSED_LOOP.name(), "01");
            put(DiagnosticTestTypes.OPEN_LOOP.name(), "02");
        }
    };

    Map<String, String> diagnoseAttrTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticAttrType.KIND_OF_TEST.name(), "01");
            put(DiagnosticAttrType.MOTOR_ID.name(), "02");
            put(DiagnosticAttrType.SIGNAL_VOLTAGE.name(), "03");
            put(DiagnosticAttrType.TARGET_RPM.name(), "04");
            put(DiagnosticAttrType.TEST_TIME.name(), "05");
            put(DiagnosticAttrType.LIFT_TESTTYPE.name(), "06");
            put(DiagnosticAttrType.LIFT_DIRECTION.name(), "07");
            put(DiagnosticAttrType.LIFT_DISTANCE.name(), "08");
        }
    };

    Map<String, String> LiftTestTypeMap = new HashMap<String, String>() {
        {
            put(LiftTestTypes.BOTH.name(), "01");
            //put(LiftTestTypes.LEFT.name(), "02");
            //put(LiftTestTypes.RIGHT.name(), "03");
        }
    };

    Map<String, String> LiftDirectionMap = new HashMap<String, String>() {
        {
            put(LiftDirectionTypes.UP.name(), "01");
            put(LiftDirectionTypes.DOWN.name(), "02");

        }
    };

    Map<String, String> subAssemblyMap = new HashMap<String, String>() {
        {
            put(SubAssemblyTypes.LIFT.name(), "56");
            put(SubAssemblyTypes.DRAFTING.name(), "57");
            put(SubAssemblyTypes.WINDING.name(), "58");
        }

    };

}

