package warehouse.dummies;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Warehouse")
public class Pickers {
	ArrayList<Picker> pickerList;
	int pickers;

	public Pickers() {

	}

	public Pickers(int n) {
		DecimalFormat uidFormat = new DecimalFormat("0000");
		pickerList = new ArrayList<Picker>();
		for (int i = 0; i < n; i++) {
			Picker p = new Picker(uidFormat.format(i + 1));
			pickerList.add(p);
		}
	}

	@XmlElement(name = "picker")
	public ArrayList<Picker> getPickerList() {
		return pickerList;
	}

	public void setPickerList(ArrayList<Picker> pl) {
		this.pickerList = pl;
	}

}
