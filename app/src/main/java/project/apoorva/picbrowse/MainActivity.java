package project.apoorva.picbrowse;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private Context mContext;
    private EditText mETSearch;
    private Button mButtonSearch;
    private GridView mGridView;
    private String mQueryTerm;

    private DisplayImageOptions mOptions;


    private String mPreviousPostKind;
    private String mPreviousPostID;
    private List<RedditImage> mListOfRedditImages;
    private ImageAdapter mImageAdapter;

    private LoadImages mLoaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mListOfRedditImages = new ArrayList<RedditImage>();

        initLayout();
        initImageLoader();

        mImageAdapter = new ImageAdapter(mContext, R.layout.grid_item, mListOfRedditImages, mOptions);
        mGridView.setAdapter(mImageAdapter);

    }


    /**
     * ************
     * INIT METHODS
     * ************
     */


    private void initLayout() {
        Log.i(TAG, "initLayout() hit");

        mETSearch = (EditText) findViewById(R.id.et_search);
        mButtonSearch = (Button) findViewById(R.id.button_search);

        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "ButtonSearch clicked.");

                mQueryTerm = mETSearch.getText().toString();
                if (mQueryTerm.equals("")) {
                    return;
                }
                //After bad post with id 2lype6
                String searchUrl = "http://www.reddit.com/r/pics.json?q=" + mQueryTerm + "&limit=2&after=t3_2lype6";

                mLoaderTask = new LoadImages();
                mLoaderTask.execute(searchUrl);
            }
        });

        mGridView = (GridView) findViewById(R.id.gridview_pics);

        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                if (mLoaderTask != null && mLoaderTask.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.i(TAG, "Still loading.. dont do anything.");
                } else {
                    String url = "http://www.reddit.com/r/pics.json?q=" + mQueryTerm + "&limit=2&after=" + mPreviousPostKind + "_" + mPreviousPostID;
                    mLoaderTask = new LoadImages();
                    mLoaderTask.execute(url);
                }

            }

        });

    }


    private void initImageLoader() {
        Log.i(TAG, "initImageLoader() hit");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();

        ImageLoader.getInstance().init(config);

        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

    }


    /**
     * ************************
     * ASYNCTASK TO LOAD IMAGES
     * ************************
     */


    private class LoadImages extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPostExecute(Integer result) {
            Log.i(TAG, "onPostExecute() hit.");

            if (result == 1) {
                Log.i(TAG, "List of reddit images retrieved.. Refreshing adapter.");
                mImageAdapter.notifyDataSetChanged();

            } else if (result == 0) {
                Log.i(TAG, "Some error retrieving.");
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.i(TAG, "LoadImagesTask hit.");
            String searchUrl = params[0];
            int countOfImagesRetrieved = 0;

            //Some responses may be skipped if they don't contain an image. We always want 20 pictures returned.
            //Keep searching until the number is reached.
            while (countOfImagesRetrieved < 20) {

                Log.i(TAG, "Search URL: " + searchUrl);

                JSONObject jsonResponse = null;
                String jsonResponseInString = "";
                BufferedReader reader = null;
                URL redditUrl = null;
                HttpURLConnection httpConnection = null;

                try {

                    //Set up HTTP connection.
                    redditUrl = new URL(searchUrl);
                    httpConnection = (HttpURLConnection) redditUrl.openConnection();
                    httpConnection.setConnectTimeout(10000);
                    httpConnection.setReadTimeout(30000);

                } catch (Exception e) {
                    Log.e(TAG, "Exception opening connection: " + e.getMessage());

                }


                try {

                    if (httpConnection.getResponseCode() == 200 || httpConnection.getResponseCode() == 201) {
                        reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }

                        jsonResponseInString = sb.toString();
                        Log.i(TAG, jsonResponseInString);
                    }
                } catch (EOFException e) {
                    Log.e(TAG, "Exception EOF " + e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "Exception IO " + e.getMessage());
                } catch (Exception e) {
                    //For any other unknown exception.
                    Log.e(TAG, "Exception " + e.getClass());

                } finally {
                    try {
                        reader.close();
                    } catch (Exception e) {

                    }
                }

                try {

                    jsonResponse = new JSONObject(jsonResponseInString);

                    //Structure of Reddit json response:
                    /*      - kind
                            - data (--> jsonSuper)
                            - - modhash
                            - - children (--> jsonMainNode)
                                - - kind
                                - - data
                                - - - domain
                                      ...
                                - - - thumbnail
                                - - - id
                                - - - url
                     */

                    JSONObject jsonSuper = jsonResponse.getJSONObject("data");

                    JSONArray jsonMainNode = jsonSuper.getJSONArray("children");

                    int lengthJsonArr = jsonMainNode.length();

                    for (int i = 0; i < lengthJsonArr; i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                        //Fetch node data from response
                        String kind = jsonChildNode.optString("kind");

                        JSONObject jsonDataChildNode = jsonChildNode.optJSONObject("data");

                        String post_id = jsonDataChildNode.optString("id");
                        String thumbnail_link = jsonDataChildNode.optString("thumbnail");
                        String url_link = jsonDataChildNode.optString("url");

                        //Save these values to have a slice for next search
                        mPreviousPostID = post_id;
                        mPreviousPostKind = kind;

                        //Create RedditImage object, and set values.
                        RedditImage singleImage = new RedditImage();

                        singleImage.setmID(post_id);
                        singleImage.setmKind(kind);
                        singleImage.setmThumbnail(thumbnail_link);
                        singleImage.setmURL(url_link);

                        Log.i(TAG, "kind: " + kind);
                        Log.i(TAG, "post_id: " + post_id);
                        Log.i(TAG, "thumbnail_link: " + thumbnail_link);
                        Log.i(TAG, "url_link: " + url_link);

                        //If thumbnail link isn't a URL, skip it.
                        if (thumbnail_link.equals("self")) {
                            Log.i(TAG, "Skipped");
                        } else {
                            //Add it to the list of ALL images returned
                            mListOfRedditImages.add(singleImage);
                            countOfImagesRetrieved++;
                        }

                    }

                    //URL for next search, only two at a time. Perform slice at the last data returned.
                    searchUrl = "http://www.reddit.com/r/pics.json?q=" + mQueryTerm + "&limit=2&after=" + mPreviousPostKind + "_" + mPreviousPostID;
                } catch (Exception e) {
                    Log.e(TAG, "Exception reading JSON: " + e.getMessage());
                    return 0;
                }

            }
            return 1;
        }
    }
}
