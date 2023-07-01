package machine.microspin.com.flyerConsole.entity;
import machine.microspin.com.flyerConsole.fragment.DriveServiceHelper;
import machine.microspin.com.flyerConsole.R;

import android.app.Activity;
import android.content.Context;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//googleDrive Stuff
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


public class InternetUtils extends AsyncTask<String, String, String> {

    private Activity currentActivity; // for the final Toast
    private Context currentContext ;
    private Drive gDrive;

    boolean internetAvailable = false;

    private ProgressDialog dialog;
    private Toast uploadToast = null;
    private Toast cancellingToast = null;

    //Toast Messages
    private String OK = "OK";
    private String SUCCESS = "Success! Uploaded to Microspin Server";
    private String NO_INTERNET = "No Internet! Please Connect and Retry!";
    private String SERVER_ERR_NO_MAIN_FOLDER = "Microspin Server ERR: NO MAIN FOLDERS! ";
    private String SERVER_ERR_NO_LOG_FOLDER = "Microspin Server ERR: NO LOG FOLDER! ";
    private String SERVER_ERR_CRASH = "Microspin Server ERR: UNDEFINED RESPONSE ";
    private String SERVER_ERR_CREATINGSUBFOLDER = "Microspin Server ERR: SUBFOLDER CREATION " ;
    private String SERVER_UNDEFINED_CRASH = "Microspin Server ERR : UNKNOWN - CHECK LOGS ";
    private String SERVER_ERR_CONTACT = "Please Contact Microspin";
    private String CANCELLED = "Upload Cancelled!";
    //Google Drive variables
    private DriveServiceHelper mDriveServiceHelper;
    private HttpTransport httpTransport;
    {
        httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
    }
    private final JsonFactory JSON_FACTORY =JacksonFactory.getDefaultInstance();
    private final String SERVICE_ACCOUNT_EMAIL = "logger@applogger.iam.gserviceaccount.com";//"microspin-service-account@microspin.iam.gserviceaccount.com";
    private static final String TAG = "DriveActivity";
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    // Predefined Main Folder Name in Google Drive for Logs
    private String MAIN_LOG_FOLDER = "Logs";
    private String googleSubFolderId = null; //subFolder ID in googleDrive
    private String subFolderPathAndroid = null; // in android - folder path
    private String subFolderName; // in google drive and android

    private Boolean AsyncCancelled = false; //
    public Boolean AsyncisOver = true;

    //constructor
    public InternetUtils(){
    }

