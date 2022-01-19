package game;

public class ShotEntity extends Entity {


	private static final int	TOP_BORDER	= -100;
	private float	moveSpeed		= -300;
	private Game game;
	private boolean used;


	public ShotEntity(Game game, String sprite, int x, int y) {
		super(game.getSprite(sprite), x, y);

		this.game = game;
		dy = moveSpeed;
	}

	public void reinitialize(int x, int y) {
		this.x = x;
		this.y = y;
		used = false;
	}


	public void move(long delta) {
	
		super.move(delta);


		if (y < TOP_BORDER) {
			game.removeEntity(this);
		}
	}

	public void collidedWith(Entity other) {

		if (used) {
			return;
		}


		if (other instanceof AlienEntity) {

			game.removeEntity(this);
			game.removeEntity(other);

			game.notifyAlienKilled();
			used = true;
		}
	}
}