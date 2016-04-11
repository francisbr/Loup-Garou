package francis.loup_garou.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import francis.loup_garou.Activities.ActivityVoyante;
import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.Roles;
import francis.loup_garou.players.Joueur;


public class FragmentVoyante extends Fragment {
    Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
        return inflater.inflate(R.layout.fragment_voyante, container, false);
    }

    public void updateList() {

        ListView listVote = (ListView) getView().findViewById(R.id.listPlayersToSee);

        Game.listAliveNames.clear();
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).isEnVie() && Game.allPlayers.get(i) != Game.me()) {
                Game.listAliveNames.add(Game.allPlayers.get(i).getName());
            }
        }

        listVote.setAdapter(MainActivity.adapterAlive);
        MainActivity.adapterAlive.notifyDataSetChanged();

        listVote.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                Joueur player = null;
                for (int i = 0; i < Game.allPlayers.size(); i++) {
                    if (Game.listAliveNames.get(position) == Game.allPlayers.get(i).getName()) {
                        player = Game.allPlayers.get(i);
                        Log.d("choose", "" + player);
                    }
                }

                showRoleVoyante(player);

            }
        });
    }

    public void showRoleVoyante(Joueur player) {
        Intent intent = new Intent(context, ActivityVoyante.class);
        intent.putExtra("name", player.getName());
        intent.putExtra("role", "" + player.getRole());
        startActivity(intent);

        MainActivity.fragmentTransaction = MainActivity.fragmentManager.beginTransaction();
        FragmentDayCycle fragmentDayCycle = new FragmentDayCycle();
        MainActivity.fragmentTransaction.replace(android.R.id.content, fragmentDayCycle);
        MainActivity.fragmentTransaction.commit();
        MainActivity.fragmentManager.executePendingTransactions();

        fragmentDayCycle.showNight();
    }
}