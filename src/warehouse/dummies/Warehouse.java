package warehouse.dummies;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import warehouse.dummies.Orders;

@XmlRootElement(name="warehouse")
@XmlType(propOrder = {"orders"})
public class Warehouse {	
	Orders orderList;
	
	//@XmlElementWrapper(name = "orders")
	//@XmlElement(name = "order")
	@XmlElement(name = "orders")
	public Orders getOrders(){
		return orderList;		
	}
	
	public void setOrders(Orders ol){
		this.orderList = ol;
	}

}
