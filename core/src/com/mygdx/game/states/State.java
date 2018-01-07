package com.mygdx.game.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Player;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

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
    protected SocketChannel sock;
    protected Selector sel;                           // selektor – opakowuje mechanizm typu 'select'
    protected SelectionKey sockKey;
    protected ByteBuffer bb;
    public ArrayList<PlayerEntry> players;
    protected String myFd;

    public class PlayerEntry{
        public String name;
        public boolean ready;
        Player player;

        public PlayerEntry(String name, boolean ready) {
            this.name = name;
            this.ready = ready;
            player= new Player();
        }
        public PlayerEntry(String name, boolean ready, Player p) {
            this.name = name;
            this.ready = ready;
            this.player = p;
        }


    }


    public State(GameStateManager gsm, SpriteBatch sb, SocketChannel sock){
        players= new ArrayList<PlayerEntry>();
        this.gameStateManager=gsm;
        this.sock=sock;
        this.batch=sb;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyGdxGame.WIDTH, MyGdxGame.HEIGHT);
        viewport = new FitViewport(MyGdxGame.WIDTH,MyGdxGame.HEIGHT,camera);
        stage = new Stage(viewport,batch);
        pointer = new Vector3();
        bb = ByteBuffer.allocate(1024);
    }

    public State(GameStateManager gsm, SpriteBatch sb, SocketChannel sock, String fd){
        players= new ArrayList<PlayerEntry>();
        this.gameStateManager=gsm;
        this.sock=sock;
        this.batch=sb;
        this.myFd=fd;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyGdxGame.WIDTH, MyGdxGame.HEIGHT);
        viewport = new FitViewport(MyGdxGame.WIDTH,MyGdxGame.HEIGHT,camera);
        stage = new Stage(viewport,batch);
        pointer = new Vector3();
        bb = ByteBuffer.allocate(1024);
    }

    public abstract void handleInput();
    public abstract void update(float deltaTime);
    public abstract void render();
    public abstract void dispose();
}
