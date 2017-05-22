package com.rombosaur.engine.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Proceso que renderiza modelos "Renderable".
 * Para usar se debe:
 * 1) Instanciar
 * 2) Llamar al prepareTextures() con TODAS las texturas que se van a usar.
 * 3) Modificar los Renderable's que se van a renderizar dinámicamente con los métodos addToScene()/clearScene()...
 * 4) Llamar al método render() en cada frame
 *
 * Los renderables tienen que tener una animación seteada. Eso se debe hacer posterior a la llamada del método prepareTextures(),
 * de lo contrario se van a logguear errores. Cualquier otra missconfiguration tiene que saltar por el log.
 *
 * Created by rombus on 21/02/17.
 */
public class Renderer implements Disposable {
    private Batch batch;
    private ObjectMap<Integer, AnimatedTexture> textures; // Array de AnimatedTextures, no hay repetidos. Están todas las texturas del nivel acá.
    private ObjectMap<Integer, ObjectMap<String, AnimationData>> animations; // Array de animaciones, no hay repetidos. Están todas las animaciones por cada renderable.id
    private Array<Renderable> renderablesInScene;        // Lo que esté acá se va a renderizar, por lo que alguien tiene que administrar esta lista.

    public Renderer(Batch batch) {
        this.batch = batch;
        textures = new ObjectMap<Integer, AnimatedTexture>();
        animations = new ObjectMap<Integer, ObjectMap<String, AnimationData>>();
        renderablesInScene = new Array<Renderable>();
    }

    /**
     * Recorre el Array de renderablesInScene y crea sus texturas.
     * Se debe llamar luego de cargar todos los renderablesInScene
     *
     * @param renderables todos los renderablesInScene DEL NIVEL actual deben estar en este arreglo.
     *                    No solo los de 1 instant (por eso se recive por parámetro.
     *                    El "renderablesInScene" de esta clase es de 1 solo instant)
     */
    public void prepareTextures(Array<Renderable> renderables){
        for(Renderable rend: renderables){
            if(!textures.containsKey(rend.id)){
                AnimatedTexture at = new AnimatedTexture(rend.texturePath, rend.framesCols, rend.framesRows);
                textures.put(rend.id, at);
            }
        }

        prepareAnimations(renderables);
    }
    public void prepareTextures(Renderable... renderables){
        prepareTextures(new Array<Renderable>(renderables));
    }

    /**
     * Calls the buildAnimation() method for each renderable.
     * This method bust be called after the renderable setId() method.
     * @param renderables
     */
    private void prepareAnimations(Array<Renderable> renderables) {
        for (Renderable renderable : renderables) {
            ObjectMap<String, AnimationData> anims = renderable.buildAnimations(getAnimations(renderable));
            textures.get(renderable.id).buildAnimations(anims);
            animations.put(renderable.id, anims);
        }
    }

    public ObjectMap<String, AnimationData> getAnimations(Renderable renderable){
        ObjectMap<String, AnimationData> anims;

        //TODO animations nunca tiene nada??? cuando creo block, no tiene las animations de hero.
        if (animations.containsKey(renderable.id)) {
            anims = animations.get(renderable.id);
        }
        else {
            anims = new ObjectMap<String, AnimationData>();
        }

        return anims;
    }

    public void clearScene(){
        renderablesInScene.clear();
    }
    public void addToScene(Renderable renderable){
        renderablesInScene.add(renderable);
    }
    public void addToScene(Renderable... renderables){
        this.renderablesInScene.addAll(renderables);
    }
    public void addToScene(Array<Renderable> renderables){
        for(Renderable renderable: renderables) {
            this.renderablesInScene.add(renderable);
        }
    }

    /**
     * That's right, el Renderer te da el objeto Animation a partir de un id String de animación.
     * Esto es pq no tengo una estructura de datos que agrupe Renderable's con Animation's, entonces
     * acá hago un "join" entre mi array de Renderable's y mi HashMap de AnimatedTexture's (que tienen los objetos Animation)
     *
     * @param renderable
     * @param name
     * @return
     */
    public Animation getAnimation(Renderable renderable, String name){
        if(textures.containsKey(renderable.id)){
            return textures.get(renderable.id).getAnimation(name);
        } else {
            Gdx.app.log("WARN", "Renderer - Se intentó obtener la animación \""+name+"\" pero el Renderable \""+renderable.texturePath+"\" no está cargado en la lista del Renderer.\nLlamaste al prepareTextures()???");
        }
        return null;
    }

    /**
     * Get the animations list corresponding to the renderable.id.
     * If you use the same asset for multiple objects, the animations must be set
     * in ONE AnimatedTexture object.
     *
     * @param renderable
     * @return
     */
    public ObjectMap<String, AnimationData> getAnimationsList(Renderable renderable){
        ObjectMap<String, AnimationData> list = new ObjectMap<String, AnimationData>();

        if(textures.containsKey(renderable.id)){
            list = textures.get(renderable.id).getAnimationsList();
        }

        return list;
    }

    /**
     * The update method is called before render()
     */
    public void update(){
    }

    public void render(){
        if(renderablesInScene.size == 0){
            Gdx.app.log("WARN", "Renderer - Pantalla en negro. No hay Renderables cargados en la scene (renderablesInScene)!!!");
            return;
        }

        batch.begin();
        for(Renderable curRenderable: renderablesInScene){
            if(curRenderable.visible) {
                AnimatedTexture at = textures.get(curRenderable.id);
                if(at == null){
                    Gdx.app.log("ERR", "Renderer - No tengo cargada la textura \""+curRenderable.texturePath+"\" en el Renderer.\nLlamaste al prepareTextures()???");
                    continue;
                }

                if(curRenderable.changeAnimation){
                    curRenderable.setAnimation(curRenderable.curAnimationId, this);
                }

                at.draw(batch, curRenderable);
                curRenderable.syncBounds(); // Synchronize the bounds object to the new position

                // We sync the prevX, prevY with x and y
                curRenderable.prevX = curRenderable.x;
                curRenderable.prevY = curRenderable.y;
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        // Llamo a dispose en cada AnimatedTexture registrada
        for(ObjectMap.Entry<Integer, AnimatedTexture> entry : textures.entries()){
            entry.value.dispose();
        }
    }
}
