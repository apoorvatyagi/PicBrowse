package project.apoorva.picbrowse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

/**
 * Created by Apoorva on 17/11/14.
 */
public class ImageAdapter extends ArrayAdapter<RedditImage> {

    private Context mContext;
    private DisplayImageOptions mOptions;
    private List<RedditImage> mListOfImages;
    private static final String TAG = "ImageAdapter";
    ViewHolder holder;


    public ImageAdapter(Context context, int resource, List<RedditImage> imagesToLoad, DisplayImageOptions options) {
        super(context, resource, imagesToLoad);
        this.mContext = context;
        this.mListOfImages = imagesToLoad;
        this.mOptions = options;
    }

    static class ViewHolder {
        public ImageView ivPicture;
        public ProgressBar progressBar;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.grid_item, null);
            Log.d(TAG, "View inflated");

            holder.ivPicture = (ImageView) v.findViewById(R.id.image);
            holder.progressBar = (ProgressBar) v.findViewById(R.id.progressbar);

            v.setTag(holder);

            Log.d(TAG, "Image view set");
        } else {
            holder = (ViewHolder) v.getTag();
        }


        if (holder.ivPicture != null && holder.progressBar != null) {

            final String imagePath = mListOfImages.get(position).getmThumbnail();

            try {
                ImageLoader.getInstance().displayImage(imagePath, holder.ivPicture, mOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }


                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.progressBar.setVisibility(View.GONE);
                    }


                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.progressBar.setVisibility(View.GONE);
                    }

                }, new ImageLoadingProgressListener() {

                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        holder.progressBar.setProgress(Math.round(100.0f * current / total));
                    }
                });

                holder.ivPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i(TAG, "Pic clicked.");
                        final String ARG_LINK = "arg_link";

                        String fullScreenUrl = mListOfImages.get(position).getmURL();
                        if (fullScreenUrl != null && !fullScreenUrl.equals("")) {
                            //start webview with the link. This can handle both, images or imgur webpages.
                            Intent viewImageIntent = new Intent(mContext, ViewImageActivity.class);
                            viewImageIntent.putExtra(ARG_LINK, fullScreenUrl);
                            mContext.startActivity(viewImageIntent);
                        }

                    }
                });
            }catch(Exception e){
                Log.e(TAG, "Exception loading image: " + e.getMessage());
            }
        }
        return v;
    }

}
