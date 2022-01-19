
package game;

import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

public class Game {


	private String WINDOW_TITLE	= "Space invaders";
	private int	width = 800;
	private int	height = 600;
	private TextureLoader textureLoader;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<Entity> removeList = new ArrayList<Entity>();
	private ShipEntity ship;
	private ShotEntity[] shots;
	private Sprite message;
	private Sprite pressAnyKey;
	private Sprite youWin;
	private Sprite gotYou;
	private int	shotIndex;
	private float moveSpeed	= 300;
	private long lastFire;
	private long firingInterval	= 1000;
	private int	alienCount;
	private boolean	waitingForKeyPress = true;
	private boolean	logicRequiredThisLoop;
	private long lastLoopTime = getTime();
	private boolean	fireHasBeenReleased;
	private long lastFpsTime;
	private int	fps;
	private static long	timerTicksPerSecond	= Sys.getTimerResolution();
	public static boolean gameRunning = true;
	private boolean	fullscreen;
	private int	mouseX;
	private static boolean isApplication;

	public Game(boolean fullscreen) {
		this.fullscreen = fullscreen;
		initialize();
	}

	public static long getTime() {
		return (Sys.getTime() * 1000) / timerTicksPerSecond;
	}

	public static void sleep(long duration) {
		try {
			Thread.sleep((duration * timerTicksPerSecond) / 1000);
		} catch (InterruptedException inte) {
		}
	}

	public void initialize() {
		try {
			setDisplayMode();
			Display.setTitle(WINDOW_TITLE);
			Display.setFullscreen(fullscreen);
			Display.create();

			if (isApplication) {
				Mouse.setGrabbed(true);
			}

			glEnable(GL_TEXTURE_2D);

			glDisable(GL_DEPTH_TEST);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			glOrtho(0, width, height, 0, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glViewport(0, 0, width, height);

			textureLoader = new TextureLoader();

		} catch (LWJGLException le) {
			System.out.println("Game exiting - exception in initialization:");
			le.printStackTrace();
			Game.gameRunning = false;

      		return;
		}

		gotYou = getSprite("lose.gif");
		pressAnyKey = getSprite("pressanykey.gif");
		youWin = getSprite("win.gif");

		message = pressAnyKey;

		shots = new ShotEntity[5];

		for (int i = 0; i < shots.length; i++) {
			shots[i] = new ShotEntity(this, "shot.gif", 0, 0);
		}

		startGame();
	}

	private boolean setDisplayMode() {
		try {
			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, 60, 60);

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
			  "width=" + width,
			  "height=" + height,
			  "freq=" + 60,
			  "bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
		  	});

		  	return true;
		} catch (Exception e) {
			e.printStackTrace();
		  	System.out.println("Unable to enter fullscreen, continuing in windowed mode");
		}

		return false;
	}


	private void startGame() {

		entities.clear();
		initEntities();
	}

	private void initEntities() {

		ship = new ShipEntity(this, "ship.gif", 370, 550);
		entities.add(ship);


		alienCount = 0;
		for (int row = 0; row < 5; row++) {
			for (int x = 0; x < 12; x++) {
				Entity alien = new AlienEntity(this, 100 + (x * 50), (50) + row * 30);
				entities.add(alien);
				alienCount++;
			}
		}
	}


	public void updateLogic() {
		logicRequiredThisLoop = true;
	}

	public void removeEntity(Entity entity) {
		removeList.add(entity);
	}

	public void notifyDeath() {
		
		message = gotYou;
		waitingForKeyPress = true;
	}

	public void notifyWin() {
		message = youWin;
		waitingForKeyPress = true;
	
	}


	public void notifyAlienKilled() {

		alienCount--;

		if (alienCount == 0) {
			notifyWin();
		}


		for ( Entity entity : entities ) {
			if ( entity instanceof AlienEntity ) {

				entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02f);
			}
		}


	}


	public void tryToFire() {

		if (System.currentTimeMillis() - lastFire < firingInterval) {
			return;
		}

		lastFire = System.currentTimeMillis();
		ShotEntity shot = shots[shotIndex++ % shots.length];
		shot.reinitialize(ship.getX() + 10, ship.getY() - 30);
		entities.add(shot);


	}

	private void gameLoop() {
		while (Game.gameRunning) {

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();


			frameRendering();


			Display.update();
		}


		Display.destroy();
	}


	public void frameRendering() {

		Display.sync(60);

		long delta = getTime() - lastLoopTime;
		lastLoopTime = getTime();
		lastFpsTime += delta;
		fps++;


		if (lastFpsTime >= 1000) {
			Display.setTitle(WINDOW_TITLE + " (FPS: " + fps + ")");
			lastFpsTime = 0;
			fps = 0;
		}


		if (!waitingForKeyPress) {
			for ( Entity entity : entities ) {
				entity.move(delta);
			}
		}


		for ( Entity entity : entities ) {
			entity.draw();
		}


		for (int p = 0; p < entities.size(); p++) {
			for (int s = p + 1; s < entities.size(); s++) {
				Entity me = entities.get(p);
				Entity him = entities.get(s);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}


		entities.removeAll(removeList);
		removeList.clear();


		if (logicRequiredThisLoop) {
			for ( Entity entity : entities ) {
				entity.doLogic();
			}

			logicRequiredThisLoop = false;
		}

		if (waitingForKeyPress) {
			message.draw(325, 250);
		}

		ship.setHorizontalMovement(0);


    mouseX = Mouse.getDX();

		boolean leftPressed   = hasInput(Keyboard.KEY_LEFT);
		boolean rightPressed  = hasInput(Keyboard.KEY_RIGHT);
		boolean firePressed   = hasInput(Keyboard.KEY_SPACE);

		if (!waitingForKeyPress) {
			if ((leftPressed) && (!rightPressed)) {
				ship.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				ship.setHorizontalMovement(moveSpeed);
			}

		
			if (firePressed) {
				tryToFire();
			}
		} else {
			if (!firePressed) {
				fireHasBeenReleased = true;
			}
			if ((firePressed) && (fireHasBeenReleased) ) {
				waitingForKeyPress = false;
				fireHasBeenReleased = false;
				startGame();
				
			}
		}


		if ((Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) && isApplication) {
			Game.gameRunning = false;
		}
	}

	private boolean hasInput(int direction) {
    switch(direction) {
    	case Keyboard.KEY_LEFT:
        return
          Keyboard.isKeyDown(Keyboard.KEY_LEFT) ||
          mouseX < 0;

      case Keyboard.KEY_RIGHT:
        return
          Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ||
          mouseX > 0;

      case Keyboard.KEY_SPACE:
        return
          Keyboard.isKeyDown(Keyboard.KEY_SPACE) ||
          Mouse.isButtonDown(0);
    }
		return false;
	}

	public static void main(String argv[]) {
		isApplication = true;
		System.out.println("Use -fullscreen for fullscreen mode");
		new Game((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();
		System.exit(0);
	}


	public void execute() {
		gameLoop();
	}

	
	public Sprite getSprite(String ref) {
		return new Sprite(textureLoader, ref);
	}
}
