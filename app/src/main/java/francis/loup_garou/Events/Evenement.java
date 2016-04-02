package francis.loup_garou.Events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;

import java.io.Serializable;
import java.util.ArrayList;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.Roles;
import francis.loup_garou.fragments.FragmentChasseur;
import francis.loup_garou.fragments.FragmentCupidon;
import francis.loup_garou.fragments.FragmentDayCycle;
import francis.loup_garou.fragments.FragmentEnd;
import francis.loup_garou.fragments.FragmentLoupGarou;
import francis.loup_garou.fragments.FragmentReceivingRole;
import francis.loup_garou.fragments.FragmentSorciere;
import francis.loup_garou.fragments.FragmentVoleur;
import francis.loup_garou.fragments.FragmentVoyante;
import francis.loup_garou.players.*;

/**
 * Created by Francis on 2016-03-06.
 */
public class Evenement implements Serializable {
    protected ArrayList<Joueur> allPlayers = new ArrayList();
    protected EventType type;
    protected Joueur voteur, playerVoted, voleurInitial = null, joueurAVolerInitial = null;
    protected int int1, int2;

    public enum EventType {
        showRole, showDay, voteLoup, showNight, startVoteVillage, voteDay, resultVoteDay, tourLoup,
        villageWin, tourVoyante, tourSorciere, upDate, loupWin, mortDuChasseur, voteDuChasseur,
        tourVoleur, tourCupidon, twoLoversfound, loversFound, changeRoles
    }

