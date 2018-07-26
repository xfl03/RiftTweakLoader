package me.xfl03.rift.tweakloader;

import java.io.File;

public class TweakMod {
    String tweakClass;
    File modFile;

    public TweakMod(String tweakClass, File modFile) {
        this.tweakClass = tweakClass;
        this.modFile = modFile;
    }
}
