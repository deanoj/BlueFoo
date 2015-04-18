package com.deanoj.bluefoo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {


    private BluetoothAdapter bluetoothAdapter;

    private Set<BluetoothDevice> pairedDevices;

    private String TAG = "MainActivity";

    private ArrayAdapter mArrayAdapter;

    private UUID uuid = UUID.randomUUID();

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Add the name and address to an array adapter to show in a ListView
                String deviceInfo = "Found device: " + device.getName() + "\n" + device.getAddress();

                mArrayAdapter.add(deviceInfo);

                Log.d(TAG, deviceInfo);
            }

        }
    };

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BLUEFOO", uuid);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }

        public void manageConnectedSocket(BluetoothSocket socket) {
            Log.d(TAG, "socket managed");
        }
    }




    public void searchBluetooth(View view) {

        bluetoothAdapter.cancelDiscovery();
        boolean result = bluetoothAdapter.startDiscovery();
        Log.d(TAG, "searching bluetooth: " + result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "item clicked");
            }
        });

        if(bluetoothAdapter != null)
        {
            if (bluetoothAdapter.isEnabled()) {

                String status;
                if (bluetoothAdapter.isEnabled()) {
                    String mydeviceaddress = bluetoothAdapter.getAddress();
                    String mydevicename = bluetoothAdapter.getName();
                    status = mydevicename + " : " + mydeviceaddress;

                    int state = bluetoothAdapter.getState();
                    status = "Status: " + mydevicename + " : " + mydeviceaddress + " : " + state;

                    Log.d(TAG, status);


                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                }
                else
                {
                    status = "Bluetooth is not Enabled.";
                }

                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
            else
            {
                // Disabled. Do something else.
                String disabledStr = "Bluetooth is disbaled";
                Log.d(TAG, disabledStr);
                Toast.makeText(this, disabledStr, Toast.LENGTH_LONG).show();
            }
        }

        Log.d(TAG, "complete");


    }

    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
