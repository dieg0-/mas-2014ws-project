/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol‡s Laverde Alfonso & Diego Enrique Ramos Avila
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

import java.util.*;


public class WarehouseAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// The order list maps the available lists to it's status (pending or
	// finished)
	@SuppressWarnings("unused")
	private Hashtable<Integer, String> orderListStatus;
	@SuppressWarnings("unused")
	private Hashtable<Integer, String> pendingOrders;
	@SuppressWarnings("unused")
	private Hashtable<Integer, String> completedOrders;
	@SuppressWarnings("unused")
	private Hashtable<Integer, String> processingOrders;
	
	InitConfig config;

	/* Agent initialization */
	protected void setup() {
		System.out.println(getLocalName() + ": Started.");
		//Load config file
		config = new InitConfig();
		config.createXML();
		config.readXML();
				
		orderListStatus = new Hashtable<Integer, String>();

		// Get the list of orders as a start-up argument
		@SuppressWarnings("unused")
		Object[] args = getArguments();

		// Add behaviours
		addBehaviour(new initialOrders());
		addBehaviour(new CreateOrder());
		addBehaviour(new availablePicker());
		System.out.println(getLocalName()+": Loaded behaviours");
		
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

	@SuppressWarnings("serial")
	public class updateOrderLists extends CyclicBehaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchOntology("Completed Order"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(myAgent.getLocalName() + ": "
						+ msg.getSender().getLocalName() + " is completed.");
				// TODO Update hash-tables for each list type
				// TODO Add templates for each type of update
			} else {
				block();
			}
		}
	}

	
	
	@SuppressWarnings("serial")
	private class CreateOrder extends CyclicBehaviour {
		public void action() {
			AgentContainer c = getContainerController();
			Object[] args = new Object[2];
			args[0] = readOrder();

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("newOrder"));

			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) { // Message received. Process it String order =
				int orderNum = Integer.parseInt(msg.getContent());
				args[1]=orderNum;
				System.out.println(myAgent.getLocalName()
						+ ": Received order...");

				try {
					System.out.println(myAgent.getLocalName()
							+ ": Creating OrderAgent");
					AgentController a = c.createNewAgent("Order"+
							Integer.toString(orderNum), "warehouse.OrderAgent",
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
	
	private class initialOrders extends OneShotBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			AgentContainer c = getContainerController();
			ArrayList<Object[]> orders = config.getOrders();
			System.out.println(myAgent.getLocalName()+": Initial orders loaded: "+orders.size());
			
			try {
				for(Object[] o:orders){
					String orderNum = (String)o[1];
					Object[] args = new Object[2];
					args[0] = o[0];
					args[1] = o[1];
					AgentController a = c.createNewAgent("Order"+
							orderNum, "warehouse.OrderAgent",
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
	private class availablePicker extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchOntology("freepicker"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(myAgent.getLocalName() + ": Picker "
						+ msg.getSender().getLocalName() + " is available.");

				// TODO Send OrderAgent to free PickerAgent
			} else {
				block();
			}

		}
	}
	
}
