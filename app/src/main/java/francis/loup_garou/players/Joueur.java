package francis.loup_garou.players;

import java.io.Serializable;

import francis.loup_garou.Roles;

/**
 * Created by Francis on 2016-03-05.
 */
public class Joueur implements Serializable{

    protected Boolean isEnVie = true, hasVoted = false;
    protected String id, name;

    public Joueur(String id, String name){

        this.id = id;
        this.name = name;
        this.isEnVie = true;
    }

    public String getId() {
        return id;
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

    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public Boolean getHasVoted() {
        return hasVoted;
    }

    public Roles getRole(){
        return null;
    }
}
