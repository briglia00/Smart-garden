package btclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import btclient.utils.C;
import btlib.BluetoothChannel;
import btlib.BluetoothUtils;
import btlib.ConnectToBluetoothServerTask;
import btlib.ConnectionTask;
import btlib.RealBluetoothChannel;
import btlib.exceptions.BluetoothDeviceNotFound;

public class MainActivity extends AppCompatActivity {
    private BluetoothChannel btChannel;
    private boolean ManualControl;
    private final String[] irrlevels = {"L","M","H"};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ManualControl = false;
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter != null && !btAdapter.isEnabled()) {
            startActivityForResult(
                new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                C.bluetooth.ENABLE_BT_REQUEST
            );
        }

        initUI();
    }

    private void setDefaultState(){
        findViewById(R.id.lamp1_button).setEnabled(false);
        findViewById(R.id.lamp2_button).setEnabled(false);
        findViewById(R.id.lamp3_minus_button).setEnabled(false);
        findViewById(R.id.lamp3_plus_button).setEnabled(false);
        findViewById(R.id.lamp4_minus_button).setEnabled(false);
        findViewById(R.id.lamp4_plus_button).setEnabled(false);
        findViewById(R.id.irr_plus_button).setEnabled(false);
        findViewById(R.id.irr_minus_button).setEnabled(false);
        findViewById(R.id.irrigation_button).setEnabled(false);
        //setAllText("OFF", "OFF", 0, 0, "OPEN", 1);
    }

    private void enableAllButtons(){
        findViewById(R.id.lamp1_button).setEnabled(true);
        findViewById(R.id.lamp2_button).setEnabled(true);
        findViewById(R.id.lamp3_minus_button).setEnabled(true);
        findViewById(R.id.lamp3_plus_button).setEnabled(true);
        findViewById(R.id.lamp4_minus_button).setEnabled(true);
        findViewById(R.id.lamp4_plus_button).setEnabled(true);
        findViewById(R.id.irr_plus_button).setEnabled(true);
        findViewById(R.id.irr_minus_button).setEnabled(true);
        findViewById(R.id.irrigation_button).setEnabled(true);
        //setAllText("OFF", "OFF", 0, 0, "OPEN", 1);
    }

    private void setAllText(String l1on, String l2on, int l3level, int l4level, String irron, int irrlevel){
        ((TextView)findViewById(R.id.lamp4_text)).setText(Integer.toString(l4level));
        ((TextView)findViewById(R.id.lamp3_text)).setText(Integer.toString(l3level));
        ((TextView)findViewById(R.id.lamp1_button)).setText(l1on);
        ((TextView)findViewById(R.id.lamp2_button)).setText(l2on);
        ((TextView)findViewById(R.id.irrigation_button)).setText(irron);
        ((TextView)findViewById(R.id.irr_text)).setText(Integer.toString(irrlevel));
    }

    private void initUI() {
        setDefaultState();
        findViewById(R.id.alarm_button).setVisibility(View.INVISIBLE);
        setAllText("OFF", "OFF", 0, 0, "OPEN", 1);
        findViewById(R.id.connect_button).setEnabled(true);
        findViewById(R.id.manual_button).setEnabled(false);

        findViewById(R.id.connect_button).setOnClickListener(l -> {
            l.setEnabled(false);
            try {
                connectToBTServer();
            } catch (BluetoothDeviceNotFound bluetoothDeviceNotFound) {
                Toast.makeText(this, "Bluetooth device not found !", Toast.LENGTH_LONG)
                        .show();
                bluetoothDeviceNotFound.printStackTrace();
            } finally {
                l.setEnabled(true);
            }
        });

        findViewById(R.id.manual_button).setOnClickListener(l -> {
            if (ManualControl){
                btChannel.sendMessage("AUTOREQ");
                ((TextView) l).setText("Require Manual Control");
                ManualControl = false;
                //set auto state
                setDefaultState();
            } else {
                btChannel.sendMessage("MANUALREQ");
                ((TextView) l).setText("Set Auto Control");
                ManualControl = true;
                //set manual state
                enableAllButtons();
            }
        });

        findViewById(R.id.lamp1_button).setOnClickListener(l -> {
            if(((TextView) l).getText().toString().equals("ON")){
                btChannel.sendMessage("LM1OF");
                ((TextView) l).setText("OFF");
            } else if(((TextView) l).getText().toString().equals("OFF")){
                btChannel.sendMessage("LM1ON");
                ((TextView) l).setText("ON");
            }
        });

        findViewById(R.id.lamp2_button).setOnClickListener(l -> {
            if(((TextView) l).getText().toString().equals("ON")){
                btChannel.sendMessage("LM2OF");
                ((TextView) l).setText("OFF");
            } else if(((TextView) l).getText().toString().equals("OFF")) {
                btChannel.sendMessage("LM2ON");
                ((TextView) l).setText("ON");
            }
        });

        findViewById(R.id.lamp3_minus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.lamp3_text)).getText().toString());
                if(val >= 1 && val <=4){
                    btChannel.sendMessage("LM3LV" + Integer.toString(val - 1));
                    ((TextView) findViewById(R.id.lamp3_text)).setText(Integer.toString(val - 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.lamp3_plus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.lamp3_text)).getText().toString());
                if(val >= 0 && val <=3){
                    btChannel.sendMessage("LM3LV" + Integer.toString(val + 1));
                    ((TextView) findViewById(R.id.lamp3_text)).setText(Integer.toString(val + 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.lamp4_minus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.lamp4_text)).getText().toString());
                if(val >= 1 && val <=4){
                    btChannel.sendMessage("LM4LV" + Integer.toString(val - 1));
                    ((TextView) findViewById(R.id.lamp4_text)).setText(Integer.toString(val - 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.lamp4_plus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.lamp4_text)).getText().toString());
                if(val >= 0 && val <=3){
                    btChannel.sendMessage("LM4LV" + Integer.toString(val + 1));
                    ((TextView) findViewById(R.id.lamp4_text)).setText(Integer.toString(val + 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.irr_plus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.irr_text)).getText().toString());
                if(val >= 1 && val <=2){
                    btChannel.sendMessage("LVLIRR" + irrlevels[val]);
                    ((TextView) findViewById(R.id.irr_text)).setText(Integer.toString(val + 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.irr_minus_button).setOnClickListener(l -> {
            try {
                int val = Integer.parseInt(((TextView) findViewById(R.id.irr_text)).getText().toString());
                if(val >= 2 && val <=3){
                    btChannel.sendMessage("LVLIRR" + irrlevels[val - 2]);
                    ((TextView) findViewById(R.id.irr_text)).setText(Integer.toString(val - 1));
                }
            } catch (NumberFormatException e) {
                //TODO
            }
        });

        findViewById(R.id.irrigation_button).setOnClickListener(l -> {
            if(((TextView) l).getText().toString().equals("OPEN")){
                btChannel.sendMessage("STRIRR");
                ((TextView) l).setText(R.string.button_close);
            } else if(((TextView) l).getText().toString().equals("CLOSE")) {
                btChannel.sendMessage("STPIRR");
                ((TextView) l).setText(R.string.button_open);
            }
        });
        findViewById(R.id.alarm_button).setOnClickListener(l -> {
            btChannel.sendMessage("DISABLEALARM");
            ManualControl = true;
            enableAllButtons();
            l.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        btChannel.close();
        setDefaultState();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    @Nullable final Intent data) {
        if(requestCode == C.bluetooth.ENABLE_BT_REQUEST && resultCode == RESULT_OK) {
            Log.d(C.APP_LOG_TAG, "Bluetooth enabled!");
        }

        if(requestCode == C.bluetooth.ENABLE_BT_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(C.APP_LOG_TAG, "Bluetooth not enabled!");
        }
    }

    private void connectToBTServer() throws BluetoothDeviceNotFound {
        final BluetoothDevice serverDevice = BluetoothUtils
                .getPairedDeviceByName(C.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME);

        final UUID uuid = BluetoothUtils.getEmbeddedDeviceDefaultUuid();
//        final UUID uuid = BluetoothUtils.generateUuidFromString(C.bluetooth.BT_SERVER_UUID);

        new ConnectToBluetoothServerTask(serverDevice, uuid, new ConnectionTask.EventListener() {
            @Override
            public void onConnectionActive(final BluetoothChannel channel) {
                ((TextView) findViewById(R.id.connect_text)).setText(String.format(
                    "Status : connected to device %s",
                    serverDevice.getName()
                ));

                findViewById(R.id.connect_button).setEnabled(false);
                findViewById(R.id.manual_button).setEnabled(true);
                btChannel = channel;
                btChannel.registerListener(new RealBluetoothChannel.Listener() {
                    @Override
                    public void onMessageReceived(String receivedMessage) {
                        if (receivedMessage.substring(0,3).equals("ALL")){
                            String[] elems = receivedMessage.substring(4).split(";");
                            if(elems[6].equals("ALARM")){
                                setDefaultState();
                                findViewById(R.id.alarm_button).setVisibility(View.VISIBLE);
                            }
                            try{
                                setAllText(elems[0], elems[1], Integer.parseInt(elems[2]),Integer.parseInt(elems[3]),
                                        elems[4], Integer.parseInt(elems[5]));
                            } catch(NumberFormatException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onMessageSent(String sentMessage) {}
                });
            }

            @Override
            public void onConnectionCanceled() {
                ((TextView) findViewById(R.id.connect_text)).setText(String.format(
                    "Status : unable to connect, device %s not found!",
                    C.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME
                ));
                setDefaultState();
            }
        }).execute();
    }
}
