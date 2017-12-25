package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.mygdx.game.MyGdxGame;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Created by Pawel on 01.12.2017.
 */

public class MenuState extends State {

    String status;
    TextActor text;

    Table playerList;
    Table menuTable;
    boolean isConnecting;


    public class MyActor extends Actor {
        Texture texture = new Texture("board.png");
        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,0,0);
        }
    }

    public class TextActor extends Actor {
        BitmapFont font;
        String text;

        public TextActor(String text) {
            this.text = text;
            font = new BitmapFont();
            font.setColor(1.0f,0.0f,0.0f,1);
        }

        @Override
        public void draw(Batch batch, float alpha){
            font.draw(batch,"Status: "+text,10,700);
        }


        public void setText(String text) {
            this.text = text;
        }
        public void setColor(float r, float g, float b, float a){
            font.setColor(r,g,b,a);
        }
    }

    private class ConnectButton extends Actor{


        Texture texture;
        float actorX,actorY;

        public ConnectButton() {
            actorX=1000;
            actorY=600;
            texture= new Texture("connect.png");
            setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
            addListener(new InputListener(){
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    tryConnectAsync();
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,actorX,actorY);
        }

    }


    public MenuState(GameStateManager gsm, SpriteBatch sb, Socket sock){
        super(gsm,sb,sock);
        Gdx.input.setInputProcessor(stage);
        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        ConnectButton connectButton = new ConnectButton();
        connectButton.setTouchable(Touchable.enabled);

        menuTable = new Table(skin);
        playerList = new Table(skin);


        this.status = "Rozłączono";
        this.isConnecting=false;
        text = new TextActor(status);

        stage.addActor(text);
        stage.addActor(connectButton);
        stage.addActor(menuTable);
        menuTable.setDebug(true);
        playerList.setDebug(true);
        menuTable.setFillParent(true);



        menuTable.add(playerList).expand().top().left().pad(100.0f,20.0f,100.0f,600.0f);

        Label gr1 = new Label("GRACZ1",skin);
        gr1.setFontScale(2.0f);

        playerList.add(gr1).bottom().padTop(10.0f);
        playerList.row();
        playerList.add(gr1).right().padTop(10.0f);



      //(new String[]{"fsd","fsdf","fsdfdsdfdf"});

    }

    @Override
    public void handleInput() {
        if(Gdx.input.justTouched()){
            //tryConnectAsync();
            //gameStateManager.push(new PlayState(gameStateManager,batch,sock));

            //Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
            //Dialog d = new Dialog("title",skin);
            //d.show(stage);
//            dispose();
        }
    }

    @Override
    public void update(float deltaTime) {
        text.setText(status);
        handleInput();
    }

    @Override
    public void render() {
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryConnectAsync(){
        if(!isConnecting){
            isConnecting=true;
        text.setColor(1.0f,1.0f,0.0f,1.0f);
        status="Trwa łączenie";

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do something important here, asynchronously to the rendering thread
                try {

                    sock = new Socket("127.0.0.1",22222);
                    text.setColor(0.0f,1.0f,0.0f,1.0f);
                    status="Połączono";
                } catch (IOException e) {
                    text.setColor(1.0f,0.0f,0.0f,1.0f);
                    status="Błąd: "+e.getMessage();
                    isConnecting=false;

                }
                // post a Runnable to the rendering thread that processes the result
                /*Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                    //todo uruchomienie stanu gry

                    }
                });*/
            }
        }).start();
        }
    }
}
