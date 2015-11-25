package objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import physicsEngine.PhysicsCalc;

public class PlanetBody {
	// arrow for displaying initial velocity.
	Illustrator arrow;
	private boolean withArrow;
	
	private int radius;
	private double[] center;
	private double[] velocity = new double[] {0,0};
	private double[] acceleration = new double[] {0,0};
	
	public Illustrator getArrow() {
		return arrow;
	}

	public void setArrow(Illustrator arrow) {
		this.arrow = arrow;
	}

	public boolean isWithArrow() {
		return withArrow;
	}

	public void setWithArrow(boolean withArrow) {
		this.withArrow = withArrow;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public double[] getCenter() {
		return center;
	}

	public void setCenter(double[] center) {
		this.center = center;
	}

	public double[] getVelocity() {
		return velocity;
	}

	public void setVelocity(double[] velocity) {
		this.velocity = velocity;
	}

	public double[] getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double[] acceleration) {
		this.acceleration = acceleration;
	}

	public PlanetBody(int radius, double[] center) {
		this.radius = radius;
		this.center = center;
	}

	public void paintBody(Graphics g) {
		
        g.setColor(Color.GRAY);
        g.fillOval((int)(center[0]-radius),(int)(center[1]-radius),2*radius,2*radius);
        g.setColor(Color.BLACK);
        g.drawOval((int)(center[0]-radius),(int)(center[1]-radius),2*radius,2*radius);	
	}
	
	public void updateMovement() {
		int timeFrame = PhysicsCalc.TIME_FRAME;
		
		center[0] += velocity[0]*timeFrame/1000;
		center[1] += velocity[1]*timeFrame/1000;
		
		velocity[0] += acceleration[0]*timeFrame/1000;
		velocity[1] += acceleration[1]*timeFrame/1000;
	}
	
	public boolean checkInBoundary(int X,int Y) {
		// For checking if mouse is inside body.
		if (Math.sqrt((X-center[0])*(X-center[0])+(Y-center[1])*(Y-center[1])) <= radius) {
			return true;
		}	
		
		return false;
	}
	
}

