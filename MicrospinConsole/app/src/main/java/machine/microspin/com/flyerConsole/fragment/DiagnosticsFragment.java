package machine.microspin.com.flyerConsole.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.flyerConsole.EditTextCustom;
import machine.microspin.com.flyerConsole.R;
import machine.microspin.com.flyerConsole.entity.IntegerInputFilter;
import machine.microspin.com.flyerConsole.entity.Packet;
import machine.microspin.com.flyerConsole.entity.Pattern;
import machine.microspin.com.flyerConsole.entity.Settings;
import machine.microspin.com.flyerConsole.entity.SettingsCommunicator;
import machine.microspin.com.flyerConsole.entity.TLV;
import machine.microspin.com.flyerConsole.entity.Utility;


public class DiagnosticsFragment extends Fragment  implements View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {

    private SettingsCommunicator mCallback;
    //diagnose menu items
    private Spinner testType;
    private Spinner motorCode;
    private Spinner diagnosisType;
    private Spinner subAssemblyType;
    private Spinner LiftTestType;
    private Spinner LiftDirection;
    private EditText runTime;
    private EditText signalValue;
    private EditText liftDistance;
    private EditTextCustom targetRPMPercent;
    private TextView maxRpmTextLabel;
    private TextView maxRpmText;
    private TextView targetRPMOut;
    //layouts in menu
    private LinearLayout motorSelect;
    private LinearLayout subAssemblySelect;
    private LinearLayout motorSetup;
    private LinearLayout liftSetup;

    //diagnose live
    private TextView diagnosisTypeLive;
    private TextView motor_SA_Label;
    private TextView motor_SA_Live;
    private TextView testTypeLive;
    private TextView targetTextLive;
    private TextView targetLabelLive;

    private TextView param1Label;
    private TextView param1Live;
    private TextView param2Label;
    private TextView param2Live;

    private TextView liftmotorsLive;
    private TextView liftDirLive;
    private TextView liftDistLive;

    private LinearLayout normalLiveLayout;
    private LinearLayout liftLiveLayout;

    //others
    private Button runDiagnose;
    private Button stopDiagnose;
    private Snackbar diagRunningSnackBar = null;
    private LinearLayout menuLayout;
    private LinearLayout liveLayout;
    //harsha added
    private Snackbar snackbarComplete ;
    private int maxRPM = 0;
    private int targetRpmCalc = 0;
    private int targetSignalVoltage = 0;

    //booleans
    private static boolean firstInit = false;
    private static boolean liftSelected = false;
    private static boolean subAssemblyMode = false;
    private static boolean isSnackbarOn = false; //needed when we make this activity a fragment

    //=================== STATIC Codes ========================
    final private static String SPINNER_TEST_TYPE = "TEST_TYPE";
    final private static String SPINNER_MOTOR_CODE = "MOTOR_TYPE";

    final private static String LAYOUT_MENU = "MENU";
    final public static String LAYOUT_LIVE = "LIVE";

    final private static String MAX_FLYER_RPM = "650";
    final private static String MAX_LIFT_RPM = "200";

    private String selectedTestTypeForLive = "";
    private String selectedMotor_SA_TypeForLive = "";
    private String targetLiftDistance = "";
    private String selectedDirectionType = "";
    private String selectedLiftMotors = "";

    private BluetoothService mService;
    private BluetoothWriter mWriter;
    private static final String TAG = "Diagnose";

    final private static String STOP_BTN = "STOP";
    final private static String RESET_BTN = "RESET";

    private String stopBtnMode = STOP_BTN;

    public boolean backButton_disable = false;

