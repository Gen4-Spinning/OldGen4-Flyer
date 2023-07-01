package machine.microspin.com.flyerConsole.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import machine.microspin.com.flyerConsole.R;
import machine.microspin.com.flyerConsole.entity.Packet;
import machine.microspin.com.flyerConsole.entity.Pattern;
import machine.microspin.com.flyerConsole.entity.SettingsCommunicator;
import machine.microspin.com.flyerConsole.entity.TLV;
import machine.microspin.com.flyerConsole.entity.Utility;
import machine.microspin.com.flyerConsole.entity.Settings;


/**
 * Fragment 1 - RUN MODE STATUS
 * Shows the Status details sent by the connected ble device.
 */

public class RunModeFragment extends Fragment implements Pattern, View.OnClickListener {

    public TextView statusText, attr1Value, attr2Value, attr3Value, reasonText, reasonTypeText, errorText;
    public TextView valueValue;
    public LinearLayout stopLayout, runLayout, idleLayout, statusBox, errorBox;
    public LinearLayout attr1Box, attr2Box, attr3Box, valueBox;
    public Button RTFAddBtn, RTFSubBtn;
    private Boolean canEdit;
    private SettingsCommunicator mCallback;
    private Boolean haltMessageIsOpen = false;
    String s = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_run_mode, container, false);

        //=>Run Screen Layout
        statusText =  rootView.findViewById(R.id.statusText);
        attr1Value =  rootView.findViewById(R.id.attr1Value);
        attr2Value =  rootView.findViewById(R.id.attr2Value);
        attr3Value =  rootView.findViewById(R.id.attr3Value);
        //=>Stop Screen Layout
        reasonText = rootView.findViewById(R.id.reasonText);
        reasonTypeText = rootView.findViewById(R.id.reasonTypeLabel);
        errorText =  rootView.findViewById(R.id.motorErrorCode);
        valueValue =  rootView.findViewById(R.id.valueValue);

        //=>Layout Reference
        stopLayout =  rootView.findViewById(R.id.stopLayout);
        runLayout =  rootView.findViewById(R.id.runLayout);
        idleLayout =  rootView.findViewById(R.id.idleLayout);
        statusBox =  rootView.findViewById(R.id.statusBox);
        errorBox =  rootView.findViewById(R.id.errorBox);
        //=>Attribute Boxes
        attr1Box =  rootView.findViewById(R.id.attr1Box);
        attr2Box =  rootView.findViewById(R.id.attr2Box);
        attr3Box =  rootView.findViewById(R.id.attr3Box);
        valueBox =  rootView.findViewById(R.id.valueBox);
        RTFAddBtn = rootView.findViewById(R.id.addRTCBtn);
        RTFSubBtn = rootView.findViewById(R.id.subRTCBtn);

        RTFAddBtn.setOnClickListener(this);
        RTFSubBtn.setOnClickListener(this);

        stopLayout.setVisibility(View.INVISIBLE);
        runLayout.setVisibility(View.INVISIBLE);
        idleLayout.setVisibility(View.INVISIBLE);

        return rootView;
    }

    public void updateContent(final String payload) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Packet packet = new Packet(Packet.INCOMING_PACKET);
                //int attrCount = packet.getAttributeCount();
                if (packet.processIncomePayload(payload)) {
                    statusBox.setVisibility(View.VISIBLE);
                    canEdit = false;

                    TLV[] attr = packet.getAttributes();
                    if (packet.getNextScreen().equals(Screen.RUN.name())) {
                        toggleVisibility(Screen.RUN.name());
                        attr1Value.setText(Settings.getLengthLimitString()); // max no of layers
                        //round this
                        s = String.format("%.2f",Float.parseFloat(attr[2].getValue()));
                        attr2Value.setText(s); // current Length
                        attr3Value.setText(attr[1].getValue());//RTF
                        //attr3Value.setText(attr[1].getValue()); // Production
                        //Instead of production, just show Target no of layers. take the value from settings :)

                        haltMessageIsOpen = false;
                        mCallback.DisableDiagnosticsTab(true);
                    }

                    if (packet.getNextScreen().equals(Screen.STOP.name())) {
                        errorBox.setVisibility(View.INVISIBLE);
                        valueBox.setVisibility(View.INVISIBLE);
                        toggleVisibility(Screen.STOP.name());
                        //=>Display Stop Reason & Data, if present
                        if (!attr[0].getType().isEmpty() || !attr[0].getType().equals("")) {
                            if (attr[0].getType().equals(StopMessageType.REASON.name())) {
                                String attr0 = Utility.formatValueByPadding(attr[0].getValue(), 2);
                                reasonText.setText(Utility.formatString(Pattern.stopReasonValueMap.get(attr0)));
                                if (attr[1].getType().equals(StopMessageType.MOTOR_ERROR_CODE.name())) {
                                    reasonTypeText.setText(getString(R.string.label_stop_motor));
                                    errorBox.setVisibility(View.VISIBLE);
                                    String attr1 = Utility.formatValueByPadding(attr[1].getValue(), 2);
                                    errorText.setText(Utility.formatString(Pattern.motorErrorCodeMap.get(attr1)));
                                }else {
                                    reasonTypeText.setText(getString(R.string.label_stop_reason));
                                }

                                if (packet.getScreenSubState().equals(ScreenSubState.HALT.name())) {
                                    if (!haltMessageIsOpen) {
                                        mCallback.raiseMessage(getString(R.string.msg_halt_restart));
                                        haltMessageIsOpen = true;
                                    }
                                } else {
                                    haltMessageIsOpen = false;
                                }
                            }

                            } else {
                                reasonText.setVisibility(View.INVISIBLE);
                                errorBox.setVisibility(View.INVISIBLE);
                            }
                        mCallback.DisableDiagnosticsTab(true);
                        }
                    if (packet.getNextScreen().equals(Screen.IDLE.name())) {
                        canEdit = true;
                        mCallback.updateIdleModeStatus(true);
                        toggleVisibility(Screen.IDLE.name());
                        haltMessageIsOpen = false;
                    }else{
                        mCallback.updateIdleModeStatus(false);
                    }
                    statusText.setText(Utility.formatString(packet.getScreenSubState()));
                }
            }
        });
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

    public void toggleVisibility(String screenName) {

        if (screenName.equals(Screen.STOP.name())) {
            runLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            stopLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.RUN.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            runLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.IDLE.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.VISIBLE);
            runLayout.setVisibility(View.INVISIBLE);
        }
    }

    public Boolean canEditSettings() {
        return canEdit;
    }


    @Override
    public void onClick(View view) {
        mCallback.onViewChangeClick(view);
    }
}
