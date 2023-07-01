package machine.microspin.com.flyerConsole.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import machine.microspin.com.flyerConsole.R;
import machine.microspin.com.flyerConsole.entity.Pattern;
import machine.microspin.com.flyerConsole.entity.SettingsCommunicator;
import machine.microspin.com.flyerConsole.entity.FileUtils;
import machine.microspin.com.flyerConsole.entity.InternetUtils;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class UtilitiesFragment extends Fragment implements OnCheckedChangeListener,View.OnClickListener  {
    private SettingsCommunicator mCallback;
    public TextView filesize;
    public TextView filesizeUnits;
    public Button upload;
    public Switch loggingEnable;
    private Toast permissionToast = null;
    private Toast fileErrorToast = null;

    public boolean loggingEnabled = false;
    private boolean fileCreated = false;

    String subFolderName = "";
    String subFolderPathAndroid = "";
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 106;

    public FileUtils loggerFile = null;
    public InternetUtils Internet = new InternetUtils();

    public int maxLogsizeinKB = 2500;
    public boolean maxLogsizeHalted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_utilities, container, false);
        filesize =  rootView.findViewById(R.id.filesizeVal);
        filesizeUnits =  rootView.findViewById(R.id.fileUnits);
        upload =  rootView.findViewById(R.id.Upload_Button);
        upload.setOnClickListener(this);
        loggingEnable = rootView.findViewById(R.id.enableLogging);
        loggingEnable.setEnabled(true);
        loggingEnable.setOnCheckedChangeListener(this);

        subFolderName = getResources().getString(R.string.code);

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

    // button Click for Upload
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.Upload_Button) {
            Internet = new InternetUtils(); // create Once here.
            Internet.AllUploadOperations(getActivity(),getContext(),subFolderName,subFolderPathAndroid);
            Internet.execute("s");

        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            //Check for permission to write to media
            if(!checkPermissionToWrite()){
                //ask for permission if not granted to write to system storage
                requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }// rest happens in the overRide function when the user responds FOR THIS CASE
            else {
                //this is the case where user has already given permission
                loggerFile = new FileUtils(subFolderName);
                // if file and subfolders are created OK,
                if (loggerFile.currentState.equals(loggerFile.OK)) {
                    fileCreated = true;
                    loggingEnabled = true;
                    mCallback.onLogStateChange(Pattern.ENABLE_LOG);
                } else {
                    //show Toast with Error
                    fileErrorToast = Toast.makeText(getActivity(), loggerFile.currentState, Toast.LENGTH_LONG);
                    if (!fileErrorToast.getView().isShown()) {
                        fileErrorToast.show();
                     }
                    fileCreated = false;
                    loggingEnabled = false;
                    loggingEnable.setChecked(false); // move the slider back to false.
                    }
                }
        } else {
            // if file created, close the file and delete the file object.
            if (maxLogsizeHalted){
                Toast.makeText(getActivity(),"Max Log File Size Reached!", Toast.LENGTH_LONG).show();
            }
            //send msg disabling log
            mCallback.onLogStateChange(Pattern.DISABLE_LOG);

            if (fileCreated) {
                try {
                    subFolderPathAndroid = loggerFile.finalSubFolderPath;
                    loggerFile.bwLogWriter.flush();
                    loggerFile.bwLogWriter.close();
                    upload.setEnabled(true); // button
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //delete the loggerFile instance
            loggerFile = null;
            loggingEnabled = false;
            fileCreated = false;
            mCallback.ResetFileSize();
            filesize.setText("0");
            filesizeUnits.setText("KB");
        }
    }

    private boolean checkPermissionToWrite() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }else {
            return true;
            }
        }

    public void UpdateFileSizeUI(double fileSize){
        String unitsText = "KB";
        fileSize = fileSize/1024;
        if (fileSize >= 1024) {
            fileSize = (fileSize/ 1024);
            unitsText = "MB";
        }
        Double rounded = Math.round(fileSize * 10) / 10.0;
        filesize.setText(Double.toString(rounded));
        filesizeUnits.setText(unitsText);
    }



    public String bytArrayToString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean canUseExternalStorage = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                }
                if (!canUseExternalStorage) {
                    if (permissionToast == null) { // first time
                        permissionToast = Toast.makeText(getActivity(), "Cannot Log without this Permission!", Toast.LENGTH_LONG);
                    }
                    if (!permissionToast.getView().isShown()) {
                        permissionToast.show();
                     }
                    loggingEnable.setChecked(false);
                    loggingEnabled = false;
                } else {
                    // user now provided permission
                    loggerFile = new FileUtils(subFolderName);
                    // if file and subfolders are created OK,
                    if (loggerFile.currentState == loggerFile.OK) {
                        fileCreated = true;
                        loggingEnabled = true;
                        mCallback.onLogStateChange(Pattern.ENABLE_LOG);
                    } else {
                        //show Toast with Error
                        if (fileErrorToast == null) {
                            fileErrorToast = Toast.makeText(getActivity(), loggerFile.currentState, Toast.LENGTH_LONG);
                        }
                        if (fileErrorToast.getView().isShown() == false) {
                            fileErrorToast.show();
                        }
                        fileCreated = false;
                        loggingEnabled = false;
                        loggingEnable.setChecked(false); // move the slider back to false.
                    }
                }
            }
        }
    }

    public void onBackPressed() {
        if (Internet!= null) {
            Internet.cancel(true);
            Log.d("CANCEL","BackPressed!");
        }
    }


}
