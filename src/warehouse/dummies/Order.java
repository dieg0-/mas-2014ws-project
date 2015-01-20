package warehouse.dummies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "warehouse.dummies.Orders")
@XmlType(propOrder = { "UID", "products" })
public class Order {
	String ordNum;
	HashMap<String, Integer> partList;
	private ArrayList<Product> productList;

	Order() {
		// Empty constructor
	}

	public Order(String ordNum, int max, boolean rand) {
		this.ordNum = ordNum;
		productList = new ArrayList<Product>();

		partList = new HashMap<String, Integer>();
		partList.put("motor", max);
		partList.put("base", max);
		partList.put("arms", max);
		partList.put("wires", max);
		partList.put("esc", max);
		partList.put("nazam", max);
		partList.put("rx", max);
		partList.put("gcu", max);
		partList.put("pmu", max);
		partList.put("iosd", max);
		partList.put("cables", max);
		partList.put("landinggear", max);
		partList.put("imu", max);
		partList.put("globalmount", max);
		partList.put("vtx", max);
		partList.put("gimbal", max);
		partList.put("cover", max);
		partList.put("blade", max);

		for (Map.Entry<String, Integer> entry : partList.entrySet()) {
			int a;
			if (rand == true) {
				Random rnd = new Random();
				a = rnd.nextInt(max);
			} else {
				a = 0;
			}
			partList.put(entry.getKey(), max-a);
			Product m = new Product(entry.getKey(), entry.getValue());
			productList.add(m);
		}

		// System.out.println(productList.size());
	}

	public String getUID() {
		return ordNum;
	}

	@XmlElement(name = "uid")
	public void setUID(String on) {
		this.ordNum = on;
	}

	@XmlElementWrapper(name = "products")
	@XmlElement(name = "product")
	public void setProducts(ArrayList<Product> pl) {
		this.productList = pl;
	}

	public ArrayList<Product> getProducts() {
		return productList;
	}

	public HashMap<String, Integer> getPartList() {
		this.partList = new HashMap<String, Integer>();
		for (Product p : productList) {
			partList.put(p.getName(), p.getQuantity());
		}
		// System.out.println("Product list:"+productList.size());
		// System.out.println("Part list: "+partList.size());
		return this.partList;
	}

	@Override
	public String toString() {
		String result = "-------------\nOrder " + ordNum + "\n"
				+ printProducts();
		return result;
	}

	String printProducts() {
		String result = "";
		for (Product p : productList) {
			result = result.concat(p.toString() + "\n");
			// System.out.println(result);
		}
		result = result.concat("-------------");
		// System.out.println(result);
		return result;
	}

}
