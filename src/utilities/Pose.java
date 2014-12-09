/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.0.d.n.
@since 27.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package utilities;

import java.io.Serializable;
import java.util.Random;

/**
 * <!--POSE CLASS-->
 * <p>Class for handling positions of the agents.</p>
 * <b>Attributes:</b>
 * <ul>
 * 	<li> <i>x:</i> x-coordinate of the position. </li>
 * 	<li> <i>y:</i> y-coordinate of the position. </li>
 *  <li> <i>mapSize:</i> size of the warehouse area, where the agents live. </li>
 * </ul> 
 * @author [DNA] Diego, Nicolas, Argentina
 *
 */
@SuppressWarnings("serial")
public class Pose implements Serializable {
	
	private double x;
	private double y;
	private double[] mapSize = {100.0, 100.0};
	
	public Pose() {
		
	}
	
	/**
	 * Constructor of the class. It receives the coordinates of a position.
	 * @param x		x-coordinate inside the map.
	 * @param y		y-coordinate inside the map.
	 */
	public Pose(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Getter for the x-coordinate.
	 * @return	x-coordinate of this instance.
	 */
	public double getX(){
		return x;
	}
	
	/**
	 * Getter for the y-coordinate.
	 * @return	y-coordinate of this instance.
	 */
	public double getY(){
		return y;
	}
	
	/**
	 * Setter of the x-coordinate
	 * @param x		x-value of the position.
	 */
	public void setX(double x){
		this.x = x;
	}
	
	/**
	 * Setter of the y-coordinate
	 * @param y		y-value of the position.
	 */
	public void setY(double y){
		this.y = y;
	}
	
	/**
	 * Initialize this instance with a random position inside the map size.
	 * @param verbose	if true, a message is printed with the random position.
	 */
	public void randomInit(boolean verbose){
		Random random = new Random();
		double rangeX = mapSize[0]; // - minimum (0)
		double scaledX = random.nextDouble() * rangeX;
		double randomX = scaledX; // + minimum (0)
		setX(randomX);
		double rangeY = mapSize[1]; // - minimum (0)
		double scaledY = random.nextDouble() * rangeY;
		double randomY = scaledY; // + minimum (0)
		setY(randomY);
		String pos = String.format("(%.2f, %.2f).", this.x, this.y);
		if (verbose) {
			System.out.println("Agent at: " + pos);
		}
	}
	
	/**
	 * Computes the Euclidean distance between this pose and other pose.
	 * @param other	the other pose to which the distance is measured.
	 * @return	the Euclidean distance between the poses.
	 */
	public double distance(Pose other){
		return Math.sqrt(Math.pow((this. x - other.getX()), 2) + 
				Math.pow((this. y - other.getY()), 2));
	}
	
	/**
	 * Parse the position of this instance to a String.
	 * @return the position of this instance.
	 */
	public String parsePose() {
		String pose = String.format("%.2f,%.2f", this.x, this.y);
		return pose;
	}
	
	/**
	 * Parse the position of this instance to an array of type Double.
	 * @return the position of this instance.
	 */
	public double[] poseToArray(){
		double aPose[] = {this.getX(), this.getY()};
		return aPose;
	}
	
	/**
	 * Parse a given array to an instance of this class.
	 * @param aPose	array with a position (x-value, y-value).
	 * @return	an instance of this class initialized with the given position.
	 */
	public Pose arrayToPose(double[] aPose){
		Pose pose = new Pose(aPose[0], aPose[1]);
		return pose;
	}
	

}
