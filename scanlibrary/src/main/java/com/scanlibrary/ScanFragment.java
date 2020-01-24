package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


import in.cashify.circleImageView.CircleImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ScanFragment extends Fragment {

    private Button scanButton;
    private ImageView sourceImageView;
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private View view;
    private ProgressDialogFragment progressDialogFragment;
    private IScanner scanner;
    private Bitmap original;
    private CircleImageView img_move;
    Bitmap scaledBitmap;

    ImageView zoomImage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scan_fragment_layout, null);
        init();
        return view;
    }

    public ScanFragment() {

    }

    private void init() {
        sourceImageView = (ImageView) view.findViewById(R.id.sourceImageView);
        scanButton = (Button) view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        sourceFrame = (FrameLayout) view.findViewById(R.id.sourceFrame);
        polygonView = (PolygonView) view.findViewById(R.id.polygonView);
        img_move = view.findViewById(R.id.img_move);
        zoomImage = view.findViewById(R.id.zoomImage);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                if(getActivity().getIntent().getBooleanExtra(ScanConstants.IS_GALLERY , false)){
             original = getGalleryBitmap();
                }

                else {
                    original = getBitmap();
                }



                if (original != null) {


                   setBitmap(original);
                }
            }
        });

        polygonView.setListener(new PolygonView.TouchPointListener() {
            @Override
            public void touchPoints(float x, float y) {
                getCroppedBitmap(original, x, y);
            }
        });
    }


    private void getCroppedBitmap(Bitmap original, float x, float y) {
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float touchX = (x) * xRatio;
        float touchY = (y) * yRatio;

        int startX = (int) (touchX - img_move.getWidth() / 2);
        if (startX < 0) startX = 0;
        if (startX > original.getWidth()) startX = original.getWidth();


        int startY = (int) (touchY - img_move.getHeight() / 2);
        if (startY < 0) startY = 0;
        if (startY > original.getHeight()) startY = original.getHeight();
        Log.e("touch", "x  : " + touchX + "   y:  " + touchY);

        int width = Math.min(img_move.getWidth(), original.getWidth() - startX);
        int height = Math.min(img_move.getHeight(), original.getHeight() - startY);

//        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        Bitmap _bitmap = Bitmap.createBitmap(original, startX, startY, width, height);
        img_move.setImageBitmap(_bitmap);
    }


    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            Bitmap bitmap = Utils.getBitmap(getActivity(), uri);
//            getActivity().getContentResolver().delete(uri, null, null);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Bitmap getGalleryBitmap() {
        try {
           return MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), getUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SELECTED_BITMAP);
        return uri;
    }

    private void setBitmap(Bitmap original) {
         scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        sourceImageView.setImageBitmap(scaledBitmap);


        img_move.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);

    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        float[] points = ((ScanActivity) getActivity()).getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private class ScanButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Map<Integer, PointF> points = polygonView.getPoints();
            if (isScanPointsValid(points)) {
                new ScanAsyncTask(points).execute();
            } else {
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

//    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
//        int width = original.getWidth();
//        int height = original.getHeight();
//        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
//        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();
//
//        float x1 = (points.get(0).x) * xRatio;
//        float x2 = (points.get(1).x) * xRatio;
//        float x3 = (points.get(2).x) * xRatio;
//        float x4 = (points.get(3).x) * xRatio;
//        float y1 = (points.get(0).y) * yRatio;
//        float y2 = (points.get(1).y) * yRatio;
//        float y3 = (points.get(2).y) * yRatio;
//        float y4 = (points.get(3).y) * yRatio;
//        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
//        Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
//        return _bitmap;
//    }


    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        int width = original.getWidth();
        int height = original.getHeight();
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;
        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
        return _bitmap;
    }




//    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {
//
//        private Map<Integer, PointF> points;
//
//        public ScanAsyncTask(Map<Integer, PointF> points) {
//            this.points = points;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            showProgressDialog(getString(R.string.scanning));
//        }
//
//        @Override
//        protected Bitmap doInBackground(Void... params) {
//            Bitmap bitmap = getScannedBitmap(original, points);
////            Uri uri = Utils.getUri(getActivity(), bitmap);
////            scanner.onScanFinish(uri);
//            return bitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//
////            bitmap.recycle();
////            dismissDialog();
//        }
//    }




    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.scanning));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
           Bitmap bitmap = getScannedBitmap(original, points);

//           Bitmap bitmap = getScannedBitmap(scaledBitmap, points);
            Uri uri = Utils.getUri(getActivity(), bitmap);
            scanner.onScanFinish(uri);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//           zoom(points , bitmap);
