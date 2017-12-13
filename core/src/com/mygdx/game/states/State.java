package com.mygdx.game.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;

import java.net.Socket;

/**
 * Created by Pawel on 01.12.2017.
 */

public abstract class State {
    protected Stage stage;
    protected OrthographicCamera camera;
    protected Vector3 pointer;
    protected GameStateManager gameStateManager;
    protected FitViewport viewport;
    protected SpriteBatch batch;
    protected Socket sock;

    public State(GameStateManager gsm, SpriteBatch sb, Socket sock){
        this.gameStateManager=gsm;
        this.sock=sock;
        this.batch=sb;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyGdxGame.WIDTH, MyGdxGame.HEIGHT);
        viewport = new FitViewport(MyGdxGame.WIDTH,MyGdxGame.HEIGHT,camera);
        stage = new Stage(viewport,batch);
        pointer = new Vector3();
    }

    public abstract void handleInput();
    public abstract void update(float deltaTime);
    public abstract void render();
    public abstract void dispose();
}
