package com.example.shouhei.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {

  private static final String TAG = "ThumbnailDownloader";
  private static final int MESSAGE_DOWNLOAD = 0;

  private boolean mHasQuit = false;
  private Handler mRequestHandler;
  private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
  private Handler mResponseHnadler;
  private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

  public interface ThumbnailDownloadListener<T> {
    void onThumbnailDownloaded(T target, Bitmap thumbnail);
  }

  public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
    mThumbnailDownloadListener = listener;
  }

  public ThumbnailDownloader(Handler responseHandler) {

    super(TAG);
    mResponseHnadler = responseHandler;
  }

  @Override
  protected void onLooperPrepared() {
    mRequestHandler =
        new Handler() {
          @Override
          public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DOWNLOAD) {
              T target = (T) msg.obj;
              Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
              handleRequest(target);
            }
          }
        };
  }

  @Override
  public boolean quit() {
    mHasQuit = true;
    return super.quit();
  }

  private void handleRequest(final T target) {
    try {
      final String url = mRequestMap.get(target);

      if (url == null) {
        return;
      }

      byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
      final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
      Log.i(TAG, "Bitmap created");

      mResponseHnadler.post(
          new Runnable() {
            @Override
            public void run() {
              if (mRequestMap.get(target) != url || mHasQuit) {
                return;
              }

              mRequestMap.remove(target);
              mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
            }
          });
    } catch (IOException ioe) {
      Log.e(TAG, "Error downloading image", ioe);
    }
  }

  public void queueThumbnail(T target, String url) {
    Log.i(TAG, "Got a URL " + url);

    if (url == null) {
      mRequestMap.put(target, url);
    } else {
      mRequestMap.put(target, url);
      mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
    }
  }

  public void clearQueue() {
    mResponseHnadler.removeMessages(MESSAGE_DOWNLOAD);
    mRequestMap.clear();
  }
}
