package francis.loup_garou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import francis.loup_garou.fragments.FragmentMaitre;
import francis.loup_garou.fragments.FragmentReceivingRole;
import francis.loup_garou.fragments.FragmentStartGame;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {
    Game myGame;

    static FragmentManager fragmentManager;
    static FragmentTransaction fragmentTransaction;

    public static String splitSym = "/:/";
    String stateTag;

    String username = "No name";
    public static Roles monRole;

    /**
     * Timeouts (in millis) for startAdvertising and startDiscovery.  At the end of these time
     * intervals the app will silently stop advertising or discovering.
     * <p/>
     * To set advertising or discovery to run indefinitely, use 0L where timeouts are required.
     */
    private static final long TIMEOUT_ADVERTISE = 1000L * 0L;
    private static final long TIMEOUT_DISCOVER = 1000L * 0L;


    /**
     * Possible states for this application:
     * IDLE - GoogleApiClient not yet connected, can't do anything.
     * READY - GoogleApiClient connected, ready to use Nearby Connections API.
     * ADVERTISING - advertising for peers to connect.
     * DISCOVERING - looking for a peer that is advertising.
     * CONNECTED - found a peer.
     */
    @Retention(RetentionPolicy.CLASS)
    @IntDef({STATE_IDLE, STATE_READY, STATE_ADVERTISING, STATE_DISCOVERING, STATE_CONNECTED})
    public @interface NearbyConnectionState {
    }

    private static final int STATE_IDLE = 1023;
    private static final int STATE_READY = 1024;
    private static final int STATE_ADVERTISING = 1025;
    private static final int STATE_DISCOVERING = 1026;
    private static final int STATE_CONNECTED = 1027;

    /**
     * GoogleApiClient for connecting to the Nearby Connections API
     **/
    static public GoogleApiClient mGoogleApiClient;

    /**
     * The hoster information if you are joining a game
     **/
    public static String hosterId;
    public static String hosterName;

    /**
     * The current state of the application
     **/
    @NearbyConnectionState
    private int mState = STATE_IDLE;

    /**
     * The endpoint ID of the connected peer, used for messaging
     **/
    private ArrayList<String> connectedIDs = new ArrayList();
    private ArrayList<String> wishingToConnectIDs = new ArrayList();
    //Pour les list view de users
    ArrayList<String> listWishName = new ArrayList();
    ArrayList<String> listInGameName = new ArrayList();
    ArrayAdapter<String> adapterWish;
    ArrayAdapter<String> adapterInGame;
    static public ArrayAdapter<String> adapterAliveNames;
    ListView listViewWish;
    ListView listViewInGame;

    //Pour les lest view de games
    ListView listViewNearbyGames;
    ArrayAdapter<String> adapterNearbyGames;
    ArrayList<String> listNearbyGamesName = new ArrayList();
    private ArrayList<String> possiblesHostersIds = new ArrayList();

    ProgressDialog progressDialog;


    FragmentGetName fragmentGetName;
    static private SharedPreferences loginPreferences;
    static private SharedPreferences.Editor loginPrefsEditor;
    static private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentGetName = new FragmentGetName();

        fragmentTransaction.replace(android.R.id.content, fragmentGetName);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();


        Log.d("writting", "name");


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        adapterWish = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listWishName);
        adapterInGame = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listInGameName);
        adapterNearbyGames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listNearbyGamesName);

        adapterAliveNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Game.playersAliveNames);
    }


    public void saveName(View view) {
        EditText textNom = (EditText) findViewById(R.id.txtUsername);
        CheckBox rememberMe = (CheckBox) findViewById(R.id.rememberMeCheckBox);
        if (!isConnectedToNetwork()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_internet);
            builder.setMessage(R.string.plz_connect_internet);
            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {


            username = textNom.getText().toString();

            if (rememberMe.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", username);
                loginPrefsEditor.commit();
                loginPrefsEditor.apply();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }


            fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();

            FragmentStartGame fragmentStartGame = new FragmentStartGame();


            fragmentTransaction.replace(android.R.id.content, fragmentStartGame);
            fragmentTransaction.commit();


            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void btnJoin(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        if (btnJoin.getText().toString().equalsIgnoreCase(getString(R.string.join_game_btn))) {
            btnJoin.setText(getString(R.string.stop_txt));

            findViewById(R.id.layoutHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.VISIBLE);

            btnAdvertise.setEnabled(false);

            startDiscovery();
        } else if (btnJoin.getText().toString().equalsIgnoreCase(getString(R.string.stop_txt))) {
            btnJoin.setText(getString(R.string.join_game_btn));
            listNearbyGamesName.clear();
            possiblesHostersIds.clear();

            adapterNearbyGames.notifyDataSetChanged();

            findViewById(R.id.layoutHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnAdvertise.setEnabled(true);

            Nearby.Connections.stopDiscovery(mGoogleApiClient, getString(R.string.serviceId));
        }

        ListView lv = (ListView) findViewById(R.id.listViewNearbyGames);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {

                sendJoinRequest(position);
            }
        });

    }

    public void startAdvertisingButton(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

        if (btnAdvertise.getText().toString().equalsIgnoreCase(getString(R.string.create_game_btn))) {
            btnAdvertise.setText(getString(R.string.stop_txt));


            adapterInGame.notifyDataSetChanged();
            listViewInGame.setAdapter(adapterInGame);


            findViewById(R.id.layoutHost).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnJoin.setEnabled(false);
            startAdvertising();

        } else if (btnAdvertise.getText().toString().equalsIgnoreCase(getString(R.string.stop_txt))) {
            btnAdvertise.setText(getString(R.string.create_game_btn));

            findViewById(R.id.layoutHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.GONE);

            btnJoin.setEnabled(true);
            Nearby.Connections.stopAdvertising(mGoogleApiClient);
        }

        ListView lv = (ListView) findViewById(R.id.listViewWantToJoin);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                answerJoinRequest(position);
            }
        });

        listViewInGame.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
            }
        });


    }

    private void sendTest(int position, String message) {
        stateTag = "test" + splitSym;
        String msg = stateTag + message;


        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs.get(position), msg.getBytes());
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
     *
     * @return true if connected or connecting, false otherwise.
     */
    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return (info != null && info.isConnectedOrConnecting());
    }

    public void answerJoinRequest(final int position) {
        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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

                        listWishName.remove(position);

                        adapterWish.notifyDataSetChanged();
                        listViewWish.setAdapter(adapterWish);

                        wishingToConnectIDs.remove(position);

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.accept_player_question).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    /**
     * Send a connection request to a given endpoint.
     *
     * @param endpointId   the endpointId to which you want to connect.
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


                            hosterId = endpointId;
                            hosterName = endpointName;

                            findViewById(R.id.layoutJoin).setVisibility(View.GONE);
                            findViewById(R.id.layoutInYourGame).setVisibility(View.VISIBLE);

                            progressDialog.dismiss();

                            Nearby.Connections.stopDiscovery(mGoogleApiClient, getString(R.string.serviceId));
                        } else {
                            progressDialog.dismiss();
                        }
                    }
                }, this);
    }

    private void showInMyGame() {
        //Efface les noms doubles
        for (int i = 0; i < listInGameName.size(); i++) {
            for (int j = i + 1; j < listInGameName.size(); j++) {
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
        stateTag = "addPlayer" + splitSym;

        // Sends a reliable message, which is guaranteed to be delivered eventually and to respect
        // message ordering from sender to receiver. Nearby.Connections.sendUnreliableMessage
        // should be used for high-frequency messages where guaranteed delivery is not required, such
        // as showing one player's cursor location to another. Unreliable messages are often
        // delivered faster than reliable messages.


        String msg = stateTag + username;
        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());

        for (int j = 0; j < listInGameName.size(); j++) {
            msg = stateTag + listInGameName.get(j);
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
        }
    }

    private void removePlayerFromLobby(String removedPlayerName) {
        stateTag = "removePlayerLobby" + splitSym;
        String msg = stateTag + removedPlayerName;

        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
    }

    //Communication entre devices BODY du jeu
    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        String rawMsg = new String(payload);
        Log.d("Received", rawMsg);

        String[] msg = rawMsg.split(splitSym);

        //Try adding info!
        ArrayList<String> infoSup = new ArrayList();
        try {
            infoSup.add(msg[2]);
            infoSup.add(msg[3]);
            infoSup.add(msg[4]);
            infoSup.add(msg[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            infoSup.add("");

        }


        //In Lobby
        if (monRole == null) {
            switch (msg[0]) {
                case "addPlayer":
                    if (msg[1].equalsIgnoreCase(username)) {
                        msg[1] = msg[1];
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
                case "dismantleLobby":
                    listInGameName.clear();
                    showInMyGame();

                    findViewById(R.id.layoutInYourGame).setVisibility(View.GONE);
                    break;
                case "test":
                    Toast.makeText(MainActivity.this, msg[1], Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        //Starting Game/in-game
        switch (msg[0]) {
            /*
            case "connectedPlayers":
                Game.connectedIDs.add(msg[1]);
                Game.connectedNames.add(msg[2]);

                Game.playersAliveIDs.add(msg[1]);
                Game.playersAliveNames.add(msg[2]);

                for (int i = 0; i < Game.connectedIDs.size(); i++) {
                    Log.d("connected player", Game.connectedIDs.get(i) + " " + Game.connectedNames.get(i));
                }
                break;*/
            case "start":
                startingGame();
                break;
            case "setRole":
                if (monRole == null)
                    setRole(msg[1]);
                break;
            case "step":
                if (monRole != Roles.Maitre) {
                    Game.playGame(msg[1], infoSup.get(0));
                }
                break;
            case "listeUpdate":
                if (infoSup.get(1).equals("clear")) {
                    Game.playersAliveNames.clear();
                    Game.playersAliveIDs.clear();
                } else {
                    Game.playersAliveNames.add(msg[1]);
                    Game.playersAliveIDs.add(infoSup.get(0));
                }
                break;
            case "listeLoup":
                Game.loupIDs.add(msg[1]);
                break;

        }


    }


    public void sendJoinRequest(final int position) {
        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        connectTo(possiblesHostersIds.get(position), listNearbyGamesName.get(position));
                        progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.waitin_answer_title), getString(R.string.txt_waiting_answer), true, false);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:


                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.join_game_question).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    @Override
    public void onConnectionRequest(final String endpointId, String deviceId, String endpointName,
                                    byte[] payload) {


        listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
        listWishName.add(endpointName);

        listViewWish.setAdapter(adapterWish);

        wishingToConnectIDs.add(endpointId);


    }

    @Override
    public void onEndpointLost(String endpointId) {
        listViewNearbyGames = (ListView) findViewById(R.id.listViewNearbyGames);
        int pos = 0;

        for (int i = 0; i < listNearbyGamesName.size(); i++) {
            if (endpointId.equalsIgnoreCase(getDeviceInfo("id", listNearbyGamesName.get(i)))) {
                pos = i;
            }
        }


        listNearbyGamesName.remove(pos);
        possiblesHostersIds.remove(pos);

        adapterNearbyGames.notifyDataSetChanged();
        listViewNearbyGames.setAdapter(adapterNearbyGames);
    }

    /**
     * Retourne l'info voulue sur un device quelqonque
     *
     * @param infoWanted "id" ou "name" indique ce que l'on veux
     * @param infoDetail donne le device voulue
     * @return
     */
    public String getDeviceInfo(String infoWanted, String infoDetail) {
        switch (infoWanted) {
            case "id":
                try {
                    for (int i = 0; i < listInGameName.size(); i++) {
                        if (listInGameName.get(i).equalsIgnoreCase(infoDetail)) {
                            return connectedIDs.get(i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //check l'autre liste anw.
                }
                for (int i = 0; i < listNearbyGamesName.size(); i++) {
                    if (listNearbyGamesName.get(i).equalsIgnoreCase(infoDetail)) {
                        return possiblesHostersIds.get(i);
                    }
                }


            case "name":
                try {
                    for (int i = 0; i < connectedIDs.size(); i++) {
                        if (connectedIDs.get(i).equalsIgnoreCase(infoDetail)) {
                            return listInGameName.get(i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //check l'autre liste anw.
                }

                for (int i = 0; i < possiblesHostersIds.size(); i++) {
                    if (possiblesHostersIds.get(i).equalsIgnoreCase(infoDetail)) {
                        return listNearbyGamesName.get(i);
                    }
                }
            default:
                Toast.makeText(MainActivity.this, "ERROR: Couldn't found requested info", Toast.LENGTH_SHORT).show();
                return "erreur";
        }

    }

    static public String getMyId() {
        return Nearby.Connections.getLocalDeviceId(mGoogleApiClient);
    }


    /**
     * Begin discovering devices advertising Nearby Connections, if possible.
     */
    private void startDiscovery() {
        if (!isConnectedToNetwork()) {
            return;
        }

        // Discover nearby apps that are advertising with the required service ID.
        String serviceId = getString(R.string.serviceId);
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

        listViewNearbyGames = (ListView) findViewById(R.id.listViewNearbyGames);


        listNearbyGamesName.add(endpointName);

        listViewNearbyGames.setAdapter(adapterNearbyGames);

        possiblesHostersIds.add(endpointId);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected(String endpointId) {

        for (int i = 0; i < connectedIDs.size(); i++) {
            if (connectedIDs.get(i).equals(endpointId)) {
                removePlayerFromLobby(listInGameName.get(i));

                listInGameName.remove(i);

                adapterInGame.notifyDataSetChanged();
                listViewInGame.setAdapter(adapterInGame);

                connectedIDs.remove(i);


            }
        }
    }

    private void setRole(String s) {

        switch (s) {
            case "LoupGarou":
                monRole = Roles.LoupGarou;
                break;
            case "Voyante":
                monRole = Roles.Voyante;
                break;
            case "Voleur":
                monRole = Roles.Voleur;
                break;
            case "Chasseur":
                monRole = Roles.Chasseur;
                break;
            case "Cupidon":
                monRole = Roles.Cupidon;
                break;
            case "Sorciere":
                monRole = Roles.Sorciere;
                break;
            case "PetiteFille":
                monRole = Roles.PetiteFille;
                break;
            case "Villageois":
                monRole = Roles.Villageois;
                break;
            case "Maitre":
                Toast.makeText(MainActivity.this, "You are now the game master", Toast.LENGTH_SHORT).show();
                monRole = Roles.Maitre;
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            fragmentGetName.setSavedName(loginPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!connectedIDs.isEmpty()) {
            dismantleLobby();
        }

        if (mGoogleApiClient != null) {
            for (int i = 0; i < wishingToConnectIDs.size(); i++) {
                Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, wishingToConnectIDs.get(i));
                listWishName.remove(i);
                wishingToConnectIDs.remove(i);
            }


            adapterWish.notifyDataSetChanged();
            mGoogleApiClient.disconnect();
        }
    }

    private void dismantleLobby() {
        stateTag = "dismantleLobby" + splitSym;
        String msg = stateTag;

        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());

        for (int i = 0; i < connectedIDs.size(); i++) {
            sendTest(i, "Dismantling Lobby");
        }

    }

    public void startGame(View view) {
        int minPlayer = 1;

        if (connectedIDs.size() < minPlayer) {
            Toast.makeText(MainActivity.this, "" + (minPlayer - connectedIDs.size()) + " " + getString(R.string.missing_player), Toast.LENGTH_LONG).show();

        } else {
            myGame = new Game(connectedIDs, listInGameName, mGoogleApiClient);


            Nearby.Connections.stopAdvertising(mGoogleApiClient);
            for (int i = 0; i < wishingToConnectIDs.size(); i++)
                Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, wishingToConnectIDs.get(i));
            wishingToConnectIDs.clear();

            monRole = Roles.Maitre;
            startingGame();

        }
    }

    public void startingGame() {
        fragmentTransaction = fragmentManager.beginTransaction();

        if (monRole == Roles.Maitre) {
            FragmentMaitre fragmentMaitre = new FragmentMaitre();

            fragmentTransaction.replace(android.R.id.content, fragmentMaitre);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();

        } else {

            final FragmentReceivingRole fragmentReceivingRole = new FragmentReceivingRole();

            new AlertDialog.Builder(this)
                    .setTitle(R.string.game_is_starting)
                    .setMessage(R.string.hide_your_device_advice)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            fragmentTransaction.replace(android.R.id.content, fragmentReceivingRole);
                            fragmentTransaction.commit();
                            fragmentManager.executePendingTransactions();

                            fragmentReceivingRole.changeTextRole(monRole);
                        }
                    })
                    .setCancelable(false)
                    .show();

        }
    }

    public void setNuit(View view) {
        String stateTag = "step" + splitSym;
        String msg = stateTag + "nuit";

        Log.d("sending to connected", "" + msg);
        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.playersAliveIDs, msg.getBytes());
    }

    public void setDay(View view) {
        String stateTag = "step" + splitSym;
        String msg = stateTag + "day" + splitSym + Game.nbLoupAlive;

        updatePlayerAlive();
        Log.d("sending to connected", "" + msg);
        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.playersAliveIDs, msg.getBytes());
    }

    public void tourLoup(View view) {
        String stateTag = "step" + splitSym;
        String msg = stateTag + "tourLoup";

        updatePlayerAlive();

        for (int i = 0; i < Game.loupIDs.size(); i++) {
            Log.d("sending to connected", "listeLoup" + splitSym + Game.loupIDs.get(i));
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.loupIDs, ("listeLoup" + splitSym + Game.loupIDs.get(i)).getBytes());
        }

        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.loupIDs, msg.getBytes());
    }

    private void updatePlayerAlive() {
        String stateTag = "listeUpdate" + splitSym;
        String msg;

        msg = stateTag + "" + splitSym + "" + splitSym + "clear";
        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.connectedIDs, msg.getBytes());

        for (int i = 0; i < Game.playersAliveIDs.size(); i++) {
            msg = stateTag + Game.playersAliveNames.get(i) + splitSym + Game.playersAliveIDs.get(i);
            Log.d("sending to connected", "" + msg);
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, Game.connectedIDs, msg.getBytes());
        }
    }


}
