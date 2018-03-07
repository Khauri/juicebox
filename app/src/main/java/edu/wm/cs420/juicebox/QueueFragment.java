package edu.wm.cs420.juicebox;

import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.wm.cs420.juicebox.database.models.JuiceboxPlaylist;
import edu.wm.cs420.juicebox.database.models.JuiceboxTrack;
import edu.wm.cs420.juicebox.user.UserUtils;
import kaaes.spotify.webapi.android.models.Playlist;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QueueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QueueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QueueFragment extends Fragment implements AdapterView.OnItemClickListener,
        UserUtils.PlaylistUpdateListener {
    private static String TAG = "juicebox-QueueFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private List<SongListItem> songList;

    private ListView lv;
    // For displaying the queue
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private QueueFragmentAdapter adapter;
    private FrameLayout hostPartyButton;
    // The two screens we have to manage
    // Yes this is a stupid way to do this
    // but we're running out of time
    private View start_screen;
    private View queue_screen;
    private View playback_controls;

    public QueueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QueueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QueueFragment newInstance(String param1, String param2) {
        Log.d("newInstance", "NewInstance has been called!");
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // listen for changes in the playlist
        UserUtils.addUpdateListener("playlist", this);
//        songList = new ArrayList();
//        adapter = new QueueFragmentAdapter(getActivity(), songList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("onCreateView", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
//        adapter = new QueueFragmentAdapter(getActivity(), songList);
//        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hostPartyButton = getView().findViewById(R.id.host_party_btn);
        hostPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewPartyActivity.class);
                startActivity(intent);
            }
        });
        start_screen = getView().findViewById(R.id.start_screen);
        queue_screen = getView().findViewById(R.id.queue_screen);
        playback_controls = getView().findViewById(R.id.playback_controls);
        checkIfParty();

        mRecyclerView = getView().findViewById(R.id.song_results);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SearchFragmentAdapter(SearchFragmentAdapter.ViewType.BIG_QUEUE);
        mRecyclerView.setAdapter(mAdapter);
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRecyclerView);
        UserUtils.addUpdateListener("playlist", this);
    }

    private void checkIfParty() {
        if(UserUtils.isInParty()){
            queue_screen.setVisibility(View.VISIBLE);
            start_screen.setVisibility(View.GONE);
            if(UserUtils.isHost()){
                // Add playback controls
                playback_controls.setVisibility(View.VISIBLE);
            }
        }else{
            queue_screen.setVisibility(View.GONE);
            start_screen.setVisibility(View.VISIBLE);
            playback_controls.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onUpdate(JuiceboxPlaylist playlist) {
        ((SearchFragmentAdapter) mAdapter).setData(playlist.upcoming_tracks);
        mAdapter.notifyDataSetChanged();
        // This definitely shouldn't go here but whatever
        checkIfParty();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//        SongListItem item = this.songList.get(position);
//        Toast.makeText(getActivity(), item.getName() + " Clicked!", Toast.LENGTH_SHORT).show();
    }

    //following two methods help us save the state of the fragment
    @Override
    public void onSaveInstanceState(final Bundle outstate){
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    public List<SongListItem> getSongList(){
        return songList;
    }

    public QueueFragmentAdapter getAdapter(){
        return adapter;
    }

    public void updateQueue(SongListItem song){
    }
}
