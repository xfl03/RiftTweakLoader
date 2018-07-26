package me.xfl03.rift.tweakloader;

import org.dimdev.riftloader.listener.InitializationListener;

public class InitListener implements InitializationListener {
    @Override
    public void onInitialization() {
        TweakLoader loader = new TweakLoader();
        loader.load();
    }
}
