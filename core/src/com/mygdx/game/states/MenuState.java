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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


/**
 * Created by Pawel on 01.12.2017.
 */

public class MenuState extends State {
    int as=0;
    String status;
    TextActor text;
    Skin skin;
    Table playerList;
    Table menuTable;
    boolean isConnecting;
    boolean ready;

    ConnectButton connectButton;
    RdyButton rdyBottun;
    RdyButton rdyBottun2;

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

        public ConnectButton(Texture tex) {
            actorX=1000;
            actorY=600;
            texture= tex;
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

    private class RdyButton extends Actor{


        Texture texture;
        float actorX,actorY;

        public RdyButton(Texture tex) {
            actorX=1000;
            actorY=600;
            texture= tex;
            setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
            addListener(new InputListener(){
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    setReady();

                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,actorX,actorY);
        }

    }



    public MenuState(GameStateManager gsm, SpriteBatch sb, SocketChannel sock){
        super(gsm,sb,sock);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        connectButton = new ConnectButton(new Texture("conn.png"));
        rdyBottun = new RdyButton(new Texture("rdy1.png"));
        rdyBottun2 = new RdyButton(new Texture("rdy2.png"));
        connectButton.setTouchable(Touchable.enabled);

        menuTable = new Table(skin);
        playerList = new Table(skin);


        this.status = "Rozłączono";
        this.isConnecting=false;
        this.ready=false;

        text = new TextActor(status);

        stage.addActor(text);
        stage.addActor(connectButton);
        stage.addActor(menuTable);
        menuTable.setDebug(true);
        playerList.setDebug(true);
        menuTable.setFillParent(true);



        menuTable.add(playerList).expand().top().left().pad(100.0f,20.0f,100.0f,600.0f);






    }

    @Override
    public void handleInput() {
        if(Gdx.input.justTouched()){

            //gameStateManager.push(new PlayState(gameStateManager,batch,sock));
            //dispose();

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
        as+=1;

        if(sock !=null)
            if(sock.isConnected()){

                readServer();
        }



    }

    @Override
    public void render() {
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();

    }

    private void tryConnectAsync(){
        if(!isConnecting){
            isConnecting=true;
        text.setColor(1.0f,1.0f,0.0f,1.0f);
        status="Trwa łączenie";

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    sel= Selector.open();
                    sock=SocketChannel.open(new InetSocketAddress("192.168.0.110",22222));
                    sock.configureBlocking(false);
                    sockKey = sock.register(sel, SelectionKey.OP_READ);
                    readServerMyFd();
                    text.setColor(0.0f,1.0f,0.0f,1.0f);

                    connectButton.remove();
                    stage.addActor(rdyBottun);
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

    private void readServer(){


        try {

            sel.selectNow();
            if(sel.selectedKeys().size() == 1 && sel.selectedKeys().iterator().next() == sockKey){

                bb.clear();
                int count = sock.read(bb);
                if(count == -1) {
                    sockKey.cancel();
                    return;
                }

                sel.selectedKeys().clear();
                String result= new String(bb.array(), "UTF-8");
                players.clear();

                String[] dane = result.split("|");
                for(String name: dane[1].split(";")){
                    boolean status=false;
                   //if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),parseBoolean(name.substring(name.length()-2))));
                    if(name.substring(name.length()-1).equals("1"))
                        status=true;
                    if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),status));
                }
                showPlayersList();

                if(dane[0].contains("g")){
                    gameStateManager.push(new PlayState(gameStateManager,batch,sock,sel,players,myFd));
                    dispose();

                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
            //todo exception handling
        }
    }

    private void readServerMyFd(){


        try {

            sel.select();
            if(sel.selectedKeys().size() == 1 && sel.selectedKeys().iterator().next() == sockKey){

                bb.clear();
                int count = sock.read(bb);
                if(count == -1) {
                    sockKey.cancel();
                    throw new IOException("Nie udało sięusatlić Fd");
                }

                sel.selectedKeys().clear();
                String result= new String(bb.array(), "UTF-8");
                myFd=result;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //todo exception handling
        }
    }

    private void setReady() {
        if(!ready){
            try {
                String msg="ready 0 0";
                sock.write(ByteBuffer.wrap(msg.getBytes()));
                ready=true;
                rdyBottun.remove();
                stage.addActor(rdyBottun2);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                String msg="notready 0 0";
                sock.write(ByteBuffer.wrap(msg.getBytes()));
                ready=false;
                rdyBottun.remove();
                stage.addActor(rdyBottun);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void showPlayersList(){
        playerList.clear();
        for(PlayerEntry player: players){
            Label placeholder = new Label("->   "+player.name+" "+player.ready,skin);
            placeholder.setFontScale(2.0f);
            if(player.ready)
                placeholder.setColor(0.0f,1.0f,0.0f,1.0f);
            else
                placeholder.setColor(1.0f,1.0f,0.0f,1.0f);
            playerList.add(placeholder).bottom().padTop(10.0f).padLeft(20.0f);
            playerList.row();
        }
    }
}