//            zoomImage.setImageBitmap(bitmap);
//            pointOfImageView(bitmap , points);

           bitmap.recycle();
            dismissDialog();
        }
    }


    void bitmapImageViewDist(){

    }


    void pointOfImageView(Bitmap bitmap , Map<Integer, PointF> points){
        int leftMargin , rightMargin , topMargin , bottomMargin;
        int totalDiffHeight = sourceImageView.getHeight() - scaledBitmap.getHeight();
        int totalDiffWidth = sourceImageView.getWidth() - scaledBitmap.getWidth();
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.setMargins(totalDiffWidth/2, totalDiffHeight/2 , totalDiffWidth/2,totalDiffHeight/2);
//        zoomImage.setLayoutParams(layoutParams);
        int partition = 1;

        leftMargin = (int)(Math.min(points.get(0).x , points.get(2).x))/partition;
        leftMargin = leftMargin+totalDiffWidth/2;
        topMargin = (int)Math.min(points.get(0).y , points.get(1).y)/partition;
        topMargin = totalDiffHeight/2 + topMargin;
        int a1 = (int)(sourceImageView.getWidth() - points.get(1).x);
        int b1 = (int)(sourceImageView.getWidth() - points.get(3).x);

        rightMargin = (int)Math.min(a1, b1)/partition;
        rightMargin = rightMargin - (totalDiffWidth/2);
       int a2 = (int)(sourceImageView.getHeight() - points.get(2).y);
        int b2= (int)(sourceImageView.getHeight() - points.get(3).y);


        bottomMargin = (int)Math.min(a2, b2)/partition;
        bottomMargin = bottomMargin - (totalDiffHeight/2);

//       polygonView.setVisibility(View.GONE);
//       Log.e("left" , leftMargin+"   "+rightMargin+"  "+topMargin+"   "+bottomMargin);
//
////      do {
////
////          if(leftMargin<=5){
////              leftMargin  = 5;
////          }
////
////          if(topMargin<=5){
////              topMargin = 5;
////          }
////
////          if(rightMargin<=5){
////              rightMargin  = 5;
////          }
////          if(bottomMargin<=5){
////              bottomMargin = 5;
////          }
          FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT);
//          layoutParams.setMargins(leftMargin , topMargin , rightMargin,bottomMargin);

        layoutParams.setMargins(leftMargin, topMargin , rightMargin,bottomMargin);
          zoomImage.setLayoutParams(layoutParams);
//        Log.e("right" , layoutParams.leftMargin+"   "+layoutParams.rightMargin+"  "+layoutParams.topMargin+"   "+layoutParams.bottomMargin);
//
//        Log.e("right" , sourceImageView.getHeight() +"  "+ zoomImage.getHeight());
         zoomImage.setImageBitmap(bitmap);
////        zoomImage.setBackgroundColor(getResources().getColor(R.color.orange));
////         polygonView.setVisibility(View.GONE);
//         zoomImage.setVisibility(View.VISIBLE);
//          partition = 2;
//
//          leftMargin = leftMargin/2;
//          topMargin = topMargin/2;
//
//          rightMargin = rightMargin/2;
//
//          bottomMargin = bottomMargin/2;

//
//      }
//        while(leftMargin!=5  &&rightMargin!=5&& bottomMargin!=5&&  topMargin!=5);
//



    }


    void zoom(Map<Integer, PointF> points , Bitmap bitmap){
//        Log.e("" , sourceImageView.)

        int initialWidhth = bitmap.getWidth();
        int initialHeight = bitmap.getHeight();
//        bitmap.reconfigure();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT , FrameLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins();
        layoutParams.gravity = Gravity.CENTER;
//        zoomImage.setLayoutParams(layoutParams);
        zoomImage.setImageBitmap(bitmap);
//        zoomImage.marg
//        zoomImage.getX()
//        while(true) {
//            int w = 0 , h = 0;
//            for (int i = 0; i < points.size(); i++) {
//                if(i == 0){
//               w =     getNextDest((int)points.get(i).x , 0);
//                h =     getNextDest((int)points.get(i).y , 0);
//
//
//                }
//
//                else if(i == 1){
//                   w =  getNextDest((int)points.get(i).x , sourceImageView.getWidth());
//                   h = getNextDest((int)points.get(i).y , 0);
//                }
//
//                else if( i == 2){
//                   w =  getNextDest((int)points.get(i).x , 0);
//                   h =  getNextDest((int)points.get(i).y , sourceImageView.getHeight());
//                }
//
//                else if(i == 3){
//                   w =  getNextDest((int)points.get(i).x , sourceImageView.getWidth());
//                   h =  getNextDest((int)points.get(i).y , sourceImageView.getHeight());
//                }
//
//                points.put(i , new PointF(w , h));
//                Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(bitmap, points.get(0).x, points.get(0).y, points.get(1).x, points.get(1).y, points.get(2).x, points.get(2).y, points.get(3).x, points.get(3).y);
//                bitmap  = _bitmap;
//                sourceImageView.setImageBitmap(bitmap);
//
//                Log.e(i + "   size", points.get(i).x + "    " + points.get(i).y);
//
//            }
//        }


    }

//    int getNextDest(int source, int dest){
//        int mean = (source+dest)/2 ;
//        if(Math.abs(mean-dest) < 50){
//            mean = dest ;
//        }
//        return mean ;
//    }


//    int getNextDest(ImageView zoomImage, ImageView sourceImageView){
//       int left = zoomImage.getLeft() - sourceImageView.getLeft();
//       int right = zoomImage.getRight() - sourceImageView.getRight();
//       int top = zoomImage.getTop() - sourceImageView.getTop();
//       int bottom = zoomImage.getBottom() - sourceImageView.getBottom();
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
//        layoutParams.gravity = Gravity.CENTER;
//        zoomImage.setLayoutParams(layoutParams);
//    }
//

    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected void dismissDialog() {
        if(progressDialogFragment!=null)
        progressDialogFragment.dismissAllowingStateLoss();
    }

}