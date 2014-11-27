package utilities;

import java.io.Serializable;
import java.util.Random;

@SuppressWarnings("serial")
public class Pose implements Serializable {
	
	private double x;
	private double y;
	private double[] mapSize = {100.0, 100.0};
	
	public Pose(){
		
	}
	
	public Pose(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	
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
	
	public double distance(Pose other){
		return Math.sqrt(Math.pow((this. x - other.getX()), 2) + 
				Math.pow((this. y - other.getY()), 2));
	}
	
	public String parsePose() {
		String pose = String.format("%.2f,%.2f", this.x, this.y);
		return pose;
	}
	

}
