package francis.loup_garou.players;

import android.util.Log;

import com.google.android.gms.nearby.Nearby;

import java.io.Serializable;

import francis.loup_garou.Events.Evenement;
import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.Roles;

/**
 * Created by Francis on 2016-03-05.
 */
public class Joueur implements Serializable {

    protected Boolean isEnVie = true, deadLastNight = false, ready = false, estCapitaine = false;
    protected String id, name;
    Joueur lover;

    public Joueur(String id, String name) {

        this.id = id;
        this.name = name;
        this.isEnVie = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Boolean isEnVie() {
        return isEnVie;
    }

    public void setEnVie(Boolean enVie) {
        isEnVie = enVie;
    }

    public Roles getRole() {
        return null;
    }

    public void setDeadLastNight(Boolean deadLastNight) {
        this.deadLastNight = deadLastNight;
    }

    public Boolean deadLastNight() {
        return deadLastNight;
    }

    public Joueur getLover() {
        return lover;
    }

    public void setLover(Joueur lover) {
        this.lover = lover;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;

        MainActivity.event.setType(Evenement.EventType.readyChanged);
        MainActivity.event.setAllPlayers(Game.allPlayers);

        Nearby.Connections.sendReliableMessage(MainActivity.mGoogleApiClient, MainActivity.hosterId, MainActivity.serialize(MainActivity.event));
    }

    public Boolean isReady() {
        return ready;
    }

    public void setCapitain(Boolean estCapitaine) {
        this.estCapitaine = estCapitaine;
    }

    public Boolean isCapitaine() {
        return estCapitaine;
    }
}