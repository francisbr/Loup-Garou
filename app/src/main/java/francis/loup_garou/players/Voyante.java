package francis.loup_garou.players;

import java.io.Serializable;

import francis.loup_garou.Roles;

/**
 * Created by Francis on 2016-01-16.
 */
public class Voyante extends Joueur implements Serializable {
    public Voyante(String id, String name) {
        super(id, name);
    }

    @Override
    public Roles getRole(){
        return Roles.Voyante;
    }
}
