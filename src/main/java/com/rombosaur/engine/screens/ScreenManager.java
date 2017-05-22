package com.rombosaur.engine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

/**
 * Manages the screen stack.
 *
 * Created by rombus on 02/03/17.
 */
public class ScreenManager {
    private Game game;
    private Stack<Screen> screenStack;
    private Screen curScreen;

    public ScreenManager(Game game){
        this.game = game;
        screenStack = new Stack<Screen>();
    }

    /**
     * Switchs to the specified screen
     *
     * @param screen Screen to switch to
     */
    public void pushScreen(Screen screen) {
        if(curScreen != null) {
            screenStack.push(curScreen);
        }
        curScreen = screen;
        game.switchScreen(screen);
    }

    /**
     * Goes to previous screen if available
     *
     * @return Current Screen. Maybe it's the new one if no errors occurred, or the same one (already showing) if it couldn't switch to the new Screen.
     */
    public Screen popScreen(){
        if(!screenStack.isEmpty()){
            curScreen = screenStack.pop();
            game.switchScreen(curScreen);
        } else {
            Gdx.app.log("DEBUG", "Empty screen stack!!!");
        }

        return curScreen;
    }
}
