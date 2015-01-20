package warehouse;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import warehouse.dummies.*;

/**
 * <!--INITIAL CONFIGURATION CLASS-->
 * <p>
 * Responsible for creating and/or loading the initial configuration file used
 * by the {@link WarehouseAgent}.
 * </p>
 * <b>Attributes:</b>
 * <ul>
 * <li><i>warehouse:</i> A Warehouse type object used when reading the XML
 * configuration file.</li>
 * </ul>
 * 
 * @author [DNA] Diego, Nicolas, Argentina
 */
public class InitConfig {

	Warehouse warehouse;
	int orders;
	int robots;
	int shelves;
	int pickers;

	/**
	 * <!--ORDER AGENT CLASS-->
	 * <p>
	 * Creates and reads a configuration file used by the {@link WarehouseAgent}
	 * .
	 * </p>
	 * 
	 * @param xml
	 *            : xml file to be written.
	 * @param orders
	 *            : Amount of orders to be generated.
	 * @param robots
	 *            : Amount of robots to be generated.
	 * @param shelves
	 *            : Amount of shelves to be generated.
	 * @param pickers
	 *            : Amount of pickers to be generated.
	 * @param maxOrder
	 *            : Maximum amount of pieces required of each part required in
	 *            an order.
	 * @param maxStock
	 *            : Maximum amount of pieces available in a shelf.
	 * @param randOrder
	 *            : if set as true, it subtracts a random number from maxOrder.
	 * @param randStock
	 *            : if set as true, it subtracts a random number from maxStock.
	 * 
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	void createXML(String xml, int orders, int robots, int shelves,
			int pickers, int maxOrder, int maxStock, boolean randOrder,
			boolean randStock) {
		Warehouse wh = new Warehouse();

		// Creating Orders
		if (orders > 0) {
			this.orders = orders;
			if (maxOrder <= 0) {
				maxOrder = 4;
			}
			Orders x = new Orders(orders, maxOrder, randOrder);
			wh.setOrders(x);
		}
		if (robots > 0) {
			this.robots = robots;
			Robots y = new Robots(robots);
			wh.setRobots(y);
		}
		if (shelves > 0) {
			this.shelves = shelves;
			if (maxStock >= 0) {
				maxStock = 100;
			}
			Shelves z = new Shelves(shelves, maxStock, randStock);
			wh.setShelves(z);
		}
		if (pickers > 0) {
			this.pickers = pickers;
			Pickers w = new Pickers(pickers);
			wh.setPickers(w);
		}
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			File XMLfile = new File("conf/warehouse/" + xml);
			jaxbMarshaller.marshal(wh, XMLfile);
			readXML(xml);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <!--ORDER AGENT CLASS-->
	 * <p>
	 * Reads a default configuration file called kiva5.config.xml. Configuration
	 * files must be stored in the folder conf/warehouse/
	 * </p>
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	void readXML() {
		try {
			// create JAXB context and initializing Marshaller
			JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			// specify the location and name of xml file to be read
			File XMLfile = new File("conf/warehouse/kiva5.config.xml");

			// this will create Java object - warehouse from the XML file
			this.warehouse = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);

		} catch (JAXBException e) {
			// some exception occured
			e.printStackTrace();
		}
	}

	/**
	 * Read a configuration file.
	 * 
	 * Configuration files must be stored in the folder conf/warehouse/
	 * 
	 * @param xml
	 *            name of the xml file to be read. e.g. "kiva.config.xml"
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	void readXML(String xml) {
		try {

			// create JAXB context and initializing Marshaller
			JAXBContext jaxbContext = JAXBContext.newInstance(Warehouse.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			// specify the location and name of xml file to be read
			File XMLfile = new File("conf/warehouse/" + xml);

			// this will create Java object - warehouse from the XML file
			this.warehouse = (Warehouse) jaxbUnmarshaller.unmarshal(XMLfile);
			try{
				this.orders = warehouse.getOrders().getOrderList().size();
			}catch(Exception e){
				this.orders = 0;
			}
			try{
				this.robots = warehouse.getRobots().getRobotList().size();
			}catch(Exception e){
				this.robots = 0;
			}
			try{
				this.shelves = warehouse.getShelves().getShelfList().size();
			}catch(Exception e){
				this.shelves = 0;
			}
			try{
				this.pickers = warehouse.getPickers().getPickerList().size();
			}catch(Exception e){
				this.pickers = 0;
			}

		} catch (JAXBException e) {
			// some exception occured
			e.printStackTrace();
		}
	}

	/**
	 * Returns an array of Objects containing the arguments required to
	 * construct all Order Agents found in the configuration file. Each element
	 * in orderArgs ArrayList is composed of an Object[] args.
	 * 
	 * @return an ArrayList containing the required arguments for each
	 *         OrderAgent.The first argument contains a HashMap <String,Integer>
	 *         with the part list the second contains the order uid.
	 */
	ArrayList<Object[]> getOrderArgs() {
		ArrayList<Object[]> orderArgs = new ArrayList<Object[]>();		
		if (this.orders > 0) {
			Orders orders = this.warehouse.getOrders();
			ArrayList<Order> ol = orders.getOrderList();
			for (Order o : ol) {
				Object[] args = new Object[2];
				args[0] = o.getPartList();
				args[1] = o.getUID();
				orderArgs.add(args);
			}
		}
		return orderArgs;
	}

