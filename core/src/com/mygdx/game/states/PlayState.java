package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.mygdx.game.Map;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Player;

import java.io.IOException;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import static java.lang.Thread.sleep;


/**
 * Created by Pawel on 03.12.2017.
 */


public class PlayState extends State {


    int[][] testmap;

    BitmapFont font;

    Texture bg;
    MyButton buttonUp;
    MyButton buttonDown;
    MyButton buttonLeft;
    MyButton buttonRight;
    MyButton bombButton;
    TextActor textActor;
    MyActor loseText;
    MyActor winText;
    MyActor tieText;
    ExitButton exitButton;
    String sock_type="";

    boolean in_game;
    boolean game_up;
    float time=0;
    float bomb_place_time=0;
    private int bg_h=0;
    private int bg_w=0;
    Player player;
    Map map;
    boolean bomb;
    int bombs=3;

    public class MyButton extends Actor {

        Texture texture;
        float actorX,actorY,width,height;


        public MyButton(Texture texture, float actorX, float actorY) {
            this.texture = texture;
            this.actorX = actorX;
            this.actorY = actorY;
            setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
            width=100;
            height=100;

        }
        public MyButton(Texture texture, float actorX, float actorY,float width,float height) {
            this.texture = texture;
            this.actorX = actorX;
            this.actorY = actorY;
            this.width=width;
            this.height=height;
            setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());

        }



        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,actorX,actorY,width,height);
        }
    }

    private class ExitButton extends Actor{


        Texture texture;
        float actorX,actorY;

        public ExitButton(Texture tex) {
            actorX=1000;
            actorY=600;
            texture= tex;
            setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
            addListener(new InputListener(){
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    exitGame();
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,actorX,actorY);
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

    public class MyActor extends Actor {
        public MyActor(Texture texture) {
            this.texture = texture;
        }

        Texture texture;
        @Override
        public void draw(Batch batch, float alpha){
            batch.draw(texture,350, 310);
        }
    }

    public PlayState(GameStateManager gsm, SpriteBatch batch, SocketChannel sock, Selector sel, HashMap<String, PlayerEntry> pls, String fd) {

        super(gsm,batch,sock,sel,pls,fd);
        sock_type="NIO";
        initAll();
    }

    public PlayState(GameStateManager gsm, SpriteBatch batch, Socket sock, Selector sel, HashMap<String, PlayerEntry> pls, String fd) {

        super(gsm,batch,sock,sel,pls,fd);
        sock_type="IO";
        initAll();
    }

    @Override
    public void handleInput() {


        if(Gdx.input.isTouched()){
            pointer = new Vector3(Gdx.input.getX(), Gdx.input.getY(),0);
            camera.unproject(pointer); // mousePos is now in world coordinates
            if(pointer.y>250 && pointer.y<500 && pointer.x <250 && pointer.x>0){
                if(bombs>0 && bomb_place_time>0.2){
                    bomb_place_time=0;
                    bombs -= 1;

                    bomb=true;
                }

            }

            if(pointer.y>430 && pointer.y<530 && pointer.x <1190 && pointer.x>1090)
                player.moveUp();
            else if(pointer.y>270 && pointer.y<370 && pointer.x <1190 && pointer.x>1090)
                player.moveDown();
            if(pointer.y>350 && pointer.y<450 && pointer.x <1105 && pointer.x>1005)
                player.moveLeft();
            if(pointer.y>350 && pointer.y<450 && pointer.x <1275 && pointer.x>1175)
                player.moveRight();

        }else{
            if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
                if(bombs>0 && bomb_place_time>0.2){
                    bombs -= 1;
                    bomb_place_time=0;
                   // int x_b = (int)(player.getPosX()+player.getWidth()/2-map.getOffset())/map.getTile_size();
                    //int y_b = (int)(player.getPosY()+player.getHeight()/2)/map.getTile_size();
                    bomb=true;
                }
            }

            if(Gdx.input.isKeyPressed(Input.Keys.UP))
                player.moveUp();
            if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
                player.moveDown();
            if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
                player.moveLeft();
            if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                player.moveRight();

        }
    }

    @Override
    public void update(float deltaTime) {
        textActor.setText(String.valueOf(bombs));
        if(in_game){
            time += deltaTime;
            bomb_place_time += deltaTime;
            if(time>2.0){
                if(bombs<3)
                bombs=bombs+1;
                time=0;
            }

            handleInput();
            player.update(deltaTime,map);
        }

    }



    @Override
    public void render() {
        stage.draw();
        /*old drawing
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, String.valueOf(pointer.x)+", "+String.valueOf(pointer.y), 100, 100);
        font.draw(batch, String.valueOf(player.getX())+", "+String.valueOf(player.getY()), 1000, 100);
        font.draw(batch, String.valueOf(player.testX)+", "+String.valueOf(player.testY), 1000, 200);
        batch.end();*/
    }

    @Override
    public void dispose() {
        stage.dispose();
    }



    private void sendPosition(float posX, float posY) throws IOException {


            String action="0";
            if(bomb){
                bomb=false;
                action="2";

            }

            String msg=action+" "+String.valueOf(posX)+" "+String.valueOf(posY);
            if(sock_type=="NIO")
                sock.write(ByteBuffer.wrap(msg.getBytes()));
            if(sock_type=="IO")
                ioSocket.getOutputStream().write(msg.getBytes());


    }

    private void readServer() throws IOException {
        try {

            sel.select();
            Set<SelectionKey> keySet = sel.selectedKeys();
            Iterator<SelectionKey> keyIterator = keySet.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey currentKey = keyIterator.next();
                keyIterator.remove();
                if(currentKey.isValid() && currentKey.isReadable()){
                    readFromSocket(currentKey);
                }

            }


        } catch (Throwable e) {
            e.printStackTrace();
            //todo exception handling
        }
    }


    private void readFromSocket(SelectionKey key) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(256);
        bb.clear();
        SocketChannel sc = (SocketChannel) key.channel();
        int count = sc.read(bb);

        if(count == -1) {
            key.cancel();
            return;
        }


        bb.flip();
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(bb);
        String result = charBuffer.toString();
        updatePlayerList(result);

    }

    private void readServerIo(){


        try {
            InputStream is = ioSocket.getInputStream();
            byte[] bytearr = new byte[512];
            int count = is.read(bytearr);
            if(count == -1) {
                throw new IOException("Błąd połączenia z serwerem");
            }
            String result = new String(bytearr).substring(0,count);
            //String result1 = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            //textActor.setText(myFd+" _ "+result);
           updatePlayerList(result);

        } catch (IOException e) {
            disconnectSocketIo();
        }
    }


    private void updatePlayerList(String result){
        String map_string = result.substring(0,100);
       // textActor.setText(map_string);
        int idx=0;
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                int temp_val = Character.getNumericValue(map_string.charAt(idx));
                map.setField(i,j,temp_val);

                idx++;
            }
        }


        String data = result.substring(101);

        for (String palyer_data : data.split(" ")) {
            String[] data_splited = palyer_data.split(";");

            if (!data_splited[0].equals(myFd)) {

                PlayerEntry cur_player = (PlayerEntry) players.get(data_splited[0]);
                String[] cords = data_splited[2].split(",");
                cur_player.player.setPosX(Float.valueOf(cords[0]));
                cur_player.player.setPosY(Float.valueOf(cords[1]));

            }
            if(data_splited[0].equals(myFd) && data_splited[1].equals("3") && in_game){
                stage.addActor(loseText);
                stage.addActor(exitButton);
                in_game=false;
                player.remove();
                player.setPosX(0);
                textActor.setText("RIP");
                game_up = false;
                disconnectSocketIo();
            }
            if(data_splited[0].equals(myFd) && data_splited[1].equals("4") && in_game){
                stage.addActor(winText);
                stage.addActor(exitButton);
                in_game=false;
                player.remove();
                game_up = false;
                disconnectSocketIo();


            }
            if(data_splited[0].equals(myFd) && data_splited[1].equals("5") && in_game){
                stage.addActor(tieText);
                stage.addActor(exitButton);
                in_game=false;
                player.remove();
                game_up = false;
                disconnectSocketIo();


            }
            if(!data_splited[0].equals(myFd) && data_splited[1].equals("8") && in_game){
                PlayerEntry cur_player = (PlayerEntry) players.get(data_splited[0]);
                cur_player.player.remove();
                players.remove(data_splited[0]);

            }



            //if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),parseBoolean(name.substring(name.length()-2))));

        }

    }

    private void exitGame(){

            dispose();
            gameStateManager.push(new MenuState(gameStateManager, batch, ioSocket));


    }

    private void disconnectSocketIo(){
        try {
            ioSocket.close();
        } catch (IOException e) {
            textActor.setColor(1.0f,0.0f,0.0f,1.0f);
            textActor.setText("Blad - Gniazdo juz rozlaczone");

        }
    }

    private void initAll(){
        Gdx.input.setInputProcessor(stage);

        font = new BitmapFont();
        font.getData().setScale(2);
        player=new Player();
        map= new Map();
        bg= new Texture("sprites/bg.png");
        textActor=new TextActor("");


        buttonDown = new MyButton(new Texture("arrowD.png"),1090,270);
        buttonUp = new MyButton(new Texture("arrowU.png"),1090,430);
        buttonLeft = new MyButton(new Texture("arrowL.png"),1005,350);
        buttonRight = new MyButton(new Texture("arrowR.png"),1175,350);
        bombButton = new MyButton(new Texture("bomb.png"),0,250,250,250);
        loseText = new MyActor(new Texture("lose.png"));
        winText = new MyActor(new Texture("win.png"));
        tieText = new MyActor(new Texture("tie.png"));
        exitButton = new ExitButton(new Texture("exit.png"));
        bg_h = bg.getHeight();
        bg_w = bg.getWidth();

        testmap = new int[10][];

        testmap[0]= new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        testmap[1]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[2]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[3]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[4]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[5]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[6]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[7]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[8]= new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        testmap[9]= new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};


        map.setFields(testmap);
        player.setModel();
        for(java.util.Map.Entry<String, PlayerEntry> splayer: players.entrySet()){
            if(!splayer.getKey().equals(myFd))
                splayer.getValue().player.setModel();
                stage.addActor(splayer.getValue().player);
        }

        stage.addActor(map);
        stage.addActor(buttonDown);
        stage.addActor(buttonUp);
        stage.addActor(buttonLeft);
        stage.addActor(buttonRight);
        stage.addActor(bombButton);
        stage.addActor(textActor);
        stage.addActor(player);
        bomb=false;
        in_game=true;
        game_up=true;
        Thread sndARcv;
        sndARcv = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connected=true;
                while(game_up && connected){


                    try {
                        sendPosition(player.getPosX(),player.getPosY());
                        sleep(10);
                        if(sock_type=="NIO")
                            readServer();
                        if(sock_type=="IO")
                            readServerIo();
                    } catch (IOException e) {
                        connected=false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }
        });
        sndARcv.setDaemon(true);
        sndARcv.start();



    }


}
