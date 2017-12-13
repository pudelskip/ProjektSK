package com.mygdx.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Stack;

/**
 * Created by Pawel on 01.12.2017.
 */

public class GameStateManager {

    private Stack<State> states;

    public GameStateManager() {
        this.states = new Stack<State>();
    }

    public void push(State state){
        states.push(state);
    }

    public void pop(State state){
        states.pop();
    }

    public void set(State state){
        states.pop();
        states.push(state);
    }

    public void update(float deltaTime){
            states.peek().update(deltaTime);
    }

    public void render(){
        states.peek().render();
    }
}