	/**
	 * Returns an array of Objects containing the arguments required to
	 * construct all Shelf Agents found in the configuration file. Each element
	 * in shelfArgs ArrayList is composed of an Object[] args.
	 * 
	 * @return an ArrayList containing the required arguments for each Shelf
	 *         Agent.The first argument contains a HashMap <String,Integer> with
	 *         the inventory, the second contains the shelf uid.
	 */

	ArrayList<Object[]> getShelfArgs() {
		ArrayList<Object[]> shelfArgs = new ArrayList<Object[]>();
		if (this.shelves > 0) {
			Shelves shelves = this.warehouse.getShelves();
			ArrayList<Shelf> sl = shelves.getShelfList();
			for (Shelf s : sl) {
				Object[] args = new Object[2];
				args[0] = s.getPartList();
				args[1] = s.getUID();
				shelfArgs.add(args);
			}
		}
		return shelfArgs;
	}

	/**
	 * Returns an array of Objects containing the arguments required to
	 * construct all Robot Agents found in the configuration file. Each element
	 * in robotArgs ArrayList is composed of an Object[] args.
	 * 
	 * @return an ArrayList containing the required arguments for each Robot
	 *         Agent.The first argument contains the robot uid.
	 */

	ArrayList<Object[]> getRobotArgs() {
		ArrayList<Object[]> robotArgs = new ArrayList<Object[]>();
		if (this.robots > 0) {
			Robots robots = this.warehouse.getRobots();
			ArrayList<Robot> rl = robots.getRobotList();
			for (Robot r : rl) {
				Object[] args = new Object[1];
				args[0] = r.getUID();
				robotArgs.add(args);
			}
		}
		return robotArgs;
	}

	/**
	 * Returns an array of Objects containing the arguments required to
	 * construct all Picker Agents found in the configuration file. Each element
	 * in pickArgs ArrayList is composed of an Object[] args.
	 * 
	 * @return an ArrayList containing the required arguments for each Picker
	 *         Agent.The first argument contains the picker uid.
	 */

	ArrayList<Object[]> getPickerArgs() {
		ArrayList<Object[]> pickerArgs = new ArrayList<Object[]>();

		if (this.pickers > 0) {
			Pickers pickers = this.warehouse.getPickers();
			ArrayList<Picker> rl = pickers.getPickerList();
			for (Picker r : rl) {
				Object[] args = new Object[1];
				args[0] = r.getUID();
				pickerArgs.add(args);
			}
		}
		return pickerArgs;
	}

}
