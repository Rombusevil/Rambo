package com.rombosaur.engine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rombosaur.engine.renderer.Renderer;


/**
 * Game base class.
 * Has the basic stuff for a game:
 *   Camera, Viewport, Stage, InputMultiplexer, SpriteBatch and Renderer.
 *
 * It also owns the ScreenManager, which contains the screen stack.
 */
public abstract class Game extends com.badlogic.gdx.Game {
	public static Game game;
	public final int width, height;
    private ScreenManager screenManager;
    private Screen firstScreen;

	protected InputMultiplexer inputMultiplexer;
	protected SpriteBatch batch;
	protected Viewport viewport;
	protected OrthographicCamera camera;
	protected Stage stage;
	protected Renderer renderer;

	/**
	 *
	 * @param width viewport width
	 * @param height viewport height
	 * @param firstScreen first screen to be shown
	 */
	public Game(int width, int height, Screen firstScreen){
		Game.game = this;
		this.width = width;
		this.height = height;
		this.firstScreen = firstScreen;
	}

	public void create () {

		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);

        screenManager = new ScreenManager(this);
        batch = new SpriteBatch();
		renderer = new Renderer(batch);
		camera = new OrthographicCamera(width, height);
		camera.setToOrtho(false);
		viewport = new StretchViewport(width, height, camera);
		stage = new Stage(viewport, batch);
		inputMultiplexer.addProcessor(stage);

		pushScreen(firstScreen);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		stage.getBatch().setProjectionMatrix(camera.combined);
		renderer.update();
		renderer.render();
		super.render();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		viewport.update(width, height);
	}

	/* ScreenManager forwards */
    public void pushScreen(Screen screen){ screenManager.pushScreen(screen); }

    public Screen popScreen(){ return screenManager.popScreen(); }
	/* --------------------- */

    /**
     * This replaces "setScreen(Screen s)" and it's package protected.
     * Ideally only the ScreenManager can use it.
     * @param screen
     */
    /*pkg protected*/ void switchScreen(Screen screen) { super.setScreen(screen); }

    /**
     * Calls to this method are forbidden
     * @param screen
     */
	@Override
	public void setScreen(Screen screen) {
		throw new GdxRuntimeException(new Error("Don't use setScreen()!!! you must use pushScreen(Screen screen) or popScreen()"));
	}

	public Camera getCamera(){
		return camera;
	}

	public Renderer getRenderer(){
		return this.renderer;
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
