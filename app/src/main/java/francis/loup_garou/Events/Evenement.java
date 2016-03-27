package francis.loup_garou.Events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;

import java.io.Serializable;
import java.util.ArrayList;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.Roles;
import francis.loup_garou.fragments.FragmentChasseur;
import francis.loup_garou.fragments.FragmentDayCycle;
import francis.loup_garou.fragments.FragmentEnd;
import francis.loup_garou.fragments.FragmentLoupGarou;
import francis.loup_garou.fragments.FragmentReceivingRole;
import francis.loup_garou.fragments.FragmentSorciere;
import francis.loup_garou.fragments.FragmentVoyante;
import francis.loup_garou.players.*;

/**
 * Created by Francis on 2016-03-06.
 */
public class Evenement implements Serializable {
    protected ArrayList<Joueur> allPlayers = new ArrayList();
    protected EventType type;
    protected Joueur voteur, playerVoted;
    protected int int1, int2;

    public enum EventType {
        showRole, showDay, voteLoup, showNight, startVoteVillage, voteDay, resultVoteDay, tourLoup, villageWin, tourVoyante, tourSorciere, upDate, loupWin, mortDuChasseur, voteDuChasseur
    }

    public void execute(Context context) {
        Log.d("Execute", "" + type);


        Game.allPlayers = allPlayers;
        Game.uptdateListsNames();

        /**PLAYERS SECTION**/
        switch (type) {
            case showRole:
                setMonRole();
                Game.nbPotionVie = 1;
                Game.nbPotionMort = 1;

                new AlertDialog.Builder(context)
                        .setTitle(R.string.game_is_starting)
                        .setMessage(R.string.hide_your_device_advice)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                                FragmentReceivingRole fragmentReceivingRole = new FragmentReceivingRole();

                                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentReceivingRole);
                                MainActivity.fragmentTransaction.commit();
                                MainActivity.fragmentManager.executePendingTransactions();

                                fragmentReceivingRole.changeTextRole(MainActivity.monRole);
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case showDay:
                if (Game.enVieEtShow(true)) {
                    FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentDayCycle.showDay("" + Game.getNbLoup());
                    Log.d("showDay 1", "" + Game.getNbLoup());
                    fragmentDayCycle.enableVote();
                }
                break;

            case showNight:
                if (Game.enVieEtShow(false)) {
                    showNight();
                }
                break;

            case tourLoup:
                if (Game.enVieEtShow(false)) {
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentLoupGarou fragmentLoupGarou = new FragmentLoupGarou();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentLoupGarou);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentLoupGarou.updateList();
                }
                break;
            case tourVoyante:
                if (Game.enVieEtShow(false)) {
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentVoyante fragmentVoyante = new FragmentVoyante();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentVoyante);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentVoyante.updateList();
                }
                break;
            case tourSorciere:
                if (Game.enVieEtShow(false) || mortCetteNuit()) {
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentSorciere fragmentSorciere = new FragmentSorciere();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentSorciere);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentSorciere.updateLists();
                }
                break;
            case startVoteVillage:
                if (Game.enVieEtShow(false)) {
                    Game.voteStarted = true;

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.conseil)
                            .setMessage(R.string.vote_jour_txt)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            case resultVoteDay:
                Game.enVieEtShow(true);
                break;
            case villageWin:
                MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                FragmentEnd fragmentEnd = new FragmentEnd();

                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentEnd);
                MainActivity.fragmentTransaction.commit();
                MainActivity.fragmentManager.executePendingTransactions();

                fragmentEnd.villageWin();
                break;
            case loupWin:
                MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                FragmentEnd fragmentEnd2 = new FragmentEnd();

                MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentEnd2);
                MainActivity.fragmentTransaction.commit();
                MainActivity.fragmentManager.executePendingTransactions();

                fragmentEnd2.loupWin();
                break;
            case mortDuChasseur:
                if (!Game.enVieEtShow(false)) {
                    Log.d("mortDuChasseur", "!Game.enVieEtShow(false): " + !Game.enVieEtShow(false));
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentChasseur fragmentChasseur = new FragmentChasseur();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentChasseur);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();
                    fragmentChasseur.updateList();
                }
                break;
        }

        /**HOST SECTION**/
        switch (type) {
            case upDate:
                Game.nbPotionVie = int1;
                Game.nbPotionMort = int2;
                break;
            case voteLoup://Quand le host recoi un vote
                Log.d("Received", "voteLoup");

                boolean kill = true, changeVote = false;
                int pos = -1;

                Log.d("allVoteurs.size()", "" + MainActivity.allVoteurs.size());
                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    if (MainActivity.allVoteurs.get(i) == voteur && !MainActivity.allVotes.isEmpty()) {
                        changeVote = true;
                        pos = i;
                    }
                }
                Log.d("pos", "" + pos);


                if (changeVote) {
                    MainActivity.allVotes.set(pos, playerVoted);

                } else {
                    Log.d("Adding a vote", voteur + " " + playerVoted);
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);

                }


                Log.d("allVotes.size()", "" + MainActivity.allVotes.size());
                if (MainActivity.allVotes.size() == Game.getNbLoup()) {

                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        Log.d("" + MainActivity.allVotes.get(0).getName(), MainActivity.allVotes.get(i).getName());
                        if (!(MainActivity.allVotes.get(0) == MainActivity.allVotes.get(i))) {
                            kill = false;
                        }
                    }


                    if (kill) {
                        Log.d("kill", MainActivity.allVotes.get(0).getName());

                        //MainActivity.allVotes.get(0).setEnVie(false);
                        kill(MainActivity.allVotes.get(0), true);


                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        MainActivity.event.setType(Evenement.EventType.showNight);
                        MainActivity.event.setAllPlayers(Game.allPlayers);

                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            if (Game.allPlayers.get(i).getRole() == Roles.LoupGarou)
                                Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                        }
                    }
                }

                break;

            case voteDay:
                Log.d("voteDay", "Starting");
                kill = true;
                changeVote = false;
                pos = -1;

                Log.d("allVoteurs.size()", "" + MainActivity.allVoteurs.size());
                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    if (MainActivity.allVoteurs.get(i) == voteur && !MainActivity.allVotes.isEmpty()) {
                        changeVote = true;
                        pos = i;
                    }
                }
                Log.d("pos", "" + pos);


                if (changeVote) {
                    Log.d("Changing vote", "" + pos + " " + voteur.getName() + " " + playerVoted.getName());
                    MainActivity.allVotes.set(pos, playerVoted);

                } else {
                    Log.d("Adding a vote", voteur.getName() + " " + playerVoted.getName());
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);

                }

                ArrayList<String> alreadyChecked = new ArrayList();
                ArrayList<Integer> nbVotesEach = new ArrayList();
                ArrayList<Joueur> playersAlive = new ArrayList();

                for (int i = 0; i < allPlayers.size(); i++) {
                    if (allPlayers.get(i).isEnVie()) {
                        playersAlive.add(allPlayers.get(i));
                    }
                }

                if (MainActivity.allVotes.size() == playersAlive.size()) {
                    Log.d("VoteDay", "All vote received!");

                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        if (!alreadyChecked.contains(MainActivity.allVotes.get(i).getId())) {
                            Log.d(".contains", "false");
                            int nbVote = 0;
                            for (int j = i; j < MainActivity.allVotes.size(); j++) {
                                if (MainActivity.allVotes.get(j).getId().equals(MainActivity.allVotes.get(i).getId())) {
                                    nbVote++;
                                }
                            }
                            alreadyChecked.add(MainActivity.allVotes.get(i).getId());
                            nbVotesEach.add(nbVote);
                        }
                    }
                    Log.d("alreadyChecked.size", "" + alreadyChecked.size());
                    Log.d("nbVotesEach.size", "" + nbVotesEach.size());

                    int plusGrandnbDeVote = -1, posMax = -1;
                    for (int i = 0; i < nbVotesEach.size(); i++) {
                        if (nbVotesEach.get(i) > plusGrandnbDeVote) {
                            plusGrandnbDeVote = nbVotesEach.get(i);
                            posMax = i;
                        }
                    }
                    Log.d("max " + plusGrandnbDeVote, "posMax " + posMax);

                    Log.d("playersAlive", "" + playersAlive.size());
                    for (int i = 0; i < playersAlive.size(); i++) {
                        Log.d("" + i, "" + playersAlive.get(i).getName() + " " + playersAlive.get(i).getId());
                    }

                    Log.d("alreadyChecked", "" + alreadyChecked.size());
                    for (int i = 0; i < alreadyChecked.size(); i++) {
                        Log.d("" + i, "" + alreadyChecked.get(i));
                    }

                    if (plusGrandnbDeVote >= (playersAlive.size() / 2) + 1) {
                        Log.d("Killing", "True " + alreadyChecked.get(posMax));
                        String killedID = alreadyChecked.get(posMax);
                        boolean killingChasseur = false;
                        Joueur chasseur = null;
                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            Log.d("" + Game.allPlayers.get(i).getId(), "" + killedID);
                            if (Game.allPlayers.get(i).getId().equals(killedID)) {

                                kill(allPlayers.get(i), false);

                                if (Game.allPlayers.get(i).getRole() == Roles.Chasseur) {
                                    Log.d("Killing", "Chasseur");
                                    chasseur = Game.allPlayers.get(i);
                                    killingChasseur = true;
                                }
                            }
                        }

                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        if (killingChasseur) {
                            MainActivity.event.setType(EventType.mortDuChasseur);
                            MainActivity.event.setAllPlayers(Game.allPlayers);

                            Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, chasseur.getId(), MainActivity.serialize(MainActivity.event));

                        } else {
                            MainActivity.event.setType(EventType.resultVoteDay);
                            MainActivity.event.setAllPlayers(Game.allPlayers);

                            for (int i = 0; i < Game.allPlayers.size(); i++)
                                Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                        }
                    } else {
                        Log.d("Killing", "False");
                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();
                    }
                }
                break;
            case voteDuChasseur:
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    Log.d("" + Game.allPlayers.get(i).getId(), "" + playerVoted);

                    if (Game.allPlayers.get(i).getId().equals(playerVoted.getId())) {
                        Game.allPlayers.get(i).setEnVie(false);
                        Log.d("Killing", "" + Game.allPlayers.get(i).getName());

                        if (Game.allPlayers.get(i).getRole() == Roles.Chasseur) {
                            MainActivity.event.setType(EventType.mortDuChasseur);
                            MainActivity.event.setAllPlayers(Game.allPlayers);
                            Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                        }
                    }
                }

                MainActivity.event.setType(EventType.resultVoteDay);
                MainActivity.event.setAllPlayers(Game.allPlayers);

                for (int i = 0; i < Game.allPlayers.size(); i++)
                    Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));

                break;
        }
    }


    private boolean mortCetteNuit() {
        if (Game.me().deadLastNight())
            return true;
        else
            return false;
    }

    private void setMonRole() {

        for (int i = 0; i < allPlayers.size(); i++) {
            String idToCompare = allPlayers.get(i).getId().split(":")[0];

            if (idToCompare.equals(MainActivity.getMyId())) {

                MainActivity.myId = MainActivity.getMyId();

                if (allPlayers.get(i) instanceof LoupGarou)
                    MainActivity.monRole = Roles.LoupGarou;
                else if (allPlayers.get(i) instanceof Villagois)
                    MainActivity.monRole = Roles.Villageois;
                else if (allPlayers.get(i) instanceof Cupidon)
                    MainActivity.monRole = Roles.Cupidon;
                else if (allPlayers.get(i) instanceof Chasseur)
                    MainActivity.monRole = Roles.Chasseur;
                else if (allPlayers.get(i) instanceof Voyante)
                    MainActivity.monRole = Roles.Voyante;
                else if (allPlayers.get(i) instanceof Sorciere)
                    MainActivity.monRole = Roles.Sorciere;
                else if (allPlayers.get(i) instanceof Voleur)
                    MainActivity.monRole = Roles.Voleur;
                else if (allPlayers.get(i) instanceof PetiteFille)
                    MainActivity.monRole = Roles.PetiteFille;
            }
        }
    }

    public void setAllPlayers(ArrayList<Joueur> allPlayers) {
        this.allPlayers = allPlayers;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public void setVoteur(Joueur voteur) {
        this.voteur = voteur;
    }

    public void setJoueurVote(Joueur joueurVote) {
        this.playerVoted = joueurVote;
    }

    private void kill(Joueur player, boolean night) {

        player.setEnVie(false);

        if (night) {
            player.setDeadLastNight(true);
        } else {
            player.setDeadLastNight(false);
        }
    }

    public void setInts(int int1, int int2) {
        this.int1 = int1;
        this.int2 = int2;
    }

    public static void showNight() {
        FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();
        MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();

        MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
        MainActivity.fragmentTransaction.commit();
        MainActivity.fragmentManager.executePendingTransactions();

        fragmentDayCycle.showNight();
    }
}


