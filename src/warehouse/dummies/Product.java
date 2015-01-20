package warehouse.dummies;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Order")
public class Product {
	String name;
	int quantity;

	public Product() {

	}

	public Product(String name, int qty) {
		this.name = name;
		this.quantity = qty;
	}

	@XmlElement
	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public void setQuantity(int qty) {
		this.quantity = qty;
	}

	public String getName() {
		return name;
	}

	public int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return name + ": " + quantity;
	}

}
