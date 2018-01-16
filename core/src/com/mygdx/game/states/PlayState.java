package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.mygdx.game.Map;
import com.mygdx.game.Player;

import java.io.IOException;

import java.net.InetSocketAddress;
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


    private int bg_h=0;
    private int bg_w=0;
    Player player;
    Map map;

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

    public PlayState(GameStateManager gsm, SpriteBatch batch, SocketChannel sock, Selector sel, HashMap<String, PlayerEntry> pls, String fd) {

        super(gsm,batch,sock,sel,pls,fd);
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
        buttonDown.addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                player.moveDown();
                return true;
            }
        });
        buttonUp.addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                player.moveUp();
                return true;
            }
        });
        buttonLeft.addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                player.moveLeft();
                return true;
            }
        });
        buttonRight.addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                player.moveRight();
                return true;
            }
        });
        bombButton.addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                //todo BOOOM
                return true;
            }
        });


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

        for(java.util.Map.Entry<String, PlayerEntry> splayer: players.entrySet()){
            if(!splayer.getKey().equals(myFd))
                stage.addActor(splayer.getValue().player);
        }
        stage.addActor(player);
        stage.addActor(map);
        stage.addActor(buttonDown);
        stage.addActor(buttonUp);
        stage.addActor(buttonLeft);
        stage.addActor(buttonRight);
        stage.addActor(bombButton);
        stage.addActor(textActor);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count=1;
                while(count>=0){
                    sendPosition(player.getPosX(),player.getPosY());
                    try {
                        readServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }
        }).start();

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



    private void sendPosition(float posX, float posY) {

        try {
            String msg="1 "+String.valueOf(posX)+" "+String.valueOf(posY);
            sock.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void updatePlayerList(String result){

        String data = result.substring(1);
        HashSet<String> playersSet = new HashSet<String>(Arrays.asList(data.split(" ")));
        for (String palyer_data : playersSet) {
            String[] data_splited = palyer_data.split(";");

            if (!data_splited[0].equals(myFd)) {
                PlayerEntry cur_player = (PlayerEntry) players.get(data_splited[0]);
                String[] cords = data_splited[2].split(",");
                cur_player.player.setPosX(Float.valueOf(cords[0]));
                cur_player.player.setPosY(Float.valueOf(cords[1]));

            }
            //if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),parseBoolean(name.substring(name.length()-2))));

        }

    }

}