    //main function -
    public void AllUploadOperations(Activity callingActivity, Context context, String subFolder_name, String subFolderPathAndroid){
        this.currentActivity = callingActivity;
        this.currentContext = context;
        this.subFolderName = subFolder_name;
        this.subFolderPathAndroid = subFolderPathAndroid;
        this.AsyncisOver = false; // the async is running

        dialog = new ProgressDialog(this.currentActivity,R.style.ProgressDialogStyle);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // dialog dismiss without button press
                AsyncCancelled = true;
                cancellingToast = Toast.makeText(callingActivity, "Cancelling...", Toast.LENGTH_LONG);
                cancellingToast.show();

            }
        });

    }

    //google drive functions.
    //1. the credential function
    private Drive getDriveService() throws IOException, GeneralSecurityException {

        InputStream is = this.currentContext.getResources().openRawResource(R.raw.key);
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                .setServiceAccountPrivateKeyFromP12File(is)
                .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE)) // driveScopes DRIVE allows you to add,delete create files.
                .build();

        return new Drive.Builder(
                httpTransport, JSON_FACTORY, credential)
                .build();
    }

    // this is invoked on UI thread. used to show gui elements like a progress bar
    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        dialog.setMessage(progress[0]);
    }

    @Override
    protected String doInBackground(String... s) {
        //Do some task
        try {
            publishProgress("Checking Internet Availability...");
            internetAvailable = isInternetAvailable();
            if (internetAvailable){
                publishProgress("Connecting to the Microspin Server");
                //generating credentials,checking if SubFolder exists,creating if not,
                try {
                    if (AsyncCancelled){
                        return CANCELLED;
                    }
                    this.gDrive = getDriveService();

                    if (AsyncCancelled){
                        return CANCELLED;
                    }
                    String result = CreateSubFolder();

                    if (AsyncCancelled){
                        return CANCELLED;
                    }
                    if (result.equals(OK)){
                        //Now subfolder is there. We check the no of files in the subfolder on the device.
                        // get the no of files and the total folder size, publish that...
                        java.io.File f = new java.io.File(this.subFolderPathAndroid);
                        java.io.File[]list = f.listFiles();
                        double folderSize = GetFolderSizeinKB(list);
                        //(cont) then get a list of all files inside the online subfolder.
                        if (AsyncCancelled){
                            return CANCELLED;
                        }
                        // check for error while uploading.and break out if error
                        for (int i = 0 ; i< list.length ; i++){
                               if (AsyncCancelled){
                                   return CANCELLED;
                                    }
                            publishProgress("Uploading " + Integer.toString(i) + "/" + Integer.toString(list.length) +
                                            " ("  + Double.toString(folderSize) + "KB)");

                            Boolean fileExist = CheckFileExistsAlready(list[i].getName());
                            Log.d("fileExists?",Boolean.toString(fileExist));
                               if (!fileExist){
                                   String uploadResult = UploadFile(list[i]);
                                   if (uploadResult.equals(OK)){
                                       list[i].delete();
                                   }
                               }else{
                                    list[i].delete();
                               }
                            }
                            return SUCCESS ;
                    }else{
                       return result;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return SERVER_ERR_CRASH + SERVER_ERR_CONTACT ;
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    return SERVER_ERR_CRASH + SERVER_ERR_CONTACT;
                }

            }else{
                return NO_INTERNET;
            }

        }catch (Exception e) {
            e.printStackTrace();
            return SERVER_UNDEFINED_CRASH + SERVER_ERR_CONTACT;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        //in case we re showing the cancelling toast, cancel it.
        if (cancellingToast!= null){
            cancellingToast.cancel();
        }
        //create a toast that says display the final result of the upload Operation
        if (uploadToast == null) { // first time
            uploadToast = Toast.makeText(this.currentActivity, result, Toast.LENGTH_LONG);
        }
        if (uploadToast.getView().isShown() == false) {
            uploadToast.show();
        }
        this.AsyncisOver = true; // to show the outside world that the Asnc process is Over. Used in enabling the back button
                             // press to go back to exiting the app in the dashboard Onbackbutton Press fn
    }

    // My functions..
    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)this.currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;

    }

    private boolean CheckFileExistsAlready(String filename)throws  IOException {

        //first get a list of all folders on the gdrive
        String pageToken = null;
        List<com.google.api.services.drive.model.File> files = new ArrayList<>();
        do {
            FileList result = gDrive.files().list()
                    .setQ("name = '" + filename + "'")  // we re only Looking for Folders.
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (File file : result.getFiles()) {
                //Log.d(TAG + "DBG", file.getName());
                files.add(file);
            }
            //List<com.google.api.services.drive.model.File> files = result.getFiles();
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        if (files.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    //Need to upload into the Log /correct subFolder. Have laready created in the root directory once.So need to Delete that.
   private String UploadFile(java.io.File file) throws  IOException,GeneralSecurityException{
        String filename = file.getName();
        com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
        body.setName(filename);
        body.setMimeType("text/plain");
        body.setParents(Collections.singletonList(googleSubFolderId));
        FileContent mediaContent = new FileContent("text/plain",file);
        com.google.api.services.drive.model.File fileUpload = gDrive.files().create(body, mediaContent).execute();
        //some error Catching Here!
        Log.d("Upload",fileUpload.getId());
        return OK;
    }

    private String CreateSubFolder() throws IOException,GeneralSecurityException {
        String mainFolderId = null;
        Boolean mainfolderBool = false;

        //first get a list of all folders on the gdrive
        String pageToken = null;
        List<com.google.api.services.drive.model.File> files = new ArrayList<>();
        do {
            FileList result = gDrive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'") // we re only Looking for Folders.
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (File file : result.getFiles()) {
                //Log.d(TAG + "DBG", file.getName());
                files.add(file);
            }
            //List<com.google.api.services.drive.model.File> files = result.getFiles();
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //files == null is the error handler when result.getfiles fails

        if (files == null || files.size() == 0) {
            // If theres no Log Folder created by MicroSpin and we create it here, it wont be shared with us.
            // remember, the service account is different from the user account. so we have created this folder in the user
            // account and shared it with the service account. so no point creating the Logs folder here.
            // so return some error message
            return SERVER_ERR_NO_MAIN_FOLDER + SERVER_ERR_CONTACT;
        } else {
            // run through all the folders and see if you find the main and subFolders
            for (com.google.api.services.drive.model.File file : files) {
                Log.d(TAG + "ERR", file.getName());
                if (file.getName().equals(MAIN_LOG_FOLDER)) {
                    mainFolderId = file.getId();
                    mainfolderBool = true;
                    break;
                }
            }

            if (!mainfolderBool){
                //No main folder, throw same error as above.
                return SERVER_ERR_NO_LOG_FOLDER + SERVER_ERR_CONTACT;
            }

            // check for Sub folder now.Once you find the subFolder,check if its within the main Folder.
            for (com.google.api.services.drive.model.File file : files){
                if (file.getName().equals(subFolderName)) {
                    googleSubFolderId = file.getId();
                    //check if the subFolder is within the main Folder
                    com.google.api.services.drive.model.File r = gDrive.files().get(googleSubFolderId).setFields("parents").execute();
                    List<String> parents = r.getParents();
                    for (String parent : parents) {
                        if (parent.equals(mainFolderId)) {
                            return OK;
                        }
                    }
                    //if we ve found a folder but the parent hasnt worked out, we can delete the subFolder
                    //for now just leave it, and create a folder inside the Log Folder by letting this code run
                }
            }

            // if we ve come here we havent found a subFolder yet, but we have validated the main Folder
            //So create the subfolder
            com.google.api.services.drive.model.File Folder = new com.google.api.services.drive.model.File()
                .setParents(Collections.singletonList(mainFolderId))
                .setMimeType("application/vnd.google-apps.folder")
                .setName(this.subFolderName);
            com.google.api.services.drive.model.File subfolderOnline = gDrive.files().create(Folder).execute(); // should we check for Errs here?
            if (subfolderOnline == null) {
                return SERVER_ERR_CREATINGSUBFOLDER + SERVER_ERR_CONTACT;
            }
            googleSubFolderId = subfolderOnline.getId();
            return OK;
        }
    }

    private double GetFolderSizeinKB(java.io.File[]list){
        double folderSizeinBytes = 0;
        double foldersizeinKB = 0;
        for (int i = 0; i < list.length; i++) {
            folderSizeinBytes += list[i].length();
        }
        if (folderSizeinBytes > 1024){
            foldersizeinKB = folderSizeinBytes/1024;
        }
        Double rounded = Math.round(foldersizeinKB * 10) / 10.0;
        return rounded ;
    }


    private void DeleteFile(File file) throws IOException{
        gDrive.files().delete(file.getId()).execute();
        }

}


