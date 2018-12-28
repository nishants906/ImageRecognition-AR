package com.example.nishant.imagerecognition;

import android.util.Log;

import com.google.ar.core.Config;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.core.Session;


public class CustomArFragment extends ArFragment {

    @Override
    protected Config getSessionConfiguration(Session session){

        getPlaneDiscoveryController().setInstructionView(null); // hiding hand gesture


        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        this.getArSceneView().setupSession(session);


        if (((MainActivity)getActivity()).setupAugmentedImageDatabase(config,session)){
            Log.d("setupdb","success");
        }
        else{
            Log.d("setupdb","fail");
        }

        return config;
    }


}
