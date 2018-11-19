package webview.youtube.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

public class BLE {
    boolean addTextflag = false;
    BluetoothGatt bg;
    byte[] character;
    Handler handler = new Handler();
    BluetoothAdapter mBluetoothAdapter;
    byte[] readBuffer = new byte[1024];
    int readBufferPosition = 0;
    BluetoothGattCharacteristic readChar;
    BluetoothGattCharacteristic writeChar;

    public void connect(String mac, final MainActivity mainActivity) {
        this.bg = this.mBluetoothAdapter.getRemoteDevice(mac).connectGatt(mainActivity, true, new BluetoothGattCallback() {
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                BLE.this.character = characteristic.getValue();
                if (BLE.this.character[0] == (byte) 13) {
                    BLE.this.readBufferPosition = 0;
                    BLE.this.handler.post(new Runnable() {
                        public void run() {
                            try {
                                String data = new String(BLE.this.readBuffer, "US-ASCII");
                                BLE.this.readBuffer = new byte[1024];
                                BLE.this.addTextflag = true;
                                mainActivity.addText(data);
                            } catch (Exception e) {
                            }
                        }
                    });
                    return;
                }
                byte[] bArr = BLE.this.readBuffer;
                BLE ble = BLE.this;
                int i = ble.readBufferPosition;
                ble.readBufferPosition = i + 1;
                bArr[i] = BLE.this.character[0];
            }

            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == 2) {
                    BLE.this.bg.discoverServices();
                }
            }

            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                for (BluetoothGattService b : BLE.this.bg.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : b.getCharacteristics()) {
                        if ((characteristic.getProperties() & 8) > 0) {
                            BLE.this.writeChar = characteristic;
                        }
                        if ((characteristic.getProperties() & 2) > 0) {
                            BLE.this.readChar = characteristic;
                            BLE.this.bg.setCharacteristicNotification(BLE.this.readChar, true);
                        }
                        if (BLE.this.readChar != null && BLE.this.writeChar != null) {
                            break;
                        }
                    }
                }
            }
        });
    }

    public void sendData(String msg) {
        this.writeChar.setValue(msg + "\n\r");
        this.bg.writeCharacteristic(this.writeChar);
        this.addTextflag = false;
    }

    public void disconnect() {
        this.bg.disconnect();
        this.bg.close();
    }
}
