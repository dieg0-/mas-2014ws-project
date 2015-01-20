package warehouse.dummies;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Warehouse")
public class Robots {
	ArrayList<Robot> robotList;
	int robots;

	public Robots() {

	}

	public Robots(int n) {
		DecimalFormat uidFormat = new DecimalFormat("0000");
		robotList = new ArrayList<Robot>();
		for (int i = 0; i < n; i++) {
			Robot r = new Robot(uidFormat.format(i + 1));
			robotList.add(r);
		}
	}

	@XmlElement(name = "robot")
	public ArrayList<Robot> getRobotList() {
		return robotList;
	}

	public void setRobotList(ArrayList<Robot> rl) {
		this.robotList = rl;
	}

}
