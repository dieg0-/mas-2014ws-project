package warehouse.dummies;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "warehouse.dummies.Pickers")
@XmlType(propOrder = { "UID" })
public class Picker {
	
	String uid;

	Picker() {

	}

	Picker(String uid) {
		this.uid = uid;
	}
	
	public String getUID(){
		return this.uid;
	}
	
	@XmlElement(name="uid")
	public void setUID(String uid){
		this.uid = uid;
	}

}
