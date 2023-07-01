package machine.microspin.com.flyerConsole.fragment;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.flyerConsole.R;
import machine.microspin.com.flyerConsole.entity.DoubleInputFilter;
import machine.microspin.com.flyerConsole.entity.IntegerInputFilter;
import machine.microspin.com.flyerConsole.entity.Pattern;
import machine.microspin.com.flyerConsole.entity.SettingsCommunicator;
import machine.microspin.com.flyerConsole.entity.Settings;
import machine.microspin.com.flyerConsole.entity.Utility;

/**
 * Fragment to handle Settings (Editable and non Editable)
 */

public class SettingsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private SettingsCommunicator mCallback;
    public EditText setting1, setting2, setting3, setting4, setting5, setting6, setting7, setting8, setting9;
    public EditText Kpsetting,Kisetting,startOffsetsetting; // pid motor options
    public EditText rampUpM_setting,rampDownM_setting; //df ramp multiplier
    public EditText rampUp_rate_setting,rampDown_rate_setting; //df stop vars

    public Button saveBtn,factorystngsBtn,PIDBtn,refreshPIDBtn,savePIDBtn;
    public ScrollView settingsScroll,PIDScroll;
    public Spinner pidOptionTypes;
    private String SETTINGS = "SETTINGS";
    private String PIDSETTINGS = "PID";

    // PID LAYOUTS
    private int PID_MOTOR_LAYOUT = 0;
    private int PID_FF_RTF_MULTIPLIER_LAYOUT = 1;
    private int PID_FF_RAMP_RATES_LAYOUT = 2;

    private String currentSettingsScreen = SETTINGS;
    public int PID_current_Layout = PID_MOTOR_LAYOUT;
    public Boolean waitingForPIDSettingsResponse = false;
    public LinearLayout PID_motor_optionView, PID_FF_rampRTF_settings, PID_FF_rampRates_settings;

    private String ZEROSTRING = "0";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_settings, container, false);

        setting1 = (EditText) rootView.findViewById(R.id.setting1);
        setting2 = (EditText) rootView.findViewById(R.id.setting2);
        setting3 = (EditText) rootView.findViewById(R.id.setting3);
        setting4 = (EditText) rootView.findViewById(R.id.setting4);
        setting5 = (EditText) rootView.findViewById(R.id.setting5);
        setting6 = (EditText) rootView.findViewById(R.id.setting6);
        setting7 = (EditText) rootView.findViewById(R.id.setting7);
        setting8 = (EditText) rootView.findViewById(R.id.setting8);
        setting9 = (EditText) rootView.findViewById(R.id.setting9);

        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);

        factorystngsBtn = (Button) rootView.findViewById(R.id.factorystngs);
        factorystngsBtn.setOnClickListener(this);
        setStatusInputFields(false);

        /* Pid Setting Stuff */
        PIDBtn = (Button)rootView.findViewById(R.id.pidBtn);
        PIDBtn.setOnClickListener(this);
        String buttonText = getString(R.string.msg_setting_next1);
        PIDBtn.setText(buttonText);

        refreshPIDBtn = (Button) rootView.findViewById(R.id.refreshPIDbtn);
        buttonText = getString(R.string.msg_PIDSetting_buttonRefresh);
        refreshPIDBtn.setText(buttonText);
        refreshPIDBtn.setOnClickListener(this);
        savePIDBtn = (Button) rootView.findViewById(R.id.savePIDbtn);
        savePIDBtn.setOnClickListener(this);

        settingsScroll = (ScrollView)rootView.findViewById(R.id.settingsScroll);
        PIDScroll = (ScrollView) rootView.findViewById(R.id.settingsPID);

        settingsScroll.setVisibility(View.VISIBLE);
        PIDScroll.setVisibility(View.INVISIBLE);

        //pid option value
        pidOptionTypes = (Spinner)rootView.findViewById(R.id.pidOptionValues);
        pidOptionTypes.setOnItemSelectedListener(this);

        List<String> motorCodeList = getValueListForPIDOptions();
        ArrayAdapter<String> spinnerArray = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item, motorCodeList);
        pidOptionTypes.setAdapter(spinnerArray);

        // Pid Linear Arrays for different types of options
        PID_motor_optionView = (LinearLayout)rootView.findViewById(R.id.PID_layout);
        PID_FF_rampRTF_settings = (LinearLayout)rootView.findViewById(R.id.Ramp_RTFMultiplier);
        PID_FF_rampRates_settings = (LinearLayout)rootView.findViewById(R.id.RampRates);
        //texts within each options - motor pid options
        Kpsetting =  (EditText)rootView.findViewById(R.id.kpSetting);
        Kisetting =  (EditText)rootView.findViewById(R.id.kiSetting);
        startOffsetsetting =  (EditText)rootView.findViewById(R.id.startoffsetSetting);
        // ff rtf multiplier vars options
        rampUpM_setting =  (EditText)rootView.findViewById(R.id.rampUpMSetting);
        rampDownM_setting =  (EditText)rootView.findViewById(R.id.rampDownMSetting);
        //ff ramp rates var options
        rampUp_rate_setting =  (EditText)rootView.findViewById(R.id.rampUpRateSetting);
        rampDown_rate_setting =  (EditText)rootView.findViewById(R.id.rampDownRateSetting);

        PID_current_Layout = PID_MOTOR_LAYOUT;

        currentSettingsScreen = SETTINGS;

        /***************************/

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveBtn) {
            if (TextUtils.isEmpty(setting1.getText().toString()))
            {
                setting1.setText("0");
            }
            if (TextUtils.isEmpty(setting2.getText().toString()))
            {
                setting2.setText("0");
            }
            if (TextUtils.isEmpty(setting3.getText().toString()))
            {
                setting3.setText("0");
            }
            if (TextUtils.isEmpty(setting4.getText().toString()))
            {
                setting4.setText("0");
            }
            if (TextUtils.isEmpty(setting5.getText().toString()))
            {
                setting5.setText("0");
            }
            if (TextUtils.isEmpty(setting6.getText().toString()))
            {
                setting6.setText("0");
            }
            if (TextUtils.isEmpty(setting7.getText().toString()))
            {
                setting7.setText("0");
            }
            if (TextUtils.isEmpty(setting8.getText().toString()))
            {
                setting8.setText("0");
            }
            if (TextUtils.isEmpty(setting9.getText().toString()))
            {
                setting9.setText("0");
            }



            String validateMessage = isValidSettings();
            if (validateMessage == null) {
                String payload = Settings.updateNewSetting(
                        setting1.getText().toString(),
                        setting2.getText().toString(),
                        setting3.getText().toString(),
                        setting4.getText().toString(),
                        setting5.getText().toString(),
                        setting6.getText().toString(),
                        setting7.getText().toString(),
                        setting8.getText().toString(),
                        setting9.getText().toString()
                );
                mCallback.onSettingsUpdate(payload.toUpperCase());
            } else {
                mCallback.raiseMessage(validateMessage);
            }

        }
        if (v.getId() == R.id.factorystngs){
            setting1.setText(Settings.getDefaultSpindleSpeedString());
            setting2.setText(Settings.getDefaultTensionDraftString());
            setting3.setText(Settings.getDefaultTwistPerInchString());
            setting4.setText(Settings.getDefaultRTFString());
            setting5.setText(Settings.getDefaultLengthLimitString());
            setting6.setText(Settings.getDefaultMaxHeightOfContentString());
            setting7.setText(Settings.getDefaultRovingWidthString());
            setting8.setText(Settings.getDefaultDeltaBobbinDiaString());
            setting9.setText(Settings.getDefaultBareBobbinString());
        }

        if (v.getId() == R.id.pidBtn){
            if (currentSettingsScreen.equals(SETTINGS)) {
                settingsScroll.setVisibility(View.INVISIBLE);
                PIDScroll.setVisibility(View.VISIBLE);
                String buttonText = getString(R.string.msg_setting_back);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.GONE);
                factorystngsBtn.setVisibility(View.GONE);
                currentSettingsScreen = PIDSETTINGS;
                ChangePIDLayoutState(PID_current_Layout);
            }else{
                settingsScroll.setVisibility(View.VISIBLE);
                PIDScroll.setVisibility(View.INVISIBLE);
                String buttonText = getString(R.string.msg_setting_next1);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.VISIBLE);
                factorystngsBtn.setVisibility(View.VISIBLE);
                currentSettingsScreen = SETTINGS;
                CancelPIDWaitingResponseState();
                mCallback.ClosePIDWaitSnackBar();
            }
        }

        if (v.getId() == R.id.refreshPIDbtn){
            if (!waitingForPIDSettingsResponse){
                pidOptionTypes.setEnabled(false);
                String buttonText = getString(R.string.msg_PIDSetting_buttonCancel);
                refreshPIDBtn.setText(buttonText);
                // depending on the current motor layout get the correct vals
                Enable_PID_EditTexts(PID_current_Layout,false);
                // send only the request option to the macine to get back the correct value
                String pidOptionSelected = Utility.formatStringCode(pidOptionTypes.getSelectedItem().toString());
                String attrValue = Pattern.PIDOptionMap.get(pidOptionSelected);
                attrValue = Utility.formatValueByPadding(attrValue,2);
                String payload = Settings.RequestPIDSettings(attrValue);
                mCallback.onPIDRequest(payload);
                waitingForPIDSettingsResponse = true;
                }else{
                    CancelPIDWaitingResponseState();
                    Snackbar.make(getView(), R.string.msg_refresh_cancelled, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                     ChangePIDLayoutState(PID_current_Layout); // change if necessary and set defaults
                 }
            }

        if (v.getId() == R.id.savePIDbtn) {
            Check_PID_TextBoxes(PID_current_Layout);
            String validateMessage = isValidPIDSettings(PID_current_Layout);

            if( validateMessage == null) {
                String pidOptionSelected = Utility.formatStringCode(pidOptionTypes.getSelectedItem().toString());
                String attrValue = Pattern.PIDOptionMap.get(pidOptionSelected);
                // check which layout and get the current value

                int attr1Int= 0;
                int attr2Int = 0;
                int attr3Int = 0;
                if (PID_current_Layout == PID_MOTOR_LAYOUT) {
                    attr1Int  = (int)(Float.parseFloat(Kpsetting.getText().toString()) * 100);
                    attr2Int = (int)(Float.parseFloat(Kisetting.getText().toString()) * 100);
                    attr3Int = Integer.parseInt(startOffsetsetting.getText().toString());
                }
                else if (PID_current_Layout == PID_FF_RTF_MULTIPLIER_LAYOUT){
                    attr1Int  = (int)(Float.parseFloat(rampUpM_setting.getText().toString()) * 100);
                    attr2Int = (int)(Float.parseFloat(rampDownM_setting.getText().toString()) * 100);
                    attr3Int = 0;
                }
                else{
                    attr1Int  = Integer.parseInt(rampUp_rate_setting.getText().toString());
                    attr2Int =  Integer.parseInt(rampDown_rate_setting.getText().toString());
                    attr3Int = 0;
                }
                String payload = Settings.updateNewPIDSetting(attrValue,
                        attr1Int,
                        attr2Int,
                        attr3Int
                );

                mCallback.onSettingsUpdate(payload.toUpperCase());
            }else{
                mCallback.raiseMessage(validateMessage);
            }
        }
    }

    private void Check_PID_TextBoxes(int pidLayout){
        if (pidLayout == PID_MOTOR_LAYOUT){
            if (TextUtils.isEmpty(Kpsetting.getText().toString()))
            { Kpsetting.setText("0"); }
            if (TextUtils.isEmpty(Kisetting.getText().toString()))
            { Kisetting.setText("0"); }
            if (TextUtils.isEmpty(startOffsetsetting.getText().toString()))
            { startOffsetsetting.setText("0"); }

        }else if (pidLayout == PID_FF_RTF_MULTIPLIER_LAYOUT){
            if (TextUtils.isEmpty(rampUpM_setting.getText().toString()))
            { rampUpM_setting.setText("0"); }
            if (TextUtils.isEmpty(rampDownM_setting.getText().toString()))
            { rampDownM_setting.setText("0"); }
        }else{
            if (TextUtils.isEmpty(rampUp_rate_setting.getText().toString()))
            { rampUp_rate_setting.setText("0"); }
            if (TextUtils.isEmpty(rampDown_rate_setting.getText().toString()))
            { rampDown_rate_setting.setText("0"); }
        }
    }

   private void SetSettingsScreenFromPID() {
        settingsScroll.setVisibility(View.VISIBLE);
        PIDScroll.setVisibility(View.INVISIBLE);
        String buttonText = getString(R.string.msg_setting_next1);
        PIDBtn.setText(buttonText);
        saveBtn.setVisibility(View.VISIBLE);
        factorystngsBtn.setVisibility(View.VISIBLE);
        currentSettingsScreen = SETTINGS;
        CancelPIDWaitingResponseState();
        mCallback.ClosePIDWaitSnackBar();
    }
    private String isValidPIDSettings(int pidLayout) {

        if (pidLayout == PID_MOTOR_LAYOUT) {
            DoubleInputFilter set1 = new DoubleInputFilter(getString(R.string.label_Kp), 0.01, 2.0);
            DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_Ki), 0.01, 2.0);
            IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_startingOffset), 0, 700);

            if (set1.filter(Kpsetting) != null) {
                return set1.filter(Kpsetting);
            }
            if (set2.filter(Kisetting) != null) {
                return set2.filter(Kisetting);
            }
            if (set3.filter(startOffsetsetting) != null) {
                return set3.filter(startOffsetsetting);
            }
        }
        else if (pidLayout == PID_FF_RTF_MULTIPLIER_LAYOUT){
            DoubleInputFilter set1 = new DoubleInputFilter(getString(R.string.label_rampUp_M), 0.5, 1.5);
            DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_rampDown_M), 0.5, 1.5);

            if (set1.filter(rampUpM_setting) != null) {
                return set1.filter(rampUpM_setting);
            }
            if (set2.filter(rampDownM_setting) != null) {
                return set2.filter(rampDownM_setting);
            }
        }
        else{
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_rampUp_rate), 5, 100);
            IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_rampDown_rate), 5, 100);

            if (set1.filter(rampUp_rate_setting) != null) {
                return set1.filter(rampUp_rate_setting);
            }
            if (set2.filter(rampDown_rate_setting) != null) {
                return set2.filter(rampDown_rate_setting);
            }
        }

        return null;
    }

    private String isValidSettings() {
        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_setting_1), 550, 650); //Spindle Speed
        DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_setting_2), 5.5, 9.9); //Draft
        DoubleInputFilter set3 = new DoubleInputFilter(getString(R.string.label_setting_3), 1.0, 1.6); //TPI
        DoubleInputFilter set4 = new DoubleInputFilter(getString(R.string.label_setting_4), 0.5, 2.0); //RTF
        IntegerInputFilter set5 = new IntegerInputFilter(getString(R.string.label_setting_5), 100, 3000); //Max layer of rowing
        IntegerInputFilter set6 = new IntegerInputFilter(getString(R.string.label_setting_6), 200, 280); //Bobbin Length
        DoubleInputFilter set7 = new DoubleInputFilter(getString(R.string.label_setting_7), 1.0, 4.0); //Rowing Width
        DoubleInputFilter set8 = new DoubleInputFilter(getString(R.string.label_setting_8), 1.0, 2.5); //Delta Bobbin
        IntegerInputFilter set9 = new IntegerInputFilter(getString(R.string.label_setting_9), 46, 52); //Bare Bobbin


        if (set1.filter(setting1) != null) {
            return set1.filter(setting1);
        }
        if (set2.filter(setting2) != null) {
            return set2.filter(setting2);
        }
        if (set3.filter(setting3) != null) {
            return set3.filter(setting3);
        }
        if (set4.filter(setting4) != null) {
            return set4.filter(setting4);
        }
        if (set5.filter(setting5) != null) {
            return set5.filter(setting5);
        }
        if (set6.filter(setting6) != null) {
            return set6.filter(setting6);
        }
        if (set7.filter(setting7) != null) {
            return set7.filter(setting7);
        }
        if (set8.filter(setting8) != null) {
            return set8.filter(setting8);
        }
        if (set9.filter(setting9) != null) {
            return set9.filter(setting9);
        }

        return null;
    }

    public void isEditMode(Boolean isEdit) {
        if (isEdit) {
            //Make settings editable
            saveBtn.setVisibility(View.VISIBLE);
            factorystngsBtn.setVisibility(View.VISIBLE);
            setStatusInputFields(true);
        } else {
            //Make settings non editable.
            saveBtn.setVisibility(View.INVISIBLE);
            factorystngsBtn.setVisibility(View.INVISIBLE);
            setStatusInputFields(false);
        }
    }

    public void updateContent() {
        setting1.setText(Settings.getSpindleSpeedString());
        setting2.setText(Settings.getTensionDraftString());
        setting3.setText(Settings.getTwistPerInchString());
        setting4.setText(Settings.getRTFString());
        setting5.setText(Settings.getLengthLimitString());
        setting6.setText(Settings.getMaxHeightOfContentString());
        setting7.setText(Settings.getRovingWidthString());
        setting8.setText(Settings.getDeltaBobbinDiaString());
        setting9.setText(Settings.getBareBobbinString());

    }

    public void setStatusInputFields(Boolean bol) {
        setting1.setEnabled(bol);
        setting2.setEnabled(bol);
        setting3.setEnabled(bol);
        setting4.setEnabled(bol);
        setting5.setEnabled(bol);
        setting6.setEnabled(bol);
        setting7.setEnabled(bol);
        setting8.setEnabled(bol);
        setting9.setEnabled(bol);

    }

