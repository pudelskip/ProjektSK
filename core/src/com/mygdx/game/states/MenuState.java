package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
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

import java.io.DataInputStream;
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

    private static final String CONFIG_FILE = "config.txt";

    int as=0;
    private String status;
    private TextActor text;
    private Skin skin;
    private Table playerList;
    private Table menuTable;
    private  boolean isConnecting;
    private boolean ready;
    private String start;
    private String sock_type="";
    private boolean menu_up;
    private String address;
    private int port = 0;
    float myX=0.0f;
    float myY=0.0f;


    private ConnectButton connectButton;
    private RdyButton rdyBottun;
    private RdyButton rdyBottun2;

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
                    if(isConnecting)
                        return true;
                    Gdx.input.getTextInput(new Input.TextInputListener(){
                        @Override
                        public void input(String text) {
                            address = text;
                            tryConnectAsyncIo();
                        }

                        @Override
                        public void canceled() {
                            address = null;
                        }
                    }, "Podaj adres ip serwera", address, "");
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
       initAll();
    }

    public MenuState(GameStateManager gsm, SpriteBatch sb, Socket sock){
        super(gsm,sb,sock);
        initAll();
        try {
            fetchConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

       // text.setText(status);
        handleInput();


        if(start.equals("1")){
            menu_up=false;
            dispose();
            gameStateManager.push(new PlayState(gameStateManager,batch,ioSocket,sel,players,myFd,myX,myY));
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



    private void tryConnectAsyncIo(){
        if(!isConnecting){
            isConnecting=true;
            text.setColor(1.0f,1.0f,0.0f,1.0f);
            status="Trwa łączenie";
            text.setText(status);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String addMsg="";
                    try {
                        ioSocket = new Socket();
                        ioSocket.connect(new InetSocketAddress(address, port), 5000);
                        readServerMyFdIo();
                        text.setColor(0.0f,1.0f,0.0f,1.0f);

                        connectButton.remove();
                        stage.addActor(rdyBottun);
                        status="Polaczono";
                        text.setText(status);
                        createRcvThread();

                    } catch (IOException e) {
                        text.setColor(1.0f,0.0f,0.0f,1.0f);
                        status="Blad - "+e.getMessage()+" "+addMsg;
                        text.setText(status);
                        isConnecting=false;
                        disconnectSocketIo();

                    } catch (Exception e) {
                        text.setColor(1.0f,0.0f,0.0f,1.0f);
                        status="Blad - "+e.getMessage();
                        text.setText(status);
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



    private void readServerIo(){


        try {
            boolean end=false;
            String result ="";
            byte[] messageByte = new byte[1000];
            DataInputStream in = new DataInputStream(ioSocket.getInputStream());
            int bytesRead = 0;

            messageByte[0] = in.readByte();
            messageByte[1] = in.readByte();
            messageByte[2] = in.readByte();
            ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 3);
            String v = new String(byteBuffer.array()).substring(0,3);
            int bytesToRead = Integer.valueOf(v);

            while(!end)
            {
                in.readFully(messageByte, 0, bytesToRead);
                result = new String(messageByte, 0, bytesToRead);
                if ( result.length() == bytesToRead )
                {
                    end = true;
                }
            }
            updatePlayerList(result);
          //  showPlayersList();

/*
            InputStream is = ioSocket.getInputStream();

            byte[] bytearr = new byte[512];
            int count = is.read(bytearr);
            if(count == -1) {
                throw new IOException("Nie udało sie usatlic Fd");
            }
            String result = new String(bytearr).substring(0,count);
*/
           // updatePlayerList(result);
           // showPlayersList();


        } catch (Throwable e) {

           // disconnectSocketIo();
            //text.setColor(1.0f,0.0f,0.0f,1.0f);
           // status="Blad tutaj - "+e.getMessage();
           // isConnecting=false;
        }
    }



    private void readServerMyFdIo() throws Exception {


        try {
            InputStream is = ioSocket.getInputStream();
            byte[] bytearr = new byte[256];
            int count = is.read(bytearr);
            if(count == -1) {

                throw new IOException("Nie udało sięusatlić Fd");
            }
            String result = new String(bytearr).substring(0,count);
            String[] data = result.split(";");

            if(data[0].contains("-1"))
                throw new Exception("Serwer jest pelen");
            if(data[0].contains("-2"))
                throw new Exception("Aktualnie trwa gra");
            myX = Float.valueOf(data[1]);
            myY = Float.valueOf(data[2]);
            myFd=data[0];

        } catch (IOException e) {
            e.printStackTrace();
            //todo exception handling
        }
    }



    private void updatePlayerList(String result){

        players.clear();

        start = result.substring(100, 101);
        String data = result.substring(101);
        HashSet<String> playersSet = new HashSet<String>(Arrays.asList(data.split(" ")));
        for (String palyer_data : playersSet) {
            String[] data_splited = palyer_data.split(";");
            int status = 0;
            //if(!name.equals("")) players.add(new PlayerEntry(name.substring(0,name.length()-1),parseBoolean(name.substring(name.length()-2))));
            if (data_splited[1].equals("1"))
                status = 1;
            if (!data_splited[0].equals(""))
                players.put(data_splited[0], new PlayerEntry(data_splited[0], status));

        }

    }

    private void setReady() {
        if(!ready){
            try {
                String msg="1 0 0";
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
            String me="";
            if(splayer.getValue().name.equals(myFd))
                me=" (ja)";
            Label placeholder = new Label("-> Gracz"+splayer.getValue().name+me,skin);
            placeholder.setFontScale(2.0f);
            if(splayer.getValue().ready==1)
                placeholder.setColor(0.0f,1.0f,0.0f,1.0f);
            else
                placeholder.setColor(1.0f,1.0f,0.0f,1.0f);
            playerList.add(placeholder).bottom().padTop(10.0f).padLeft(20.0f);
            playerList.row();
        }
    }


    private void initAll(){
        Gdx.input.setInputProcessor(stage);
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));

        this.connectButton = new ConnectButton(new Texture("conn.png"));
        this.rdyBottun = new RdyButton(new Texture("rdy1.png"));
        this.rdyBottun2 = new RdyButton(new Texture("rdy2.png"));
        this.connectButton.setTouchable(Touchable.enabled);

        this.menuTable = new Table(skin);
        this.playerList = new Table(skin);


        this.status = "Rozłączono";
        this.isConnecting=false;
        this.ready=false;
        this.start="0";

        this.text = new TextActor(status);

        stage.addActor(text);
        stage.addActor(connectButton);
        stage.addActor(menuTable);
       // this.menuTable.setDebug(true);
      //  this.playerList.setDebug(true);
        this.menuTable.setFillParent(true);

        this.menuTable.add(playerList).expand().top().left().pad(100.0f,20.0f,100.0f,600.0f);


    }

    private void createRcvThread(){
        menu_up=true;
        Thread recv;
        recv =new Thread(new Runnable() {
            @Override
            public void run() {
                int count=1;
                while(menu_up){

                    if(ioSocket !=null)
                        if(ioSocket.isConnected()){
                            readServerIo();
                        }


                    try {
                        sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            showPlayersList();

                        }
                    });

                }

            }
        });
        recv.setDaemon(true);
        recv.start();
    }

    private void disconnectSocketIo(){
        try {
            if(ioSocket != null)
                ioSocket.close();
        } catch (IOException e) {
            text.setColor(1.0f,0.0f,0.0f,1.0f);
            status="Blad - Gniazdo juz rozlaczone";
            isConnecting=false;
        }
    }

    private void fetchConfig() throws IOException{
        FileHandle fileConfig = Gdx.files.internal(CONFIG_FILE);
        String fileString = fileConfig.readString();
        String ls = System.getProperty("line.separator");
        String []elements = fileString.split(ls);
        if(elements.length < 2)
            return;
        String line;
        int index;
        line = elements[0];
        if (line != null){
            if(line.contains("ip")){
                index = line.indexOf(":");
                if(index > 0) {
                    line = line.substring(index + 1).trim();
                    address = line;
                }
            }
        }
        line = elements[1];
        if(line != null){
            if(line.contains("port")){
                index = line.indexOf(":");
                if(index > 0){
                    line = line.substring(index + 1).trim();
                    if(line.length() > 0)
                        port = Integer.valueOf(line);
                }
            }
        }
    }
}
