package com.scanlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * Created by Rishika on 2020-02-10.
 */
public class Layout extends LinearLayout {
    ImageView imageView;
    Float x = 0f , y = 0f ;
    private Bitmap bitmap;

    private Bitmap imageBitmap;
    boolean isInit;
    private int centreX;
    private int centreY;

    public Layout(Context context) {
        super(context);
        init();
    }

    public Layout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Layout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }




    private void init(){


    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


//        int w = Math.min(this.getWidth() , this.getHeight());
////        Paint paint = new Paint();
////        paint.setColor(getResources().getColor(R.color.colorAccent));
////        canvas.drawCircle(w/2, w/2 , w/3 ,paint );

        if(bitmap == null)
        {
            bitmap  = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.wal);
        }

        if(bitmap != null)
        {
            Bitmap bitmap1 = getscannerBitmap(bitmap);

            canvas.drawBitmap(bitmap1 , 20 , 20 , null );



            canvas.drawColor(Color.TRANSPARENT);



            if(imageBitmap != null)
            {
                centreX = (canvas.getWidth()  - imageBitmap.getWidth()) /2;
                centreY = (canvas.getHeight() - imageBitmap.getHeight()) /2;
                canvas.drawBitmap(imageBitmap, centreX, centreY, null);
            }

        }

    }






    protected Bitmap getscannerBitmap(Bitmap localBitmap)
    {


        int width = Math.min(localBitmap.getWidth(), localBitmap.getHeight());
        width = width/2;

        Bitmap dstBitmap = Bitmap.createBitmap (
                width,
               width,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(dstBitmap);
        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setColor(this.getResources().getColor(R.color.blue));
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, width, width);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        float left;
        float top;

        if(!isInit)
        {
            left = (width/2 - localBitmap.getWidth())/2;
            top = (width/2 - localBitmap.getHeight())/2;
        }
        else
        {
             left = (width/2 - x);
             top = (width/2 - y);
        }


        canvas.drawBitmap(localBitmap, left, top, paint);
        // Free the native object associated with this bitmap.

         // Return the circular bitmap
        return dstBitmap;
    }


    public void setImageBitmap(Bitmap scaledBitmap, Bitmap imageBitmap)
    {
        this.bitmap = scaledBitmap;

        this.imageBitmap = imageBitmap;

        invalidate();
    }
}

