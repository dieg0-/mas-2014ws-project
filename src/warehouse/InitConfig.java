package warehouse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


//import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import warehouse.dummies.Warehouse;
import warehouse.dummies.Order;
import warehouse.dummies.Product;

public class InitConfig {
	
	void createXML(){
		Warehouse wh = new Warehouse();
		
		//Creating Orders
		ArrayList<Order> orderlist = new ArrayList();
		Order o1 = new Order("0001");
		Order o2 = new Order("0002");
		orderlist.add(o1);
		orderlist.add(o2);
		
		wh.setOrderList(orderlist);
		
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			File XMLfile = new File("conf/warehouse/kiva2.config.xml");
			jaxbMarshaller.marshal(wh, XMLfile);
			//jaxbMarshaller.marshal(wh, System.out);
			System.out.println("Configuration created");

		}catch(JAXBException e){
			e.printStackTrace();
		}
	}
	
	void readXML(){
		try {

			   // create JAXB context and initializing Marshaller
			   JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);

			   Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			   // specify the location and name of xml file to be read
			   File XMLfile = new File("conf/warehouse/kiva2.config.xml");

			   // this will create Java object - warehouse from the XML file
			   Warehouse wh = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);
			   
			   ArrayList<Order> orderList = wh.getOrderList();
			   printOrders(orderList);
			   
			   
			  } catch (JAXBException e) {
			   // some exception occured
			   e.printStackTrace();
			  }

	}
	
	void printOrders(ArrayList<Order> ol){
		
		for(Order o:ol){
			System.out.println(o.toString());
		}
		
	}
	
	

}


