package warehouse.dummies;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import warehouse.dummies.Order;

@XmlRootElement
@XmlType(propOrder = {"orderList"})
public class Warehouse {
	
	
	private ArrayList<Order> orderList;
	
		public ArrayList<Order> getOrderList(){
			return orderList;		
		}
		
		@XmlElementWrapper(name = "orders")
		@XmlElement(name = "order")
		public void setOrderList(ArrayList<Order> ol){
			this.orderList = ol;
		}
	
}
