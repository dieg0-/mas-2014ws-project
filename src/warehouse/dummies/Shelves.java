package warehouse.dummies;

/**
 * Created by Argen on 19.01.15.
 */

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Warehouse")
public class Shelves {
	ArrayList<Shelf> shelfList;
	int shelves;

	public Shelves() {

	}

	public Shelves(int n, int max, boolean rand) {
		DecimalFormat uidFormat = new DecimalFormat("0000");
		shelfList = new ArrayList<Shelf>();
		for (int i = 0; i < n; i++) {
			Shelf s = new Shelf(uidFormat.format(i + 1), max, rand);
			shelfList.add(s);
		}
	}

	@XmlElement(name = "shelf")
	public ArrayList<Shelf> getShelfList() {
		return shelfList;
	}

	public void setShelfList(ArrayList<Shelf> sl) {
		this.shelfList = sl;
	}
}
