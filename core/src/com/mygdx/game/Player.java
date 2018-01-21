package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Pawel on 01.12.2017.
 */

public class Player extends Actor {
    public int testX;
    public int testY;



    private Vector3 position;
    private Vector3 movement;
    private int speed=150;


    private float width;
    private float height;

    private Texture model;





    public void setPosX(float x) {
        this.position.x = x;
    }

    public void setPosY(float y) {
        this.position.y = y;
    }

    public float getPosX() {
        return this.position.x;
    }

    public float getPosY() {
        return this.position.y;
    }

    public Texture getModel() {
        return model;
    }

    public void setModel(){
        this.model = new Texture("badlogic.jpg");
    }


    public Player(){
        this.position = new Vector3(640,200,0);
        this.movement = new Vector3();
        this.height=50;
        this.width=50;
    }


    public Player(Player p){
        this.position = p.position;
        this.movement = p.movement;

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(model !=null)
            batch.draw(model,position.x,position.y,width,height);
    }

    public void update(float dt, Map map){
        movement.scl(dt);
        position.add(movement);
        if(checkCollision(map))
           position.sub(movement);

        movement.set(0,0,0);

    }

    public void moveUp(){
        movement.y=speed;
    }

    public void moveDown(){
        movement.y=-speed;
    }
    public void moveLeft(){
        movement.x=-speed;
    }
    public void moveRight(){
        movement.x=speed;
    }


    private boolean checkCollision(Map map){
        int offset = map.getOffset();
        int tailSize = map.getTile_size();
        int playerIndexX=(int)(position.x-offset+width/2)/tailSize;
        int playerIndexY=(int)(position.y+height/2)/tailSize;
        testX=playerIndexX;
        testY=playerIndexY;

        //Top center
        if(map.getField((9-playerIndexY)-1,playerIndexX)==1)
            if(position.y+height>=(playerIndexY+1)*tailSize)
                return true;

        //Top left
        if(map.getField((9-playerIndexY)-1,playerIndexX-1)==1)
            if(position.x<=(playerIndexX)*tailSize+offset && position.y+height>=(playerIndexY+1)*tailSize)
                return true;

        //Top right
        if(map.getField((9-playerIndexY)-1,playerIndexX+1)==1)
            if(position.x+width>=(playerIndexX+1)*tailSize+offset && position.y+height>=(playerIndexY+1)*tailSize)
                return true;
        //Left
        if(map.getField((9-playerIndexY),playerIndexX-1)==1)
            if(position.x<=(playerIndexX)*tailSize+offset)
        return true;
        //Right
        if(map.getField((9-playerIndexY),playerIndexX+1)==1)
            if(position.x+width>=(playerIndexX+1)*tailSize+offset)
                return true;
        //Bottom center
        if(map.getField((9-playerIndexY)+1,playerIndexX)==1)
            if(position.y<=(playerIndexY)*tailSize)
                return true;
        //Bottom left
        if(map.getField((9-playerIndexY)+1,playerIndexX-1)==1)
            if(position.x<=(playerIndexX)*tailSize+offset && position.y<=(playerIndexY)*tailSize)
                return true;
        //Bottom right
        if(map.getField((9-playerIndexY)+1,playerIndexX+1)==1)
            if(position.x+width>=(playerIndexX+1)*tailSize+offset && position.y<=(playerIndexY)*tailSize)
                return true;


        return false;
    }
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
