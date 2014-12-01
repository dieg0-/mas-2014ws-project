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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import utilities.Pose;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShelfAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> inventory;
	//private boolean busy;
	protected Pose position;
	
	protected DFAgentDescription dfd;
	
	protected void setup(){
		position = new Pose();
		position.randomInit(true);
		inventory = new HashMap<String, Integer>();
		initInventory();
		//this.busy = false;
		// Testing purposes.. This shouldn't be predefined for all agents.
		/**
		inventory.put("vtx", 16);
		inventory.put("wires", 12);
		inventory.put("motor", 18);
		**/
		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		
		ServiceDescription sdOffer = new ServiceDescription();
		sdOffer.setType("offer-pieces");
		sdOffer.setName("Offer-Service");
		this.dfd.addServices(sdOffer);
		
		ServiceDescription sdLocate = new ServiceDescription();
		sdLocate.setType("send-location");
		sdLocate.setName("Locate-Service");
		this.dfd.addServices(sdLocate);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new OrderRequestServer());
	}
	
	protected void takeDown(){
		System.out.println("Shelf Agent " + getAID().getName() + " terminating.");
	}
	
	/**
	 * @description The inventory should be updated each time the order picker takes pieces
	 *              from the shelf.
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
				inventory.put(piece, inventory.get(piece) - amount);
				System.out.println(myAgent.getName() + ": Only " + inventory.get(piece) + " " + piece + "s left.");

			}
		} );
	}
	
	public boolean checkPieceInInventory(String piece, int amount){
		boolean answer = false;
		if(this.inventory.containsKey(piece)){
			if(this.inventory.get(piece) >= amount)
				answer = true;
		}
		return answer;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean checkWholeInventory(HashMap<String, Integer> order){
		boolean answer = true;
		Set orderSet = order.entrySet();
		Iterator iter = orderSet.iterator();
		while(iter.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>)iter.next();
			if(!checkPieceInInventory(lookup.getKey(), lookup.getValue()))
				return false;
		}
		
		return answer;
	}
	
	public void initInventory(){
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader("conf/shelves/shelfDefault.txt"));
			String line = "";

			while ((line = in.readLine()) != null) {
			    String parts[] = line.split(",");
			    inventory.put(parts[0], Integer.parseInt(parts[1]));
			}
	        in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(inventory.toString());
        
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

		@SuppressWarnings("unchecked")
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(template);
			if (message != null) {
				System.out.println("Shelf received order");
				//System.out.print(myAgent.getLocalName() + ": ");
				//System.out.println("message-> " + message.getContent());
				//String[] parsedMessage = message.getContent().split(",");
				// The first part of the content contains the piece requested, and the second
				//  the amount needed.
				//System.out.println("MSG: " +  message.getContent());
				//System.out.println("LENGTH: " + parsedMessage.length);
				//String piece = parsedMessage[0];
				//int amount = Integer.parseInt(parsedMessage[1]);
				HashMap<String, Integer> mappy;
				try {
					System.out.println(message.getContentObject());
					mappy = (HashMap<String, Integer>)message.getContentObject();
					System.out.println("Shelf received objects:");
					System.out.println(mappy.toString());
					if(checkWholeInventory(mappy)){
						System.out.println("All pieces are available. Sending position...");
						ACLMessage reply = message.createReply();
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent("Enough pieces available");
						double myPosition[] = position.poseToArray();
						reply.setContentObject(myPosition);
						myAgent.send(reply);
					}else{
						System.out.println("Insufficient pieces");
					}
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//ACLMessage reply = message.createReply();
				/**
				// Checks if the piece is available.
				if(inventory.containsKey(piece)){
					Integer availablePieces = inventory.get(piece);
					// Check how many are there in stock.
					if(availablePieces >= amount){
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent("Enough pieces available");
						System.out.println(myAgent.getLocalName() + ": Enough " + piece + "s available.");
						// This shouldn't happen yet, the picker should select a shelf...
						// just for testing purposes:
						updateInventory(piece, amount);
					}else{
						reply.setPerformative(ACLMessage.PROPOSE);
						System.out.println(myAgent.getLocalName() + ": Only " + availablePieces + " " + piece + "s available.");
						reply.setContent("Only " + availablePieces + " are available.");
					}
				// Ideally, a shelf shouldn't respond if it doesn't have the available piece.
				}else{
					reply.setPerformative(ACLMessage.REFUSE);
					System.out.println(myAgent.getLocalName() + ": Sorry, " + piece + "s are not available.");
					reply.setContent("The piece is unfortunately not available");
				}
				myAgent.send(reply);**/
			}
			else {
				block();
			}
		}
	}  


	


}
