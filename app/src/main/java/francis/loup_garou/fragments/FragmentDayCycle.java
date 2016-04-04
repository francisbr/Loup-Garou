package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentDayCycle extends Fragment {
    ListView listViewAlive;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_day_cycle, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    public void showDay() {
        int k = 0;
        k++;
        Log.d("showDay", "GO");

        updateTextNbLoup();

        //LISTE ALIVE
        listViewAlive = (ListView) getView().findViewById(R.id.listPlayersAlive);

        Game.listAliveNames.clear();
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (Game.allPlayers.get(i).isEnVie()) {
                Game.listAliveNames.add(Game.allPlayers.get(i).getName());
            }
        }

        listViewAlive.setAdapter(MainActivity.adapterAlive);
        MainActivity.adapterAlive.notifyDataSetChanged();

        Game.listDeadNames.clear();
        for (int i = 0; i < Game.allPlayers.size(); i++) {
            if (!Game.allPlayers.get(i).isEnVie()) {
                Game.listDeadNames.add(Game.allPlayers.get(i).getName());
            }
        }
        ListView listViewDead = (ListView) getView().findViewById(R.id.listLastDead);
        listViewDead.setAdapter(MainActivity.adapterDeadNames);
        MainActivity.adapterDeadNames.notifyDataSetChanged();


        //AFFICHE LE LAYOUT
        getView().findViewById(R.id.layoutDay).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutNight).setVisibility(View.GONE);


    }

    public void showNight() {

        getView().findViewById(R.id.layoutNight).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutDay).setVisibility(View.GONE);

    }

    public void enableVote() {

        listViewAlive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                if (Game.voteStarted) {
                    MainActivity.sendVoteDay(position);
                    Game.voteStarted = false;

                }
            }
        });

    }

    public void updateTextNbLoup(){
        //NB LOUP
        TextView tx = (TextView) getView().findViewById(R.id.nbLoupAliveTxtview);
        tx.setText("" + Game.getNbLoup());
        Log.d("nbLoupAlive", "" + Game.getNbLoup());

    }

}
