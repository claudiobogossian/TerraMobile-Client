package br.inova.mobile.photo;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import br.inova.mobile.exception.ExceptionHandler;

public class BitmapRendererTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int                            data = 0;
        
        private File                           imageFile;
        private ImageView                      imageView;
        
        private int                            width;
        private int                            height;
        
        public BitmapRendererTask(
                                  File imageFile,
                                  ImageView imageView,
                                  int width,
                                  int height) {
                
                // Use a WeakReference to ensure the ImageView can be garbage collected
                this.imageViewReference = new WeakReference<ImageView>(imageView);
                
                this.imageFile = imageFile;
                this.imageView = imageView;
                
                this.width = width;
                this.height = height;
                
                loadBitmap();
        }
        
        public void loadBitmap() {
                if (cancelPotentialWork(imageFile, imageView)) {
                        final AsyncDrawable asyncDrawable = new AsyncDrawable(this);
                        imageView.setImageDrawable(asyncDrawable);
                        this.execute();
                }
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
                return decodeSampledBitmapFromFile();
        }
        
        /**
         * Decodes image and scales it to reduce memory consumption
         * 
         * @param File
         *                the picture file
         * 
         * @author Paulo Luan
         * */
        private Bitmap decodeSampledBitmapFromFile() {
                Bitmap bitmapImage = null;
                
                try {
                        // First decode with inJustDecodeBounds=true to check dimensions
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);
                        
                        // Calculate inSampleSize
                        options.inSampleSize = calculateInSampleSize(options, width, height);
                        
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;
                        bitmapImage = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return bitmapImage;
        }
        
        /**
         * Calculates the sample size based on the resolution of the param.
         * 
         * @param BitmapFactory
         *                .Options the options of the bitmap file
         * @param reqWidth
         *                the width of the output bitmap.
         * @param reqHeigth
         *                the heigth of the output bitmap.
         * @author Paulo Luan
         * */
        private int calculateInSampleSize(
                                          BitmapFactory.Options options,
                                          int reqWidth,
                                          int reqHeight) {
                // Raw height and width of image
                final int height = options.outHeight;
                final int width = options.outWidth;
                int inSampleSize = 1;
                
                if (height > reqHeight || width > reqWidth) {
                        
                        final int halfHeight = height / 2;
                        final int halfWidth = width / 2;
                        
                        // Calculate the largest inSampleSize value that is a power of 2 and
                        // keeps both
                        // height and width larger than the requested height and width.
                        while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                                inSampleSize *= 2;
                        }
                }
                
                return inSampleSize;
        }
        
        private class AsyncDrawable extends BitmapDrawable {
                private final WeakReference<BitmapRendererTask> bitmapWorkerTaskReference;
                
                public AsyncDrawable(BitmapRendererTask bitmapRendererTask) {
                        bitmapWorkerTaskReference = new WeakReference<BitmapRendererTask>(bitmapRendererTask);
                }
                
                public BitmapRendererTask getBitmapWorkerTask() {
                        return bitmapWorkerTaskReference.get();
                }
        }
        
        private boolean cancelPotentialWork(File imageFile, ImageView imageView) {
                final BitmapRendererTask bitmapRendererTask = getBitmapWorkerTask(imageView);
                
                if (bitmapRendererTask != null) {
                        final int bitmapData = bitmapRendererTask.data;
                        // If bitmapData is not yet set or it differs from the new data
                        if (bitmapData == 0) {
                                // Cancel previous task
                                bitmapRendererTask.cancel(true);
                        }
                        else {
                                // The same work is already in progress
                                return false;
                        }
                }
                // No task associated with the ImageView, or an existing task was cancelled
                return true;
        }
        
        private BitmapRendererTask getBitmapWorkerTask(ImageView imageView) {
                if (imageView != null) {
                        final Drawable drawable = imageView.getDrawable();
                        if (drawable instanceof AsyncDrawable) {
                                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                                return asyncDrawable.getBitmapWorkerTask();
                        }
                }
                return null;
        }
        
        /**
         * Clear Bitmap image from memory and calls the Garbage Collector.
         * */
        public static void clearBitmap(Bitmap bm) {
                bm.recycle();
                System.gc();
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
                if (isCancelled()) {
                        bitmap = null;
                }
                
                if (imageViewReference != null && bitmap != null) {
                        final ImageView imageView = imageViewReference.get();
                        final BitmapRendererTask bitmapRendererTask = getBitmapWorkerTask(imageView);
                        
                        if (this == bitmapRendererTask && imageView != null) {
                                imageView.setImageBitmap(bitmap);
                        }
                }
        }
        
}