    public void execute(Context context) {
        Log.d("Execute", "" + type);


        Game.allPlayers = allPlayers;
        Game.uptdateListsNames();

        /**PLAYERS SECTION**/
        switch (type) {
            case showRole:
                MainActivity.wishingToConnectIDs.clear();
                MainActivity.listInGameName.clear();
                MainActivity.connectedIDs.clear();
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
            case tourVoleur:
                if (Game.enVieEtShow(false)) {
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentVoleur fragmentVoleur = new FragmentVoleur();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentVoleur);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentVoleur.updateList();
                }
                break;
            case tourCupidon:
                if (Game.enVieEtShow(false)) {
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
                    FragmentCupidon fragmentCupidon = new FragmentCupidon();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentCupidon);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentCupidon.updateList();
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
            case changeRoles:
                Voleur joueurAVolerFinal = new Voleur(joueurAVolerInitial.getId(), joueurAVolerInitial.getName());
                Joueur voleurFinal = null;

                if (joueurAVolerInitial instanceof Chasseur) {
                    voleurFinal = new Chasseur(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof Cupidon) {
                    voleurFinal = new Cupidon(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof LoupGarou) {
                    voleurFinal = new LoupGarou(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof PetiteFille) {
                    voleurFinal = new PetiteFille(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof Sorciere) {
                    voleurFinal = new Sorciere(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof Villagois) {
                    voleurFinal = new Villagois(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof Voleur) {
                    voleurFinal = new Voleur(voleurInitial.getId(), voleurInitial.getName());
                } else if (joueurAVolerInitial instanceof Voyante) {
                    voleurFinal = new Voyante(voleurInitial.getId(), voleurInitial.getName());
                }
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    if (Game.allPlayers.get(i).getId() == voleurFinal.getId()) {
                        Game.allPlayers.set(i, voleurFinal);
                    } else if (Game.allPlayers.get(i).getId() == joueurAVolerFinal.getId()) {
                        Game.allPlayers.set(i, joueurAVolerFinal);
                    }
                }

                break;

            case loversFound:
                Joueur player1 = null, player2 = null;
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    if (Game.allPlayers.get(i).getLover() != null) {
                        player1 = Game.allPlayers.get(i);
                        player2 = Game.allPlayers.get(i).getLover();
                    }
                }

                if (player1 == Game.me()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Cupidon")
                            .setMessage("You are in love with " + player2.getName())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setIcon(R.drawable.heart2)
                            .setCancelable(false)
                            .show();
                } else if (player2 == Game.me()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Cupidon")
                            .setMessage("You are in love with " + player1.getName())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setIcon(R.drawable.heart2)
                            .setCancelable(false)
                            .show();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Cupidon")
                            .setMessage("You are not in love")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setIcon(R.drawable.heart2)
                            .setCancelable(false)
                            .show();
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
                Log.d("Evenement.voteLoup", "Received voteLoup de " + voteur.getName());

                boolean killBoolean = true, changeVote = false;
                int pos = -1;

                Log.d("Evenement.voteLoup", "allVoteurs.size() = " + MainActivity.allVoteurs.size());
                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    Log.d("" + MainActivity.allVoteurs.get(i).getId(), "" + voteur.getId());
                    if (MainActivity.allVoteurs.get(i).getId().equals(voteur.getId()) && MainActivity.allVoteurs.size() != 0) {
                        changeVote = true;
                        pos = i;
                        Log.d("changeVoteTrue", "" + pos);
                    }
                }
                Log.d("Evenement.voteLoup", "pos du joueur si deja vote (-1 si premiere fois): " + pos);


                if (changeVote) {
                    Log.d("Evenement.voteLoup", "Change le vote de " + voteur.getName() + " pour tuer " + playerVoted.getName());
                    MainActivity.allVotes.set(pos, playerVoted);
                    MainActivity.showLogs(voteur.getName() + " changed his vote to " + playerVoted.getName());
                } else {
                    Log.d("Evenement.voteLoup", "Ajoute le vote de " + voteur.getName() + " pour tuer " + playerVoted.getName());
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);
                    MainActivity.showLogs(voteur.getName() + " wants to eat " + playerVoted.getName());
                }


                Log.d("Evenement.voteLoup", "allVotes.size() = " + MainActivity.allVotes.size());
                if (MainActivity.allVotes.size() == Game.getNbLoup()) {
                    killBoolean = true;
                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        Log.d("Evenement.voteLoup", "allVotes.get(0):" + MainActivity.allVotes.get(0));
                        Log.d("Evenement.voteLoup", "allVotes.get(i):" + MainActivity.allVotes.get(i));
                        Log.d("Evenement.voteLoup", "allVotes.get(0).getName():" + MainActivity.allVotes.get(0).getName());
                        Log.d("Evenement.voteLoup", "allVotes.get(i).getName():" + MainActivity.allVotes.get(i).getName());
                        if (!MainActivity.allVotes.get(0).getId().equals(MainActivity.allVotes.get(i).getId())) {
                            killBoolean = false;
                        }
                    }
                    Log.d("Evenement.voteLoup", "killBoolean = " + killBoolean);

                    if (killBoolean) {
                        Log.d("Evenement.voteLoup", "kill " + MainActivity.allVotes.get(0).getName());

                        //MainActivity.allVotes.get(0).setEnVie(false);
                        kill(MainActivity.allVotes.get(0), true);
                        MainActivity.showLogs("Every werewolf agreed to eat " + MainActivity.allVotes.get(0).getName());

                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();


                        MainActivity.event.setType(Evenement.EventType.showNight);
                        MainActivity.event.setAllPlayers(Game.allPlayers);

                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            if (Game.allPlayers.get(i) instanceof LoupGarou)
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
                    /*
                    Log.d("Changing vote", "" + pos + " " + voteur.getName() + " " + playerVoted.getName());
                    MainActivity.allVotes.set(pos, playerVoted);
                    */

                } else {
                    Log.d("Adding a vote", voteur.getName() + " " + playerVoted.getName());
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);

                    MainActivity.showLogs(voteur.getName() + " wants to kill " + playerVoted.getName());
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
                    MainActivity.showLogs("Everyone has voted!");

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
                        Joueur deadPlayer = null;
                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            Log.d("" + Game.allPlayers.get(i).getId(), "" + killedID);
                            if (Game.allPlayers.get(i).getId().equals(killedID)) {
                                deadPlayer = allPlayers.get(i);
                                kill(allPlayers.get(i), false);

                                if (Game.allPlayers.get(i).getRole() == Roles.Chasseur) {
                                    Log.d("Killing", "Chasseur");
                                    killingChasseur = true;
                                }
                            }
                        }

                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        if (killingChasseur) {
                            MainActivity.event.setType(EventType.mortDuChasseur);
                            MainActivity.event.setAllPlayers(Game.allPlayers);

                            Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, deadPlayer.getId(), MainActivity.serialize(MainActivity.event));

                            MainActivity.showLogs(deadPlayer.getName() + ", the hunter, was killed by the village!");

                        } else {
                            MainActivity.event.setType(EventType.resultVoteDay);
                            MainActivity.event.setAllPlayers(Game.allPlayers);

                            for (int i = 0; i < Game.allPlayers.size(); i++)
                                Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));

                            MainActivity.showLogs(deadPlayer.getName() + " was killed by the village!");
                        }
                    } else {
                        Log.d("Killing", "False");
                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        MainActivity.showLogs("The village wasn't able to agree on someone to execute, no one dies!");
                    }
                }
                break;
            case voteDuChasseur:
                MainActivity.event.setType(EventType.mortDuChasseur);
                MainActivity.event.setAllPlayers(Game.allPlayers);

                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    Log.d("" + Game.allPlayers.get(i).getId(), "" + playerVoted);

                    if (Game.allPlayers.get(i).getId().equals(playerVoted.getId())) {
                        Game.allPlayers.get(i).setEnVie(false);
                        Log.d("Killing", "" + Game.allPlayers.get(i).getName());

                        if (Game.allPlayers.get(i).getRole() == Roles.Chasseur) {
                            Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                        }
                    }
                }

                MainActivity.event.setType(EventType.resultVoteDay);
                MainActivity.event.setAllPlayers(Game.allPlayers);

                for (int i = 0; i < Game.allPlayers.size(); i++)
                    Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));

                break;
            case twoLoversfound:
                Joueur player1 = null, player2 = null;
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    if (Game.allPlayers.get(i).getLover() != null) {
                        player1 = Game.allPlayers.get(i);
                        player2 = Game.allPlayers.get(i).getLover();
                    }
                }

                MainActivity.showLogs(player1.getName() + " and " + player2.getName() + " are now in love and will die for each other");

                MainActivity.event.setType(EventType.loversFound);
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

    public void setVoleurInitial(Joueur voleurInitial) {
        this.voleurInitial = voleurInitial;
    }

    public void setjoueurAVolerInitial(Joueur joueurAVolerInitial) {
        this.joueurAVolerInitial = joueurAVolerInitial;
    }

    public static void kill(Joueur player, boolean night) {

        player.setEnVie(false);
        try {
            player.getLover().setEnVie(false);
            MainActivity.showLogs(player.getLover().getName() + " is dying of love with " + player.getName());
        } catch (NullPointerException e) {
            //no lovers to kill
        }

        if (night) {
            player.setDeadLastNight(true);
            try {
                player.getLover().setDeadLastNight(true);
            } catch (NullPointerException e) {
                //no lovers to kill
            }
        } else {
            player.setDeadLastNight(false);
            try {
                player.getLover().setDeadLastNight(false);
            } catch (NullPointerException e) {
                //no lovers to kill
            }
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


