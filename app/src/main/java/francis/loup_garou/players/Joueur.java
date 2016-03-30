package francis.loup_garou.players;

import java.io.Serializable;

import francis.loup_garou.Roles;

/**
 * Created by Francis on 2016-03-05.
 */
public class Joueur implements Serializable{

    protected Boolean isEnVie = true, deadLastNight = false, hasVoted = false, roleAChange = false;
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

    public Boolean getRoleAChange() {
        return roleAChange;
    }

    public void setEnVie(Boolean enVie) {
        isEnVie = enVie;
    }

    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public void setRoleAChange(Boolean roleAChange) {
        this.roleAChange = roleAChange;
    }

    public Boolean HasVoted() {
        return hasVoted;
    }

    public Roles getRole(){
        return null;
    }

    public void setDeadLastNight(Boolean deadLastNight) {
        this.deadLastNight = deadLastNight;
    }

    public Boolean deadLastNight() {
        return deadLastNight;
    }
}
