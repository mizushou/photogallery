package com.example.shouhei.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

  private static final String TAG = "PhotoGalleryFragment";
  private RecyclerView mPhotoRecyclerView;
  private List<GalleryItem> mItems = new ArrayList<>();

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

  private void setupAdapter() {
    if (isAdded()) {
      mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }
  }

  private class PhotoHolder extends RecyclerView.ViewHolder {
    private TextView mTitleTextView;

    public PhotoHolder(View itemView) {
      super(itemView);

      mTitleTextView = (TextView) itemView;
    }

    public void bindGalleryItem(GalleryItem item) {
      mTitleTextView.setText(item.toString());
    }
  }

  private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
    private List<GalleryItem> mGalleryItems;

    public PhotoAdapter(List<GalleryItem> galleryItems) {
      mGalleryItems = galleryItems;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      TextView textView = new TextView(getActivity());
      return new PhotoHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
      GalleryItem galleryItem = mGalleryItems.get(position);
      holder.bindGalleryItem(galleryItem);
    }

    @Override
    public int getItemCount() {
      return mGalleryItems.size();
    }
  }
}
