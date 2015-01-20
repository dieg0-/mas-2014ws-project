/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 1.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package warehouse;

//import order.OrderAgent;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;
import java.util.*;


public class WarehouseAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// The order list maps the available lists to it's status (pending or
	// finished)
	private ArrayList<String> pendingOrders;
	private ArrayList<String> completedOrders;
	private ArrayList<String> assignedOrders;
	int currentOrder;
	InitConfig config;

	/** 
	 * Agent initialization. 
	 * <li>Creates and/or reads the xml condfiguration file. 
	 * <li>Initializes the order list status
	 * <li>Loads the initial behaviours
	 * 
	 */
	protected void setup() {
		System.out.println(getLocalName() + ": Started.");
		//Load config file
		config = new InitConfig();
		config.createXML(10,2,2,0);
		System.out.println(getLocalName()+": Configuration created succesfuly.");
		config.readXML();
		System.out.println(getLocalName()+": Configuration read succesfuly.");
		   
		pendingOrders = new ArrayList<String>();		
		assignedOrders = new ArrayList<String>();		
		completedOrders = new ArrayList<String>();		

		// Add behaviours
		addBehaviour(new initialOrders());
		addBehaviour(new CreateOrder());
		addBehaviour(new updateOrderLists());
		addBehaviour(new initialRobots());
		addBehaviour(new initialShelves());
		System.out.println(getLocalName()+": Loaded behaviours");
		//System.out.println(getLocalName()+": Initial orders loaded: "+pendingOrders.size());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}
	
	@SuppressWarnings("serial")
	/**
	 * Cyclic behavior that updates order's list status.
	 */
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
				//System.out.println(assignedOrder);
				pendingOrders.remove(assignedOrder);
				assignedOrders.add(assignedOrder);
				System.out.println(myAgent.getLocalName()+": Updating order status. Pending: "+pendingOrders.size()+". Assigned: "+assignedOrders.size()+". Completed: "+completedOrders.size()+".");
			} else	if (completedMsg != null) {
				String order = completedMsg.getSender().getLocalName();
				System.out.println(myAgent.getLocalName() + ": "
						+ order + " is completed.");
				assignedOrders.remove(order);
				completedOrders.add(order);
				if (pendingOrders.size()==0 && assignedOrders.size()==0){
					System.out.println(myAgent.getLocalName()+": All orders succesfully completed.");
				}else{
					System.out.println(myAgent.getLocalName()+": Updating order status. Pending: "+pendingOrders.size()+". Assigned: "+assignedOrders.size()+". Completed: "+completedOrders.size()+".");				// TODO Update hash-tables for each list type
				}
			} else {
				block();
			}
		}
	}

	
	/**
	 * Cyclic behaviour that creates random orders on the fly. 
	 */
	@SuppressWarnings("serial")
	private class CreateOrder extends CyclicBehaviour {
		public void action() {
			AgentContainer c = getContainerController();
			Object[] args = new Object[2];
			args[0] = readOrder();
			DecimalFormat uidFormat = new DecimalFormat("0000");
			
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("newOrder"));

			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) { // Message received. Process it String order =
				
				int orderNum = Integer.parseInt(msg.getContent());
				String ordNum = uidFormat.format(orderNum);
				pendingOrders.add("Order "+ordNum);
				args[1]=ordNum;
				System.out.println(myAgent.getLocalName()
						+ ": Received new order...");

				try {
					System.out.println(myAgent.getLocalName()
							+ ": Creating OrderAgent");
					AgentController a = c.createNewAgent("Order "+
							ordNum, "warehouse.OrderAgent",
							args);
					// System.out.println("Attempting to start OrderAgent");
					a.start();
					System.out.println(myAgent.getLocalName()
							+ ": Created new order succesfully");

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				block();
			}
		}
		
		/**
		 * Creates a part list with random quantities.
		 * @return partList	HashMap<String,Integer> part list created randomly.
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
	 * Loads initial orders stored in the xml configuration file.
	 */
	private class initialOrders extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> orders = config.getOrderArgs();
			System.out.println(myAgent.getLocalName()+": Initial orders loaded: "+orders.size());
			currentOrder=orders.size();
			try {
				for(Object[] o:orders){
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
				//System.out.println(pendingOrders.toString());
			}catch (Exception e) {
				System.out.println("There is something wrong");
				e.printStackTrace();
			}			
		}
	}

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
	
}
