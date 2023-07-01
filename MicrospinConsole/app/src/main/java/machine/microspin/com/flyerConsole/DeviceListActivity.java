package machine.microspin.com.flyerConsole;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;

import java.util.Arrays;

import machine.microspin.com.flyerConsole.adapter.DeviceAdapter;
import machine.microspin.com.flyerConsole.entity.Settings;

public class DeviceListActivity extends AppCompatActivity implements BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback, DeviceAdapter.OnAdapterItemClickListener {

    private static final String TAG = "Flyer Console";
    final private static String filterParameter = "Flyer";

    private ProgressBar pgBar;
    private Menu mMenu;
    private DeviceAdapter mAdapter;

    private BluetoothService mService;
    private boolean mScanning;

    private Toast accessToast;

    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pgBar = (ProgressBar) findViewById(R.id.pg_bar);
        pgBar.setVisibility(View.GONE);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(lm);

        mAdapter = new DeviceAdapter(this, mBluetoothAdapter.getBondedDevices());
        mAdapter.setOnAdapterItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mService = BluetoothService.getDefaultInstance();

        mService.setOnScanCallback(this);
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_scan) {
            startStopScan();
            return true;
        }

        if (mScanning){
            mService.stopScan();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startStopScan() {
        if (!mScanning) {
            if (!checkPermissionToUseLocation()){
                Log.d("DEVICE","no permissison");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
            else {
                mService.startScan();
            }
        } else {
            mService.stopScan();
        }
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d("DEVICE", "onDeviceDiscovered: " + device.getName() + " - " + device.getAddress() + " - " + Arrays.toString(device.getUuids()));
        //******* RELEASE CHANGE V2
        //if(device.getName().contains(filterParameter)) {
            BluetoothDeviceDecorator dv = new BluetoothDeviceDecorator(device, rssi);
            int index = mAdapter.getDevices().indexOf(dv);
            if (index < 0) {
                mAdapter.getDevices().add(dv);
                mAdapter.notifyItemInserted(mAdapter.getDevices().size() - 1);
            } else {
                mAdapter.getDevices().get(index).setDevice(device);
                mAdapter.getDevices().get(index).setRSSI(rssi);
                mAdapter.notifyItemChanged(index);
            }
        //}
    }

    @Override
    public void onStartScan() {
        Log.d(TAG, "onStartScan");
        mScanning = true;
        pgBar.setVisibility(View.VISIBLE);
        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_stop);
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_start_scanning, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();
    }

    @Override
    public void onStopScan() {
        Log.d(TAG, "onStopScan");
        mScanning = false;
        pgBar.setVisibility(View.GONE);
        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_scan);
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_stop_scanning, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(TAG, "onDataRead");
    }

    @Override
    public void onStatusChange(BluetoothStatus status) {
        Log.d(TAG, "onStatusChange: " + status);


        if(status == BluetoothStatus.CONNECTING){
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_connecting, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
        if (status == BluetoothStatus.CONNECTED) {
            Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show();
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_connected, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            startActivity(new Intent(DeviceListActivity.this, DashboardRunMode.class));
        }
        if(status == BluetoothStatus.NONE){
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_not_connect, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }

    }

    @Override
    public void onDeviceName(String deviceName) {
        Log.d(TAG, "onDeviceName: " + deviceName);
    }

    @Override
    public void onToast(String message) {
        Log.d(TAG, "onToast");
    }

    @Override
    public void onDataWrite(byte[] buffer) {
        Log.d(TAG, "onDataWrite");
    }

    @Override
    public void onItemClick(BluetoothDeviceDecorator device, int position) {
        Settings.device = device.getDevice();
        mService.connect(device.getDevice());
    }

    private boolean checkPermissionToUseLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean canUseAccessLocation = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseAccessLocation = true;
                }
                if (!canUseAccessLocation) {
                    if (accessToast == null) { // first time
                        accessToast = Toast.makeText(this, "Cannot Scan/Get Signal Strength without this Permission!", Toast.LENGTH_LONG);
                    }
                    if (!accessToast.getView().isShown()) {
                        accessToast.show();
                    }
                    mService.stopScan();
                } else {
                    mService.startScan();
                }
            }
        }
    }


}

