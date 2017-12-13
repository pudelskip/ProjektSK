package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.mygdx.game.Map;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Player;

import java.net.Socket;

/**
 * Created by Pawel on 03.12.2017.
 */


public class PlayState extends State {
    int[][] testmap;

    BitmapFont font;

    Texture bg;
    Texture buttonUp;
    Texture buttonDown;
    Texture buttonLeft;
    Texture buttonRight;

    private int bg_h=0;
    private int bg_w=0;
    Player player;
    Map map;



    public PlayState(GameStateManager gsm, SpriteBatch batch, Socket sock) {

        super(gsm,batch,sock);
        font = new BitmapFont();
        font.getData().setScale(2);
        player=new Player();
        map= new Map();
        bg= new Texture("sprites/bg.png");
        buttonDown = new Texture("sprites/button.png");
        buttonUp = new Texture("sprites/button.png");
        buttonLeft = new Texture("sprites/button.png");
        buttonRight = new Texture("sprites/button.png");
        camera.setToOrtho(false,1280, 720);

        bg_h = bg.getHeight();
        bg_w = bg.getWidth();

        testmap = new int[10][];

        testmap[0]= new int[]{1, 1, 1, 1, 0, 1, 1, 1, 1, 1};
        testmap[1]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[2]= new int[]{1, 0, 1, 1, 0, 0, 0, 1, 0, 1};
        testmap[3]= new int[]{1, 0, 1, 0, 0, 0, 0, 0, 0, 1};
        testmap[4]= new int[]{0, 0, 0, 0, 1, 1, 0, 0, 0, 1};
        testmap[5]= new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[6]= new int[]{1, 0, 0, 0, 0, 0, 0, 1, 0, 1};
        testmap[7]= new int[]{1, 0, 1, 0, 0, 0, 0, 1, 0, 1};
        testmap[8]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[9]= new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};


        map.setFields(testmap);

        stage.addActor(player);
        stage.addActor(map);
    }

    @Override
    public void handleInput() {
        if(Gdx.input.isTouched()){
            pointer = new Vector3(Gdx.input.getX(), Gdx.input.getY(),0);
            camera.unproject(pointer); // mousePos is now in world coordinates

            if(pointer.y>420 && pointer.y<520 && pointer.x <1170 && pointer.x>1070)
                player.moveUp();
            else if(pointer.y>280 && pointer.y<380 && pointer.x <1170 && pointer.x>1070)
                player.moveDown();
            if(pointer.y>350 && pointer.y<450 && pointer.x <1080 && pointer.x>980)
                player.moveLeft();
            if(pointer.y>350 && pointer.y<450 && pointer.x <1260 && pointer.x>1160)
                player.moveRight();


        }
    }

    @Override
    public void update(float deltaTime) {

        handleInput();
        player.update(deltaTime,map);


    }

    @Override
    public void render() {
        stage.draw();
        /*
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(bg,(int)camera.position.x-MyGdxGame.HEIGHT/2,0,MyGdxGame.HEIGHT,MyGdxGame.HEIGHT);
        batch.draw(buttonUp,1070,420,100,100);
        batch.draw(buttonDown,1070,280,100,100);
        batch.draw(buttonLeft,980,350,100,100);
        batch.draw(buttonRight,1160,350,100,100);
        batch.draw(player.getModel(), player.getPosX(), player.getPosY(),player.getWidth(),player.getHeight());






        batch.end();

        map.render(camera,batch);


        batch.begin();
        font.draw(batch, String.valueOf(pointer.x)+", "+String.valueOf(pointer.y), 100, 100);

        font.draw(batch, String.valueOf(player.getX())+", "+String.valueOf(player.getY()), 1000, 100);
        font.draw(batch, String.valueOf(player.testX)+", "+String.valueOf(player.testY), 1000, 200);
        batch.end();*/
    }

    @Override
    public void dispose() {
        //player.getModel().dispose();
        //bg.dispose();
        stage.dispose();
    }
}
