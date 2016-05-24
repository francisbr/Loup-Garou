package francis.loup_garou;

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
import android.os.CountDownTimer;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import francis.loup_garou.Activities.ActivityGameSettings;
import francis.loup_garou.Events.Evenement;
import francis.loup_garou.fragments.FragmentBackground;
import francis.loup_garou.fragments.FragmentDayCycle;
import francis.loup_garou.fragments.FragmentGetName;
import francis.loup_garou.fragments.FragmentMaitre;
import francis.loup_garou.fragments.FragmentReceivingRole;
import francis.loup_garou.fragments.FragmentSorciere;
import francis.loup_garou.fragments.FragmentStartGame;
import francis.loup_garou.players.Cupidon;
import francis.loup_garou.players.Joueur;
import francis.loup_garou.players.LoupGarou;
import francis.loup_garou.players.Sorciere;
import francis.loup_garou.players.Voleur;
import francis.loup_garou.players.Voyante;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {
    Game myGame;
    MenuItem mnuSettings;
    public static MenuItem mnuShowRole;

    public static FragmentManager fragmentManager;
    public static FragmentTransaction fragmentTransaction;

    public static String splitSym = "/:/";
    String stateTag;

    static String username = "No name";
    public static Roles monRole;
    //public static String myId;

    /**
     * Timeouts (in millis) for startAdvertising and startDiscovery.  At the end of these time
     * intervals the app will silently stop advertising or discovering.
     * <p/>
     * To set advertising or discovery to run indefinitely, use 0L where timeouts are required.
     */
    private static final long TIMEOUT_ADVERTISE = 1000L * 0L;
    private static final long TIMEOUT_DISCOVER = 1000L * 0L;
    public static Evenement event;
    private boolean hosting;

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
    public static ArrayList<String> connectedIDs = new ArrayList();
    public static ArrayList<String> wishingToConnectIDs = new ArrayList();
    //Pour les list view de users
    public static ArrayList<String> listWishName = new ArrayList();
    public static ArrayList<String> listInGameName = new ArrayList();
    ArrayAdapter<String> adapterWish;
    ArrayAdapter<String> adapterInGame;
    ListView listViewWish;
    ListView listViewInGame;

    public static ArrayAdapter<String> adapterAlive;
    public static ArrayAdapter<String> adapterDeadNames;
    public static ArrayAdapter<String> adapterSavable;

    //Pour les list view de games
    ListView listViewNearbyGames;
    ArrayAdapter<String> adapterNearbyGames;
    static ArrayList<String> listNearbyGamesName = new ArrayList();
    private static ArrayList<String> possiblesHostersIds = new ArrayList();

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        adapterWish = new ArrayAdapter<String>(this, R.layout.custom_listview, listWishName);
        adapterInGame = new ArrayAdapter<String>(this, R.layout.custom_listview, listInGameName);
        adapterNearbyGames = new ArrayAdapter<String>(this, R.layout.custom_listview, listNearbyGamesName);

        adapterDeadNames = new ArrayAdapter<String>(this, R.layout.custom_listview, Game.listDeadNames);

        adapterAlive = new ArrayAdapter<String>(this, R.layout.custom_listview, Game.listAliveNames);

        adapterSavable = new ArrayAdapter<String>(this, R.layout.custom_listview, Game.listDeadLastNightNames);

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

           // fragmentTransaction.setCustomAnimations(R.animator.anim, R.animator.anim2);
            fragmentTransaction.replace(android.R.id.content, fragmentStartGame);
            fragmentTransaction.commit();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Nearby.CONNECTIONS_API)
                        .build();
            }
        }
    }

    public void btnJoin(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        if (btnJoin.getText().toString().equalsIgnoreCase(getString(R.string.join_game_btn))) {
            btnJoin.setText(getString(R.string.stop_txt));

            mnuSettings.setVisible(false);
            findViewById(R.id.layoutHost).setVisibility(View.GONE);
            findViewById(R.id.layoutJoin).setVisibility(View.VISIBLE);

            btnAdvertise.setEnabled(false);

            hosting = false;

            startDiscovery();

        } else if (btnJoin.getText().toString().equalsIgnoreCase(getString(R.string.stop_txt))) {
            btnJoin.setText(getString(R.string.join_game_btn));
            listNearbyGamesName.clear();
            possiblesHostersIds.clear();

            adapterNearbyGames.notifyDataSetChanged();

            mnuSettings.setVisible(false);
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

    /**
     * se rend visible au alentour
     * @param view
     */
    public void startAdvertisingButton(View view) {
        Button btnAdvertise = (Button) findViewById(R.id.btnCreateGame);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);
        listViewInGame = (ListView) findViewById(R.id.listViewInGame);

        if (btnAdvertise.getText().toString().equalsIgnoreCase(getString(R.string.create_game_btn))) {

            if (mGoogleApiClient.isConnected()) {
                btnAdvertise.setText(getString(R.string.stop_txt));

                adapterInGame.notifyDataSetChanged();
                listViewInGame.setAdapter(adapterInGame);

                mnuSettings.setVisible(true);
                findViewById(R.id.layoutHost).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutJoin).setVisibility(View.GONE);

                btnJoin.setEnabled(false);

                hosting = true;
                startAdvertising();
            } else {
                Toast.makeText(this,"Wait for the device to connect",Toast.LENGTH_LONG).show();
                if (!mGoogleApiClient.isConnecting()){
                    mGoogleApiClient.connect();
                }
            }

        } else if (btnAdvertise.getText().toString().equalsIgnoreCase(getString(R.string.stop_txt))) {
            btnAdvertise.setText(getString(R.string.create_game_btn));

            mnuSettings.setVisible(false);
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

    /**
     * test
     * @param position
     * @param message
     */
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

    /**
     * Répond à une demande de connection
     * @param position
     */
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

    /**
     * Affiche les joueurs de ma partie
     */
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

    /**
     * Envoie la liste des joueurs à tout le monde
     */
    private void sendListPlayersInGame() {
        stateTag = "addPlayer" + splitSym;
        String msg;

        for (int j = 0; j < listInGameName.size(); j++) {
            msg = stateTag + listInGameName.get(j);
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
        }
    }

    /**
     * retire une personne du lobby
     * @param removedPlayerName le nom de la personne
     */
    private void removePlayerFromLobby(String removedPlayerName) {
        stateTag = "removePlayerLobby" + splitSym;
        String msg = stateTag + removedPlayerName;

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());
    }

    /**
     * quand le device recoie un message
     * @param endpointId l'id de l'émetteur
     * @param payload les données du message
     * @param isReliable si le message est reliable
     */
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
                showMasterFragment();
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

    /**
     * Envoie une demande de connection
     * @param position l'index de la partie
     */
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

    /**
     * Quand l'appareil recoie une demande de connection
     * @param endpointId l'id du demandeur
     * @param deviceId id local
     * @param endpointName le nom du demandeur
     * @param payload données de la demande
     */
    @Override
    public void onConnectionRequest(final String endpointId, String deviceId, String endpointName,
                                    byte[] payload) {
        try {
            listViewWish = (ListView) findViewById(R.id.listViewWantToJoin);
            listWishName.add(endpointName);

            listViewWish.setAdapter(adapterWish);

            wishingToConnectIDs.add(endpointId);

        } catch (NullPointerException e) {
            Joueur player = null;
            for (int i = 0; i < disconnectedPlayers.size(); i++) {
                if (disconnectedPlayers.get(i).getId().split(":")[0].equals(endpointId.split(":")[0])) {
                    player = disconnectedPlayers.get(i);

                    for (int j = 0; j < Game.allPlayers.size(); j++) {
                        if (Game.allPlayers.get(j).getId().split(":")[0].equals(endpointId.split(":")[0])) {
                            Game.allPlayers.get(j).setId(endpointId);
                        }
                    }

                    Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, endpointId,
                            null, MainActivity.this)
                            .setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    MainActivity.event.setType(Evenement.EventType.showRole);
                                    MainActivity.event.setAllPlayers(Game.allPlayers);
                                    Nearby.Connections.sendReliableMessage(mGoogleApiClient, endpointId, MainActivity.serialize(MainActivity.event));
                                }
                            });

                } else {
                    Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, endpointId);
                }
            }
            try {
                disconnectedPlayers.remove(player);
            } catch (NullPointerException f) {

            }
        }
    }

    /**
     * un appareil n'est plus trouvable
     * @param endpointId
     */
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

    /**
     * retoure notre id
     * @return
     */
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

    /**
     * trouve un appareil proche
     * @param endpointId
     * @param deviceId
     * @param serviceId
     * @param endpointName
     */
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

    ArrayList<Joueur> disconnectedPlayers = new ArrayList();

    /**
     * when you loose connection from a connected device
     * @param endpointId
     */
    @Override
    public void onDisconnected(final String endpointId) {

        for (int i = 0; i < connectedIDs.size(); i++) {
            if (connectedIDs.get(i).equals(endpointId)) {
                removePlayerFromLobby(listInGameName.get(i));

                listInGameName.remove(i);

                adapterInGame.notifyDataSetChanged();
                listViewInGame.setAdapter(adapterInGame);

                connectedIDs.remove(i);
            }
        }

        if (hosting) {

            if (disconnectedPlayers.size() == 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Player lost connection")
                        .setMessage("Would you like to wait for him to reconnect?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                waitPlayer(endpointId);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeDisconnectedPlayer(endpointId);

                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    /**
     * wait for a player to reconnect
     * @param endpointId
     */
    private void waitPlayer(String endpointId) {
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).getId().equals(endpointId)) {
                disconnectedPlayers.add(Game.allPlayers.get(i));
            }
        }

        startAdvertising();

        //start timer
        new CountDownTimer(60000, 30000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Nearby.Connections.stopAdvertising(mGoogleApiClient);
            }
        }.start();
    }

    /**
     * remove player from list and continue the game without him
     * @param endpointId
     */
    private void removeDisconnectedPlayer(String endpointId) {
        int pos = -1;

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).getId().equals(endpointId)) {
                pos = i;
            }
        }

        Game.allPlayers.remove(pos);
    }

    /**
     * se souvient du nom rentré
     */
    protected void rememberMyName() {

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
    protected void onPause() {
        super.onPause();
        try {
            Game.me().setReady(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Game.me().setReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FragmentMaitre.enableButtons(Evenement.everyoneReady());
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * Défait le lobby
     */
    private void dismantleLobby() {
        stateTag = "dismantleLobby" + splitSym;
        String msg = stateTag;

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, connectedIDs, msg.getBytes());

        for (int i = 0; i < connectedIDs.size(); i++) {
            sendTest(i, "Dismantling Lobby");
        }

    }

    /**
     * commence la partie
     * @param view
     */
    public void startGame(View view) {
        int minPlayer = 1;

        if (connectedIDs.size() < minPlayer) {
            Toast.makeText(MainActivity.this, "" + (minPlayer - connectedIDs.size()) + " " + getString(R.string.missing_player), Toast.LENGTH_LONG).show();

        } else {
            monRole = Roles.Maitre;
            showMasterFragment();

            myGame = new Game(connectedIDs, listInGameName, mGoogleApiClient, useCustom);

            Nearby.Connections.stopAdvertising(mGoogleApiClient);
            for (int i = 0; i < wishingToConnectIDs.size(); i++)
                Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, wishingToConnectIDs.get(i));

            wishingToConnectIDs.clear();
            listInGameName.clear();
            connectedIDs.clear();

            mnuSettings.setVisible(false);
        }
    }

    /**
     * montre lecran du maitre du jeu
     */
    public void showMasterFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();

        FragmentMaitre fragmentMaitre = new FragmentMaitre();

        fragmentTransaction.replace(android.R.id.content, fragmentMaitre);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    /**
     * Notifie les joueurs que c la nuit
     * @param view
     */
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

        showLogs("" + getString(R.string.logNightStart));
    }

    public void returnNight(View view) {
        Evenement.showNight();
    }

    /**
     * montre lecran du jour
     * @param view
     */
    public void goDay(View view) {
        FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();
        MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();

        MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
        MainActivity.fragmentTransaction.commit();
        MainActivity.fragmentManager.executePendingTransactions();

        fragmentDayCycle.showDay();
        fragmentDayCycle.enableVote();

        Game.me().setReady(true);
    }

    /**
     * Notifie les joueurs que c le jour
     * @param view
     */
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

            for (int i = 0; i < Game.allPlayers.size(); i++) {
                if (Game.allPlayers.get(i).getRole() == Roles.Chasseur && Game.allPlayers.get(i).deadLastNight()) {
                    MainActivity.event.setType(Evenement.EventType.mortDuChasseur);
                    MainActivity.event.setAllPlayers(Game.allPlayers);
                    Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                } else {
                    MainActivity.event.setType(Evenement.EventType.showDay);
                    MainActivity.event.setAllPlayers(Game.allPlayers);
                    Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                }
            }

        }

        showLogs("" + getString(R.string.logDayStart));
    }

    /**
     * notifie les loup
     * @param view
     */
    public void tourLoup(View view) {
        event.setType(Evenement.EventType.tourLoup);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof LoupGarou)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

        showLogs("" + getString(R.string.logWerewolvesTurn));

    }

    /**
     * notifie la voyante
     * @param view
     */
    public void tourVoyante(View view) {
        event.setType(Evenement.EventType.tourVoyante);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof Voyante)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

        showLogs("" + getString(R.string.logSeerTurn));
    }

    /**
     * notifie la sorciere
     * @param view
     */
    public void tourSorciere(View view) {
        event.setType(Evenement.EventType.tourSorciere);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof Sorciere)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

        showLogs("" + getString(R.string.logWitchTurn));
    }

    /**
     * notifie le voleur
     * @param view
     */
    public void tourVoleur(View view) {
        event.setType(Evenement.EventType.tourVoleur);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof Voleur)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

        showLogs("" + getString(R.string.logThiefTurn));
    }

    /**
     * notifie cupidon
     * @param view
     */
    public void tourCupidon(View view) {
        event.setType(Evenement.EventType.tourCupidon);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i) instanceof Cupidon)
                Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }

        showLogs("" + getString(R.string.logCupidTurn));
    }

    /**
     * dit au meitre du jeu les 2 amoureux
     * @param player1
     * @param player2
     */
    public static void send2lovers(Joueur player1, Joueur player2) {

        player1.setLover(player2);
        player2.setLover(player1);

        event.setType(Evenement.EventType.twoLoversfound);
        event.setAllPlayers(Game.allPlayers);

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));
    }

    /**
     * les action de la sorciere
     * @param action la string qui defini laction
     * @param position l'index du joueurs avec qui elle intéragit
     */
    public static void actionSorciere(String action, int position) {
        event.setType(Evenement.EventType.upDate);
        switch (action) {
            case "kill":
                event.kill(FragmentSorciere.getPlayerEnVie(position), true);
                Game.nbPotionMort--;
                break;
            case "save":
                FragmentSorciere.getPlayerDeadLastNight(position).setEnVie(true);
                FragmentSorciere.getPlayerDeadLastNight(position).setDeadLastNight(false);
                Game.nbPotionVie--;
                break;
        }

        event.setAllPlayers(Game.allPlayers);
        event.setInts(Game.nbPotionVie, Game.nbPotionMort);
        //event.setPlayerKilled();

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));
    }

    /**
     * envoie son vote de loup au game master
     * @param player joueur voté
     */
    public static void sendVoteLoup(Joueur player) {

        event.setType(Evenement.EventType.voteLoup);
        event.setAllPlayers(Game.allPlayers);
        event.setJoueurVote(player);

        event.setVoteur(Game.me());

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));

    }

    /**
     * envoie son vote de chasseur au game master
     * @param player joueur voté
     */
    public static void sendVoteChasseur(Joueur player) {
        event.setType(Evenement.EventType.voteDuChasseur);
        event.setAllPlayers(Game.allPlayers);
        event.setJoueurVote(player);

        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));
    }

    /**
     * notifie les joueurs qu'ils doivent voter pour le joueur a mettre au bucher
     * @param view
     */
    public void startVoteVillage(View view) {
        event.setType(Evenement.EventType.startVoteVillage);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++)
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));

        showLogs("" + getString(R.string.logVillageVoteStart));
    }

    /**
     * envoi son vote pour le bucher au hoster
     * @param position
     */
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

    /**
     * envoi son vote pour le capitaine au hoster
     * @param position
     */
    public static void sendVoteCapitain (int position){
        event.setType(Evenement.EventType.voteCapitain);
        event.setAllPlayers((Game.allPlayers));
        event.setVoteur(Game.me());
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.listAliveNames.get(position).equals(Game.allPlayers.get(i).getName())) {
                event.setJoueurVote(Game.allPlayers.get(i));
            }
        }
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));
    }

    /**
     * send les infos de qui la voyante a vu au host pour qu'il l'affiche dans ses logs
     * @param playerVu
     * @param playerVoit
     */
    public static void sendLogVoyante(Joueur playerVu, Joueur playerVoit){
        event.setType(Evenement.EventType.logVoyante);
        event.setAllPlayers(Game.allPlayers);
        event.setJoueurLogVoyante(playerVu, playerVoit);
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));
    }

    /**
     * vole le role d'un joueur (voleur part)
     * @param player
     */
    public static void voleRole(Joueur player) {
        event.setVoleurInitial(Game.me());
        event.setjoueurAVolerInitial(player);
        event.setType(Evenement.EventType.changeRoles);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
        }
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, hosterId, serialize(event));

        Evenement.showNight();

    }

    /**
     * Game fini, retourne au debut
     * @param view
     */
    public void endGame(View view) {

        Game.allPlayers.clear();
        mGoogleApiClient.disconnect();

        clearAllLists();

        recreate();
    }

    /**
     * Efface toute les listes
     */
    public static void clearAllLists() {
        connectedIDs.clear();
        wishingToConnectIDs.clear();
        listWishName.clear();
        listInGameName.clear();

        listNearbyGamesName.clear();
        possiblesHostersIds.clear();
    }

    /**
     * encrypte un objet pour l'envoyer
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * ouvre l'objet pour le comprendre
     * @param data
     * @return
     */
    public static Object deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return is.readObject();
        } catch (NullPointerException e) {
        } catch (ClassNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    /**
     * Affiche les évènements au maitre du jeu
     * @param msg le message a afficher
     */
    public static void showLogs(String msg) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ");
        String strDate = sdf.format(c.getTime());
        int seconds = c.get(Calendar.SECOND), minutes = c.get(Calendar.MINUTE), hours = c.get(Calendar.HOUR_OF_DAY);

        String time = "[" + hours + ":" + minutes + ":" + seconds + "] ";

        FragmentMaitre.mDebugInfo.append("\n" + strDate + msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mnuSettings = menu.findItem(R.id.action_settings);
        mnuShowRole = menu.findItem(R.id.action_show_role);
        return true;
    }

    /**
     * afficher lactivity settings pour modifier les parametre
     * @param item
     */
    public void showSettings(MenuItem item) {
        Intent intent = new Intent(this, ActivityGameSettings.class);

        intent.putExtra("nbPlayer", listInGameName.size());

        if (!useCustom) {
            Game.setNbRoles(listInGameName.size());
        }
        intent.putExtra("nbLoup", Game.nbLoup);
        intent.putExtra("nbChasseur", Game.nbChasseur);
        intent.putExtra("nbSorciere", Game.nbSorciere);
        intent.putExtra("nbVoyante", Game.nbVoyante);
        intent.putExtra("nbPetiteFille", Game.nbPetiteFille);
        intent.putExtra("nbVoleur", Game.nbVoleur);
        intent.putExtra("useCustom", useCustom);

        if (Game.nbCupidon == 1) {
            intent.putExtra("haveCupid", true);
        } else {
            intent.putExtra("haveCupid", false);
        }

        startActivityForResult(intent, 1);
    }

    public void showRoleMenu (MenuItem item) {
        MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
        FragmentReceivingRole fragmentReceivingRole = new FragmentReceivingRole();

        MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentReceivingRole);
        MainActivity.fragmentTransaction.commit();
        MainActivity.fragmentManager.executePendingTransactions();

        fragmentReceivingRole.changeTextRole(MainActivity.monRole);
    }

    boolean useCustom;

    /**
     * recoie les modification de settings, de l'activity settings
     * @param requestCode
     * @param resultCode
     * @param data les données
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("useCustom", false)) {
                    Game.nbLoup = data.getIntExtra("nbLoup", -1);
                    Game.nbSorciere = data.getIntExtra("nbSorciere", -1);
                    Game.nbVoyante = data.getIntExtra("nbVoyante", -1);
                    Game.nbChasseur = data.getIntExtra("nbChasseur", -1);
                    Game.nbPetiteFille = data.getIntExtra("nbPetiteFille", -1);
                    Game.nbVoleur = data.getIntExtra("nbVoleur", -1);
                    Game.nbCupidon = data.getIntExtra("nbCupid", -1);
                    useCustom = true;
                } else {
                    useCustom = false;
                }
            }
        }
    }

    /**
     * notifie les joueurs qu"ils doivent voté pour le capitaine
     * @param view
     */
    public void startVoteCapitain(View view){
        event.setType(Evenement.EventType.startVoteCapitain);
        event.setAllPlayers(Game.allPlayers);

        for (int i = 0; i < Game.allPlayers.size(); i++)
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, Game.allPlayers.get(i).getId(), serialize(event));
    }
}