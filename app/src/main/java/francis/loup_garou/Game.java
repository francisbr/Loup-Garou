package francis.loup_garou;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import java.util.ArrayList;
import java.util.Random;

import francis.loup_garou.fragments.FragmentDayCycle;

/**
 * Created by Francis on 2016-01-16.
 */
public class Game {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private GoogleApiClient mGoogleApiClient;
    String splitSym = MainActivity.splitSym;

    static boolean haveMaitre = false;
    int nbLoup, nbVoyante, nbVoleur, nbChasseur, nbCupidon, nbSorciere, nbPetiteFille;
    int min = -1;

    //Players in game!
    ArrayList<String> connectedIDs = new ArrayList();
    ArrayList<String> connectedNames = new ArrayList();
    ArrayList<String> playersAlive = new ArrayList();

    public Game(ArrayList<String> connectedIDs, ArrayList<String> listInGameName, GoogleApiClient mGoogleApiClient) {
        Log.d("Game", "Created");

        this.connectedIDs = connectedIDs;
        this.connectedNames = listInGameName;
        this.mGoogleApiClient = mGoogleApiClient;

        playersAlive = (ArrayList<String>) connectedNames.clone();
        setNbRoles();
        setRolesToPlayers();

        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, ("start" + splitSym).getBytes());


    }


    private void setRolesToPlayers() {
        ArrayList<String> tempList = (ArrayList<String>) connectedIDs.clone();

        //Notifie les loup-garou
        sendRoles(nbLoup, Roles.LoupGarou, tempList);

        //Notifie la voyante
        sendRoles(nbVoyante, Roles.Voyante, tempList);

        //Notifie le voleur
        sendRoles(nbVoleur, Roles.Voleur, tempList);

        //Notifie le Chasseur
        sendRoles(nbChasseur, Roles.Chasseur, tempList);

        //Notifie cupidon
        sendRoles(nbCupidon, Roles.Cupidon, tempList);

        //Notifie la Sorciere
        sendRoles(nbSorciere, Roles.Sorciere, tempList);

        //Notifie la petite fille
        sendRoles(nbPetiteFille, Roles.PetiteFille, tempList);

        //Notifie le maitre du jeu
        //sendRoles(nbMaitre, Roles.Maitre, tempList);


        if (min == -1 && MainActivity.monRole!=null) {
            Log.d("monRole", "" + Roles.Villageois);
            MainActivity.monRole = Roles.Villageois;
        }
        String msg = "setRole" + splitSym + Roles.Villageois;
        if (!tempList.isEmpty()) {
            Log.d("Sending bad?", msg);
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, tempList, msg.getBytes());
        }
    }


    public void sendRoles(int nb, Roles role, ArrayList tempList) {
        String stateTag = "setRole" + splitSym;
        String msg;


        Random rand = new Random();
        int randomNum;


        for (int i = 0; i < nb; i++) {
            msg = "" + role;
            //              rand.nextInt((max - min) + 1) + min;
            randomNum = rand.nextInt((tempList.size() - 1 - min) + 1) + min;
            if (randomNum == -1) {
                Log.d("monRole", "" + role);
                MainActivity.monRole = role;
                min = 0;
            } else {
                Log.d("Sending", stateTag + msg);
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, (String) tempList.get(randomNum), (stateTag + msg).getBytes());
                tempList.remove(randomNum);
            }
        }

    }

    public static void playGame(String step){
        MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
        FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();

        switch (step){
            case "nuit":
                Log.d("starting", "nuit");



                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
                MainActivity.fragmentTransaction.commit();
                MainActivity.fragmentManager.executePendingTransactions();

                fragmentDayCycle.showNight();

                break;

            case "day":


                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
                MainActivity.fragmentTransaction.commit();
                MainActivity.fragmentManager.executePendingTransactions();

                fragmentDayCycle.showDay();

                break;

        }

    }

    private void setNbRoles() {

        switch (connectedIDs.size()) {
            //Testing
            case 1:
                nbCupidon = 1;
                break;
            case 2:
                nbPetiteFille = 1;
                break;
            case 3:
                nbLoup = 1;
                nbSorciere = 1;
                break;
            case 4:
                nbLoup = 1;
                nbSorciere = 1;
                break;
            case 5:
                nbLoup = 1;
                nbSorciere = 1;
                break;


            //Minimum for a real game
            case 8:
                nbLoup = 2;
                nbVoyante = 1;
            case 9:
                nbLoup = 2;
                nbVoyante = 1;
                break;
            case 10:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                break;
            case 11:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                break;
            case 12:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                break;
            case 13:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                break;
            case 14:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                break;
            case 15:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                break;
            case 16:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                nbVoleur = 1;
                break;
            case 17:
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                nbVoleur = 1;
                break;
            case 18:
                nbLoup = 4;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                nbVoleur = 1;
                break;
        }
    }

}
