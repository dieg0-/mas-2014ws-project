package warehouse.dummies;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import warehouse.dummies.Orders;

@XmlRootElement(name = "warehouse")
@XmlType(propOrder = { "orders", "robots", "shelves" , "pickers"})
public class Warehouse {
	Orders orderList;
	Robots robotList;
	Shelves shelfList;
	Pickers pickerList;

	// @XmlElementWrapper(name = "orders")
	// @XmlElement(name = "order")
	@XmlElement(name = "orders")
	public Orders getOrders() {
		return orderList;
	}

	public void setOrders(Orders ol) {
		this.orderList = ol;
	}

	@XmlElement(name = "robots")
	public Robots getRobots() {
		return robotList;
	}

	public void setRobots(Robots rl) {
		this.robotList = rl;
	}

	@XmlElement(name = "shelves")
	public Shelves getShelves() {
		return shelfList;
	}

	public void setShelves(Shelves sl) {
		this.shelfList = sl;
	}

	@XmlElement(name = "pickers")
	public Pickers getPickers() {
		return pickerList;
	}

	public void setPickers(Pickers pl) {
		this.pickerList = pl;
	}

}
