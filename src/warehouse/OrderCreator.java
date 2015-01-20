/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 1.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package warehouse;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

/**
 * Sends the required messages and number order parameter to WarehouseAgent in order to create new
 * orders. Arguments in the jade GUI must be separated by a comma.
 * @param start	Order number at which it will start creating numbers. 
 * @param qty	Number of orders to create
 */
public class OrderCreator extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Random randomGenerator = new Random();
	int randomOrder;
	int start;
	int qty;
	
	protected void setup() {
		Object[] args = getArguments();
		start = Integer.parseInt((String) args[0]);
		qty = Integer.parseInt((String) args[1]);
		
		randomOrder = randomGenerator.nextInt(1000);
		//System.out.println(getLocalName()+":  Started.");
		//System.out.println(getLocalName() + ": Created random order.");
		
		addBehaviour(new sendOrder());
	}

	protected void takeDown() {
		// Printout a dismissal message
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}
		
	private class sendOrder extends OneShotBehaviour {
		  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action() {
			System.out.println(myAgent.getLocalName()+": Sending"+Integer.toString(qty) +"order(s)...");
			
			for (int i = 0; i<qty;i++){
				ACLMessage order = new ACLMessage(ACLMessage.INFORM);
				  order.setOntology("newOrder");
				  order.setContent(Integer.toString(start+i));			  
				  order.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
				  send(order);
				  doDelete();
			}
			
			  
			} 
		  }
	
	
	
}
