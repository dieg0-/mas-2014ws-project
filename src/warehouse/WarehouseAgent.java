/**
COPYRIGHT NOTICE (C) 2015. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.0 
@since 20.01.2015 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package warehouse;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.text.DecimalFormat;
import java.util.*;
import station.RobotAgent;
import shelf.ShelfAgent;

/**
 * <!--WAREHOUSE AGENT CLASS-->
 * Warehouse agent deals with the initialization and launching of the whole
 * system. It creates XML configuration files, which are used to instantiate
 * the other agents. From the system point of view, it handles the order queue
 * and order queue status; i.e. how many orders has been completed, how many
 * are pending and how many are assigned.
 * <p>
 * <b>Attributes:</b>
 * <ul>
 * 	<li> <i>pendingOrders:</i> a list .... </li>
 *  <li> <i>completedOrders:</i> a list .... </li>
 * 	<li> <i>assignedOrders:</i> a list .... </li>
 * 	<li> <i>currentOrder:</i> counter for ... </li>
 *  <li> <i>config:</i> an instance of the class {@link InitConfig} which ....
 * </ul>
 * </p>
 * @author [DNA] Diego, Nicolas, Argentina
 */
public class WarehouseAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<String> pendingOrders;
	private ArrayList<String> completedOrders;
	private ArrayList<String> assignedOrders;
	int currentOrder;
	InitConfig config;

	/** 
	 * Agent initialization.
	 * <ol> 
	 * 	<li>Creates and/or reads a XML configuration file. </li> 
	 * 	<li>Initializes the order list status. </li>
	 * 	<li>Loads the initial behaviors. </li>
	 * </ol>
	 */
	protected void setup() {
		System.out.println(getLocalName() + ": Started.");
		// Load configuration file.
		config = new InitConfig();
		/* Create and read an XML file with the given agents (file, orders, shelfs, robots, pickers).
		 * 
		 */
		config.createXML("kiva.config.xml",100,10,3,2,7,30,true,true);
		System.out.println(getLocalName()+": Configuration created succesfuly.");
		//config.readXML("kiva5.config.xml");
		System.out.println(getLocalName()+": Configuration read succesfuly.");
		   
		pendingOrders = new ArrayList<String>();		
		assignedOrders = new ArrayList<String>();		
		completedOrders = new ArrayList<String>();		
		// Add behaviors
		addBehaviour(new initialOrders());
		addBehaviour(new CreateOrder());
		addBehaviour(new updateOrderLists());
		addBehaviour(new initialRobots());
		addBehaviour(new initialShelves());
		addBehaviour(new initialPickers());
		//System.out.println(getLocalName()+": Loaded behaviours");
	}

	/**
	 * <!--TAKEDOWN-->
	 * Safe delete of the agent.
	 */
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}
	
	
	/**
	 * <!--UPDATE ORDER LISTS BEHAVIOUR-->
	 * Behavior which updates the status of the order queue regarding of
	 * messages the {@link OrderAgent} sent. It just moves orders from one
	 * list to another.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> print out with the current status of the order queue.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	@SuppressWarnings("serial")
	public class updateOrderLists extends CyclicBehaviour {

		public void action() {
			MessageTemplate completedMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchOntology("Completed Order"));
			ACLMessage completedMsg = myAgent.receive(completedMT);
			
			MessageTemplate assignedMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("requestParts"));
			ACLMessage assignMsg = myAgent.receive(assignedMT);
			
			if (assignMsg !=null){
				String assignedOrder = assignMsg.getSender().getLocalName();
				pendingOrders.remove(assignedOrder);
				assignedOrders.add(assignedOrder);
				System.out.print(myAgent.getLocalName()+" [update order status]: "); 
				System.out.println("Pending: " + pendingOrders.size() + ", Assigned: " + assignedOrders.size() + ", Completed: " + completedOrders.size() + ".\n");
			} else	if (completedMsg != null) {
				String order = completedMsg.getSender().getLocalName();
				System.out.print(myAgent.getLocalName()+" [report]: ");
				System.out.println(order + " is completed.");
				assignedOrders.remove(order);
				completedOrders.add(order);
				if (pendingOrders.size()==0 && assignedOrders.size()==0) {
					System.out.println(myAgent.getLocalName() + " [done]: All orders succesfully completed.\n");
				} else {
					System.out.print(myAgent.getLocalName()+" [update order status]: "); 
					System.out.println("Pending: " + pendingOrders.size() + ", Assigned: " + assignedOrders.size() + ", Completed: " + completedOrders.size() + ".\n");
					}
			} else {
				block();
			}
		}
	}

	/**
	 * <!--CREATE ORDER BEHAVIOUR-->
	 * Behavior which handles the creation of new {@link OrderAgent}. It creates
	 * an agent whit a random part request and adds it to the pending list. It will
	 * allow to dynamically create orders once the initial ones are completed.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> new agent order in the pending queue.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	@SuppressWarnings("serial")
	private class CreateOrder extends CyclicBehaviour {
		/**
		 * Main process of the behavior. Creates a new part request as a hashmap,
		 * creates a new order base on such hashmap and adds it to the queue.
		 */
		public void action() {
			AgentContainer c = getContainerController();
			Object[] args = new Object[2];
			args[0] = readOrder();
			DecimalFormat uidFormat = new DecimalFormat("0000");
			
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("newOrder"));

			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				int orderNum = Integer.parseInt(msg.getContent());
				String ordNum = uidFormat.format(orderNum);
				pendingOrders.add("Order "+ordNum);
				args[1]=ordNum;
				System.out.println(myAgent.getLocalName() + ": Received new order...");

				try {
					//System.out.println(myAgent.getLocalName() + ": Creating OrderAgent");
					AgentController a = c.createNewAgent("Order "+
							ordNum, "warehouse.OrderAgent",
							args);
					a.start();
					System.out.println(myAgent.getLocalName() + ": Created new order succesfully");

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				block();
			}
		}
		
		/**
		 * Creates a part list with random quantities.
		 * @return partList	a hashmap with part request created randomly.
		 */
		HashMap<String,Integer> readOrder(){
			Random rnd = new Random();
			HashMap<String,Integer> partList = new HashMap <String,Integer>();
			partList.put("motor", rnd.nextInt(10));
			partList.put("base", 1);
			partList.put("arms", rnd.nextInt(5));
			partList.put("wires", rnd.nextInt(20));
			partList.put("esc", rnd.nextInt(10));
			partList.put("nazam", 1);
			partList.put("rx", 1);
			partList.put("gcu", 1);
			partList.put("pmu", 1);
			partList.put("iosd", 1);
			partList.put("cables", rnd.nextInt(20));
			partList.put("landinggear", 1);
			partList.put("imu", 1);
			partList.put("globalmount", 1);
			partList.put("vtx", 1);
			partList.put("gimbal", 1);
			partList.put("cover", 1);
			partList.put("blade", rnd.nextInt(6));		
			return partList;
		}
	}
	
	/**
	 * <!--INITIAL ORDERS BEHAVIOUR-->
	 * Behavior which handles the creation of {@link OrderAgent}. It reads the
	 * parameters of the XML configuration file, initializes each agent with them and
	 * launches them in the system.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> launch order agents in the system.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	private class initialOrders extends OneShotBehaviour{
		
		private static final long serialVersionUID = 1L;
		
		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> orders = config.getOrderArgs();
			System.out.println(myAgent.getLocalName()+": Initial orders loaded: "+orders.size());
			currentOrder=orders.size();
			try {
				for(Object[] o:orders) {
					String orderNum = (String)o[1];
					Object[] args = new Object[2];
					args[0] = o[0];
					args[1] = o[1];
					pendingOrders.add("Order "+orderNum);
					AgentController a = c.createNewAgent("Order "+
							orderNum, "warehouse.OrderAgent",
							args);
					a.start();
				}
			} catch (Exception e) {
				System.out.println("There is something wrong");
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * <!--INITIAL ROBOTS BEHAVIOUR-->
	 * Behavior which handles the creation of {@link RobotAgent}. It reads the
	 * parameters of the XML configuration file, initializes each agent with them and
	 * launches them in the system.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> launch robot agents in the system.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	@SuppressWarnings("serial")
	private class initialRobots extends OneShotBehaviour{
		
		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> robots = config.getRobotArgs();
			System.out.println(myAgent.getLocalName()+": Initial robots loaded: "+robots.size());
			try {
				for(Object[] r:robots){
					String robotNum = (String)r[0];
					Object[] args = new Object[1];
					args[0] = r[0];
					AgentController a = c.createNewAgent("Robot "+
									robotNum, "station.RobotAgent",
							args);
					a.start();
				}
			}catch (Exception e) {
				System.out.println("There is something wrong");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * <!--INITIAL SHELVES BEHAVIOUR-->
	 * Behavior which handles the creation of {@link ShelfAgent}. It reads the
	 * parameters of the XML configuration file, initializes each agent with them and
	 * launches them in the system.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> launch shelf agents in the system.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	@SuppressWarnings("serial")
	private class initialShelves extends OneShotBehaviour{		
		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> shelves = config.getShelfArgs();
			System.out.println(myAgent.getLocalName()+": Initial shelves loaded: "+shelves.size());
			try {
				for(Object[] s:shelves){
					String shelfNum = (String)s[1];
					Object[] args = new Object[2];
					args[0] = s[0];
					args[1] = s[1];
					AgentController a = c.createNewAgent("Shelf "+
									shelfNum, "shelf.ShelfAgent", args);
					a.start();
				}
			}catch (Exception e) {
				System.out.println("There is something wrong");
				e.printStackTrace();
			}
		}
	}
	/**
	 * <!--INITIAL PICKERS BEHAVIOUR-->
	 * Behavior which handles the creation of {@link PickerAgent}. It reads the
	 * parameters of the XML configuration file, initializes each agent with them and
	 * launches them in the system.
	 * <p>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> launch shelf agents in the system.
	 * </ul>
	 * </p>
	 * @author [DNA] Diego, Nicolas, Argentina
	 */
	@SuppressWarnings("serial")
	private class initialPickers extends OneShotBehaviour{		
		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> pickers = config.getPickerArgs();
			System.out.println(myAgent.getLocalName()+": Initial pickers loaded: "+pickers.size());
			try {
				for(Object[] p:pickers){
					String pickerNum = (String)p[0];
					Object[] args = new Object[1];
					args[0] = p[0];
					AgentController a = c.createNewAgent("Picker "+
									pickerNum, "station.PickerAgent", args);
					a.start();
				}
			}catch (Exception e) {
				System.out.println("There is something wrong");
				e.printStackTrace();
			}
		}
	}
	
}
