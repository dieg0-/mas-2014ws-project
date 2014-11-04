package warehouse;

import jade.core.Agent;
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
	private Hashtable orderlist;

	/* Agent initialization */
	protected void setup() {
		System.out.println("Hello, I am " + getLocalName());
		orderlist = new Hashtable();
		// Get the list of orders as a start-up argument
		Object[] args = getArguments();

		addBehaviour(new IncomingOrder());
		addBehaviour(new CompletedOrder());

	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Seller-agent " + getAID().getName()
				+ " terminating.");
	}

	public void updateCatalogue(final int orderNum, final boolean status) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				orderlist.put(orderNum, new Boolean(status));
			}
		});
	}

	private class IncomingOrder extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);//Call for proposals?
			ACLMessage msg = myAgent.receive(mt);
			ACLMessage reply = msg.createReply();

			if (msg != null) {
			      // Message received. Process it
				String order = msg.getContent();
				
				String parts = (String) orderlist.get(order);	
				
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("Order received. Begin processing");
			}else{
				block();
			}	
			myAgent.send(reply);
		}
	}
	
	private class CompletedOrder extends CyclicBehaviour{
		public void action(){
			
		}
	}

}
