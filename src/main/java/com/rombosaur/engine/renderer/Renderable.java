package com.rombosaur.engine.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.rombosaur.engine.screens.Game;

/**
 * Model, tiene toda la data necesaria para que se renderize un objeto.
 * Es solo data (getters y setters, capaz complejos, pero eso es _todo).
 *
 * No se puede hacer un setAnimation() teniendo solo un Renderable, se debe
 * agregar el renderable al Renderer y pedirle al Renderer que te de el objeto Animation
 * que neceistás para setear el campo "curAnimation" de esta clase.
 *
 * Los objetos de esta clase son Pooleables, como son lightweight espero poder
 * crearlos at runtime cuando sea necesario. TODO: Veremos...
 *
 * Created by rombus on 21/02/17.
 */
public abstract class Renderable implements Pool.Poolable {
    /**
     * This is the value every Renderable id will have if it's not initialized.
     */
    public static int UNINITIALIZED_ID = -1;

    /**
     * You don't want to change this variable directly! Use setAnimation() instead.
     *
     * "curAnimation" and "looping" are set together in setAnimation() method.
     * Contains the Animation object of the playing animation.
     */
    protected Animation curAnimation;

    /**
     * Tells the renderer to loop (or not) an animation.
     * This value is a copy of "curAnimation.loop", this way I can optimize code later.
     */
    protected boolean looping;

    /**
     * Id of an animation that the renderer will play when "changeAnimation" is true.
     * If you change this variable directly, make sure to set "changeAnimation" to true also, otherwise the animation wont change.
     * Is preferred to use the setAnimation() method.
     */
    protected String curAnimationId;

    /**
     * Set this flag to true when you set an animation "curAnimationId", so the renderer knows that a call to the change animation method is needed.
     */
    protected boolean changeAnimation;

    /**
     * Unique to each texturePath. Used as a primary key for the Texture.
     */
    public int id = UNINITIALIZED_ID;

    public float x = 0;
    public float y = 0;

    // Used for collision detection
    public float prevX = 0, prevY = 0;

    /**
     * Sprites are naturally pointing to the right, so if it's flipped is pointing to the left
     */
    public boolean flipHorizontally = false;
    public boolean flipVertically = false;

    /**
     * The width of the rendered graphic, not the width of the original image (you can generate zoom in/out here).
     */
    public float width;

    /**
     * The height of the rendered graphic, not the height of the original image (you can generate zoom in/out here).
     */
    public float height;

    /**
     * A Rectangle for collision checking.
     * Use the setBounds() method to instantiate the Rectangle,
     * you can create the object manually but be aware that the x and y are modified
     * with the offsetX and offsetY by the "syncBounds()" method.
     */
    public Rectangle bounds;

    /**
     * Offset in X axis of the bounds object
     */
    public float offsetX = 0;

    /**
     * Offset in Y axis of the bounds object
     */
    public float offsetY = 0;


    /**
     * Flag that the renderer uses to show (or not) this renderable.
     */
    public boolean visible;


    /**
     * Counter for determining which animation frame to show.
     * Don't mess up with this!
     */
    public float animElapsedTime;

    /**
     * Path to the image asset to be used in this renderable.
     */
    public String texturePath;

    /**
     * Number of columns in the spriteSheet defined in "texturePath".
     */
    public int framesCols;

    /**
     * Number of rows in the spriteSheet defined in "texturePath".
     */
    public int framesRows;  // columnas y filas del spritesheet


    /**
     * A Vector3 for generating unprojected coordinates for the bounding box
     */
    private Vector3 v3;

    /**
     * The game's Camera for generating unprojected coordinates for the bounding box
     */
    private Camera camera;


    public Renderable(){
        visible = true; // Defaults to visible, because why not?
        camera = Game.game.getCamera();
    }


    /**
     * Obtiene el objeto Animation a partir del name y lo setea como animación activa.
     * Se encarga de setear el flag de looping también.
     * IMPORTANTE: Para que te devuelva la animación, este Renderable debe estar cargado
     * a la lista del Renderer que tiene todas las texturas que va a poder renderizar (propiedad @textures).
     *
     * @param name nombre de la animación
     * @param renderer El renderer guarda la lista de Renderables asociada a sus objetos Animation
     */
    public void setAnimation(String name, Renderer renderer){
        curAnimation = renderer.getAnimation(this, name);
        looping = renderer.getAnimations(this).get(name).loop;
    }

    /**
     * Setea el nombre identificador de animación para que el renderer
     * cambie la animación a la especificada en su próxima pasada.
     *
     * @param name id de animación
     */
    public void setAnimation(String name){
        curAnimationId = name;
        changeAnimation = true;
    }

    /**
     * Se debe llamar al setId luego de haber asignado el campo "texturePath" ya que se usa
     * para calcular el ID. Después de _todo, el id de cada Renderable es su textura...
     * Los ID's y los path's de las texturas son 1 a 1, si la textura ya fue cargada antes
     * te devuelve el mismo ID y no se vuelve a cargar.
     */
    public void setId(){
        if(texturePath == null || texturePath.equals("")){
            throw new RuntimeException("Error de programación. Se intentó setear un id de renderable sin tener antes el texturePath definido.");
        }

        this.id = RenderablesIdGen.getInstance().getId(texturePath);
        Gdx.app.log("DEBUG", "Getting id con texturePath: "+texturePath+" resultado: "+id);
    }

    /**
     * Syncs the bounding box Rectangle with the sprite
     */
    public void syncBounds(){
        v3 = camera.project(new Vector3(x+offsetX, y+offsetY, 0));
        bounds.x = v3.x;
        bounds.y = v3.y;
    }

    /**
     * Creates the bounds object with the width and height specified.
     * The x and y are meaningless here, as are relative to the sprite x and y
     * taking into account the offsetX and offsetY (so it's managed by the syncBounds() method)
     *
     * @param width
     * @param height
     */
    public void setBounds(float width, float height) {
        bounds = new Rectangle(0, 0, width, height);
    }

    /**
     * Don't use the constructor, use the init() method,
     * this way the Renderable can be pooled.
     *
     * @param x screen x coordinate
     * @param y screen y coordinate
     */
    public abstract void init(float x, float y);

    /**
     * This method is called by the Renderer.
     * Here you setup all your animations and you can call setAnimation().
     * @param animationsMap The animations map for this renderable.id (can already exist if you use the same asset for another object)
     * @return you should return animationsMap param for chaining reasons
     */
    public abstract ObjectMap<String, AnimationData> buildAnimations(ObjectMap<String, AnimationData> animationsMap);
}
