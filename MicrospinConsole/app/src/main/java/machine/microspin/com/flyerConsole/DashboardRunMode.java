package machine.microspin.com.flyerConsole;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import machine.microspin.com.flyerConsole.entity.Packet;
import machine.microspin.com.flyerConsole.entity.Pattern;
import machine.microspin.com.flyerConsole.entity.Settings;
import machine.microspin.com.flyerConsole.entity.SettingsCommunicator;
import machine.microspin.com.flyerConsole.entity.TLV;
import machine.microspin.com.flyerConsole.fragment.DiagnosticsFragment;
import machine.microspin.com.flyerConsole.fragment.RunModeFragment;
import machine.microspin.com.flyerConsole.fragment.SettingsFragment;
import machine.microspin.com.flyerConsole.fragment.UtilitiesFragment;

@SuppressWarnings("ALL")
public class DashboardRunMode extends AppCompatActivity implements SettingsCommunicator, BluetoothService.OnBluetoothEventCallback, TabLayout.OnTabSelectedListener {

    public static final String TAG = "MicroSpin";
    private static String deviceAddress;
    public static Boolean isExpectingSettings = false;
    public static Boolean isInIdleMode = false;
    public static Boolean isWaitingForAck = false;
    public TabLayout tabLayout;
    boolean doubleBackToExitPressedOnce = false;
    public boolean enablePIDMode = false;
    private boolean passwordSetRight = false;
    private Menu menu;

    public Snackbar PIDwaitSnackBar = null;
    public Snackbar diagFinishSnackbar = null;

    final private static String SETTINGS_REQ_PAYLOAD = "7E020B0101029900010002007E";

    private BluetoothService mService;
    private BluetoothWriter mWriter;

    private static Boolean isSecondConnectionTry = false;
    private SettingsFragment settingsFragmentForPID = null;
    private byte[] logPayload;
    public double fileSizeDouble = 0;

    private boolean firstTimeUpdatePIDBtn = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //******* Release Change v2
        if (Settings.device != null) {
            setTitle(Settings.device.getName());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3); // HARSHA :Not optimized but is ok for us.cant create too many more tabs in screen. is Ok for 3

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(this);

