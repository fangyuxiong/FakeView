package com.xfy.fakeview;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

public class ImageSpan extends DynamicDrawableSpan {
    private Drawable mDrawable;
    private Uri mContentUri;
    private int mResourceId;
    private Context mContext;
    private String mSource;

    /**
     * @deprecated Use {@link #ImageSpan(Context, Bitmap)} instead.
     */
    @Deprecated
    public ImageSpan(Bitmap b) {
        this(null, b, ALIGN_BOTTOM);
    }
    public ImageSpan(int verticalAlignment) {
        super(verticalAlignment);
    }

    /**
     * @deprecated Use {@link #ImageSpan(Context, Bitmap, int) instead.
     */
    @Deprecated
    public ImageSpan(Bitmap b, int verticalAlignment) {
        this(null, b, verticalAlignment);
    }

    public ImageSpan(Context context, Bitmap b) {
        this(context, b, ALIGN_BOTTOM);
    }

    /**
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     */
    public ImageSpan(Context context, Bitmap b, int verticalAlignment) {
        super(verticalAlignment);
        mContext = context;
        mDrawable = context != null
                ? new BitmapDrawable(context.getResources(), b)
                : new BitmapDrawable(b);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0); 
    }

    public ImageSpan(Drawable d) {
        this(d, ALIGN_BOTTOM);
    }

    /**
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     */
    public ImageSpan(Drawable d, int verticalAlignment) {
        super(verticalAlignment);
        mDrawable = d;
    }

    public ImageSpan(Drawable d, String source) {
        this(d, source, ALIGN_BOTTOM);
    }

    /**
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     */
    public ImageSpan(Drawable d, String source, int verticalAlignment) {
        super(verticalAlignment);
        mDrawable = d;
        mSource = source;
    }

    public ImageSpan(Context context, Uri uri) {
        this(context, uri, ALIGN_BOTTOM);
    }

    /**
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     */
    public ImageSpan(Context context, Uri uri, int verticalAlignment) {
        super(verticalAlignment);
        mContext = context;
        mContentUri = uri;
        mSource = uri.toString();
    }

    public ImageSpan(Context context, int resourceId, int size) {
        super(ALIGN_BOTTOM);
        mContext = context;
        mResourceId = resourceId;
        this.size = size;
    }

    private int size;

    @Override
    protected Drawable getCachedDrawable() {
    	try {
    		return super.getCachedDrawable();
    	 } catch (OutOfMemoryError e) {
//             Log.e("sms", "Failed to loaded content " + mContentUri, e);
             // 内存溢出了，返回一个空的drawable
             return null;
         }
    }
    
    @Override
    public Drawable getDrawable() {
        Drawable drawable = null;
        
        if (mDrawable != null) {
            drawable = mDrawable;
        } else  if (mContentUri != null) {
            Bitmap bitmap = null;
            try {
                InputStream is = mContext.getContentResolver().openInputStream(
                        mContentUri);
                bitmap = BitmapFactory.decodeStream(is);
                drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                drawable.setBounds(0, 0, size, size);
                is.close();
            } catch (Throwable e) {
                Log.e("sms", "Failed to loaded content " + mContentUri, e);
            }
        } else {
            try {
                drawable = mContext.getResources().getDrawable(mResourceId);
                drawable.setBounds(0, 0, size, size);
            } catch (Throwable e) {
                Log.e("sms", "Unable to find resource: " + mResourceId);
            }                
        }

        return drawable;
    }

    /**
     * Returns the source string that was saved during construction.
     */
    public String getSource() {
        return mSource;
    }

}