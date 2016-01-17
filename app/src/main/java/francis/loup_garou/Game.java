package francis.loup_garou;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import java.util.ArrayList;
import java.util.Random;

import francis.loup_garou.fragments.FragmentGetName;
import francis.loup_garou.fragments.FragmentReceivingRole;

/**
 * Created by Francis on 2016-01-16.
 */
public class Game {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private GoogleApiClient mGoogleApiClient;
    String splitSym = "/:/";

    int nbMaitre, nbLoup, nbVoyante, nbVoleur, nbChasseur, nbCupidon, nbSorciere, nbPetiteFille;
    int min = -1;

    //Players in game!
    ArrayList<String> connectedIDs = new ArrayList();
    ArrayList<String> connectedNames = new ArrayList();

    public Game(ArrayList<String> connectedIDs, ArrayList<String> listInGameName, GoogleApiClient mGoogleApiClient) {
        Log.d("Game", "Created");

        this.connectedIDs = connectedIDs;
        this.connectedNames = listInGameName;
        this.mGoogleApiClient = mGoogleApiClient;

        setNbRoles();
        setRolesToPlayers();

        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, ("start" + splitSym).getBytes());


    }


    private void setRolesToPlayers() {
        ArrayList<String> tempList = (ArrayList<String>) connectedIDs.clone();

        //Notifie les loup-garou
        notifieRole(nbLoup, Roles.LoupGarou, tempList);

        //Notifie la voyante
        notifieRole(nbVoyante, Roles.Voyante, tempList);

        //Notifie le voleur
        notifieRole(nbVoleur, Roles.Voleur, tempList);

        //Notifie le Chasseur
        notifieRole(nbChasseur, Roles.Chasseur, tempList);

        //Notifie cupidon
        notifieRole(nbCupidon, Roles.Cupidon, tempList);

        //Notifie la Sorciere
        notifieRole(nbSorciere, Roles.Sorciere, tempList);

        //Notifie la petite fille
        notifieRole(nbPetiteFille, Roles.PetiteFille, tempList);

        //Notifie le maitre du jeu
        notifieRole(nbMaitre, Roles.Maitre, tempList);


        if (min == -1) {
            Log.d("Sending", "roleHost" + splitSym + Roles.Villageois);
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, ("roleHost" + splitSym + Roles.Villageois).getBytes());
        }
        String msg = "setRole" + splitSym + Roles.Villageois;
        if (!tempList.isEmpty()) {
            Log.d("Sending bad?", msg);
            Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, tempList, msg.getBytes());
        }
    }


    public void notifieRole(int nb, Roles role, ArrayList tempList) {
        String stateTag = "setRole" + splitSym;
        String msg;


        Random rand = new Random();
        int randomNum;


        for (int i = 0; i < nb; i++) {
            msg = "" + role;
            //              rand.nextInt((max - min) + 1) + min;
            randomNum = rand.nextInt((tempList.size() - 1 - min) + 1) + min;
            if (randomNum == -1) {
                Log.d("Sending", "roleHost" + splitSym + msg);
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, connectedIDs, ("roleHost" + splitSym + msg).getBytes());
                min = 0;
            } else {
                Log.d("Sending", stateTag + msg);
                Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, (String) tempList.get(randomNum), (stateTag + msg).getBytes());
                tempList.remove(randomNum);
            }
        }

    }


    private void setNbRoles() {
        int i = 0;

        switch (connectedIDs.size() + 1) {
            //Testing
            case 2:
                nbMaitre = 1;
                break;
            case 3:
                nbMaitre = 1;
                nbVoleur = 1;
                nbSorciere = 1;
                break;


            //Minimum for a real game
            case 9:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                break;
            case 10:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                break;
            case 11:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                break;
            case 12:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                break;
            case 13:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                break;
            case 14:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                break;
            case 15:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                break;
            case 16:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                nbVoleur = 1;
                break;
            case 17:
                nbMaitre = 1;
                nbLoup = 2;
                nbVoyante = 1;
                nbChasseur = 1;
                nbSorciere = 1;
                nbPetiteFille = 1;
                nbCupidon = 1;
                nbVoleur = 1;
                break;
            case 18:
                nbMaitre = 1;
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
