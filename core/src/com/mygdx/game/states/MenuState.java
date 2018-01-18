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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.sleep;


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
    String start="0";
    String sock_type="";
    boolean menu_up;

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
                    if(sock_type=="NIO")
                        tryConnectAsyncNio();
                    if(sock_type=="IO")
                        tryConnectAsyncIo();
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
        sock_type="NIO";
       initAll();
    }

    public MenuState(GameStateManager gsm, SpriteBatch sb, Socket sock){
        super(gsm,sb,sock);
        sock_type="IO";
        initAll();
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
        deltaTime = Math.min(deltaTime, 0.1f);
       // text.setText(status);
        handleInput();

        if(start.equals("1")){
            menu_up=false;
            if(sock_type=="NIO")
                 gameStateManager.push(new PlayState(gameStateManager,batch,sock,sel,players,myFd));
            if(sock_type=="IO")
                gameStateManager.push(new PlayState(gameStateManager,batch,ioSocket,sel,players,myFd));
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

    private void tryConnectAsyncNio(){
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
                    readServerMyFdNio();
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

    private void tryConnectAsyncIo(){
        if(!isConnecting){
            isConnecting=true;
            text.setColor(1.0f,1.0f,0.0f,1.0f);
            status="Trwa łączenie";

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        ioSocket = new Socket("192.168.0.110", 22222);
                        readServerMyFdIo();
                        text.setColor(0.0f,1.0f,0.0f,1.0f);

                        connectButton.remove();
                        stage.addActor(rdyBottun);
                        status="Połączono";
                        createRcvThread();

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

    private void readServerIo(){


        try {
            InputStream is = ioSocket.getInputStream();
            byte[] bytearr = new byte[256];
            int count = is.read(bytearr);
            if(count == -1) {
                sockKey.cancel();
                throw new IOException("Nie udało sięusatlić Fd");
            }
            String result = new String(bytearr).substring(0,count);
            text.setText(String.valueOf(count));

            updatePlayerList(result);
            showPlayersList();


        } catch (Throwable e) {
            e.printStackTrace();
            //todo exception handling
        }
    }

    private void readServerMyFdNio(){


        try {
            ByteBuffer bb = ByteBuffer.allocate(256);
            sel.select();
            if(sel.selectedKeys().size() == 1 && sel.selectedKeys().iterator().next() == sockKey){

                bb.clear();
                int count = sock.read(bb);
                if(count == -1) {
                    sockKey.cancel();
                    throw new IOException("Nie udało sięusatlić Fd");
                }

                sel.selectedKeys().clear();

                bb.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(bb);
                String result = charBuffer.toString();

                myFd=result;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //todo exception handling
        }
    }

    private void readServerMyFdIo(){


        try {
            InputStream is = ioSocket.getInputStream();
            byte[] bytearr = new byte[256];
            int count = is.read(bytearr);
            if(count == -1) {
                sockKey.cancel();
                throw new IOException("Nie udało sięusatlić Fd");
            }
            String result = new String(bytearr).substring(0,count);
            myFd=result;

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
        text.setText(String.valueOf(count));

        updatePlayerList(result);
        showPlayersList();

    }

    private void updatePlayerList(String result){

        players.clear();

        start = result.substring(100, 101);
        String data = result.substring(101);
        HashSet<String> playersSet = new HashSet<String>(Arrays.asList(data.split(" ")));
        for (String palyer_data : playersSet) {
            String[] data_splited = palyer_data.split(";");
            boolean status = false;
            //if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),parseBoolean(name.substring(name.length()-2))));
            if (data_splited[1].equals("1"))
                status = true;
            if (!data_splited[0].equals(""))
                players.put(data_splited[0], new PlayerEntry(data_splited[0], status));

        }

    }

    private void setReady() {
        if(!ready){
            try {
                String msg="1 0 0";
                if(sock_type=="NIO")
                    sock.write(ByteBuffer.wrap(msg.getBytes()));
                if(sock_type=="IO")
                    ioSocket.getOutputStream().write(msg.getBytes());
                ready=true;
                rdyBottun.remove();
                stage.addActor(rdyBottun2);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                String msg="0 0 0";
                if(sock_type=="NIO")
                    sock.write(ByteBuffer.wrap(msg.getBytes()));
                if(sock_type=="IO")
                    ioSocket.getOutputStream().write(msg.getBytes());
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
        for(Map.Entry<String, PlayerEntry> splayer: players.entrySet()){
            Label placeholder = new Label("->   "+splayer.getValue().name+" "+splayer.getValue().ready,skin);
            placeholder.setFontScale(2.0f);
            if(splayer.getValue().ready)
                placeholder.setColor(0.0f,1.0f,0.0f,1.0f);
            else
                placeholder.setColor(1.0f,1.0f,0.0f,1.0f);
            playerList.add(placeholder).bottom().padTop(10.0f).padLeft(20.0f);
            playerList.row();
        }
    }


    private void initAll(){
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

    private void createRcvThread(){
        menu_up=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count=1;
                while(menu_up){
                    if(sock_type=="IO"){

                        if(ioSocket !=null)
                            if(ioSocket.isConnected()){
                                readServerIo();
                            }
                    }
                    if(sock_type=="NIO"){

                        if(sock !=null)
                            if(sock.isConnected()){
                                readServer();
                            }
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
}
