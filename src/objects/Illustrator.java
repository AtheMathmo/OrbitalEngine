package objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

// Currently only arrows exist as illustrations, later it would be wise to use nested/abstract class structure,
public class Illustrator {
	private Polygon arrowHead = new Polygon();
	
	
	private int[] startPoint;
	private int[] endPoint;
	
	public int[] getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(int[] startPoint) {
		this.startPoint = startPoint;
	}

	public int[] getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(int[] endPoint) {
		this.endPoint = endPoint;
	}
	
	private void formArrowHead() {
		arrowHead.reset();
		int hLength = endPoint[0] - startPoint[0];
		int vLength = endPoint[1] - startPoint[1];
		double distance = Math.sqrt(hLength*hLength + vLength*vLength);
		
		arrowHead.addPoint(endPoint[0],endPoint[1]);
		arrowHead.addPoint(endPoint[0]-(int)(10.0*hLength/distance+4.0*vLength/distance),endPoint[1]-(int)(10.0*vLength/distance-4.0*hLength/distance));
		arrowHead.addPoint(endPoint[0]-(int)(10.0*hLength/distance+-4.0*vLength/distance),endPoint[1]-(int)(10.0*vLength/distance+4.0*hLength/distance));
	
	}

	public void drawArrow(Graphics g) {
		formArrowHead();
		g.setColor(Color.RED);
		
		g.fillPolygon(arrowHead);
		g.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1]);
	}
	
	public void moveArrow(int x, int y) {
		// To keep arrow attached to body when they are moved. (Could also allow independent moving later).
		arrowHead.translate(x-startPoint[0], y-startPoint[1]);
		
		endPoint[0] += x -startPoint[0];
		endPoint[1] += y -startPoint[1];
		
		startPoint[0] = x;
		startPoint[1] = y;
	}
	
	public int[] arrowRepaintArea() {
		// Calculates region that arrow could occupy. Primitive at the moment.
		int hDistance = Math.abs(endPoint[0]-startPoint[0]);
		int vDistance = Math.abs(endPoint[1]-startPoint[1]);
		// Numbers are chosen to include arrow head
		return new int[] {startPoint[0]-hDistance-4,startPoint[1]-vDistance-4,2*hDistance+8,2*vDistance+8};
	}
}
