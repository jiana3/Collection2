
public class Rectangle {

	private double length;
	private double width;
	public double perimeter;
	public double area;
	
	public Rectangle() {
		length = 1;
		width = 1;
		area = length*width;
		perimeter = (length+width)*2;
	}

	public Rectangle(double length, double width) {
		this.length = length;
		this.width = width;
		area = length*width;
		perimeter = (length+width)*2;
	}
}
