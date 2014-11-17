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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class WarehouseAgent extends Agent {
	// The order list maps the available lists to it's status (pending or
	// finished)
	private Hashtable<Integer, String> orderListStatus;
	

	/* Agent initialization */
	protected void setup() {
		AgentContainer c = getContainerController();
		System.out.println("Agent " + getLocalName() + " started.");
		orderListStatus = new Hashtable<Integer, String>();

		// Get the list of orders as a start-up argument
		Object[] args = getArguments();
	/**	
		try {
			System.out.println("Attepting to create OrderAgent");
			AgentController a = c.createNewAgent(
					"12345", "warehouse.OrderAgent", args);
			System.out.println("Attempting to start OrderAgent");
			a.start();
			System.out.println("Created OrderAgent Succesfully");
		} catch (Exception e) {
			e.printStackTrace();
 		}**/
		
		

		// Add behaviours
		 addBehaviour(new IncomingOrder());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agent " + getAID().getName() + " terminating.");
	}

	public void updateOrderList(final int orderNum, final String parts) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				orderListStatus.put(orderNum, new String(parts));
				try {
					//AgentController a = c.createNewAgent(Integer.toString(orderNum), "order.OrderAgent", null);
					//a.start();
					
					System.out.println("Created OrderAgent Succesfully");
				} catch (Exception e) {
				}
			}
		});

	}

	private class IncomingOrder extends CyclicBehaviour {
		public void action() {
			AgentContainer c = getContainerController();
			Object [] args = new Object[2];
	        args[0] = "3";
	        args[1] = "Allo there";
	        
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			
			ACLMessage msg = myAgent.receive(mt);
			

			if (msg != null) { // Message received. Process it String order =
				int orderNum = Integer.parseInt(msg.getContent());
				System.out.println("Warehouse Manager received order...");
				
				
				try {
					System.out.println("Attepting to create OrderAgent");
					AgentController a = c.createNewAgent(Integer.toString(orderNum), "warehouse.OrderAgent", args);
					System.out.println("Attempting to start OrderAgent");
					a.start();
					System.out.println("Created OrderAgent Succesfully");
				} catch (Exception e) {
					e.printStackTrace();
		 		}
				
			} else {
				block();
			}
			
		}
	}

}
