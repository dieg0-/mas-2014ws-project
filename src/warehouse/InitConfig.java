package warehouse;


import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;



//import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import warehouse.dummies.Warehouse;
import warehouse.dummies.Order;

public class InitConfig {
	
	Warehouse warehouse;
	
	void createXML(){
		Warehouse wh = new Warehouse();
		DecimalFormat uidFormat = new DecimalFormat("0000");
		//Creating Orders
		ArrayList<Order> orderlist = new ArrayList<Order>();
		for (int i=0; i<10;i++){
			Order o = new Order(uidFormat.format(i+1));
			orderlist.add(o);
		}
		
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
			   this.warehouse = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);
			   
			   //ArrayList<Order> orderList = this.warehouse.getOrderList();
			   //printOrders(orderList);
			   
			   System.out.println("Configuration read succesfuly.");
			   
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
	
	ArrayList<Object[]> getOrders(){
		
		ArrayList<Order> ol = this.warehouse.getOrderList();
		ArrayList<Object[]> orderArgs = new ArrayList<Object[]>();
	
		for(Order o:ol){
			Object[] args = new Object[ol.size()];
			args[0]=o.getPartList();
			args[1]=o.getUID();	
			orderArgs.add(args);
		}		
		return orderArgs;
	}
	
	

}


