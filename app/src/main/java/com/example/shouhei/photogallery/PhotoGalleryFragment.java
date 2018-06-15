package com.example.shouhei.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

  private static final String TAG = "PhotoGalleryFragment";
  private RecyclerView mPhotoRecyclerView;
  private List<GalleryItem> mItems = new ArrayList<>();
  private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

  public static PhotoGalleryFragment newInstance() {

    return new PhotoGalleryFragment();
  }

  private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
    @Override
    protected List<GalleryItem> doInBackground(Void... voids) {
      //      try {
      //        String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
      //        Log.i(TAG, "Fetched contents of URL: " + result);
      //      } catch (IOException ioe) {
      //        Log.e(TAG, "Failed to fetch URL: ", ioe);
      //      }
      return new FlickrFetchr().fetchItems();
    }

    @Override
    protected void onPostExecute(List<GalleryItem> items) {
      mItems = items;
      setupAdapter();
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    Log.d(TAG, "onCreate() called");
    new FetchItemsTask().execute();

    Handler responseHandler = new Handler();
    mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
    mThumbnailDownloader.setThumbnailDownloadListener(
        new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
          @Override
          public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            photoHolder.bindDrawable(drawable);
          }
        });
    mThumbnailDownloader.start();
    mThumbnailDownloader.getLooper();
    Log.i(TAG, "Background thread started");
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

    mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
    mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

    setupAdapter();

    return v;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mThumbnailDownloader.clearQueue();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mThumbnailDownloader.quit();
    Log.i(TAG, "Background thread destroyed");
  }

  private void setupAdapter() {
    if (isAdded()) {
      mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }
  }

  private class PhotoHolder extends RecyclerView.ViewHolder {
    //    private TextView mTitleTextView;
    private ImageView mItemImageView;

    public PhotoHolder(View itemView) {
      super(itemView);

      //      mTitleTextView = (TextView) itemView;
      mItemImageView = itemView.findViewById(R.id.item_image_view);
    }

    //  public void bindGalleryItem(GalleryItem item) {
    //      mTitleTextView.setText(item.toString());
    //    }
    //  }

    public void bindDrawable(Drawable drawable) {
      mItemImageView.setImageDrawable(drawable);
    }
  }

  private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
    private List<GalleryItem> mGalleryItems;

    public PhotoAdapter(List<GalleryItem> galleryItems) {
      mGalleryItems = galleryItems;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
      //      TextView textView = new TextView(getActivity());
      //      return new PhotoHolder(textView);
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      View view = inflater.inflate(R.layout.list_item_gallery, viewGroup, false);
      return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder photoholder, int position) {
      GalleryItem galleryItem = mGalleryItems.get(position);
      //      photoholder.bindGalleryItem(galleryItem);
      Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
      photoholder.bindDrawable(placeholder);
      mThumbnailDownloader.queueThumbnail(photoholder, galleryItem.getUrl());
    }

    @Override
    public int getItemCount() {

      return mGalleryItems.size();
    }
  }
}
