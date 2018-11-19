package webview.youtube.bluetoothcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    AlertDialog a;
    ArrayAdapter arr;
    BLE bt = new BLE();
    Builder d;
    boolean flag = false;
    ListView l;
    LeScanCallback leScan = new LeScanCallback() {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            boolean addflag = true;
            for (int i = 0; i < MainActivity.this.arr.getCount(); i++) {
                if (device.getAddress().equals(((String) MainActivity.this.arr.getItem(i)).substring(((String) MainActivity.this.arr.getItem(i)).indexOf(10) + 1))) {
                    addflag = false;
                    break;
                }
            }
            if (addflag) {
                MainActivity.this.arr.add(device.getName() + "\n" + device.getAddress());
                MainActivity.this.arr.notifyDataSetChanged();
            }
        }
    };
    EditText msg;
    RelativeLayout relativeLayout;
    Button send;
    boolean showlocalmsgs;
    TextView t;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.showlocalmsgs = getSharedPreferences("User", 0).getBoolean("show", false);
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, "No BLE Support.", Toast.LENGTH_LONG).show();
            finish();
        }
        @SuppressLint("WrongConstant") BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
        this.bt.mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!this.bt.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        } else if (VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_COARSE_LOCATION")) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
            }
        }
        this.t = new TextView(this);
        this.t.setTextSize(20.0f);
        this.relativeLayout = (RelativeLayout) findViewById(R.id.r);
        this.msg = (EditText) findViewById(R.id.msg);
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    MainActivity.this.bt.sendData(MainActivity.this.msg.getText().toString());
                    if (MainActivity.this.showlocalmsgs) {
                        MainActivity.this.addText(MainActivity.this.msg.getText().toString());
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ResourceType")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bt /*2131427432*/:
                startActivity(new Intent().setAction("android.settings.BLUETOOTH_SETTINGS"));
                break;
            case R.id.connect /*2131427433*/:
                this.d = new Builder(this);
                View v = getLayoutInflater().inflate(R.layout.paired, null);
                this.d.setView(v);
                this.l = (ListView) v.findViewById(R.id.listView);
                this.arr = new ArrayAdapter(this, 0b1000010010000000000000011, 0);
                this.l.setAdapter(this.arr);
                this.bt.mBluetoothAdapter.startLeScan(this.leScan);
                this.l.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        try {
                            String device = (String) MainActivity.this.l.getItemAtPosition(position);
                            MainActivity.this.bt.connect(device.substring(device.indexOf(10) + 1), MainActivity.this);
                            MainActivity.this.bt.mBluetoothAdapter.stopLeScan(MainActivity.this.leScan);
                            MainActivity.this.a.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this.getBaseContext(), "Not Connected", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                this.a = this.d.create();
                this.a.show();
                break;
            case R.id.disconnect /*2131427434*/:
                try {
                    this.bt.disconnect();
                    break;
                } catch (Exception e) {
                    break;
                }
            case R.id.settings /*2131427435*/:
                Builder d = new Builder(this);
                View v1 = getLayoutInflater().inflate(R.layout.settings, null);
                d.setView(v1);
                Switch show = (Switch) v1.findViewById(R.id.switch1);
                if (this.showlocalmsgs) {
                    show.setChecked(true);
                } else {
                    show.setChecked(false);
                }
                show.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            MainActivity.this.showlocalmsgs = true;
                        } else {
                            MainActivity.this.showlocalmsgs = false;
                        }
                    }
                });
                d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.a.dismiss();
                    }
                });
                this.a = d.create();
                this.a.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onPause() {
        Editor e = getSharedPreferences("User", 0).edit();
        e.putBoolean("show", this.showlocalmsgs);
        e.commit();
        try {
            this.bt.disconnect();
        } catch (Exception e2) {
        }
        super.onPause();
    }

    public void addText(String msg) {
        if (this.bt.addTextflag) {
            this.t.append(Html.fromHtml("<font color='#0000FF'>Buddy : " + msg + "</font><br/>"));
        } else {
            this.t.append(Html.fromHtml("<font color='#000000'>Me : " + msg + "</font><br/>"));
        }
        this.relativeLayout.removeAllViews();
        this.relativeLayout.addView(this.t);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            Toast.makeText(getBaseContext(), "Bluetooth is needed", Toast.LENGTH_LONG).show();
            finish();
        } else if (VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_COARSE_LOCATION")) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
        }
    }
}
