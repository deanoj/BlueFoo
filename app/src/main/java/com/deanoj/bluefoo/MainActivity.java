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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {


    private BluetoothAdapter bluetoothAdapter;

    private Set<BluetoothDevice> pairedDevices;

    private String TAG = "MainActivity";

    private ArrayAdapter mArrayAdapter;

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    private final UUID MY_UUID = UUID.randomUUID();

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
                bluetoothDevices.add(device);

                Log.d(TAG, deviceInfo);
            }

        }
    };

    private class MyThread extends Thread {
        public void run() {
            while (true) {

                try {
                    Log.d(TAG, "MyThread running");
                    sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendFile(View view) {
        Log.d(TAG, "Send file...");
        MyThread mThread = new MyThread();
        mThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        Log.d(TAG, "manageConnectedSocket: ");
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

                ConnectThread connectThread = new ConnectThread(bluetoothDevices.get(position));
                connectThread.start();

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



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                Log.d(TAG, "waiting for bytes to be read");
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                    //        .sendToTarget();

                    Log.d(TAG, "read bytes: " + bytes);
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
