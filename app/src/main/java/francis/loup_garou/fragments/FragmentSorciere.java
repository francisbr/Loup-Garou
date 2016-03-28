package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.players.Joueur;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSorciere extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TEST

        return inflater.inflate(R.layout.fragment_sorciere, container, false);
    }

    public void updateLists() {

        ListView listSavable = (ListView) getView().findViewById(R.id.savablePlayersList);

        listSavable.setAdapter(MainActivity.adapterSavable);
        MainActivity.adapterSavable.notifyDataSetChanged();
        hideLists();

        listSavable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                MainActivity.actionSorciere("save", position);
                MainActivity.adapterSavable.notifyDataSetChanged();
                hideLists();
            }
        });


        ListView listKillable = (ListView) getView().findViewById(R.id.witchKillablePlayersList);

        Game.listAliveNames.clear();
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).isEnVie() && Game.me() != Game.allPlayers.get(i)) {
                Game.listAliveNames.add(Game.allPlayers.get(i).getName());
            }
        }

        listKillable.setAdapter(MainActivity.adapterAlive);
        MainActivity.adapterAlive.notifyDataSetChanged();

        hideLists();

        listKillable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.actionSorciere("kill", position);
                hideLists();
            }
        });


    }

    public void hideLists() {
        if (Game.nbPotionVie == 0) {
            getView().findViewById(R.id.noPotLifetxt).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.savablePlayersList).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.noPotLifetxt).setVisibility(View.GONE);
            getView().findViewById(R.id.savablePlayersList).setVisibility(View.VISIBLE);
        }
        if (Game.nbPotionMort == 0) {
            getView().findViewById(R.id.noPotKilltxt).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.witchKillablePlayersList).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.noPotKilltxt).setVisibility(View.GONE);
            getView().findViewById(R.id.witchKillablePlayersList).setVisibility(View.VISIBLE);
        }
    }

    public static Joueur getPlayerDeadLastNight(int pos) {
        ArrayList<Joueur> tempList = new ArrayList<>();

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).deadLastNight()) {
                tempList.add(Game.allPlayers.get(i));
            }
        }

        return tempList.get(pos);
    }

    public static Joueur getPlayerEnVie(int pos) {
        ArrayList<Joueur> tempList = new ArrayList<>();

        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).isEnVie()) {
                tempList.add(Game.allPlayers.get(i));
            }
        }

        return tempList.get(pos);
    }


}
