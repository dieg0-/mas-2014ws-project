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
	AgentContainer c = getContainerController();

	/* Agent initialization */
	protected void setup() {
		System.out.println("Agent " + getLocalName() + " started.");
		System.out.println(getAID());
		System.out.println(getAID().getName());
		System.out.println(getAID().getLocalName());
		orderListStatus = new Hashtable<Integer, String>();

		// Get the list of orders as a start-up argument
		Object[] args = getArguments();
		// int orderNum = Integer.parseInt((String)args[0]);
		// String partList = (String) args[1];

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
					AgentController a = c.createNewAgent(
							Integer.toString(orderNum), "OrderAgent", null);
					a.start();
				} catch (Exception e) {
				}
			}
		});

	}

	private class IncomingOrder extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			
			ACLMessage msg = myAgent.receive(mt);
			//ACLMessage msg = myAgent.receive();
			
			//ACLMessage reply = msg.createReply();

			if (msg != null) { // Message received. Process it String order =
				msg.getContent();
				System.out.println("Received message");
				// String parts = (Boolean) orderlist.get(order);

				// reply.setPerformative(ACLMessage.INFORM);
				// reply.setContent("Order received. Begin processing");
			} else {
				block();
			}
			// myAgent.send(reply);
		}
	}

}