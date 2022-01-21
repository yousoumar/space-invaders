package game;

import java.awt.Rectangle;

public abstract class Entity {


	protected float	x;
	protected float	y;
	protected Sprite	sprite;
	protected float	dx;
	protected float	dy;
	private Rectangle	me	= new Rectangle();
	private Rectangle	him	= new Rectangle();

	protected Entity(Sprite sprite, int x, int y) {
		this.sprite = sprite;
		this.x = x;
		this.y = y;
	}

	public void move(long delta) {
		x += (delta * dx) / 1000;
		y += (delta * dy) / 1000;
	}



	public void setHorizontalMovement(float dx) {
		this.dx = dx;
	}

	public void setVerticalMovement(float dy) {
		this.dy = dy;
	}

	
	public float getHorizontalMovement() {
		return dx;
	}

	public float getVerticalMovement() {
		return dy;
	}


	public void draw() {
		sprite.draw((int) x, (int) y);
	}

	public void doLogic() {
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public boolean collidesWith(Entity other) {
		me.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
		him.setBounds((int) other.x, (int) other.y, other.sprite.getWidth(), other.sprite.getHeight());

		return me.intersects(him);
	}

	public abstract void collidedWith(Entity other);
}