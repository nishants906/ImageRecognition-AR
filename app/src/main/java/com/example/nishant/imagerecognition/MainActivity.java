package com.example.nishant.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private CustomArFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getPlaneDiscoveryController().hide();


    }

    public boolean setupAugmentedImageDatabase(Config config, Session session){

        AugmentedImageDatabase augmentedImageDatabase = null;
        Bitmap bitmap = loadimage();

        if (bitmap ==null){
            return false;

        }

        AugmentedImageDatabase augmentedImageDatabase1 = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("airplane",bitmap);

        config.setAugmentedImageDatabase(augmentedImageDatabase1);
        return true;


    }
    public Bitmap loadimage() {
        try (InputStream is = getAssets().open("airplane.jpg")) {
            return BitmapFactory.decodeStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void placeObject(ArFragment fragment, Anchor anchor, Uri model){

        ModelRenderable.builder()
                .setSource(fragment.getContext(),model)
                .build()
                .thenAccept(renderable -> addnodetoscene(fragment, anchor, renderable))
                .exceptionally(throwable -> {
                    Log.d("enter1",throwable.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).setTitle("Error");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                });


    }

    public void addnodetoscene(ArFragment fragment, Anchor anchor,Renderable renderable){

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        node.select();


    }
}
