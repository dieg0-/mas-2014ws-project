/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 1.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package shelf;

import java.util.HashMap;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ShelfAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> inventory;
	
	protected void setup(){
		System.out.println("Hello, I am "+getLocalName());
		inventory = new HashMap<String, Integer>();
		
		addBehaviour(new OrderRequestServer());
	}
	
	protected void takeDown(){
		System.out.println("Shelf Agent " + getAID().getName() + " terminating.");
	}
	
	/**
	 * @description The inventory should be updated each time the order picker takes pieces
	 *              from the shelf, and each time the shelf is refilled.
	 * @param piece
	 * @param amount
	 */
	public void updateInventory(final String piece, final int amount) {
		addBehaviour(new OneShotBehaviour() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action() {
				inventory.put(piece, new Integer(amount));
			}
		} );
	}
	
	/**
	 * @description Verifies if the pieces requested are available.
	 * @author diego
	 *
	 */
	private class OrderRequestServer extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(template);
			if (message != null) {
				String[] parsedMessage = message.getContent().split(",");
				// The first part of the content contains the piece requested, and the second
				//  the amount needed.
				String piece = parsedMessage[0];
				int amount = Integer.parseInt(parsedMessage[1]);
				
				ACLMessage reply = message.createReply();
				
				// Checks if the piece is available.
				if(inventory.containsKey(piece)){
					Integer availablePieces = inventory.get(piece);
					// Check how many are there in stock.
					if(availablePieces >= amount){
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent("Enough pieces available");
					}else{
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent("Only " + availablePieces + " are available.");
					}
				// Ideally, a shelf shouldn't respond if it doesn't have the available piece.
				}else{
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("The piece is unfortunately not available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  


	


}