/**************NEW FUNCTIONS ADDED FOR PID SEtting MODE ********8/
 *
 */
private List<String> getValueListForPIDOptions() {
    List<String> list = new ArrayList<>();
    int length;
    length = Pattern.PID_OPTIONS.values().length;
    while (length > 0) {
        String value = Pattern.PID_OPTIONS.values()[length-1].name();
        list.add(Utility.formatString(value));
        length--;
    }
    return list;
    }

    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // if not motors, then show the options of the start and stop variables, else show the motor variables
        String pidOptionsSelected = Utility.formatStringCode((pidOptionTypes.getSelectedItem().toString()));
        if (pidOptionsSelected.equals(Pattern.PID_OPTIONS.RTF_MULTIPLIERS.toString())) {
            PID_current_Layout = PID_FF_RTF_MULTIPLIER_LAYOUT;
        } else if (pidOptionsSelected.equals(Pattern.PID_OPTIONS.RAMP_RATES.toString())) {
            PID_current_Layout = PID_FF_RAMP_RATES_LAYOUT;
        } else {
            PID_current_Layout = PID_MOTOR_LAYOUT;
        }
        ChangePIDLayoutState(PID_current_Layout);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

     public void Enable_PID_EditTexts(int current_layout,boolean setting) {
         if (current_layout == PID_MOTOR_LAYOUT) {
             Kisetting.setEnabled(setting);
             Kpsetting.setEnabled(setting);
             startOffsetsetting.setEnabled(setting);
         }
         if (current_layout == PID_FF_RTF_MULTIPLIER_LAYOUT) {
             rampUpM_setting.setEnabled(setting);
             rampDownM_setting.setEnabled(setting);
         }
         if (current_layout == PID_FF_RAMP_RATES_LAYOUT) {
             rampUp_rate_setting.setEnabled(setting);
             rampDown_rate_setting.setEnabled(setting);
         }
     }


    public void updatePIDContent() {
        if (PID_current_Layout == PID_MOTOR_LAYOUT) {
            Kpsetting.setText(Settings.MakeFloatString(Settings.pid_req_attr1 / 100.0f,2));
            Kisetting.setText(Settings.MakeFloatString(Settings.pid_req_attr2 / 100.0f,2));
            startOffsetsetting.setText(Settings.MakeIntString(Settings.pid_req_attr3));
            Kisetting.setEnabled(true);
            Kpsetting.setEnabled(true);
            startOffsetsetting.setEnabled(true);
        }
        else if(PID_current_Layout == PID_FF_RTF_MULTIPLIER_LAYOUT){
            rampUpM_setting.setText(Settings.MakeFloatString(Settings.pid_req_attr1 / 100.0f,2));
            rampDownM_setting.setText(Settings.MakeFloatString(Settings.pid_req_attr2/100.0f,2));
            rampUpM_setting.setEnabled(true);
            rampDownM_setting.setEnabled(true);
        }
        else {
            rampUp_rate_setting.setText(Settings.MakeIntString(Settings.pid_req_attr1));
            rampUp_rate_setting.setEnabled(true);
            rampDown_rate_setting.setText(Settings.MakeIntString(Settings.pid_req_attr2));
            rampDown_rate_setting.setEnabled(true);
        }
        pidOptionTypes.setEnabled(true);
    }

    public void CancelPIDWaitingResponseState(){
        pidOptionTypes.setEnabled(true);
        String buttonText = getString(R.string.msg_PIDSetting_buttonRefresh);
        refreshPIDBtn.setText(buttonText);
        waitingForPIDSettingsResponse = false;
    }

    public void ChangePIDLayoutState(int layout){
        // set the PID motor linear layout on and set all the default options of that.
        if (layout == PID_MOTOR_LAYOUT) {
            PID_motor_optionView.setVisibility(View.VISIBLE);
            PID_FF_rampRTF_settings.setVisibility(View.GONE);
            PID_FF_rampRates_settings.setVisibility(View.GONE);

            Kisetting.setText(ZEROSTRING);
            Kpsetting.setText(ZEROSTRING);
            startOffsetsetting.setText(ZEROSTRING);
            Kisetting.setEnabled(false);
            Kpsetting.setEnabled(false);
            startOffsetsetting.setEnabled(false);
        }

        if (layout == PID_FF_RTF_MULTIPLIER_LAYOUT) {
            PID_motor_optionView.setVisibility(View.GONE);
            PID_FF_rampRTF_settings.setVisibility(View.VISIBLE);
            PID_FF_rampRates_settings.setVisibility(View.GONE);

            rampUpM_setting.setText(ZEROSTRING);
            rampDownM_setting.setText(ZEROSTRING);
            rampUpM_setting.setEnabled(false);
            rampDownM_setting.setEnabled(false);
        }

        if (layout == PID_FF_RAMP_RATES_LAYOUT) {
            PID_motor_optionView.setVisibility(View.GONE);
            PID_FF_rampRTF_settings.setVisibility(View.GONE);
            PID_FF_rampRates_settings.setVisibility(View.VISIBLE);

            rampUp_rate_setting.setText(ZEROSTRING);
            rampUp_rate_setting.setEnabled(false);
            rampDown_rate_setting.setText(ZEROSTRING);
            rampDown_rate_setting.setEnabled(false);
        }
        waitingForPIDSettingsResponse = false;
    }

    public void MakePIDButtonVisible(boolean visible){
        if (visible){
            PIDBtn.setVisibility(View.VISIBLE);
        }else{
            if (currentSettingsScreen.equals(PIDSETTINGS)){
                settingsScroll.setVisibility(View.VISIBLE);
                PIDScroll.setVisibility(View.INVISIBLE);
                String buttonText = getString(R.string.msg_setting_next1);
                PIDBtn.setText(buttonText);
                saveBtn.setVisibility(View.VISIBLE);
                factorystngsBtn.setVisibility(View.VISIBLE);
                currentSettingsScreen = SETTINGS;
                CancelPIDWaitingResponseState();
                mCallback.ClosePIDWaitSnackBar();
            }
            PIDBtn.setVisibility(View.GONE);
        }
    }

}

