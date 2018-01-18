package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.states.GameStateManager;
import com.mygdx.game.states.MenuState;

import java.net.Socket;
import java.nio.channels.SocketChannel;


public class MyGdxGame extends ApplicationAdapter {

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;

    private GameStateManager gsm;
	SpriteBatch batch;
	SocketChannel sock=null;
	Socket socket = null;
	boolean running;



	@Override
	public void create () {
		running=true;
        gsm = new GameStateManager();
		batch = new SpriteBatch();
		gsm.push(new MenuState(gsm,batch,socket));


	}

	@Override
	public void render () {



		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		gsm.update(Gdx.graphics.getDeltaTime());
		gsm.render();





	}
	
	@Override
	public void dispose () {
		batch.dispose();


	}


}
