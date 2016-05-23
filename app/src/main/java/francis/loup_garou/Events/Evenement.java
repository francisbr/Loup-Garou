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
import francis.loup_garou.fragments.FragmentMaitre;
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
    protected Joueur voteur, playerVoted, voleurInitial = null, joueurAVolerInitial = null, joueurLogVoyanteVu, joueurlogVoyanteVoit;
    protected int int1, int2;

    public enum EventType {
        showRole, showDay, voteLoup, showNight, startVoteVillage, voteDay, resultVoteDay, tourLoup,
        villageWin, tourVoyante, tourSorciere, upDate, loupWin, mortDuChasseur, voteDuChasseur,
        tourVoleur, tourCupidon, twoLoversfound, loversFound, readyChanged, nothing, changeRoles,
        startVoteCapitain, voteCapitain, logVoyante
    }

    public void execute(Context context) {
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

                MainActivity.clearAllLists();
                MainActivity.printLists();
                break;
            case showDay:
                if (Game.enVieEtShow(true)) {
                    FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();
                    MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();

                    MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
                    MainActivity.fragmentTransaction.commit();
                    MainActivity.fragmentManager.executePendingTransactions();

                    fragmentDayCycle.showDay();
                    fragmentDayCycle.enableVote();

                    MainActivity.mnuShowRole.setVisible(true);
                }
                break;

            case showNight:
                if (Game.enVieEtShow(false)) {
                    showNight();
                    MainActivity.mnuShowRole.setVisible(false);
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
            case startVoteCapitain:
                if (Game.enVieEtShow(false)) {
                    Game.voteCapitainStarted = true;
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.tour_de_table)
                            .setMessage(R.string.vote_capitain_txt)
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
                            .setTitle(R.string.cupidon)
                            .setMessage(R.string.inLoveWith + " " + player2.getName())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setIcon(R.drawable.heart2)
                            .setCancelable(false)
                            .show();
                } else if (player2 == Game.me()) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.cupidon)
                            .setMessage(R.string.inLoveWith + " " + player1.getName())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setIcon(R.drawable.heart2)
                            .setCancelable(false)
                            .show();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.cupidon)
                            .setMessage(R.string.notInLove)
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
                //Good version 1.0

                boolean kill = true, changeVote = false;
                int pos = -1;

                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    if (MainActivity.allVoteurs.get(i).getId().equals(voteur.getId()) && MainActivity.allVoteurs.size() != 0) {
                        changeVote = true;
                        pos = i;
                    }
                }


                if (changeVote) {
                    MainActivity.allVotes.set(pos, playerVoted);
                    MainActivity.showLogs(voteur.getName() + " " + R.string.logChangedVote + " " + playerVoted.getName());
                } else {
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);
                    MainActivity.showLogs(voteur.getName() + " " + R.string.logWantsToEat + " " + playerVoted.getName());
                }

                if (MainActivity.allVotes.size() == Game.getNbLoup()) {
                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        if (!MainActivity.allVotes.get(0).getId().equals(MainActivity.allVotes.get(i).getId())) {
                            kill = false;
                        }
                    }

                    if (kill) {

                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            if (Game.allPlayers.get(i).getId().equals(MainActivity.allVotes.get(0).getId())) {
                                kill(Game.allPlayers.get(i), true);
                            }
                        }
                        MainActivity.showLogs("" + R.string.logAllWolvesAgreed + " " + MainActivity.allVotes.get(0).getName());

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
                changeVote = false;

                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    if (MainActivity.allVoteurs.get(i) == voteur && !MainActivity.allVotes.isEmpty()) {
                        changeVote = true;
                    }
                }

                if (changeVote) {

                } else {
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);
                    if (voteur.isCapitaine()) {
                        MainActivity.allVoteurs.add(voteur);
                        MainActivity.allVotes.add(playerVoted);
                    }

                    MainActivity.showLogs(voteur.getName() + " " + R.string.logWantsToKill + " " + playerVoted.getName());
                }

                ArrayList<String> alreadyChecked = new ArrayList();
                ArrayList<Integer> nbVotesEach = new ArrayList();
                ArrayList<Joueur> playersAlive = new ArrayList();

                boolean capEnVie = false;
                for (int i = 0; i < allPlayers.size(); i++) {
                    if (allPlayers.get(i).isEnVie()) {
                        playersAlive.add(allPlayers.get(i));
                        if (allPlayers.get(i).isCapitaine()) {
                            capEnVie = true;
                        }
                    }
                }

                boolean nextCondition = false;
                if (capEnVie) {
                    if (MainActivity.allVotes.size() == playersAlive.size() + 1) {
                        nextCondition = true;
                    }
                } else {
                    if (MainActivity.allVotes.size() == playersAlive.size()) {
                        nextCondition = true;
                    }
                }

                if (nextCondition) {
                    MainActivity.showLogs("" + R.string.logEveryoneVoted);

                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        if (!alreadyChecked.contains(MainActivity.allVotes.get(i).getId())) {
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

                    int plusGrandnbDeVote = -1, posMax = -1;
                    for (int i = 0; i < nbVotesEach.size(); i++) {
                        if (nbVotesEach.get(i) > plusGrandnbDeVote) {
                            plusGrandnbDeVote = nbVotesEach.get(i);
                            posMax = i;
                        }
                    }
                    boolean memeNombreDeVotes = false;

                    for (int i = 0; i < nbVotesEach.size(); i++) {
                        if (i != posMax && nbVotesEach.get(i) == plusGrandnbDeVote) {
                            memeNombreDeVotes = true;
                        }
                    }

                    if (!memeNombreDeVotes) {
                        String killedID = alreadyChecked.get(posMax);
                        boolean killingChasseur = false;
                        Joueur deadPlayer = null;
                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            if (Game.allPlayers.get(i).getId().equals(killedID)) {
                                deadPlayer = allPlayers.get(i);
                                kill(allPlayers.get(i), false);

                                if (Game.allPlayers.get(i).getRole() == Roles.Chasseur) {
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

                            MainActivity.showLogs(deadPlayer.getName() + " " + R.string.logHunterKilled);

                        } else {
                            MainActivity.event.setType(EventType.resultVoteDay);
                            MainActivity.event.setAllPlayers(Game.allPlayers);

                            for (int i = 0; i < Game.allPlayers.size(); i++)
                                Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));

                            MainActivity.showLogs(deadPlayer.getName() + " " + R.string.logVillageVoted);
                        }
                    } else {
                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        MainActivity.showLogs("" + R.string.logNoVillageVote);
                    }
                }
                break;
            case voteDuChasseur:
                MainActivity.event.setType(EventType.mortDuChasseur);
                MainActivity.event.setAllPlayers(Game.allPlayers);

                for (int i = 0; i < Game.allPlayers.size(); i++) {

                    if (Game.allPlayers.get(i).getId().equals(playerVoted.getId())) {
                        Game.allPlayers.get(i).setEnVie(false);

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
            case voteCapitain:
                changeVote = false;

                for (int i = 0; i < MainActivity.allVoteurs.size(); i++) {
                    if (MainActivity.allVoteurs.get(i) == voteur && !MainActivity.allVotes.isEmpty()) {
                        changeVote = true;
                    }
                }

                if (changeVote) {


                } else {
                    MainActivity.allVoteurs.add(voteur);
                    MainActivity.allVotes.add(playerVoted);

                    MainActivity.showLogs(voteur.getName() + " " + R.string.logWantsToKill + " " + playerVoted.getName());
                }

                ArrayList<String> alreadyChecked2 = new ArrayList();
                ArrayList<Integer> nbVotesEach2 = new ArrayList();
                ArrayList<Joueur> playersAlive2 = new ArrayList();

                for (int i = 0; i < allPlayers.size(); i++) {
                    if (allPlayers.get(i).isEnVie()) {
                        playersAlive2.add(allPlayers.get(i));
                    }
                }

                if (MainActivity.allVotes.size() == playersAlive2.size()) {
                    MainActivity.showLogs("" + R.string.logEveryoneVoted);

                    for (int i = 0; i < MainActivity.allVotes.size(); i++) {
                        if (!alreadyChecked2.contains(MainActivity.allVotes.get(i).getId())) {
                            int nbVote = 0;
                            for (int j = i; j < MainActivity.allVotes.size(); j++) {
                                if (MainActivity.allVotes.get(j).getId().equals(MainActivity.allVotes.get(i).getId())) {
                                    nbVote++;
                                }
                            }
                            alreadyChecked2.add(MainActivity.allVotes.get(i).getId());
                            nbVotesEach2.add(nbVote);
                        }
                    }

                    int plusGrandnbDeVote = -1, posMax = -1;
                    for (int i = 0; i < nbVotesEach2.size(); i++) {
                        if (nbVotesEach2.get(i) > plusGrandnbDeVote) {
                            plusGrandnbDeVote = nbVotesEach2.get(i);
                            posMax = i;
                        }
                    }
                    boolean memeNombreDeVotes = false;

                    for (int i = 0; i < nbVotesEach2.size(); i++) {
                        if (i != posMax && nbVotesEach2.get(i) == plusGrandnbDeVote) {
                            memeNombreDeVotes = true;
                        }
                    }



                    if (!memeNombreDeVotes) {
                        String capitainID = alreadyChecked2.get(posMax);
                        for (int i = 0; i < Game.allPlayers.size(); i++) {
                            if (Game.allPlayers.get(i).getId().equals(capitainID)) {
                                Game.allPlayers.get(i).setCapitain(true);
                            }
                        }

                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        MainActivity.event.setType(EventType.resultVoteDay);
                        MainActivity.event.setAllPlayers(Game.allPlayers);

                        for (int i = 0; i < Game.allPlayers.size(); i++)
                            Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));

                        MainActivity.showLogs(capitainID + " " + R.string.logCapitaineVoté);

                    } else {
                        MainActivity.allVoteurs.clear();
                        MainActivity.allVotes.clear();

                        MainActivity.showLogs("" + R.string.logCapitainePasVoté);
                    }
                }
                break;
            case twoLoversfound:
                Joueur player1 = null, player2 = null;
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    if (Game.allPlayers.get(i).getLover() != null) {
                        player1 = Game.allPlayers.get(i);
                        player2 = Game.allPlayers.get(i).getLover();
                    }
                }

                MainActivity.showLogs(player1.getName() + " " + R.string.and + " " + player2.getName() + " " + R.string.logLoversFound);

                MainActivity.event.setType(EventType.loversFound);
                MainActivity.event.setAllPlayers(Game.allPlayers);

                for (int i = 0; i < Game.allPlayers.size(); i++)
                    Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                break;
            case readyChanged:
                FragmentMaitre.enableButtons(everyoneReady());

                MainActivity.event.setType(EventType.nothing);
                MainActivity.event.setAllPlayers(Game.allPlayers);
                for (int i = 0; i < Game.allPlayers.size(); i++)
                    Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, Game.allPlayers.get(i).getId(), MainActivity.serialize(MainActivity.event));
                break;
            case logVoyante:
                MainActivity.showLogs(joueurlogVoyanteVoit.getName() + " (" + R.string.voyante + ") " + R.string.logVoyanteAVu +" "+ joueurLogVoyanteVu.getName() + R.string.logSeerSawRole + joueurLogVoyanteVu.getRole() + ")");
                break;
        }
    }

    public static boolean everyoneReady() {
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (!Game.allPlayers.get(i).isReady()) {
                return false;
            }
        }
        return true;
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
            MainActivity.showLogs(player.getLover().getName() + " " + R.string.logLoverDies + " " + player.getName());
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

    public void setJoueurLogVoyante(Joueur joueurLogVoyanteVu, Joueur joueurlogVoyanteVoit) {
        this.joueurLogVoyanteVu = joueurLogVoyanteVu;
        this.joueurlogVoyanteVoit = joueurlogVoyanteVoit;
    }
}


