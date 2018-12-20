package com.example.halac.keyloggers_notify;

import android.bluetooth.BluetoothAdapter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoState;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

public class MindWaveSensor extends AppCompatActivity {
    final String TAG = "MainActivityTag";

    // COMM SDK handles
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;

    // internal variables
    private boolean bRunning = false;

    private Button headsetButton;
    private Button startButton;

    private NskAlgoSdk nskAlgoSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wave_mind);

        nskAlgoSdk = new NskAlgoSdk();
        nskAlgoSdk.NskAlgoInit(NskAlgoType.NSK_ALGO_TYPE_MED.value + NskAlgoType.NSK_ALGO_TYPE_ATT.value + NskAlgoType.NSK_ALGO_TYPE_BLINK.value, getFilesDir().getAbsolutePath());
        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                showToast("Please enable your Bluetooth and re-run this program !", Toast.LENGTH_LONG);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error:" + e.getMessage());
            return;
        }

        headsetButton = this.findViewById(R.id.headsetButton);
        startButton = this.findViewById(R.id.startButton);

        headsetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headsetButton.setEnabled(false);
                startButton.setEnabled(false);

                // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
                tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);

                if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                    // Prepare for connecting
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }

                // (4) Demo of  using connect() and start() to replace connectAndStart(),
                // please call start() when the state is changed to STATE_CONNECTED
                tgStreamReader.connect();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bRunning == false) {
                    nskAlgoSdk.NskAlgoStart(false);
                } else {
                    nskAlgoSdk.NskAlgoPause();
                }
            }
        });

        nskAlgoSdk.setOnStateChangeListener(new NskAlgoSdk.OnStateChangeListener() {
            @Override
            public void onStateChange(int state, int reason) {
                String stateStr = "";
                String reasonStr = "";
                for (NskAlgoState s : NskAlgoState.values()) {
                    if (s.value == state) {
                        stateStr = s.toString();
                    }
                }
                for (NskAlgoState r : NskAlgoState.values()) {
                    if (r.value == reason) {
                        reasonStr = r.toString();
                    }
                }
                Log.d(TAG, "NskAlgoSdkStateChangeListener: state: " + stateStr + ", reason: " + reasonStr);
                final String finalStateStr = stateStr + " | " + reasonStr;
                final int finalState = state;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here

                        if (finalState == NskAlgoState.NSK_ALGO_STATE_RUNNING.value || finalState == NskAlgoState.NSK_ALGO_STATE_COLLECTING_BASELINE_DATA.value) {
                            bRunning = true;
                            startButton.setText("Pause");
                            startButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_STOP.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);

                            headsetButton.setEnabled(true);

                            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                                // Prepare for connecting
                                tgStreamReader.stop();
                                tgStreamReader.close();
                            }

                            System.gc();
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_PAUSE.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_ANALYSING_BULK_DATA.value) {
                            bRunning = true;
                            startButton.setText("Start");
                            startButton.setEnabled(false);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_INITED.value || finalState == NskAlgoState.NSK_ALGO_STATE_UNINTIED.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                        }
                    }
                });
            }
        });

        nskAlgoSdk.setOnEyeBlinkDetectionListener(new NskAlgoSdk.OnEyeBlinkDetectionListener() {
            @Override
            public void onEyeBlinkDetect(int strength) {
                RegistrableSensorManager.Instance.writeMindWaveEvent("Blink");
            }
        });
    }

    @Override
    public void onBackPressed() {
        nskAlgoSdk.NskAlgoUninit();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    //tgStreamReader.startRecordRawData();

                    MindWaveSensor.this.runOnUiThread(new Runnable() {
                        public void run() {
                            startButton.setEnabled(true);
                        }
                    });

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    //tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);

                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }

                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.
            //Log.i(TAG,"onDataReceived");
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
//                    short attValue[] = {(short)data};
//                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    RegistrableSensorManager.Instance.writeMindWaveEvent("Attention");
                    break;
                case MindDataType.CODE_MEDITATION:
//                    short medValue[] = {(short)data};
//                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    RegistrableSensorManager.Instance.writeMindWaveEvent("Meditation");
                    break;
                default:
                    break;
            }
        }
    };

    public void showToast(final String msg, final int timeStyle) {
        MindWaveSensor.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }
        });
    }
}