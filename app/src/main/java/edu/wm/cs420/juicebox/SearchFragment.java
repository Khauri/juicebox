package edu.wm.cs420.juicebox;

import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnAddToQueueListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    private static String TAG = "juicebox-searchfragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private OnAddToQueueListener queueListener;

    private EditText searchBar;
    private TextView searchHintText;
    private ProgressBar progressBar;
    private Handler searchTimer = new Handler();
    private Button searchButton;
    private ListView lv;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search2, container, false);
        //return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        searchBar = getView().findViewById(R.id.search_bar);
        // watchtches for
        searchBar.addTextChangedListener(
            new TextWatcher() {
                private String searchQuery;
                private Runnable delaySearch = new Runnable() {
                    @Override
                    public void run() {
                        if(TextUtils.isEmpty(searchQuery))
                            return;
                        performSearch(searchQuery);
                    }
                };

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    searchQuery = editable.toString();
                    searchTimer.removeCallbacks(delaySearch);
                    searchTimer.postDelayed(delaySearch, 250);
                }
            }
        );
        mRecyclerView = getView().findViewById(R.id.song_results);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SearchFragmentAdapter(SearchFragmentAdapter.ViewType.SEARCH);
        mRecyclerView.setAdapter(mAdapter);
        progressBar = getView().findViewById(R.id.search_progress);
        searchHintText = getView().findViewById(R.id.search_hint_text);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddToQueueListener) {
            queueListener = (OnAddToQueueListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        queueListener = null;
    }

    /**
     *
     */
    public interface OnAddToQueueListener {
        // TODO: Update argument type and name
        void onAddToQueue(SongListItem song);
    }

    public OnAddToQueueListener getListener(){
        return queueListener;
    }

    public void performSearch(String searchQuery){
        // user cleared the string
        if(TextUtils.isEmpty(searchQuery)){
            searchHintText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }
        // 0. Clear the results?
        // 1. Hide search hint
        searchHintText.setVisibility(View.GONE);
        // 2. display spinner
        progressBar.setVisibility(View.VISIBLE);
        // 3. Search for tracks
        SpotifyUtils.getSpotifyService().searchTracks(searchQuery, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                // 4. Remove spinner
                progressBar.setVisibility(View.GONE);
                List<Track> trackList = tracksPager.tracks.items;
                ((SearchFragmentAdapter) mAdapter).setData(trackList);
                mAdapter.notifyDataSetChanged();
                Log.d(TAG, "success: HELLO");
//                       lv.setAdapter(new SearchFragmentAdapter(getContext(), trackList,
//                               SearchFragment.this));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "performSearch/searchTracks/failure: " + error.getMessage());
            }
        });
    }

}
