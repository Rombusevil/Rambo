package com.rombosaur.engine.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Por cada 1 texture del Renderer hay 1 AnimatedTexture.
 * Si usás la misma textura en más de 1 objeto, van a compartir el array animations!!! por lo que tenés que tener cuidado con los nombres de las animaciones
 * Cada objeto que utilice la textura X se va a renderizar por el objeto AnimatedTexture
 * correspondiente a la textura X.
 *
 * Created by rombus on 28/01/17.
 */
public class AnimatedTexture implements Disposable{
    private final Texture texture;
    private final TextureRegion[] frames;     // Frames del spritesheet
    private float curX, curY, curWidth, curHeight;
    private ObjectMap<String, Animation> animations; // Lista de animaciones disponibles
    private ObjectMap<String, AnimationData> animationsList; // List that holds the animations of this Texture

    public AnimatedTexture(String textureFrames, int framesCol, int framesRow) {
        this.texture =  new Texture(textureFrames);
        frames = getFramesArrayFromSheet(texture, framesCol, framesRow);
    }

    /**
     * For building the animations first the engine needs to prepare all the textures.
     * Is in that moment where the map of animations is created (there's one map for each animated texture)
     *
     * @param animations
     */
    public void buildAnimations(ObjectMap<String, AnimationData> animations){
        this.animationsList = animations;
        this.animations = addAnimations(animations);
    }

    /**
     * Convierte una grilla de frames en un array TextureRegion para pasarselo a la
     * clase de animación de libgdx.
     *
     * @param textureFrames Grilla de frames
     * @param framesCol Cantidad de columnas en el textureFrames
     * @param framesRow Cantidad de filas en el textureFrames
     * @return
     */
    private TextureRegion[] getFramesArrayFromSheet(Texture textureFrames, int framesCol, int framesRow){
        TextureRegion[][] tmp = TextureRegion.split(textureFrames, textureFrames.getWidth() / framesCol, textureFrames.getHeight() / framesRow);
        TextureRegion[] frames = new TextureRegion[framesCol * framesRow];

        int frameIdx= 0;
        for(int i=0; i < framesRow; i++){
            for (int j=0; j < framesCol; j++){
                frames[frameIdx++] = tmp[i][j];
            }
        }

        return frames;
    }

    /**
     * Carga todas las animaciones especificadas en el ObjectMap animations a este objeto.
     * Convierte AnimationData en Animation.
     *
     * @param animations
     * @return
     */
    private ObjectMap<String, Animation> addAnimations(ObjectMap<String, AnimationData> animations){
        ObjectMap<String, Animation> result = new ObjectMap<String, Animation>();

        ObjectMap.Entries<String, AnimationData> it = animations.iterator();
        while(it.hasNext()) {
            ObjectMap.Entry<String, AnimationData> cur = it.next();

            AnimationData curData = cur.value;
            short[] curFrames = curData.frames;
            int cantFrames = curData.frames.length;

            if (cantFrames > frames.length) {
                throw new ArrayIndexOutOfBoundsException("Se pidió agregar una animación que tiene más frames que los disponibles!!!");
            }

            // Convierto la lista de índices de frames a una lista de TextureRegions
            TextureRegion[] curAnimFrames = new TextureRegion[cantFrames];
            for (int i = 0; i < cantFrames; i++) {
                curAnimFrames[i] = frames[curFrames[i]];
            }

            // Creo la animación con los datos generados y la agrego a la lista
            Animation newAnimation = new Animation((float)1/curData.fps, curAnimFrames); // Convierto el fps a spf (seconds per frame). Es importante el casteo a float!!!
            result.put(cur.key, newAnimation);
        }

        return result;
    }

    public Animation getAnimation(String name){
        if(!animations.containsKey(name)){ throw new RuntimeException("Se pidió una animación inexistente!!!"); }
        return animations.get(name);
    }

    /**
     * Método    stateless de draw, va a renderizar el renderable que le pases.
     * Saca toda la info de ahí, no hay nada que se guarde en este objeto.
     *
     */
    public void draw(Batch batch, Renderable renderable) {
        if(renderable.curAnimation != null) {
            renderable.animElapsedTime += Gdx.graphics.getDeltaTime();
            TextureRegion curFrame = renderable.curAnimation.getKeyFrame(renderable.animElapsedTime, renderable.looping);

            curX = renderable.x;
            curY = renderable.y;
            curWidth = renderable.width;
            curHeight = renderable.height;


            if(renderable.flipVertically){
                curY += renderable.height;
                curHeight *= -1;
            }

            if(renderable.flipHorizontally){
                curX += renderable.width;
                curWidth *= -1;
            }

            batch.draw(curFrame, curX, curY, curWidth, curHeight);

        } else {
            Gdx.app.log("WARN", "AnimatedTexture sin animación. No se muestra nada!!!");
        }
    }

    public ObjectMap<String, AnimationData> getAnimationsList(){
        return animationsList;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
