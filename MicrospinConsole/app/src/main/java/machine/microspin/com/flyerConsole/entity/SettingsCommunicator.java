package machine.microspin.com.flyerConsole.entity;

import android.view.View;

/**
 * Interface to implement Fragment <-> Activity Communication.
 */

public interface SettingsCommunicator {

    void onLogStateChange(String payload);
    void onSettingsUpdate (String payload);
    void onPIDRequest(String payload);
    void ClosePIDWaitSnackBar();
    void onViewChangeClick(View view);
    void updateIdleModeStatus(Boolean bol);
    void raiseMessage(String Message);
    void updateRTF(String type);
    void writeDiagStartCommand(String payload);
    void writeStopDiagnosis();
    void CloseDiagSnackBar();
    void ResetFileSize();
    void DisableDiagnosticsTab(boolean isChecked);
    void DisableTabsForDiagnostics(boolean isChecked);

}
