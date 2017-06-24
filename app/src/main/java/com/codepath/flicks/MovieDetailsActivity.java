package com.codepath.flicks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.codepath.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.codepath.flicks.MovieListActivity.API_BASE_URL;
import static com.codepath.flicks.MovieListActivity.API_KEY_PARAM;

public class MovieDetailsActivity extends AppCompatActivity {

    // the movie to display
    Movie movie;

    // reference to movie list
    MovieListActivity movieList;

    // the view objects
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvOverview)
    TextView tvOverview;
    @BindView(R.id.rbVoteAverage)
    RatingBar rbVoteAverage;

    ImageView ivTrailer;

    // youtube video id, if exists
    String videoId;

    Context context;

    Integer movieId;

    // instance fields
    AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        client = new AsyncHttpClient();

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());


        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        // get movie id
        movieId = movie.getId();

        // set up ivTrailer
        ivTrailer = (ImageView) findViewById(R.id.ivTrailer);

        // get youtube video Id
        getYoutubeVideo(movieId);

        ivTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // make sure there is a videoID is valid, i.e. actually exists in the view
                if (videoId != null) {

                    // create intent for the new activity
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    // serialize the movie using parceler, use its short name as a key
                    intent.putExtra(Movie.class.getSimpleName(), videoId);
                    // show the activity
                    startActivity(intent);
                }

            }
        });




    }


    // get the youtube video from the API
    public void getYoutubeVideo(Integer movieId) {
        //create the URL
        String url = API_BASE_URL + "/movie/" + movieId + "/videos";
        Log.i("HI", url);

        // set the request parameters
        RequestParams params = new RequestParams();

        params.put(API_KEY_PARAM, getString(R.string.api_key));

        // execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into a movie list
                try {
                    JSONArray results = response.getJSONArray("results");

                    // if there is video, pass the key to the movie details

                    if (results.length() > 0) {
                        JSONObject objectMovie = results.getJSONObject(0);
                        String videoId = objectMovie.getString("key");
                        setVideoId(videoId);

                    } else setVideoId(null);


                } catch (JSONException e) {
                    Log.d("Error youtube", "Failed to parse get youtube video");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("Error youtube", "Failed to get data from now playing endpoint");
            }
        });
    }



    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}


