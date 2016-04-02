package francis.loup_garou;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import francis.loup_garou.Events.Evenement;
import francis.loup_garou.fragments.FragmentDayCycle;
import francis.loup_garou.fragments.FragmentDead;
import francis.loup_garou.fragments.FragmentLoupGarou;
import francis.loup_garou.players.Chasseur;
import francis.loup_garou.players.Cupidon;
import francis.loup_garou.players.Joueur;
import francis.loup_garou.players.LoupGarou;
import francis.loup_garou.players.PetiteFille;
import francis.loup_garou.players.Sorciere;
import francis.loup_garou.players.Villagois;
import francis.loup_garou.players.Voleur;
import francis.loup_garou.players.Voyante;

/**
 * Created by Francis on 2016-01-16.
 */
public class Game {
    public static boolean voteStarted = false;

    int nbLoup, nbVoyante, nbVoleur, nbChasseur, nbCupidon, nbSorciere, nbPetiteFille;
    public static int nbPotionVie = 1, nbPotionMort = 1;

    //Players in game!\
    /**
     * static public ArrayList<String> connectedIDs = new ArrayList();
     * static public ArrayList<String> connectedNames = new ArrayList();
     * static public ArrayList<String> playersAliveIDs = new ArrayList();
     * static public ArrayList<String> playersAliveNames = new ArrayList();
     **/

    public static ArrayList<String> listAliveNames = new ArrayList();
    public static ArrayList<String> listDeadNames = new ArrayList();
    public static ArrayList<String> listDeadLastNightNames = new ArrayList();


    /**
     * New Object Oriented Code
     **/
    static public ArrayList<Joueur> allPlayers = new ArrayList();
    static public ArrayList<Joueur> tempRoleList = new ArrayList();


    public Game(ArrayList<String> connectedIDs, ArrayList<String> listInGameName, GoogleApiClient mGoogleApiClient) {
        Log.d("Game", "Created");

        Log.d("allPlayers", "Creating");
        for (int i = 0; i < connectedIDs.size(); i++) {
            Log.d("" + i, "" + listInGameName.get(i));
            allPlayers.add(new Joueur(connectedIDs.get(i), listInGameName.get(i)));
        }

        setNbRoles();
        setRolesToPlayers();

        Collections.shuffle(tempRoleList);

        allPlayers = (ArrayList<Joueur>) tempRoleList.clone();


        MainActivity.event.setType(Evenement.EventType.showRole);
        MainActivity.event.setAllPlayers(allPlayers);
        for (int i = 0; i < allPlayers.size(); i++) {
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
        }

    }


    private void setRolesToPlayers() {
        ArrayList<Joueur> tempList = (ArrayList<Joueur>) allPlayers.clone();

        //choisi les loups
        choosePlayer(nbLoup, Roles.LoupGarou, tempList);

        //choisi la voyante
        choosePlayer(nbVoyante, Roles.Voyante, tempList);

        //choisi le voleur
        choosePlayer(nbVoleur, Roles.Voleur, tempList);

        //choisi le Chasseur
        choosePlayer(nbChasseur, Roles.Chasseur, tempList);

        //choisi cupidon
        choosePlayer(nbCupidon, Roles.Cupidon, tempList);

        //choisi la Sorciere
        choosePlayer(nbSorciere, Roles.Sorciere, tempList);

        //choisi la petite fille
        choosePlayer(nbPetiteFille, Roles.PetiteFille, tempList);

        if (!tempList.isEmpty()) {
            for (int i = 0; i < tempList.size(); i++) {
                tempRoleList.add(new Villagois(tempList.get(i).getId(), tempList.get(i).getName()));
            }
        }
    }


    public void choosePlayer(int nb, Roles role, ArrayList<Joueur> tempList) {
        Random rand = new Random();
        int randomNum;


        for (int i = 0; i < nb; i++) {
            //          rand.nextInt((max - min) + 1) + min;
            randomNum = rand.nextInt(tempList.size());
            String choosenPLayerId = tempList.get(randomNum).getId(), choosenPLayerName = tempList.get(randomNum).getName();

            if (role == Roles.LoupGarou) {
                tempRoleList.add(new LoupGarou(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.PetiteFille) {
                tempRoleList.add(new PetiteFille(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.Chasseur) {
                tempRoleList.add(new Chasseur(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.Cupidon) {
                tempRoleList.add(new Cupidon(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.Sorciere) {
                tempRoleList.add(new Sorciere(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.Voyante) {
                tempRoleList.add(new Voyante(choosenPLayerId, choosenPLayerName));
            } else if (role == Roles.Voleur) {
                tempRoleList.add(new Voleur(choosenPLayerId, choosenPLayerName));
            }

            tempList.remove(randomNum);
        }


    }

    private void setNbRoles() {

        switch (allPlayers.size()) {
            //Testing
            case 1:
                nbSorciere = 1;
                break;
            case 2:
                nbCupidon = 1;
                nbLoup = 1;
                break;
            case 3:
                nbLoup = 2;
                nbVoyante = 1;
                break;
            case 4:
                nbLoup = 1;
                nbSorciere = 1;
                nbVoyante = 1;
                nbChasseur = 1;
                break;
            case 5:
                nbLoup = 1;
                nbPetiteFille = 1;
                break;
            case 6:
                nbLoup = 2;
                nbPetiteFille = 1;
                nbVoyante = 1;
                break;
            case 7:
                nbLoup = 2;
                nbPetiteFille = 1;
                nbVoyante = 1;
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

    public static int getNbLoup() {
        int nb = 0;


        Log.d("Game.getNbLoup()", "allPlayers.size() = " + allPlayers.size());
        for (int i = 0; i < allPlayers.size(); i++) {
            Log.d("nb", "" + nb);
            if (allPlayers.get(i) instanceof LoupGarou && allPlayers.get(i).isEnVie()) {
                Log.d("" + allPlayers.get(i).getName(), "loup");
                Log.d("nb", "++");
                nb++;
            }
        }

        Log.d("nb", "" + nb);
        return nb;
    }

    public static Joueur me() {
        Joueur player = null;

        for (int i = 0; i < allPlayers.size(); i++) {
            if (allPlayers.get(i).getId().split(":")[0].equals(MainActivity.myId)) {
                return allPlayers.get(i);
            }
        }

        return player;
    }

    public static void uptdateListsNames() {
        listAliveNames.clear();
        listDeadNames.clear();
        listDeadLastNightNames.clear();

        for (int i = 0; i < allPlayers.size(); i++) {
            if (allPlayers.get(i).isEnVie()) {
                listAliveNames.add(allPlayers.get(i).getName());
            } else {
                listDeadNames.add(allPlayers.get(i).getName());

                if (allPlayers.get(i).deadLastNight()) {
                    listDeadLastNightNames.add(allPlayers.get(i).getName());
                }
            }
        }

        MainActivity.adapterAlive.notifyDataSetChanged();
        MainActivity.adapterDeadNames.notifyDataSetChanged();
    }

    public static boolean enVieEtShow(boolean show) {
        FragmentDead fragmentDead = new FragmentDead();
        Log.d("enVieEtShow", "!me().isEnVie() : " + !me().isEnVie() + " et show : " + show);
        if (!me().isEnVie()) {
            if (show) {
                Log.d("enVieEtShow", "true, true");
                MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();

                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDead);
                MainActivity.fragmentTransaction.commit();
                MainActivity.fragmentManager.executePendingTransactions();

            } else {
                Log.d("enVieEtShow", "true, false");
            }

            return false;
        } else {
            Log.d("enVieEtShow", "false, ...");
            return true;
        }
    }
}
