package physicsEngine;

import java.util.ArrayList;

import objects.PlanetBody;

/* Contains static methods for the computation needed by the physics engine.
 */
public class PhysicsCalc {
	
	public static int gScale = 1000;
	public static double coeffRestitution = 1.0;
	
	public static final int TIME_FRAME = 10;
	public static final int GRAV_MIN = 500;
	public static final int GRAV_MAX = 5000;
	public static final double REST_MIN = 0.1;
	public static final double REX_MAX = 1.4;
	
	public static void CalculateForces(ArrayList<PlanetBody> bodyList) {
		// Reset current system state.
		for (PlanetBody body : bodyList)
			body.setAcceleration(new double[] {0,0});		
		
		for (int i = 0; i < bodyList.size(); i++) {
			PlanetBody sourceBody = bodyList.get(i);
			for (int j = i+1; j < bodyList.size(); j++) {
				PlanetBody targetBody = bodyList.get(j);
				// Here we use F=ma and F = G*m1*m2/r^2. We find acceleration of source body depending on others.
				double horizDistance = (targetBody.getCenter()[0]-sourceBody.getCenter()[0]);
				double vertDistance = (targetBody.getCenter()[1]-sourceBody.getCenter()[1]);
				double distance = Math.sqrt(horizDistance*horizDistance + vertDistance*vertDistance);
					
				double sourceAcceleration = gScale * calculateMass(targetBody) / (distance * distance);
				double targetAcceleration = - gScale * calculateMass(sourceBody) / (distance * distance);
				
				// Update the acceleration of the source body.
				double[] currentSourceA = sourceBody.getAcceleration();
				double[] currentTargetA = targetBody.getAcceleration();
				sourceBody.setAcceleration(new double[] {currentSourceA[0] + sourceAcceleration*horizDistance/distance, currentSourceA[1] + sourceAcceleration*vertDistance/distance});
				targetBody.setAcceleration(new double[] {currentTargetA[0] + targetAcceleration*horizDistance/distance, currentTargetA[1] + targetAcceleration*vertDistance/distance});
				
				
			}
		}
		
		// Now we find the colliding bodies, to update velocities and resultant forces.
		for (int i = 0; i < bodyList.size(); i++) {
			PlanetBody sourceBody = bodyList.get(i);
			for (int j = i+1; j < bodyList.size(); j++) {
				PlanetBody targetBody = bodyList.get(j);
				double horizDistance = (targetBody.getCenter()[0]-sourceBody.getCenter()[0]);
				double vertDistance = (targetBody.getCenter()[1]-sourceBody.getCenter()[1]);
				double distance = Math.sqrt(horizDistance*horizDistance + vertDistance*vertDistance);
				
				if (distance <= sourceBody.getRadius() + targetBody.getRadius()) {
					CalculatePostCollisionMotion(sourceBody, targetBody);
				}
				
			}
		}
				
	}
	
	public static void CalculatePostCollisionMotion(PlanetBody b1, PlanetBody b2) {
		/* Here conservation of momentum and change in kinetic energy is used 
		 * (with a coefficient of restitution).
		 * We decompose velocity into normal and tangential components, and resolve from there.
		 */
		double[] v1;
		double[] v2;
		
		double m1 = calculateMass(b1);
		double m2 = calculateMass(b2);
		double[] u1 = b1.getVelocity();
		double[] u2 = b2.getVelocity();
		
		double distance = Math.sqrt(Math.pow(b1.getCenter()[0]-b2.getCenter()[0], 2)+Math.pow(b1.getCenter()[1]-b2.getCenter()[1], 2));
		double[] normalVec = new double[] {(b2.getCenter()[0]-b1.getCenter()[0])/distance,(b2.getCenter()[1]-b1.getCenter()[1])/distance};
		double[] tangentVec = new double[] {-normalVec[1],normalVec[0]};
		
		// We resolve into normal and tangent components by taking dot products.
		double u1Normal = normalVec[0]*u1[0] + normalVec[1]*u1[1];
		double u2Normal = normalVec[0]*u2[0] + normalVec[1]*u2[1];
		double u1Tangent = tangentVec[0]*u1[0] + tangentVec[1]*u1[1];
		double u2Tangent = tangentVec[0]*u2[0] + tangentVec[1]*u2[1];
		
		double v1Normal = (m1*u1Normal+m2*u2Normal+m2*coeffRestitution*(u2Normal-u1Normal))/(m1+m2);
		double v2Normal = (m1*u1Normal+m2*u2Normal+m1*coeffRestitution*(u1Normal-u2Normal))/(m1+m2);
		
		v1 = new double[] {v1Normal*normalVec[0] + u1Tangent*tangentVec[0],v1Normal*normalVec[1] + u1Tangent*tangentVec[1]};
		v2 = new double[] {v2Normal*normalVec[0] + u2Tangent*tangentVec[0],v2Normal*normalVec[1] + u2Tangent*tangentVec[1]};
		
		b1.setVelocity(v1);
		b2.setVelocity(v2);
		
		// Update acceleration, by using resultant force.
		double[] a1 = b1.getAcceleration();
		double[] a2 = b2.getAcceleration();
		
		double a1Normal = normalVec[0]*a1[0]+normalVec[1]*a1[1];
		double a2Normal = normalVec[0]*a2[0]+normalVec[1]*a2[1];
		
		b1.setAcceleration(new double[] {a1[0]-a1Normal*normalVec[0],a1[1]-a1Normal*normalVec[1]});
		b2.setAcceleration(new double[] {a2[0]-a2Normal*normalVec[0],a2[1]-a2Normal*normalVec[1]});
	}
		
	static double calculateMass(PlanetBody body) {
		double radius = body.getRadius();
		
		return Math.PI * radius * radius;

	}
	
	static void updateAcceleration(PlanetBody body) {
	}

}
