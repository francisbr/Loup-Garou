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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {

    android.app.FragmentManager fragmentManager;
    android.app.FragmentTransaction fragmentTransaction;

    String splitSym = "/:/";
    String username = "No name";

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

    /** The hoster information if you are joining a game **/
    public String hosterId;
    public String hosterName;

    /** The current state of the application **/
    @NearbyConnectionState
    private int mState = STATE_IDLE;

    /** The endpoint ID of the connected peer, used for messaging **/
    private ArrayList<String> connectedIDs = new ArrayList();
    private ArrayList<String> wishingToConnectIDs = new ArrayList();
    //Pour les list view de users
    ArrayList<String> listWishName = new ArrayList();
    ArrayList<String> listInGameName = new ArrayList();
    ArrayAdapter<String> adapterWish;
    ArrayAdapter<String> adapterInGame;
    ListView listViewWish;
    ListView listViewInGame;

    //Pour les lest view de games
    ListView listViewNearbyGames;
    ArrayAdapter<String> adapterNearbyGames;
    ArrayList<String> listNearbyGamesName = new ArrayList();
    private ArrayList<String> possiblesHostersIds = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isConnectedToNetwork() == false){

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please connect to wi-fi").setPositiveButton("ok", dialogClickListener).show();

        } else {
            fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();

            FragmentGetName fragmentGetName = new FragmentGetName();

            fragmentTransaction.replace(android.R.id.content, fragmentGetName);
            fragmentTransaction.commit();
        }




        //setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        adapterWish = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listWishName);
        adapterInGame = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listInGameName);
        adapterNearbyGames = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listNearbyGamesName);

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

    public void btnJoin(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        if (btnJoin.getText().toString().equalsIgnoreCase("Rejoindre")) {
            btnJoin.setText("Stop");

            findViewById(R.id.layoutListHost).setVisibility(View.GONE);
            findViewById(R.id.layoutTitleHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.VISIBLE);

            btnAdvertise.setEnabled(false);

            startDiscovery();
        } else if (btnJoin.getText().toString().equalsIgnoreCase("Stop")) {
            btnJoin.setText("Rejoindre");

            findViewById(R.id.layoutListHost).setVisibility(View.GONE);
            findViewById(R.id.layoutTitleHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnAdvertise.setEnabled(true);
        }

        ListView lv = (ListView) findViewById(R.id.listViewNearbyGames);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3)
            {

                sendJoinRequest(position);
            }
        });

    }

    public void startAdvertisingButton(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        if (btnAdvertise.getText().toString().equalsIgnoreCase("Creer")) {
            btnAdvertise.setText("Stop");

            findViewById(R.id.layoutListHost).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutTitleHost).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnJoin.setEnabled(false);

            startAdvertising();
        } else if (btnAdvertise.getText().toString().equalsIgnoreCase("Stop")) {
            btnAdvertise.setText("Creer");

            findViewById(R.id.layoutListHost).setVisibility(View.GONE);
            findViewById(R.id.layoutTitleHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnJoin.setEnabled(true);

            Nearby.Connections.stopAdvertising(mGoogleApiClient);
        }

        ListView lv = (ListView) findViewById(R.id.listViewWantToJoin);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3)
            {
               answerJoinRequest(position);
            }
        });


    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Begin advertising for Nearby Connections, if possible.
     */
    private void startAdvertising() {
        if (!isConnectedToNetwork()) {
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
                if (result.getStatus().isSuccess()) {

                } else {

                    // If the user hits 'Advertise' multiple times in the timeout window,
                    // the error will be STATUS_ALREADY_ADVERTISING
                    int statusCode = result.getStatus().getStatusCode();
                    if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                    } else {
                    }
                }
            }
        });
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

    public void answerJoinRequest(final int position){
        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

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

                                            listInGameName.add(listWishName.get(position));

                                            adapterInGame.notifyDataSetChanged();
                                            listViewInGame.setAdapter(adapterInGame);
                                            connectedIDs.add(wishingToConnectIDs.get(position));


                                            listWishName.remove(position);

                                            adapterWish.notifyDataSetChanged();
                                            listViewWish.setAdapter(adapterWish);

                                            wishingToConnectIDs.remove(position);

                                            sendListPlayersInGame();


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

    /**
     * Send a connection request to a given endpoint.
     * @param endpointId the endpointId to which you want to connect.
     * @param endpointName the name of the endpoint to which you want to connect. Not required to
     *                     make the connection, but used to display after success or failure.
     */
    private void connectTo(String endpointId, final String endpointName) {

        // Send a connection request to a remote endpoint. By passing 'null' for the name,
        // the Nearby Connections API will construct a default name based on device model
        // such as 'LGE Nexus 5'.
        byte[] myPayload = null;
        Nearby.Connections.sendConnectionRequest(mGoogleApiClient, username, endpointId, myPayload,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String endpointId, Status status,
                                                     byte[] bytes) {
                        if (status.isSuccess()) {
                            Toast.makeText(MainActivity.this, "Connected to " + endpointName,
                                    Toast.LENGTH_SHORT).show();

                            hosterId = endpointId;
                            hosterName = endpointName;

                            findViewById(R.id.layoutJoin).setVisibility(View.GONE);
                            findViewById(R.id.layoutInYourGame).setVisibility(View.VISIBLE);


                        } else {
                        }
                    }
                }, this);
    }

    private void showInMyGame() {
        //Efface les noms doubles
        for (int i = 0 ; i < listInGameName.size() ; i++) {
            for (int j = i + 1 ; j < listInGameName.size() ; j++){
                if (listInGameName.get(i).equalsIgnoreCase(listInGameName.get(j))) {
                    listInGameName.remove(i);
                }
            }
        }

        ListView listInGameJoin = (ListView) findViewById(R.id.listViewInYourGame);


        adapterInGame.notifyDataSetChanged();
        listInGameJoin.setAdapter(adapterInGame);

    }



    private void sendListPlayersInGame() {
        String stateTag = "addPlayer" + splitSym;

        // Sends a reliable message, which is guaranteed to be delivered eventually and to respect
        // message ordering from sender to receiver. Nearby.Connections.sendUnreliableMessage
        // should be used for high-frequency messages where guaranteed delivery is not required, such
        // as showing one player's cursor location to another. Unreliable messages are often
        // delivered faster than reliable messages.

        for (int i = 0; i < connectedIDs.size(); i++) {
            String msg = stateTag + username + " (Host)";
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs.get(i), msg.getBytes());

            for (int j = 0; j < listInGameName.size(); j++) {
                msg = stateTag + listInGameName.get(j);
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs.get(i), msg.getBytes());
            }
        }
    }

    private void removePlayerFromLobby(String removedPlayerName) {
        String stateTag = "removePlayerLobby" + splitSym;
        String msg = stateTag + removedPlayerName;

        for (int i = 0; i < connectedIDs.size(); i++) {
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs.get(i), msg.getBytes());
        }
    }

    //Communication entre devices BODY du jeu
    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        String rawMsg = new String(payload);

        String[]  msg = rawMsg.split(splitSym);

        switch (msg[0]) {
            case "addPlayer":
                if (msg[1].equalsIgnoreCase(username)) {
                    msg[1] = "Moi";
                }
                listInGameName.add(msg[1]);
                showInMyGame();
                break;
            case "removePlayerLobby":
                for (int i = 0; i < listInGameName.size(); i++) {
                    if (msg[1].equalsIgnoreCase(listInGameName.get(i))) {
                        listInGameName.remove(i);
                    }
                }
                showInMyGame();
                break;
        }



    }


    public void sendJoinRequest(final int position){
        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        connectTo(possiblesHostersIds.get(position) , listNearbyGamesName.get(position));

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:


                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Join this game?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onConnectionRequest(final String endpointId, String deviceId, String endpointName,
                                    byte[] payload) {

        Toast.makeText(MainActivity.this, "connection request from " + endpointName,Toast.LENGTH_SHORT).show();

        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listWishName.add(endpointName);

        listViewWish.setAdapter(adapterWish);

        wishingToConnectIDs.add(endpointId);


    }

    @Override
    public void onEndpointLost(String endpointId) {

        for (int i = 0; i < listWishName.size() ; i++){
            if (listWishName.get(i) == endpointId){
                Toast.makeText(MainActivity.this, "endPointLost " + listWishName.get(i),Toast.LENGTH_SHORT).show();

                listWishName.remove(i);

                adapterWish.notifyDataSetChanged();
                listViewWish.setAdapter(adapterWish);

                wishingToConnectIDs.remove(i);

            }
        }

    }


    /**
     * Begin discovering devices advertising Nearby Connections, if possible.
     */
    private void startDiscovery() {
        if (!isConnectedToNetwork()) {
            return;
        }

        // Discover nearby apps that are advertising with the required service ID.
        String serviceId = "com.google.example.connectionsquickstart.service";
        Nearby.Connections.startDiscovery(mGoogleApiClient, serviceId, TIMEOUT_DISCOVER, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {

                        } else {

                            // If the user hits 'Discover' multiple times in the timeout window,
                            // the error will be STATUS_ALREADY_DISCOVERING
                            int statusCode = status.getStatusCode();
                            if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                            } else {
                            }
                        }
                    }
                });
    }

    @Override
    public void onEndpointFound(final String endpointId, String deviceId, String serviceId,
                                final String endpointName) {

        Toast.makeText(MainActivity.this, "endpointFound " + endpointName,Toast.LENGTH_SHORT).show();

        listViewNearbyGames = (ListView) findViewById(R.id.listViewNearbyGames);



        listNearbyGamesName.add(endpointName);

        listViewNearbyGames.setAdapter(adapterNearbyGames);

        possiblesHostersIds.add(endpointId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(MainActivity.this, "onConnected",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDisconnected(String endpointId) {

        for (int i = 0 ; i < connectedIDs.size() ; i++){
            if (connectedIDs.get(i).equals(endpointId)){
                Toast.makeText(MainActivity.this, "Disconnected from " + listInGameName.get(i),Toast.LENGTH_SHORT).show();
                removePlayerFromLobby(listInGameName.get(i));

                listInGameName.remove(i);

                adapterInGame.notifyDataSetChanged();
                listViewInGame.setAdapter(adapterInGame);

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
