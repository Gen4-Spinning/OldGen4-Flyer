package machine.microspin.com.flyerConsole;

import android.app.Application;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;

import java.util.UUID;

public class StartApplication extends Application {

    private static final UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothConfiguration config = new BluetoothConfiguration();

        config.bluetoothServiceClass = BluetoothClassicService.class;

        config.context = getApplicationContext();
        config.bufferSize = 1024;
        config.characterDelimiter = '\r';
        config.deviceName = "Bluetooth Sample 1";
        config.callListenersInMainThread = true;

        config.uuid = UUID_DEVICE;

        BluetoothService.init(config);
    }

}
