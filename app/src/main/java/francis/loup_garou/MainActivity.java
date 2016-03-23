package francis.loup_garou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import francis.loup_garou.Activities.ActivityVoyante;
import francis.loup_garou.Events.Evenement;
import francis.loup_garou.fragments.FragmentBackground;
import francis.loup_garou.fragments.FragmentDead;
import francis.loup_garou.fragments.FragmentEnd;
import francis.loup_garou.fragments.FragmentGetName;
import francis.loup_garou.fragments.FragmentMaitre;
import francis.loup_garou.fragments.FragmentStartGame;
import francis.loup_garou.players.Joueur;
import francis.loup_garou.players.LoupGarou;
import francis.loup_garou.players.Voyante;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {
    Game myGame;

    public static FragmentManager fragmentManager;
    public static FragmentTransaction fragmentTransaction;

    public static String splitSym = "/:/";
    String stateTag;

    static String username = "No name";
    public static Roles monRole;
    public static String myId;

    /**
     * Timeouts (in millis) for startAdvertising and startDiscovery.  At the end of these time
     * intervals the app will silently stop advertising or discovering.
     * <p/>
     * To set advertising or discovery to run indefinitely, use 0L where timeouts are required.
     */
    private static final long TIMEOUT_ADVERTISE = 1000L * 0L;
    private static final long TIMEOUT_DISCOVER = 1000L * 0L;
    public static Evenement event;


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
    ListView listViewWish;
    ListView listViewInGame;

    public static ArrayAdapter<String> adapterAlive;
    public static ArrayAdapter<String> adapterDeadNames;

    //Pour les list view de games
    ListView listViewNearbyGames;
    ArrayAdapter<String> adapterNearbyGames;
    ArrayList<String> listNearbyGamesName = new ArrayList();
    private ArrayList<String> possiblesHostersIds = new ArrayList();

    ProgressDialog progressDialog;

    //Loup-garou votes
    public static ArrayList<Joueur> allVotes = new ArrayList();
    public static ArrayList<Joueur> allVoteurs = new ArrayList();


    FragmentGetName fragmentGetName;
    static public SharedPreferences loginPreferences;
    static public SharedPreferences.Editor loginPrefsEditor;
    static public Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        FragmentBackground fragmentBackground = new FragmentBackground();

        fragmentTransaction.replace(android.R.id.content, fragmentBackground);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();



        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentGetName = new FragmentGetName();

        fragmentTransaction.replace(android.R.id.content, fragmentGetName);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();


        Log.d("writting", "name");




        adapterWish = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listWishName);
        adapterInGame = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listInGameName);
        adapterNearbyGames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listNearbyGamesName);

        adapterAlive = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Game.listAliveNames);
        adapterDeadNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Game.listDeadNames);

        event = new Evenement();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
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

            if(!mGoogleApiClient.isConnected()) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Nearby.CONNECTIONS_API)
                        .build();

                mGoogleApiClient.connect();
            }
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


        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs.get(position), msg.getBytes());
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
        // message ordering from sender to receiver. Nearby.Connections.sendReliableMessage
        // should be used for high-frequency messages where guaranteed delivery is not required, such
        // as showing one player's cursor location to another. Unreliable messages are often
        // delivered faster than reliable messages.


        String msg = stateTag + username;
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());

        for (int j = 0; j < listInGameName.size(); j++) {
            msg = stateTag + listInGameName.get(j);
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
        }
    }

    private void removePlayerFromLobby(String removedPlayerName) {
        stateTag = "removePlayerLobby" + splitSym;
        String msg = stateTag + removedPlayerName;

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
    }

    //Communication entre devices BODY du jeu
    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        String rawMsg = new String(payload);


        Object obj = deserialize(payload);

        if (obj instanceof Evenement) {
            ((Evenement) obj).execute(this);
        }


        /** OLD **/
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

            }
        }

        //Starting Game/in-game
        switch (msg[0]) {
            case "start":
                startingGame();
                break;
            case "setRole":
                if (monRole == null)
                    setRole(msg[1]);
                break;
            case "step":
                if (monRole != Roles.Maitre) {

                    if (msg[1].equals("voteDay")) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.conseil)
                                .setMessage(R.string.vote_jour_txt)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }

                    Game.playGame(msg[1], infoSup.get(0));
                }
                break;
            case "listeUpdate":/*
                if (infoSup.get(1).equals("clear")) {
                    Game.playersAliveNames.clear();
                    Game.playersAliveIDs.clear();
                } else {
                    Game.playersAliveNames.add(msg[1]);
                    Game.playersAliveIDs.add(infoSup.get(0));
                }*/
                break;
            case "listeLoup":
                Game.loupIDs.add(msg[1]);
                break;
            case "voteLoup":
                receivingVote("loup", msg[1], infoSup.get(0), infoSup.get(1));
                break;
            case "voteDay":
                receivingVote("village", msg[1], infoSup.get(0), infoSup.get(1));
                break;
            case "kill":/*
                Game.deadLastNightID.add(msg[1]);
                Game.deadLastNightName.add(msg[2]);

                adapterDeadNames.notifyDataSetChanged();*/
                break;
            case "killVillage":/*
                if (infoSup.get(1).equals("no one")) {
                    Toast.makeText(MainActivity.this, R.string.no_executions, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, infoSup.get(0) + getString(R.string.has_been_executed), Toast.LENGTH_SHORT).show();

                Game.deadLastNightName.clear();
                Game.deadLastNightID.clear();

                adapterDeadNames.notifyDataSetChanged();
                adapterAliveNames.notifyDataSetChanged();*/
                break;


        }

        //Toast related messages
        switch (msg[0]) {
            case "test":
                Toast.makeText(MainActivity.this, msg[1], Toast.LENGTH_SHORT).show();
                break;

            case "newVote":
                if (!username.equals(msg[1]))
                    Toast.makeText(MainActivity.this, "" + msg[1] + getString(R.string.has_voted_to_kill) + infoSup.get(0), Toast.LENGTH_SHORT).show();
                break;
            case "changeVote":
                if (!username.equals(msg[1]))
                    Toast.makeText(MainActivity.this, "" + msg[1] + getString(R.string.has_change_vote) + infoSup.get(0), Toast.LENGTH_SHORT).show();
                break;
            case "killed":
                Toast.makeText(MainActivity.this, getString(R.string.successful_wolf_kill) + msg[1], Toast.LENGTH_LONG).show();
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

    protected void rememberMyName(){

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            fragmentGetName.setSavedName(loginPreferences);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        rememberMyName();
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

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());

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

        FragmentMaitre fragmentMaitre = new FragmentMaitre();

        fragmentTransaction.replace(android.R.id.content, fragmentMaitre);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void setNuit(View view) {

        if (Game.getNbLoup() == 0) {
            MainActivity.event.setType(Evenement.EventType.villageWin);
            MainActivity.event.setAllPlayers(Game.allPlayers);

            for (int i = 0; i < Game.allPlayers.size(); i++) {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
            }
            event.execute(this);

        } else {

            MainActivity.event.setType(Evenement.EventType.showNight);
            MainActivity.event.setAllPlayers(Game.allPlayers);

            for (int i = 0; i < Game.allPlayers.size(); i++) {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
            }
        }
    }

    public void setDay(View view) {
        int nbPlayerAlive = 0;
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).isEnVie()) {
                nbPlayerAlive++;
            }
        }
        if (Game.getNbLoup() == nbPlayerAlive) {
            MainActivity.event.setType(Evenement.EventType.loupWin);
            MainActivity.event.setAllPlayers(Game.allPlayers);

            for (int i = 0; i < Game.allPlayers.size(); i++) {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
            }

            event.execute(this);
        } else {
            MainActivity.event.setType(Evenement.EventType.showDay);
            MainActivity.event.setAllPlayers(Game.allPlayers);
            for (int i = 0; i < Game.allPlayers.size(); i++) {
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
            }
        }
    }

    public void tourLoup(View view) {
        event.setType(Evenement.EventType.tourLoup);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof LoupGarou)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

    }

    public void tourVoyante(View view) {
        event.setType(Evenement.EventType.tourVoyante);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof Voyante)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }
    }

    public static void sendVoteLoup(Joueur player) {

        event.setType(Evenement.EventType.voteLoup);
        event.setAllPlayers(Game.allPlayers);
        event.setJoueurVote(player);
        Log.d("playerVoted", player.getName());

        event.setVoteur(Game.me());

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));

    }

    public void receivingVote(String typeVote, String voteur, String voteID, String voteursName) {

        boolean kill = true, changeVote = false;
        int pos = -1;

        Log.d("Entering", "voteVillage");


    }

    public void startVoteVillage(View view) {
        event.setType(Evenement.EventType.startVoteVillage);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++)
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
    }

    public static void sendVoteDay(int position) {
        event.setType(Evenement.EventType.voteDay);
        event.setAllPlayers(Game.allPlayers);
        event.setVoteur(Game.me());
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.listAliveNames.get(position).equals(Game.allPlayers.get(i).getName())) {
                event.setJoueurVote(Game.allPlayers.get(i));
            }
        }


        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));

    }


    public void endGame(View view) {
        Log.d("MainActivity.endGame", "you suck");

        mGoogleApiClient.disconnect();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentGetName = new FragmentGetName();

        fragmentTransaction.replace(android.R.id.content, fragmentGetName);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();

        onStart();
    }


    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
        } catch (IOException e) {
            Log.d("SERIALIZE", "IOException 1");
            e.printStackTrace();
        }
        try {
            os.writeObject(obj);
        } catch (IOException e) {
            Log.d("SERIALIZE", "IOException 2");
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    public static Object deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
        } catch (IOException e) {
            Log.d("DESERIALIZE", "IOException 1");
            e.printStackTrace();
        }
        try {
            return is.readObject();
        } catch (NullPointerException e) {
            Log.d("DESERIALIZE", "No object to deserialize");
        } catch (ClassNotFoundException e) {
            Log.d("DESERIALIZE", "Class not found");
        } catch (IOException e) {
            Log.d("DESERIALIZE", "IOException 2");
            e.printStackTrace();
        }
        return null;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


}
