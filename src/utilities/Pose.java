package utilities;

import java.util.Random;

public class Pose {
	
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
	
	
	public void randomInit(){
		Random random = new Random();
		double rangeX = mapSize[0]; // - minimum (0)
		double scaledX = random.nextDouble() * rangeX;
		double randomX = scaledX; // + minimum (0)
		setX(randomX);
		double rangeY = mapSize[1]; // - minimum (0)
		double scaledY = random.nextDouble() * rangeY;
		double randomY = scaledY; // + minimum (0)
		setY(randomY);
		
		System.out.println("Shelf created at: (" + this.x + ", " + this.y + ").");
	}
	
	public double distance(Pose other){
		return Math.sqrt(Math.pow((this. x - other.getX()), 2) + 
				Math.pow((this. y - other.getY()), 2));
	}
	
	

}
