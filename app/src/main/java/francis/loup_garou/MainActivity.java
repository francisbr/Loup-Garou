package francis.loup_garou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import francis.loup_garou.fragments.FragmentGetName;
import francis.loup_garou.fragments.FragmentStartGame;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {

    android.app.FragmentManager fragmentManager;
    android.app.FragmentTransaction fragmentTransaction;

    String username;

    /**
     * Timeouts (in millis) for startAdvertising and startDiscovery.  At the end of these time
     * intervals the app will silently stop advertising or discovering.
     *
     * To set advertising or discovery to run indefinitely, use 0L where timeouts are required.
     */
    private static final long TIMEOUT_ADVERTISE = 1000L * 30L;
    private static final long TIMEOUT_DISCOVER = 1000L * 30L;

    /**
     * Possible states for this application:
     *      IDLE - GoogleApiClient not yet connected, can't do anything.
     *      READY - GoogleApiClient connected, ready to use Nearby Connections API.
     *      ADVERTISING - advertising for peers to connect.
     *      DISCOVERING - looking for a peer that is advertising.
     *      CONNECTED - found a peer.
     */
    @Retention(RetentionPolicy.CLASS)
    @IntDef({STATE_IDLE, STATE_READY, STATE_ADVERTISING, STATE_DISCOVERING, STATE_CONNECTED})
    public @interface NearbyConnectionState {}
    private static final int STATE_IDLE = 1023;
    private static final int STATE_READY = 1024;
    private static final int STATE_ADVERTISING = 1025;
    private static final int STATE_DISCOVERING = 1026;
    private static final int STATE_CONNECTED = 1027;

    /** GoogleApiClient for connecting to the Nearby Connections API **/
    private GoogleApiClient mGoogleApiClient;

    /** The current state of the application **/
    @NearbyConnectionState
    private int mState = STATE_IDLE;

    /** The endpoint ID of the connected peer, used for messaging **/
    private ArrayList<String> connectedIDs = new ArrayList();
    private ArrayList<String> wishingToConnectIDs = new ArrayList();
    //Pour le list view
    ArrayList<String> listWish = new ArrayList();
    ArrayList<String> listInGame = new ArrayList();
    ArrayAdapter<String> adapterWish;
    ArrayAdapter<String> adapterInGame;
    ListView listeViewWish;
    ListView listeViewInGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        FragmentGetName fragmentGetName = new FragmentGetName();

        fragmentTransaction.replace(android.R.id.content, fragmentGetName);
        fragmentTransaction.commit();


        //setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        adapterWish = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listWish);
        adapterInGame = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listInGame);

    }


    public void saveName(View view) {
        EditText textNom = (EditText) findViewById(R.id.txtUsername);

        username = textNom.getText().toString();

        textNom.setText("");


        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        FragmentStartGame fragmentStartGame = new FragmentStartGame();


        fragmentTransaction.replace(android.R.id.content, fragmentStartGame);
        fragmentTransaction.commit();

    }
    public void startAdvertisingButton(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);

        Log.d("btn", btnAdvertise.getText().toString());

        if (btnAdvertise.getText().toString().equalsIgnoreCase("Creer")) {
            btnAdvertise.setText("Stop");

            findViewById(R.id.layoutInAndWishList).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutInAndWishTitle).setVisibility(View.VISIBLE);

            startAdvertising();
        } else if (btnAdvertise.getText().toString().equalsIgnoreCase("Stop")) {
            btnAdvertise.setText("Creer");

            findViewById(R.id.layoutInAndWishList).setVisibility(View.GONE);
            findViewById(R.id.layoutInAndWishTitle).setVisibility(View.GONE);
        }

        ListView lv = (ListView) findViewById(R.id.wantToJoinListView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3)
            {
                Log.d("listView", "Clicked " + position);

                answerNotification(position);
            }
        });


    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onConnected", "");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended", "" + i);
    }

    @Override
    public void onClick(View v) {
        /*switch(v.getId()) {
            case R.id.btnCreateGame:
                startAdvertising();
                break;
            case R.id.button_discover:
                startDiscovery();
                break;
            case R.id.button_send:
                sendMessage();
                break;

        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("onConnectionFailed", connectionResult.toString());
    }

    /**
     * Begin advertising for Nearby Connections, if possible.
     */
    private void startAdvertising() {
        Log.d("Advertising","Start");
        if (!isConnectedToNetwork()) {
            Log.d("Network","Not connected");
            return;
        }

        // Advertising with an AppIdentifer lets other devices on the network discover
        // this application and prompt the user to install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // Advertise for Nearby Connections. This will broadcast the service id defined in
        // AndroidManifest.xml. By passing 'null' for the name, the Nearby Connections API
        // will construct a default name based on device model such as 'LGE Nexus 5'.
        Nearby.Connections.startAdvertising(mGoogleApiClient, username, appMetadata, TIMEOUT_ADVERTISE,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                Log.d("Advertising:onResult", "" + result);
                if (result.getStatus().isSuccess()) {

                    updateViewVisibility(STATE_ADVERTISING);
                } else {

                    // If the user hits 'Advertise' multiple times in the timeout window,
                    // the error will be STATUS_ALREADY_ADVERTISING
                    int statusCode = result.getStatus().getStatusCode();
                    if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                    } else {
                        updateViewVisibility(STATE_READY);
                    }
                }
            }
        });
        Log.d("Advertising","Done");
    }

    private void updateViewVisibility(@NearbyConnectionState int newState) {
        mState = newState;
        switch (mState) {
            case STATE_IDLE:
                // The GoogleAPIClient is not connected, we can't yet start advertising or
                // discovery so hide all buttons
                 /*
                findViewById(R.id.layout_nearby_buttons).setVisibility(View.GONE);
                findViewById(R.id.layout_message).setVisibility(View.GONE);
                */
                break;
            case STATE_READY:
                // The GoogleAPIClient is connected, we can begin advertising or discovery.
                /*
                findViewById(R.id.layout_nearby_buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_message).setVisibility(View.GONE);
                */
                break;
            case STATE_ADVERTISING:
                break;
            case STATE_DISCOVERING:
                break;
            case STATE_CONNECTED:
                // We are connected to another device via the Connections API, so we can
                // show the message UI.
                /*
                findViewById(R.id.layout_nearby_buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_message).setVisibility(View.VISIBLE);
                */
                break;
        }
    }

    /**
     * Check if the device is connected (or connecting) to a WiFi network.
     * @return true if connected or connecting, false otherwise.
     */
    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return (info != null && info.isConnectedOrConnecting());
    }

    public void answerNotification(final int position){
        listeViewWish = (ListView) findViewById(R.id.wantToJoinListView);
        listeViewInGame = (ListView) findViewById(R.id.inGameListView);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, wishingToConnectIDs.get(position),
                                null, MainActivity.this)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {

                                            listInGame.add(listWish.get(position));

                                            adapterInGame.notifyDataSetChanged();
                                            listeViewInGame.setAdapter(adapterInGame);
                                            connectedIDs.add(wishingToConnectIDs.get(position));


                                            listWish.remove(position);

                                            adapterWish.notifyDataSetChanged();
                                            listeViewWish.setAdapter(adapterWish);

                                            wishingToConnectIDs.remove(position);


                                            updateViewVisibility(STATE_CONNECTED);
                                        } else {
                                        }
                                    }
                                });

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, wishingToConnectIDs.get(position));

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Add this player too your game?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onConnectionRequest(final String endpointId, String deviceId, String endpointName,
                                    byte[] payload) {

        listeViewWish = (ListView) findViewById(R.id.wantToJoinListView);



        listWish.add(endpointName);

        listeViewWish.setAdapter(adapterWish);

        wishingToConnectIDs.add(endpointId);


    }

    @Override
    public void onEndpointFound(String endpointId, String deviceId, String serviceId, String endpointName) {
        Log.d("onEndpointFound", endpointId);
    }

    @Override
    public void onEndpointLost(String endpointId) {
        Log.d("onEndpointLost", endpointId);

        for (int i = 0 ; i < listWish.size() ; i++){
            if (listWish.get(i) == endpointId){

                listWish.remove(i);

                adapterWish.notifyDataSetChanged();
                listeViewWish.setAdapter(adapterWish);

                wishingToConnectIDs.remove(i);

            }
        }

    }

    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        Log.d("onMessageReceived", endpointId);
    }

    @Override
    public void onDisconnected(String endpointId) {

        for (int i = 0 ; i < connectedIDs.size() ; i++){
            if (connectedIDs.get(i).equals(endpointId)){

                listInGame.remove(i);

                adapterInGame.notifyDataSetChanged();
                listeViewInGame.setAdapter(adapterInGame);

                connectedIDs.remove(i);

            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
}
