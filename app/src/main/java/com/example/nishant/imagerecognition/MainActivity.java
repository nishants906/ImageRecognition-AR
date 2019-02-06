package com.example.nishant.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CustomArFragment arFragment;
    private boolean shoulAddplane = true;
    ImageView imview;



    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 299;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTensorFlowAndLoadModel();
        imview = findViewById(R.id.imview);
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        arFragment.getPlaneDiscoveryController().hide();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        textViewResult = findViewById(R.id.textViewResult);


    }

    public boolean setupAugmentedImageDatabase(Config config, Session session){

        AugmentedImageDatabase augmentedImageDatabase = null;
        Map<String,Bitmap> bitmap = loadimage();

        config.setFocusMode(Config.FocusMode.AUTO);

        if (bitmap ==null){
            return false;
        }

         augmentedImageDatabase = new AugmentedImageDatabase(session);

        for ( Map.Entry<String, Bitmap> entry : bitmap.entrySet()) {

            augmentedImageDatabase.addImage(entry.getKey(), entry.getValue());
        }

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;


    }
    public Map<String,Bitmap> loadimage() {
        Map<String,Bitmap> bm = new HashMap<>();
        try (InputStream is = getAssets().open("bk.png")) {
            bm.put("airplane",BitmapFactory.decodeStream(is));

        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream is = getAssets().open("bk.png")) {
            bm.put("airplane",BitmapFactory.decodeStream(is));

        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream is = getAssets().open("fox.jpg")) {
            bm.put("fox",BitmapFactory.decodeStream(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
     }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onUpdateFrame(FrameTime frameTime)
    {
        int[] a = arFragment.getArSceneView().getArFrame().getCamera().getImageIntrinsics().getImageDimensions();
//        Log.d("image-dimensions", String.valueOf(a[0])+" "+String.valueOf(a[0]) );
        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
//        Log.d("data1234",frame.getUpdatedTrackables(AugmentedImage.class).to String());


//        for (AugmentedImage augmentedImage:augmentedImages){
////            Log.d("datadone",augmentedImage.getName());
//
//
//            if (augmentedImage.getTrackingState() == TrackingState.TRACKING){

                Log.d("occuring","occuring1");
                Image cameraImage = null;
                try {

                    cameraImage = frame.acquireCameraImage();

//The camera image received is in YUV YCbCr Format. Get buffers for each of the planes and use them to create a new bytearray defined by the size of all three buffers combined
                ByteBuffer cameraPlaneY = cameraImage.getPlanes()[0].getBuffer();
                ByteBuffer cameraPlaneU = cameraImage.getPlanes()[1].getBuffer();
                ByteBuffer cameraPlaneV = cameraImage.getPlanes()[2].getBuffer();

//Use the buffers to create a new byteArray that
                byte[] compositeByteArray = new byte[(cameraPlaneY.capacity() + cameraPlaneU.capacity() + cameraPlaneV.capacity())];

                cameraPlaneY.get(compositeByteArray, 0, cameraPlaneY.capacity());
                cameraPlaneU.get(compositeByteArray, cameraPlaneY.capacity(), cameraPlaneU.capacity());
                cameraPlaneV.get(compositeByteArray, cameraPlaneY.capacity() + cameraPlaneU.capacity(), cameraPlaneV.capacity());

                ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(compositeByteArray, ImageFormat.NV21, cameraImage.getWidth(), cameraImage.getHeight(), null);
                yuvImage.compressToJpeg(new Rect(0, 0, cameraImage.getWidth(), cameraImage.getHeight()), 75, baOutputStream);
                byte[] byteForBitmap = baOutputStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.length);

                    imview.setImageBitmap(bitmap);
//                bitmap.setWidth(200);
//                bitmap.setHeight(200);

                cameraImage.close();

                    final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                    textViewResult.setText(results.toString());
                    Log.d("occuring","occuring2");
                } catch (NotYetAvailableException e) {
                    Log.e("erroroccur",e.toString());
                }

//                if (augmentedImage.getName().equals("airplane") && shoulAddplane){
//                    Log.d("datadone",augmentedImage.getName());
//
//                    placeObject(arFragment,augmentedImage.createAnchor(augmentedImage.getCenterPose()),Uri.parse("Airplane.sfb"),augmentedImage.getName());
//
//                }
//                else if (augmentedImage.getName().equals("fox")){
//                    placeObject(arFragment,augmentedImage.createAnchor(augmentedImage.getCenterPose()),Uri.parse("fox.sfb"), augmentedImage.getName());
//
//                }
//            }
//        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void placeObject(ArFragment fragment, Anchor anchor, Uri model, String name){

//        ModelRenderable.builder()
//                .setSource(fragment.getContext(),model)
//                .build()
//                .thenAccept(renderable -> addnodetoscene(fragment, anchor, renderable))
//                .exceptionally(throwable -> {
//                    Log.d("enter1",throwable.getMessage());
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setMessage(throwable.getMessage()).setTitle("Error");
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    return null;
//                });


        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        Node infoCard = new Node();
        infoCard.setParent(anchorNode);
        Quaternion q1 = infoCard.getLocalRotation();
        Quaternion q2 = Quaternion.axisAngle(new Vector3(-70f, 1f, 0.3f), 5f);
        infoCard.setLocalRotation(Quaternion.multiply(q1, q2));


        ViewRenderable.builder()
                .setView(this, R.layout.planet_card_view)
                .build()
                .thenAccept(
                        (renderable) -> {


                            infoCard.setRenderable(renderable);
                            TextView textView = (TextView) renderable.getView();
                            textView.setText("10% off on "+name);
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load plane card view.", throwable);
                        });


    }


    public void add2dnodetoscene(ArFragment fragment, Anchor anchor,Renderable renderable){




    }

    public void addnodetoscene(ArFragment fragment, Anchor anchor,Renderable renderable){

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(renderable);
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(() -> classifier.close());
    }
}
