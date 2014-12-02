package warehouse;


import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;




import warehouse.dummies.Orders;
//import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import warehouse.dummies.Warehouse;
import warehouse.dummies.Order;

public class InitConfig {
	
	Warehouse warehouse;
	
	void createXML(){
		Warehouse wh = new Warehouse();
		
		//Creating Orders
		Orders x = new Orders(10);
		wh.setOrders(x);
		
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			File XMLfile = new File("conf/warehouse/kiva3.config.xml");
			jaxbMarshaller.marshal(wh, XMLfile);
			//jaxbMarshaller.marshal(wh, System.out);
			System.out.println("Configuration created");

		}catch(JAXBException e){
			e.printStackTrace();
		}
	}
	/**
	 * Reads a default configuration file called kiva3.config.xml.
	 * Configuration files must be stored in the folder conf/warehouse/
	 */	
	
	void readXML(){
		try {

			   // create JAXB context and initializing Marshaller
			   JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);

			   Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			   // specify the location and name of xml file to be read
			   File XMLfile = new File("conf/warehouse/kiva3.config.xml");

			   // this will create Java object - warehouse from the XML file
			   this.warehouse = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);
			   
			   System.out.println("Configuration read succesfuly.");
			   
			  } catch (JAXBException e) {
			   // some exception occured
			   e.printStackTrace();
			  }

	}
	
	/**
	 * Read a configuration file.
	 * 
	 * Configuration files must be stored in the folder conf/warehouse/
	 * @param xml name of the xml file to be read. e.g. "kiva.config.xml"
	 */	
	void readXML(String xml){
		try {

			   // create JAXB context and initializing Marshaller
			   JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);

			   Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			   // specify the location and name of xml file to be read
			   File XMLfile = new File("conf/warehouse/"+xml);

			   // this will create Java object - warehouse from the XML file
			   this.warehouse = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);
			   
			   
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
	/**
	 * Returns an array of Objects containing the arguments required to construct all OrderAgent
	 * found in the config file. Each element in orderArgs is composed of Object[] args.
	 * @return orderArgs ArrayList<Object[]> orderArgs
	 * @return args[0] HashMap<String,Integer> partList
	 * @return args[1] int order uid
	 */
	ArrayList<Object[]> getOrderArgs(){
		
		Orders orders = this.warehouse.getOrders();
		ArrayList<Order> ol = orders.getOrderList();
		ArrayList<Object[]> orderArgs = new ArrayList<Object[]>();
		
		//System.out.println(ol.toString());
	
		for(Order o:ol){
			Object[] args = new Object[ol.size()];
			args[0]=o.getPartList();
			args[1]=o.getUID();	
			orderArgs.add(args);
		}		
		return orderArgs;
		
	}
	
	

}


