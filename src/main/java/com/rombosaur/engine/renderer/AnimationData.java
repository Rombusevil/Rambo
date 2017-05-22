package com.rombosaur.engine.renderer;

/**
 * Created by rombus on 21/02/17.
 */
public class AnimationData {
    public short[] frames;
    public byte fps;
    public boolean loop;

    public AnimationData() {}

    public AnimationData(short[] frames, byte fps, boolean loop) {
        this.frames = frames;
        this.fps = fps;
        this.loop = loop;
    }
}