        //check if PID option needs to be ON
        enablePIDMode = false;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int enablePID_read = sharedPref.getInt("PID", 0);
        if (enablePID_read == 1) {
            enablePIDMode = true;
        }

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        // ===== Request for Settings ======
        mWriter.writeln(SETTINGS_REQ_PAYLOAD.toUpperCase());
        isExpectingSettings = true;

    }

    //================================== BLUETOOTH EVENT CALLBACKS =================================
    @Override
    public void onDataRead(byte[] bytes, int length) {
        final RunModeFragment runFragment = (RunModeFragment) getSupportFragmentManager().getFragments().get(0);
        final SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().getFragments().get(1);
        final UtilitiesFragment utilitiesFragment = (UtilitiesFragment) getSupportFragmentManager().getFragments().get(3);
        final DiagnosticsFragment diagnosticsFragment = (DiagnosticsFragment) getSupportFragmentManager().getFragments().get(2);

        if (runFragment instanceof RunModeFragment) {
            try {
                String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
                if (!payload.isEmpty()) {
                    //=============================================================
                    String packetPayload;
                    switch (payload) {
                        case "XXNORMAL":
                            packetPayload = "7E01170103010201030702000406043f99999a0504400ccccd7E";
                            break;
                        case "XXPAUSE1"://USER PRESS
                            packetPayload = "7E01130103010311030102000800020000000200007E";
                            break;
                        case "XXPAUSE2": //sliver cut
                            packetPayload = "7E01130103010311030102000900020000000200007E";
                            break;
                        case "XXHALT": // RPM ERROR TO CHECK
                            packetPayload = "7E01130103010312030102003202020002000200007E";
                            break;
                        case "XXLAYEROVER": // LAYERSOVER flyer motor, and layer over
                            packetPayload = "7E01130103010312030102000A00020000000200007E";
                            break;
                        case "XXLIFTERR": // lift motor and lift err
                            packetPayload = "7E01130103010312030102000B00020000000200007E";
                            break;
                        case "XXIDLE":
                            packetPayload = "7E01000101010100007E";
                            break;
                        case "XXHOMING":
                            packetPayload = "7E01170103010204030702000406043f99999a0504400ccccd7E";
                            break;
                        case "XXSETTING":
                            packetPayload = "7E012501030204000160240320410ccccd3f6666663f2e147b004101363f8ccccd3f99999a00300019001E7E";
                            break;
                        case "XXACK":
                            packetPayload = "7E010A010102049901000000007E";
                            break;
                        case "XXSAVED":
                            packetPayload = "7E010A010102048801000000007E";
                            break;
                        case "XXPIDSETTING":
                            packetPayload = "7E0111010302060001AA16032000F5001400FA7E";
			                break;
                        case "XXDFSTARTSETTING":
                            packetPayload = "7E011001020206000101240020255A00D7007B7E";
                            break;
                        case "XXDFSTOPSETTING":
                            packetPayload = "7E01100102020600010124002000FA000000007E";                            
			                break;
                        case "XXPIDSAVED":
                            packetPayload = "7E010A010102068801000000007E";
                            break;
                        case "XXDLIVEC":
                            packetPayload = "7E01120101020500040102000102020032030204B0030204B07E";
                            break;
                        case "XXDLIVEO":
                            packetPayload = "7E01120101020500040102000202020032030205DC030204B07E";
                            break;
                        case "XXDSUCCESS":
                            packetPayload = "7E010B010102059800000000007E";
                            break;
                        case "XXDFAIL":
                            packetPayload = "7E010B010102059700000000007E";
                            break;
                        case "XXDINCOMPLETE":
                            packetPayload = "7E010B010102059600000000007E";
                            break;
                        default:
                            packetPayload = payload;
                    }

                    // if its not a packet with 7E at the beginning, log it!s
                    if (Packet.LogPacket(packetPayload)){
                        if (utilitiesFragment.loggingEnabled){
                            try{
                                //String logHexString = utilitiesFragment.bytArrayToString(bytes);
                                utilitiesFragment.loggerFile.bwLogWriter.append(packetPayload);
                                utilitiesFragment.loggerFile.bwLogWriter.newLine();
                                fileSizeDouble = fileSizeDouble + (double)packetPayload.length();
                                if (fileSizeDouble > utilitiesFragment.maxLogsizeinKB * 1024){
                                    utilitiesFragment.loggingEnable.setChecked(false);
                                    utilitiesFragment.maxLogsizeHalted = true;
                                }else {
                                    utilitiesFragment.UpdateFileSizeUI(fileSizeDouble);
                                }
                            }catch (IOException e) {
                                // HERE THE FILE MAY BE MANUALLY DELETED. We can break out, and close the file class,and show a toast.
                                //do nothing, skip to next packet..
                            }
                        }else{
                            fileSizeDouble = 0;
                        }
                    }
                    else {
                    if (packetPayload.length() >= 20) { //size of header is 20 . Min Size of packet 20
                        if (Packet.getHeadersScreen(packetPayload).equals(Pattern.Screen.SETTING.name())) {
                            if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.ACK.name())) {
                                if (isWaitingForAck) {
                                    isWaitingForAck = false;
                                    Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updated, Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                }
                            } else if (Settings.processSettingsPacket(packetPayload)) {
                                //if the Settings was requested by Device to Machine
                                if (isExpectingSettings) {
                                    settingsFragment.updateContent();
                                    isExpectingSettings = false;
                                }
                            }
                            } else if (Packet.getHeadersScreen(packetPayload).equals(Pattern.Screen.PID_REQUEST.name())){
                                if (settingsFragment.waitingForPIDSettingsResponse) { // we re getting settings from the machine to show on the app
                                    if (Settings.processPIDSettingsPacket(packetPayload)) {
                                        settingsFragment.updatePIDContent();
                                        settingsFragment.CancelPIDWaitingResponseState();
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_refresh_received, Snackbar.LENGTH_SHORT)
                                                .setAction("Action", null).show();
                                    }
                                } else { // were seding settings from the app to the machine
                                    if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.SAVED.name())) { //saved settings
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_saved_pid, Snackbar.LENGTH_SHORT)
                                                .setAction("Action", null).show();
                                        settingsFragment.Enable_PID_EditTexts(settingsFragment.PID_current_Layout,false);
                                        settingsFragment.waitingForPIDSettingsResponse = false;
                                    }
                                }
                            }
                            /*****************/
                            else if (Packet.getHeadersScreen(packetPayload).equals(Pattern.Screen.DIAGNOSTICS.name())) {
                                if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.DIAG_SUCCESS.name())) {
                                    diagFinishSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_success, Snackbar.LENGTH_INDEFINITE);
                                    // change snackbar text color
                                    View snackbarView = diagFinishSnackbar.getView();
                                    int snackbarTextId = android.support.design.R.id.snackbar_text;
                                    TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                    textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    diagFinishSnackbar.setAction("Action", null).show();
                                } else if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.DIAG_FAIL.name())) {
                                    diagFinishSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_fail, Snackbar.LENGTH_INDEFINITE);
                                    // change snackbar text color
                                    // change snackbar text color
                                    View snackbarView = diagFinishSnackbar.getView();
                                    int snackbarTextId = android.support.design.R.id.snackbar_text;
                                    TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                                    diagFinishSnackbar.setAction("Action", null).show();
                                } else if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.DIAG_INCOMPLETE.name())) {
                                    diagFinishSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_Incomplete, Snackbar.LENGTH_INDEFINITE);
                                    // change snackbar text color
                                    View snackbarView = diagFinishSnackbar.getView();
                                    int snackbarTextId = android.support.design.R.id.snackbar_text;
                                    TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                    textView.setTextColor(getResources().getColor(R.color.darkOrange));
                                    diagFinishSnackbar.setAction("Action", null).show();
                                } else {
                                    diagnosticsFragment.toggleViewOn(diagnosticsFragment.LAYOUT_LIVE);
                                    Packet packet = new Packet(Packet.INCOMING_PACKET);
                                    if (packet.processIncomePayload(packetPayload)) {
                                        TLV[] attr = packet.getAttributes();
                                        diagnosticsFragment.updateLiveData(attr);
                                    }
                                }
                            }
                            else {
                                //Update Run, Stop & Idle Mode Screen(s)
                                runFragment.updateContent(packetPayload);
                                Boolean canEditSettings = runFragment.canEditSettings();
                                settingsFragment.isEditMode(canEditSettings);

                                if (firstTimeUpdatePIDBtn) {
                                    //make the enable PID button the correct Visibility when we get the first msg from the app
                                    settingsFragment.MakePIDButtonVisible(enablePIDMode);
                                    firstTimeUpdatePIDBtn = false;
                                }

                            }
                        }
                    }
                    //=================================================================
                }
            } catch (UnsupportedEncodingException | NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }


    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.NONE) {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_disconnected, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onDeviceName(String s) {

    }

    @Override
    public void onToast(String message) {
        Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onDataWrite(byte[] bytes) {

    }

    //============================== FRAGMENT IMPLEMENTED FUNCTIONS ================================
    @Override
    public void onSettingsUpdate(String payload) {
        mWriter.writeln(payload.toUpperCase());
        isWaitingForAck = true;
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updating, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();
    }

    @Override
    public void writeDiagStartCommand(String payload) {
        mWriter.writeln(payload.toUpperCase());
    }

    @Override
    public void writeStopDiagnosis() {
        mWriter.writeln(Pattern.STOP_DIAG_RUN.toUpperCase());
    }



    @Override
    public void onPIDRequest(String payload) {
        mWriter.writeln(payload.toUpperCase());
        PIDwaitSnackBar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_refresh_waiting, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null);
        PIDwaitSnackBar.show();
    }

    public void CloseDiagSnackBar(){
        if (diagFinishSnackbar != null) {
            diagFinishSnackbar.dismiss();
        }
    }

    @Override
    public void ClosePIDWaitSnackBar() {
        if (PIDwaitSnackBar != null){
            PIDwaitSnackBar.dismiss();
        }
    }

    @Override
    public void ResetFileSize() {
        fileSizeDouble = 0;
    }


    @Override
    public void onLogStateChange(String payload) {
        mWriter.writeln(payload.toUpperCase());
    }


    @Override
    public void onViewChangeClick(View view) {
        switch (view.getId()) {
            case R.id.addRTCBtn:
                mWriter.writeln(Pattern.UPDATE_RTF_ADD.toUpperCase());
                break;
            case R.id.subRTCBtn:
                mWriter.writeln(Pattern.UPDATE_RTF_SUB.toUpperCase());
                break;
        }
    }

    @Override
    public void updateIdleModeStatus(Boolean bol) {
        isInIdleMode = bol;
    }

    @Override
    public void raiseMessage(String Message) {
        if (Message.equals(getString(R.string.msg_halt_restart))) {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_restart, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }).show();
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void updateRTF(String type) {

    }

    //==================================== CUSTOM FUNCTIONS ========================================


    //===================================== ACTIVITY EVENTS ========================================
    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
    }

    @Override
    public void onBackPressed() {
        final UtilitiesFragment utilitiesFragment = (UtilitiesFragment) getSupportFragmentManager().getFragments().get(3);
        final DiagnosticsFragment diagnosticsFragment = (DiagnosticsFragment) getSupportFragmentManager().getFragments().get(2);
        if (diagnosticsFragment.backButton_disable == false) {
        if (doubleBackToExitPressedOnce) {
            //CLOSE LOGGING iF STILL on
            if (utilitiesFragment.loggingEnabled) {
                try {
                    utilitiesFragment.loggerFile.bwLogWriter.close();
                } catch (IOException e) {
                    //maybe do the same as what we did in the running if theres an error while logging...
                    e.printStackTrace();
                }
             }
            //finish and remove all other tasks
            finishAndRemoveTask();
            return;
            } else {
                //if the utilities fragment upload is happening , the back button doesnt link to the exit command. It only
                // is linked to the exit the upload command which is taken care of inside the utilities fragment itself.!
                if (utilitiesFragment.Internet.AsyncisOver == true) {
                    this.doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }
            }
        }
    }

    //======================================= MENU EVENTS ==========================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        TogglePIDMenu(menu, enablePIDMode);
        this.menu = menu;
        return true;
    }

    void TogglePIDMenu(Menu menu, boolean enable) {
        MenuItem enablePID = menu.findItem(R.id.enablePID);
        MenuItem disablePID = menu.findItem(R.id.disablePID);
        if (enable) {
            enablePID.setVisible(false);
            disablePID.setVisible(true);
        } else {
            enablePID.setVisible(true);
            disablePID.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.enablePID) {
            passwordSetRight = false;
            CreatePasswordDialog(); //  menu and prefernece file gets updated inside this call
        }

        if (id == R.id.disablePID) {
            CreateSingleLineTextDialog("PID Mode Disabled!","disablePID");
        }

        if (id == R.id.action_quit) {
            mService.disconnect();
            mService = null;
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void DoPasswordActions(boolean passwordSetRight){
        if (passwordSetRight) {
            CreateSingleLineTextDialog("PIDMode Enabled!","DoNothing");
            // update the preference file
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("PID", 1);
            editor.commit();
            //update Menu
            enablePIDMode = true;
            invalidateOptionsMenu();
        }else{
            CreateSingleLineTextDialog("Wrong Password!","doNothing");
        }
    }


    private void DisablePID() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("PID", 0);
        editor.commit();
        enablePIDMode = false;
        invalidateOptionsMenu();
    }

    void CreateSingleLineTextDialog(String message,final String action){
        AlertDialog.Builder builder1 = new AlertDialog.Builder( new ContextThemeWrapper(this, R.style.AppTheme_DialogTheme));
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener()
                {

                    public void onClick (DialogInterface dialog,int id){
                        dialog.cancel();
                        if (action.equals("disablePID")){
                            DisablePID();
                            settingsFragmentForPID = (SettingsFragment) getSupportFragmentManager().getFragments().get(1);
                            settingsFragmentForPID.MakePIDButtonVisible(enablePIDMode);
                        }
                    }
                }
        );



        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    //Dialog for Password
    void CreatePasswordDialog(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_DialogTheme));
        builder1.setMessage("Enter password to enable PID mode");
        builder1.setCancelable(true);

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final View passwordView = inflater.inflate(R.layout.password_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder1.setView(passwordView);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener()
            {
                public void onClick (DialogInterface dialog,int id){
                    EditText password = (EditText) passwordView.findViewById(R.id.passwordDialog);
                    String passwordString = password.getText().toString();
                    if (passwordString.equals(Pattern.PID_PASSWORD)){
                        passwordSetRight = true;
                    }
                dialog.cancel();
                DoPasswordActions(passwordSetRight);
                settingsFragmentForPID = (SettingsFragment) getSupportFragmentManager().getFragments().get(1);
                settingsFragmentForPID.MakePIDButtonVisible(enablePIDMode);
                 }
            }
        );

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener()
                {
                public void onClick (DialogInterface dialog,int id){
                dialog.cancel();
                }
            }
        );

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    //==================================== FRAGMENT SELECTION ======================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RunModeFragment();
                case 1:
                    return new SettingsFragment();
                case 2:
                    return new DiagnosticsFragment();
                case 3:
                    return new UtilitiesFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        // Get Tab Title(s)
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.label_status);
                case 1:
                    return getResources().getString(R.string.label_settings);
                case 2:
                    return getResources().getString(R.string.label_diagnostics);
                case 3:
                    return getResources().getString(R.string.label_utilities);
            }
            return null;
        }
    }

    //========================  TAB Events ===============================
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        CloseDiagSnackBar();
            switch (tab.getPosition()) {
                case 0:
                    mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
                    break;
                case 1:
                    mWriter.writeln(Pattern.DISABLE_MACHINE_START_SETTINGS.toUpperCase());
                    break;
            	case 2: // Diagnostics
                    mWriter.writeln(Pattern.DISABLE_MACHINE_START_DIAGNOSE.toUpperCase());
                    break;

            }
        }

    public void DisableTabsForDiagnostics(boolean isChecked){
        View status = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(0);
        View settings = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(1);
        if (isChecked) {
            status.setClickable(false);
            status.setAlpha( 0.3F);
            settings.setClickable(false);
            settings.setAlpha( 0.3F);
        }else {
            status.setClickable(true);
            status.setAlpha( 1F);
            settings.setClickable(true);
            settings.setAlpha( 1F);
    }
    }

    public void DisableDiagnosticsTab(boolean isChecked) {
        View diagnostics = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2);
        if (isChecked) {
            diagnostics.setClickable(false);
            diagnostics.setAlpha(0.3F);
        } else {
            diagnostics.setClickable(true);
            diagnostics.setAlpha(1F);
        }
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

}