    public DiagnosticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_diagnostics, container, false);
        if(Settings.device != null) {
            // setTitle(Settings.device.getName());
            getActivity().setTitle(Settings.device.getName());
        }

        //------------------DIAG  MENU----------------
        // first all Spinners
        diagnosisType = rootView.findViewById(R.id.DiagnosisTestType);
        diagnosisType.setOnItemSelectedListener(this);

        motorCode = rootView.findViewById(R.id.motorValue);
        motorCode.setOnItemSelectedListener(this);

        subAssemblyType = rootView.findViewById(R.id.subAssemblyType);
        subAssemblyType.setOnItemSelectedListener(this);

        //test type is open/closed loop
        testType = rootView.findViewById(R.id.testType);
        testType.setOnItemSelectedListener(this);

        //Lift Options
        LiftTestType = rootView.findViewById(R.id.LiftTestType);
        LiftTestType.setOnItemSelectedListener(this);

        LiftDirection = rootView.findViewById(R.id.DirectionType);
        LiftDirection.setOnItemSelectedListener(this);

        //Text boxes
        runTime =  rootView.findViewById(R.id.testRunTime);
        signalValue = rootView.findViewById(R.id.signalValue);
        liftDistance =  rootView.findViewById(R.id.LiftDistVal);

        targetRPMPercent =  rootView.findViewById(R.id.targetRpmPercent);
        targetRPMPercent.setOnFocusChangeListener(this);
        maxRpmTextLabel =  rootView.findViewById(R.id.maxMotorVal);
        maxRpmText = rootView.findViewById(R.id.maxRpmVal);
        targetRPMOut =   rootView.findViewById(R.id.targetRPMout);

        //in menu layout - sub assembly and motors
        motorSelect =  rootView.findViewById(R.id.motorSelect);
        subAssemblySelect =  rootView.findViewById(R.id.SubAssemblySelect);

        //motor options and lift options
        motorSetup =  rootView.findViewById(R.id.MotorSetup);
        liftSetup =  rootView.findViewById(R.id.LiftSetup);

        //------------------LIVE DIAG------------------------//
        //live boxes
        diagnosisTypeLive =  rootView.findViewById(R.id.diagnosisTypeLive);
        motor_SA_Live = rootView.findViewById(R.id.motor_SA_Live);
        motor_SA_Label =  rootView.findViewById(R.id.motor_SA_Label);
        testTypeLive =  rootView.findViewById(R.id.typeOfTestLive);
        param1Label =  rootView.findViewById(R.id.param1Label);
        param1Live =  rootView.findViewById(R.id.param1Live);
        param2Label =  rootView.findViewById(R.id.param2Label);
        param2Live =  rootView.findViewById(R.id.param2Live);
        targetLabelLive =  rootView.findViewById(R.id.targetlabel);
        targetTextLive =  rootView.findViewById(R.id.targetText);

        liftDirLive =  rootView.findViewById(R.id.liftDirLiveVal);
        liftDistLive =  rootView.findViewById(R.id.actualLiftDistLive);
        liftmotorsLive = rootView.findViewById(R.id.liftMotorSelectLive);
        //layouts in  live
        normalLiveLayout =  rootView.findViewById(R.id.normalLive);
        liftLiveLayout =  rootView.findViewById(R.id.LiftLive);

        //--------------------MAIN BUTTON-------------------------------
        runDiagnose =  rootView.findViewById(R.id.runDiagnose);
        runDiagnose.setOnClickListener(this);

        stopDiagnose = (Button)rootView. findViewById(R.id.stopBtn);
        stopDiagnose.setOnClickListener(this);
        stopDiagnose.setText(STOP_BTN);

        //---------------------LAYOUTS----------------------------------
        //Layouts
        menuLayout = rootView.findViewById(R.id.diagnoseMenu);
        liveLayout = rootView.findViewById(R.id.diagnoseLive);

        //------------------------------
        List<String> motorCodeList = getValueListForSpinner(SPINNER_MOTOR_CODE);
        motorCode.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, motorCodeList));
        List<String> testTypeList = getValueListForSpinner(SPINNER_TEST_TYPE);
        testType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, testTypeList));
        List<String> diagnosticTypeList = getValueListForSpinner("DIAGNOSTIC_TYPE");
        diagnosisType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, diagnosticTypeList));
        List<String> SATypeList = getValueListForSpinner("SUBASSEMBLIES");
        subAssemblyType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, SATypeList));
        List<String> liftTestTypesList = getValueListForSpinner("LIFT_TEST_TYPES");
        LiftTestType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, liftTestTypesList));
        List<String> liftDirectionsList = getValueListForSpinner("LIFT_DIRECTIONS");
        LiftDirection.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, liftDirectionsList));

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        setDiagnosisSelectionLayout("MOTOR_DIAGNOSIS");
        subAssemblyMode = false;
        liftSelected = false;
        setOptionsSetupLayout("MOTOR");
        toggleViewOn(LAYOUT_MENU);
        setDefaultValue();


        // Inflate the layout for this fragment
        return rootView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SettingsCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsCommunicator");
        }
    }


    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }




    //====================================== OTHER EVENTS ==========================================
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.runDiagnose:
                runDiagnose(); // sends the packet
                backButton_disable = true; //disables the hardware back button
                break;
            case R.id.stopBtn:
                if (stopBtnMode.equals(STOP_BTN)) {
                    mCallback.writeStopDiagnosis();
                    mCallback.CloseDiagSnackBar();
                    stopDiagnose.setText(RESET_BTN);
                    stopBtnMode = RESET_BTN;
                    if (diagRunningSnackBar != null) {
                        diagRunningSnackBar.dismiss();
                    }
                } else {
                    //ZERO THE LIVE SCREEN VALUES NOW THAT WE RE LEAVING IT
                    param1Live.setText("0");
                    param2Live.setText("0");
                    //SETUP THE NEW MENU SCREEN
                    stopDiagnose.setText(STOP_BTN);
                    stopBtnMode = STOP_BTN;
                    mCallback.DisableTabsForDiagnostics(false);
                    backButton_disable = false;

                    toggleEnableMode(true);
                    setDiagnosisSelectionLayout("MOTOR_DIAGNOSIS");
                    subAssemblyMode = false;
                    liftSelected = false;
                    setOptionsSetupLayout("MOTOR");
                    toggleViewOn(LAYOUT_MENU);
                    setDefaultValue();
                }
                break;
        }
    }


    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (!firstInit) {
            firstInit = true;
        } else {
            if (parent.getId() == R.id.testType) {
                if (pos == 0) {
                    signalValue.setEnabled(true);
                    targetRPMPercent.setEnabled(false);
                    targetRPMPercent.setText("0");
                    targetRPMOut.setText("0");
                    runTime.setText("0");
                } else {
                    signalValue.setEnabled(false);
                    signalValue.setText("0");
                    targetRPMPercent.setEnabled(true);
                    runTime.setText("0");
                }
            }
            if (parent.getId() == R.id.motorValue) {
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
            }

            if (parent.getId() == R.id.DiagnosisTestType){
                String diagnosisSelected = Utility.formatStringCode((diagnosisType.getSelectedItem().toString()));
                if (diagnosisSelected.equals(Pattern.DiagnosticTypes.MOTOR.name())){
                    setDiagnosisSelectionLayout("MOTOR_DIAGNOSIS");
                    subAssemblyMode = false;
                }else{
                    setDiagnosisSelectionLayout("SUBASSEMBLY_DIAGNOSIS");
                    subAssemblyMode = true;
                }
                setOptionsSetupLayout("MOTOR");
                liftSelected = false;
            }

            if (parent.getId() == R.id.subAssemblyType) {
                String subAssemblySelected = Utility.formatStringCode((subAssemblyType.getSelectedItem().toString()));
                if (subAssemblySelected.equals(Pattern.SubAssemblyTypes.LIFT.name())){
                    setOptionsSetupLayout("LIFT");
                    maxRpmTextLabel.setText(getResources().getString(R.string.label_lift_RPM));
                    maxRpmText.setText(MAX_LIFT_RPM);
                    liftSelected = true;
                }else{
                    setOptionsSetupLayout("MOTOR");
                    maxRpmTextLabel.setText(getResources().getString(R.string.label_diagnose_flyer_maxRPM));
                    maxRpmText.setText(MAX_FLYER_RPM);
                    liftSelected = false;
                }

            }

        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }



    //==================================== CUSTOM FUNCTIONS ========================================

    private int GetMaxRPM(String motorSelected)
    {   int maxRpm1 = 1500; // in flyer for all motors from Phillipines2
       /* if (motorSelected.equals(Pattern.MotorTypes.FRONT_ROLLER.toString())){
            maxRpm1 = 3000;
        } else {
            maxRpm1 = 1500;
        }*/
        return maxRpm1;
    }

    private void runDiagnose() {
        Packet diagnosePacket = new Packet(Packet.OUTGOING_PACKET);
        TLV[] attributes = new TLV[5];  //Specified in the requirements
        String attrType;
        String attrValue;
        String attrLength;

        //****** Handling=> testType Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.KIND_OF_TEST.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        String testTypeSelected = Utility.formatStringCode(testType.getSelectedItem().toString());
        attrValue = Pattern.diagnoseTestTypesMap.get(testTypeSelected);
        TLV testType = new TLV(attrType, attrLength, attrValue);
        attributes[0] = testType;
        selectedTestTypeForLive = testTypeSelected;
        //****** Handling=> MotorCode Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.MOTOR_ID.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (!subAssemblyMode){
            String motorCodeSelected = Utility.formatStringCode(motorCode.getSelectedItem().toString());
            attrValue = Pattern.motorMap.get(motorCodeSelected);
            TLV motorCode = new TLV(attrType, attrLength, attrValue);
            attributes[1] = motorCode;
            selectedMotor_SA_TypeForLive = motorCodeSelected;
        }else{
            String subAssemblySelected = Utility.formatStringCode(subAssemblyType.getSelectedItem().toString());
            attrValue = Pattern.subAssemblyMap.get(subAssemblySelected);
            TLV subAssemblyCode = new TLV(attrType, attrLength, attrValue);
            attributes[1] = subAssemblyCode;
            selectedMotor_SA_TypeForLive = subAssemblySelected;
        }

        if (!liftSelected) {

            //****** Handling=> Signal Voltage Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.SIGNAL_VOLTAGE.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(signalValue.getText().toString())) {
                signalValue.setText("0");
            }
            targetSignalVoltage = Integer.parseInt(signalValue.getText().toString());
            attrValue = Utility.convertIntToHexString(targetSignalVoltage);
            TLV signalValue = new TLV(attrType, attrLength, attrValue);
            attributes[2] = signalValue;

            //****** Handling=> Target RPM Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TARGET_RPM.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(targetRPMOut.getText().toString())) {
                targetRPMPercent.setText("0");
                targetRPMOut.setText("0");
            }
            Integer i2 = Integer.parseInt(targetRPMOut.getText().toString());
            attrValue = Utility.convertIntToHexString(i2);
            TLV targetRPM = new TLV(attrType, attrLength, attrValue);
            attributes[3] = targetRPM;

            //******* Handling=> RunTime Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TEST_TIME.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(runTime.getText().toString())) {
                runTime.setText("0");
            }
            Integer i3 = Integer.parseInt(runTime.getText().toString());
            attrValue = Utility.convertIntToHexString(i3);

            TLV runTime = new TLV(attrType, attrLength, attrValue);
            attributes[4] = runTime;
        }else{
            //we need Lift type, lift direction and lift distance
            //1. Lift Type
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.LIFT_TESTTYPE.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            String SelectedLiftType =  Utility.formatStringCode((LiftTestType.getSelectedItem().toString()));
            selectedLiftMotors = SelectedLiftType;
            attrValue = Pattern.LiftTestTypeMap.get(SelectedLiftType);
            TLV liftType = new TLV(attrType, attrLength, attrValue);
            attributes[2] = liftType;

            //2.Lift direction
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.LIFT_DIRECTION.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            selectedDirectionType =  Utility.formatStringCode((LiftDirection.getSelectedItem().toString()));
            attrValue = Pattern.LiftDirectionMap.get(selectedDirectionType);
            TLV direction = new TLV(attrType, attrLength, attrValue);
            attributes[3] = direction;

            //Lift Distance
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.LIFT_DISTANCE.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(liftDistance.getText().toString())) {
                liftDistance.setText("0");
            }
            int targetLiftDistanceInt = Integer.parseInt(liftDistance.getText().toString());
            targetLiftDistance = Integer.toString(targetLiftDistanceInt); // string for using in Live diag
            attrValue = Utility.convertIntToHexString(targetLiftDistanceInt);
            TLV lift_distance = new TLV(attrType, attrLength, attrValue);
            attributes[4] = lift_distance;

        }
        String screen = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.DIAGNOSTICS.name());
        String machineId = Settings.getMachineId();
        String machineType = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.FLYER.name());
        String messageType = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
        String screenSubState = Pattern.COMMON_NONE_PARAM;

        //****CHANGE FOR RELEASE V3 -- harsha
        //****CHANGE FOR RELEASE V2
        //Data Validation for Test RPM & Signal Voltage
        String validateMessage = isValidData();
        if (validateMessage == null) {
            String payload = diagnosePacket.makePacket(screen,
                    machineId,
                    machineType,
                    messageType,
                    screenSubState,
                    attributes);

            mCallback.writeDiagStartCommand(payload);

            /**added here for the fragment**/
            diagRunningSnackBar = Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.msg_diagnose_running, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null);
            diagRunningSnackBar.show();
            toggleEnableMode(false);
            SetUpCorrectLiveScreen();
            toggleViewOn(LAYOUT_LIVE);
            mCallback.DisableTabsForDiagnostics(true);
        } else {
            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), validateMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    private List<String> getValueListForSpinner(final String entity) {
        List<String> list = new ArrayList<>();
        int length;
        switch (entity) {
            case "MOTOR_TYPE":
                length = Pattern.MotorTypes.values().length; // use the spinner motor type not the motor type.because we dont want lift
                while (length > 2) { // we dont want lift left anf lift right
                    String value = Pattern.MotorTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "TEST_TYPE":
                length = Pattern.DiagnosticTestTypes.values().length;
                while (length > 0) {
                    String value = Pattern.DiagnosticTestTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "DIAGNOSTIC_TYPE":
                length = Pattern.DiagnosticTypes.values().length;
                while (length > 0) {
                    String value = Pattern.DiagnosticTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "SUBASSEMBLIES":
                length = Pattern.SubAssemblyTypes.values().length;
                while (length > 0) {
                    String value = Pattern.SubAssemblyTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;

            case "LIFT_TEST_TYPES":
                length = Pattern.LiftTestTypes.values().length;
                while (length > 0) {
                    String value = Pattern.LiftTestTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;

            case "LIFT_DIRECTIONS":
                length = Pattern.LiftDirectionTypes.values().length;
                while (length > 0) {
                    String value = Pattern.LiftDirectionTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
        }
        return list;
    }

    private void toggleEnableMode(final Boolean bol) {
                testType.setEnabled(bol);
                motorCode.setEnabled(bol);
                runTime.setEnabled(bol);
                if (bol) {
                    runDiagnose.setVisibility(View.VISIBLE);
                } else {
                    runDiagnose.setVisibility(View.INVISIBLE);
                }
    }

    private void SetUpCorrectLiveScreen() {
        if (!subAssemblyMode) {
            diagnosisTypeLive.setText(Pattern.DiagnosticTypes.MOTOR.name());
            motor_SA_Label.setText(getResources().getString(R.string.label_diagnose_motor_name));
            //in subassembly mode we re actually ramping to flyer virtual target.
            if (selectedTestTypeForLive.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
                targetLabelLive.setText("Target Signal Voltage %");
                targetTextLive.setText(Integer.toString(targetSignalVoltage));
            }
            if (selectedTestTypeForLive.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
                targetLabelLive.setText("Target RPM ");
                targetTextLive.setText(Integer.toString(targetRpmCalc));
            }
        } else {
            diagnosisTypeLive.setText(Pattern.DiagnosticTypes.SUBASSEMBLY.name());
            motor_SA_Label.setText(getResources().getString(R.string.label_SA_name));
        }
        motor_SA_Live.setText(selectedMotor_SA_TypeForLive);
        testTypeLive.setText(selectedTestTypeForLive);

        if (!liftSelected){
            normalLiveLayout.setVisibility(View.VISIBLE);
            liftLiveLayout.setVisibility(View.GONE);
            if (!subAssemblyMode){
                param1Label.setText(getResources().getString(R.string.label_diagnose_ip_signal));
                param2Label.setText(getResources().getString(R.string.label_diagnose_live_rpm));
            }else{
                if (selectedMotor_SA_TypeForLive.equals(Pattern.SubAssemblyTypes.DRAFTING.name())){
                    param1Label.setText(getResources().getString(R.string.label_drafting_FR_RPM));
                    param2Label.setText(getResources().getString(R.string.label_drafting_BR_RPM));
                }else{
                    param1Label.setText(getResources().getString(R.string.label_drafting_FL_RPM));
                    param2Label.setText(getResources().getString(R.string.label_drafting_BB_RPM));
                }
            }
        } else {
            normalLiveLayout.setVisibility(View.GONE);
            liftLiveLayout.setVisibility(View.VISIBLE);
            liftDirLive.setText(selectedDirectionType);
            targetLabelLive.setText("Target Distance(mm)");
            targetTextLive.setText(targetLiftDistance);
            liftmotorsLive.setText(selectedLiftMotors);
            liftDistLive.setText("0");
        }
    }

    public void toggleViewOn(final String layout) {
                switch (layout) {
                    case LAYOUT_LIVE:
                        if (liveLayout.getVisibility() == View.INVISIBLE) {
                            menuLayout.setVisibility(View.INVISIBLE);
                            liveLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LAYOUT_MENU:
                        menuLayout.setVisibility(View.VISIBLE);
                        liveLayout.setVisibility(View.INVISIBLE);
                        break;
                }
    }

    private void setDiagnosisSelectionLayout(String diagnosisTypeStr){
        //set the vals below the dividing line to all zero ( because both testTypes are closed loop )
        runTime.setText("0");
        signalValue.setText("0");
        targetRPMPercent.setText("0");
        targetRPMPercent.setEnabled(true);
        targetRPMOut.setText("0");

        switch (diagnosisTypeStr) {
            case "MOTOR_DIAGNOSIS":
                diagnosisType.setSelection(0);
                motorSelect.setVisibility(View.VISIBLE); // set the Motor Spinner available an
                motorCode.setSelection(0); // set motor selction to be flyer
                subAssemblySelect.setVisibility(View.GONE); // hide subAssembly spinner
                testType.setSelection(1); //  set closed Loop test (so that its same as sub assembly option)
                testType.setEnabled(true); // enable the test type spinner (open/closed)
                maxRpmTextLabel.setText(getResources().getString(R.string.label_diagnose_motor_maxRPM)); //set the label max rpm
                //get the correct motor Type
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                break;
            case "SUBASSEMBLY_DIAGNOSIS":
                diagnosisType.setSelection(1);
                motorSelect.setVisibility(View.GONE);
                subAssemblySelect.setVisibility(View.VISIBLE);
                subAssemblyType.setSelection(0); // set Drafting as visible..
                testType.setSelection(1); // closed Loop
                testType.setEnabled(false);
                maxRpmTextLabel.setText(getResources().getString(R.string.label_diagnose_flyer_maxRPM));
                maxRpmText.setText(MAX_FLYER_RPM);
                maxRPM = 650;
                break;
        }


    }

    private void setOptionsSetupLayout(String diagnosisType) {
        switch (diagnosisType) {
            case "MOTOR":
                motorSetup.setVisibility(View.VISIBLE);
                liftSetup.setVisibility(View.GONE);
                runTime.setText("0");
                signalValue.setText("0");
                targetRPMPercent.setText("0");
                if (testType.getSelectedItem().equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.name())){
                    targetRPMPercent.setEnabled(false);
                }else {
                    targetRPMPercent.setEnabled(true);
                }
                targetRPMOut.setText("0");
                break;
            case "LIFT":
                motorSetup.setVisibility(View.GONE);
                liftSetup.setVisibility(View.VISIBLE);
                LiftTestType.setSelection(0);
                LiftDirection.setSelection(0);
                liftDistance.setText("0");
                break;
        }
    }

    private void setDefaultValue() {
                runTime.setText("0");
                signalValue.setText("0");
                targetRPMPercent.setText("0");
                signalValue.setEnabled(false); // on start the default is closed loop
                targetRPMOut.setText("0");
                //-----------------------//
                // set the correct max Rpm in the menu screen.
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                //put the logic here only for what the maxRpm should be
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                //----------------------//
                //dont need to set up live screen because for every entry it only runs once.
                // so defaults already set. NEED TO CHECK
        }

    public void updateLiveData(final TLV[] attributes) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                /*String testType = Utility.formatValueByPadding(attributes[0].getValue(), 1);
                testType = Utility.ReverseLookUp(Pattern.diagnoseTestTypesMap, testType);
                String motorCode = Utility.formatValueByPadding(attributes[1].getValue(), 1);
                motorCode = Utility.ReverseLookUp(Pattern.motorMap, motorCode);*/
                String signalVoltage = attributes[2].getValue();
                String targetRPM = attributes[3].getValue();

                /*
                testTypeLive.setText(Utility.formatString(testType));
                motorCodeLive.setText(Utility.formatString(motorCode));
                */
                if (!liftSelected) {
                    param1Live.setText(signalVoltage);
                    param2Live.setText(targetRPM);
                }else{
                    liftDistLive.setText(signalVoltage);
                }
                //runTimeLive.setText(testRunTime);
            }
        });

    }

    private String isValidData() {
        if (!liftSelected) {
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_diagnose_ip_signal), 10, 90);
            IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_diagnose_target_rpm_percent), 10, 90);
            IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_diagnose_run_time), 30, 300);

            String testTypeSelected = Utility.formatStringCode((testType.getSelectedItem().toString()));
            //only check the box you want to use
            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
                if (set1.filter(signalValue) != null) {
                    return set1.filter(signalValue);
                }
            }

            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
                if (set2.filter(targetRPMPercent) != null) {
                    return set2.filter(targetRPMPercent);
                }
            }

            if (set3.filter(runTime) != null) {
                return set3.filter(runTime);
            }
        }else{
            //check only lift distance
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_lift_distance), 2, 250);
            if (set1.filter(liftDistance) != null) {
                return set1.filter(liftDistance);
            }
        }
        return null;
    }


/* HANDLE OUTSIDE*/
   /* @Override
    public void onBackPressed() {
        //Back navigation to Menu Screen.
        //Dont care if the snack bar is shown or not, cos we are closing the app.
        /*if (isSnackbarOn == true) {
            if (snackbarVersion.isShown()) {
                snackbarVersion.dismiss();
            }
        }*/
     /*   // always go back to idle mode on one press
        mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
        //super.onBackPressed();

        //finish this activity
        finish();

    }*/


    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            if (TextUtils.isEmpty(targetRPMPercent.getText().toString())) {
                targetRPMOut.setText(Integer.toString(0));
                targetRPMPercent.setText(Integer.toString(0));
            } else {
                int currentTargetPercent = Integer.parseInt(targetRPMPercent.getText().toString());
                targetRpmCalc = (currentTargetPercent * maxRPM) / 100;
                targetRPMOut.setText(Integer.toString(targetRpmCalc));
            }
        }
    }

}


