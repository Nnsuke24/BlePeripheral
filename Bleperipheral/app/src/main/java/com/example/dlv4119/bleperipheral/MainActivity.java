package com.example.dlv4119.bleperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothGattServer gattServer;
    BluetoothDevice mDevice;
    BluetoothGattCharacteristic mCharacteristic;
    private static final String mSERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    /** characteristicのUUID */
    private static final String mCHARACTERISTIC_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();

        setGattServer();

        // 設定
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingBuilder.setConnectable(false);
        settingBuilder.setTimeout(0);
        settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        AdvertiseSettings settings = settingBuilder.build();

        // アドバタイジングデータ
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString("65432461-1EFE-4ADB-BC7E-9F7F8E27FDC1")));
        AdvertiseData advertiseData = dataBuilder.build();

        //アドバタイズを開始
        advertiser.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.d("startAdvertising()", "Advertiseに成功!!");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.d("startAdvertising()", "Advertiseに失敗、、、");
            }
        });
    }

    public void setGattServer() {

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        // GattServer
        gattServer = manager.openGattServer(getApplicationContext(), new BluetoothGattServerCallback() {
            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                if (value != null) {
                    Log.d("TAG", "value ~ " + new String(value));
                }
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "ABC".getBytes());
            }

            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mDevice = device;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mDevice = null;
                }
            }
        });

        // Serviceを登録
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(mSERVICE_UUID),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(mCHARACTERISTIC_UUID),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                                BluetoothGattCharacteristic.PROPERTY_READ |
                                BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE);
        // characteristicUUIDをserviceUUIDにのせる
        service.addCharacteristic(mCharacteristic);
        // serviceUUIDをサーバーにのせる
        gattServer.addService(service);
    }
}
