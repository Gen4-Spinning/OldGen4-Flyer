package machine.microspin.com.flyerConsole.entity;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FileUtils {
    public boolean mediaWritable = false;
    public long emptySpaceMB = 0;

    public String OK = "OK";
    public String currentState = OK;
    public String finalSubFolderPath = "";

    String TAG = "FILE";
    String NOT_ENOUGH_SPACE = "Not Enough Space.Less than 10MB!" ;

    private String folder_main = "MicroSpin";

    private String error_Writable = "Media Not Writable!";
    private String error_mainDir = "Main Directory Creation Error!";
    private String error_subDir = "Sub Directory Creation Error!";
    private String error_FileCreation = "File Creation Error!";

    private File mainFolderFile;
    private File subFolderFile;
    public File logFile;
    private FileWriter logWriter;
    public BufferedWriter bwLogWriter;
    //constructor
    public FileUtils(String subFolder ){
        this.mediaWritable = isExternalStorageWritable();

        if (!this.mediaWritable) {
            this.currentState = error_Writable;
        }

        this.emptySpaceMB = getAvailableExternalMemorySize();
        Log.d(TAG, Long.toString(this.emptySpaceMB));

        //Create the MicroSpin Directory
        mainFolderFile = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!mainFolderFile.exists()) {
            if (!mainFolderFile.mkdirs()) {
                this.currentState = error_mainDir;
            }
        }

        //Now create the subFolder needed
        if (this.currentState == OK) {
            subFolderFile = new File(Environment.getExternalStorageDirectory() + "/" + folder_main + "/", subFolder);
            if (!subFolderFile.exists()) {
                if (!subFolderFile.mkdirs()) {
                    this.currentState = error_subDir;
                }
            }
        }

    //now create the File
        if (this.currentState == OK) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
                Date now = new Date();
                String machineIdentifier = Settings.device.getName();
                String fileName = machineIdentifier + "_" +formatter.format(now) + ".txt";//like 2016_01_12.txt
                finalSubFolderPath = subFolderFile.getAbsolutePath();
                logFile = new File(subFolderFile.getAbsolutePath(), fileName);
                logWriter = new FileWriter(logFile, true);
                bwLogWriter = new BufferedWriter(logWriter);

            } catch (IOException e) {
                e.printStackTrace();
                this.currentState = error_FileCreation;
            }
        }

    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private long getAvailableExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize /1024;
    }


}
