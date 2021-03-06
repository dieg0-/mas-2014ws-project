package warehouse.dummies;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Warehouse")
public class Orders {
	ArrayList<Order> orderList;
	int orders;

	public Orders() {

	}

	public Orders(int n,int max, boolean rand) {
		DecimalFormat uidFormat = new DecimalFormat("0000");
		orderList = new ArrayList<Order>();
		for (int i = 0; i < n; i++) {
			Order o = new Order(uidFormat.format(i + 1), max, rand);
			orderList.add(o);
		}
	}

	@XmlElement(name = "order")
	public ArrayList<Order> getOrderList() {
		return orderList;
	}

	public void setOrderList(ArrayList<Order> ol) {
		this.orderList = ol;
	}

}
