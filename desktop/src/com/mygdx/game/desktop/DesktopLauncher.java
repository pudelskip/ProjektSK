package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MyGdxGame;


public class DesktopLauncher {
	public static void main (String[] arg) {

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=MyGdxGame.WIDTH;
		config.height = MyGdxGame.HEIGHT;
		config.title = "Bomber SK Project";
		new LwjglApplication(new MyGdxGame(), config);



		/**LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		 final ApplicationListener application = new MyGdxGame();
		 config.width=MyGdxGame.WIDTH;
		 config.height = MyGdxGame.HEIGHT;

		 final LwjglFrame frame = new LwjglFrame(application, config);

		 frame.addComponentListener(new ComponentAdapter() {
		@Override
		public void componentMoved(ComponentEvent e) {
		// somehow pause your game here
		//MyGame.getInstance().pauseGame();
		}
		});*/
	}
}
