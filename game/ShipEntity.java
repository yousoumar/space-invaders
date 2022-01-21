package game;

public class ShipEntity extends Entity {
  
  
  private static final int  RIGHT_BORDER      = 750;
  private static final int  LEFT_BORDER       = 10;  
	private Game game;

	public ShipEntity(Game game,String ref,int x,int y) {
		super(game.getSprite(ref), x, y);
		
		this.game = game;
	}

	public void move(long delta) {
		if ((dx < 0) && (x < LEFT_BORDER)) {
			return;
		}

		if ((dx > 0) && (x > RIGHT_BORDER)) {
			return;
		}
		
		super.move(delta);
	}
	
	
	public void collidedWith(Entity other) {
	
		if (other instanceof AlienEntity) {
			game.notifyDeath();
		}
	}
}