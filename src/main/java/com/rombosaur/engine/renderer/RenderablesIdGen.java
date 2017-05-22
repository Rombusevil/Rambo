package com.rombosaur.engine.renderer;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Para acceder más rápido a las texturas de los Renderables en el Renderer,
 * pongo un ID en cada renderable (así no uso el string del texturePath como id)
 * Para no pensar en problemas de hash collision, tengo esta clase que mantiene una lista
 * con todos los texturePath's que se usen asociados a un ID único autoincremental.
 *
 * Created by rombus on 23/02/17.
 */
public class RenderablesIdGen {
    private static RenderablesIdGen instance = null;
    private int idCounter;
    public ObjectMap<String, Integer> textureIds;

    private RenderablesIdGen(){
        idCounter = 0;
        textureIds = new ObjectMap<String, Integer>();
    }

    public static RenderablesIdGen getInstance(){
        if(instance == null){
            instance = new RenderablesIdGen();
        }
        return instance;
    }

    /**
     * Te da un ID único para este texturePath.
     * Si el texturePath ya existe, te da el ID que creó
     * en el momento en que cargó la textura.
     *
     * @param texturePath
     * @return
     */
    public int getId(String texturePath){
        int result;

        if(textureIds.containsKey(texturePath)){
            result = textureIds.get(texturePath);
        }
        else {
            textureIds.put(texturePath, idCounter);
            result = idCounter++;
        }

        return result;
    }
}
