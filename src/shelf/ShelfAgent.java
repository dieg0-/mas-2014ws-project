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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import utilities.Pose;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShelfAgent extends Agent {
	
	public static String shelfDir = "conf/shelves/shelf";

	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> inventory;
	
	protected Pose position;	
	protected DFAgentDescription dfd;
	protected String uid;
		
	@SuppressWarnings("unchecked")
	protected void setup(){
		position = new Pose();
		position.randomInit(false);
		System.out.println(getLocalName()+": started at ("+position.parsePose()+").");
		
		/** The inventory is initialized via argument passing **/
		Object[] args = this.getArguments();
		if(args.length >= 2){
			inventory = (HashMap<String,Integer>)args[0];
			uid = (String) args[1];
		}else{
			/** This enables to create Shelf Agents dynamically without arguments **/
			inventory = new HashMap<String, Integer>();
			initInventory("DEFAULT");
		}

		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		
		/** Shelves offering this service are visible to the pickers and available for usage **/
		ServiceDescription sdOffer = new ServiceDescription();
		sdOffer.setType("offer-pieces");
		sdOffer.setName("Offer-Service");
		this.dfd.addServices(sdOffer);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new OrderRequestServer());
		addBehaviour(new waitForExternalMessages());
	}
	
	/**
	 * @description Kills the Shelf Agent.
	 */
	protected void takeDown(){
		System.out.println("Shelf Agent " + getAID().getName() + " terminating.");
	}
	
	/**
	 * 
	 * @param piece The piece requested.
	 * @param amount The amount requested of that piece.
	 * @return True if the piece (at least one) is availabe, else False.
	 */
	public boolean checkPieceInInventory(String piece, int amount){
		boolean answer = false;
		if(this.inventory.containsKey(piece)){
			if(this.inventory.get(piece) > 0)
				answer = true;
		}
		return answer;
	}
	
	/**
	 * @description The shelf refills its inventory (all pieces) according to the given amount
	 * @param amount The amount of pieces to be refilled.
	 */
	@SuppressWarnings("rawtypes")
	public void restock(int amount){
		Set orderSet = inventory.entrySet();
		Iterator iter = orderSet.iterator();
		while(iter.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>)iter.next();
			inventory.put(lookup.getKey(), lookup.getValue() + amount);
		}
	}
	
	/**
	 * 
	 * @param order A HashMap containing the requested pieces.
	 * @return The number of pieces available that were pointed out in the request.
	 */
	@SuppressWarnings("rawtypes")
	public int checkAvailabilityPercentage(HashMap<String, Integer> order){
		int availablePieces = 0;
		Set orderSet = order.entrySet();
		Iterator iter = orderSet.iterator();
		while(iter.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>)iter.next();
			if(checkPieceInInventory(lookup.getKey(), lookup.getValue())){
				if(lookup.getValue() > inventory.get(lookup.getKey())){
					availablePieces = availablePieces + inventory.get(lookup.getKey());
				}else{
					availablePieces = availablePieces + lookup.getValue();
				}
			}
		}
		
		return availablePieces;
	}
	
	/**
	 * @description runs a OneShot behavior that updates the inventory (subtracts the recently given pieces)
	 * @param order A HashMap containing the requested pieces.
	 */
	@SuppressWarnings("rawtypes")
	public void updateRequestedInventory(final HashMap<String, Integer> order){
		addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;	

			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				Set orderSet = order.entrySet();
				Iterator iter = orderSet.iterator();
				while(iter.hasNext()){
					Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>)iter.next();
					String piece = lookup.getKey();
					int amount = lookup.getValue();
					if(inventory.containsKey(piece)){
						if(inventory.get(piece) >= amount){
							inventory.put(piece, inventory.get(piece) - amount);
						}else{
							inventory.put(piece, 0);
						}
					}
				}
			}						
		});
	}
	
	/**
	 * @description Initializes the inventory of a shelf according to a text file.
	 * @param inventoryType The type of inventory (matches a file name)
	 */
	public void initInventory(String inventoryType){
		BufferedReader in;
		try {
			File inventoryFile = new File(shelfDir + inventoryType + ".txt");
			if(!inventoryFile.exists())
				inventoryType = "Default";
			in = new BufferedReader(new FileReader(shelfDir + inventoryType + ".txt"));
			String line = "";

			while ((line = in.readLine()) != null) {
			    String parts[] = line.split(",");
			    inventory.put(parts[0], Integer.parseInt(parts[1]));
			}
	        in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	/**
	 * 
	 * @param hm The HashMap to be copied.
	 * @return A new HashMap with the same values and keys as hm.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap<String, Integer> copyHM(HashMap<String, Integer> hm){
		HashMap<String, Integer> newHM = new HashMap<String, Integer>();
		
		Set orderSet = hm.entrySet();
		Iterator iter = orderSet.iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>)iter.next();
			String piece = lookup.getKey();
			int amount = lookup.getValue();
			newHM.put(piece, amount);
		}
		
		return newHM;
	}
	
	/**
	 * @description This class implements a cyclic behavior that waits for messages from Picker Agents requesting
	 *              pieces.
	 */
	private class OrderRequestServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(template);
			if (message != null) {
				//System.out.println(myAgent.getLocalName() +": Order request received");
				deregisterService();
				HashMap<String, Integer> mappy;
				try {
					mappy = (HashMap<String, Integer>)message.getContentObject();
					ACLMessage reply = message.createReply();
					reply.setPerformative(ACLMessage.CFP);
					int availablePieces = checkAvailabilityPercentage(mappy);
					if(availablePieces > 0){
						String sAvailablePieces = String.valueOf(availablePieces);
						//System.out.println(myAgent.getLocalName() + ": Some pieces availabe. Sending proposal...");	
						/** If the shelf has pieces to offer according to the request, send a proposal message **/
						reply.setPerformative(ACLMessage.PROPOSE);
						/** The amount of available pieces is stored in the language field **/
						reply.setLanguage(sAvailablePieces);
						double myPosition[] = position.poseToArray();
						reply.setContentObject(myPosition);
						myAgent.send(reply);
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						MessageTemplate informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
						ACLMessage informMessage = myAgent.receive(informTemplate);
						if(informMessage != null){
							/** If this shelf is not selected for usage, it is commanded to re register. **/
							if(informMessage.getContent().matches("REREGISTER")){
								registerService();
							/** If the service is done, the shelf update its inventory and re registers **/
							}else if(informMessage.getContent().matches("UPDATE-REREGISTER-BE-HAPPY")){
								updateRequestedInventory(mappy);
								registerService();
							/** If the shelf is select for usage, it sends its inventory to the corresponding Order Agent **/
							}else if(informMessage.getContent().matches("YOU-ARE-THE-ONE")){
								//System.out.println(myAgent.getLocalName() + ": I've been selected. Preparing to provide service..");
								/** The Order's AID is stores in the language field **/
								String sName = informMessage.getLanguage();
								AID orderID = new AID(sName, AID.ISGUID);
								ACLMessage notify = new ACLMessage(ACLMessage.REQUEST);
								notify.setOntology("Check Part List");
								notify.addReceiver(orderID);
								HashMap<String, Integer> copyInventory = new HashMap<String, Integer>();
								copyInventory = copyHM(inventory);
								notify.setContentObject(copyInventory);
								send(notify);
							}
						}else{
							/** Safety Mechanism: If null is received, initialize behavior to wait for further messages **/
							addBehaviour(new cyclicMessageWaiter(myAgent, mappy));
						}
					}else{
						registerService();
						/** If the shelf has no pieces to offer according to the request, send a refusal message **/
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("Not enough pieces available");
						System.out.println(myAgent.getLocalName() + " [out of stock]: 0 requested pieces available.");
						myAgent.send(reply);
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
			}
			else {
				block();
			}
		}
		
		public void registerService(){
			try {
				DFService.register(myAgent, dfd);
				//System.out.println(myAgent.getLocalName() + ": registering service.");
			}
			catch (FIPAException fe) {
			}
		}
		
		public void deregisterService(){
			try {
				DFService.deregister(myAgent);
				//System.out.println(myAgent.getLocalName() + ": deregistering service.");

			}
			catch (FIPAException fe) {
			}
		}
	}  
	
	/**
	 * 
	 * @description This class implements a behavior in charge of waiting for the different types of messages
	 *              that shelf agents are expected to receive.
	 *
	 */
	private class cyclicMessageWaiter extends SimpleBehaviour {

		private static final long serialVersionUID = 1L;
		protected HashMap<String, Integer> order = new HashMap<String, Integer>();
		
		/**
		 * @param a The agent (A Shelf)
		 * @param mappy The requested order represented as a HashMap
		 */
		public cyclicMessageWaiter(Agent a, HashMap<String, Integer> mappy) {
			super(a);
			this.order = mappy;
		}
		
		public void action(){
			boolean terminationFlag = false;
			
			while(!terminationFlag){
				MessageTemplate informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage informMessage = myAgent.receive(informTemplate);
				if(informMessage != null){
					/** If this shelf is not selected for usage, it is commanded to re register. **/
					if(informMessage.getContent().matches("REREGISTER")){
						terminationFlag = true;
						registerService();
					/** If the service is done, the shelf update its inventory and re registers **/
					}else if(informMessage.getContent().matches("UPDATE-REREGISTER-BE-HAPPY")){
						updateRequestedInventory(this.order);
						terminationFlag = true;
						registerService();
					/** If the shelf is select for usage, it sends its inventory to the corresponding Order Agent **/
					}else if(informMessage.getContent().matches("YOU-ARE-THE-ONE")){
						//System.out.println(myAgent.getLocalName() + ": I've been selected. Preparing to provide service..");
						try {
							/** The Order's AID is stores in the language field **/
							String sName = informMessage.getLanguage();
							AID orderID = new AID(sName, AID.ISGUID);
							ACLMessage notify = new ACLMessage(ACLMessage.REQUEST);
							notify.setOntology("Check Part List");
							notify.addReceiver(orderID);
							HashMap<String, Integer> copyInventory = new HashMap<String, Integer>();
							copyInventory = copyHM(inventory);
							notify.setContentObject(copyInventory);
							send(notify);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				}else{
					block();
				}
			}
		}
		
		public void registerService(){
			try {
				DFService.register(myAgent, dfd);
				//System.out.println(myAgent.getLocalName() + ": registering service.");
			}
			catch (FIPAException fe) {

			}
		}

		@Override
		public boolean done() {
			return true;
		}
		
		
	}
	
	
	/**
	 * @description This class implements a cyclic behavior that's always running waiting for 
	 *              external messages. The external messages are intended to notify that the
	 *              shelf agent should refill its inventory.
	 */
	public class waitForExternalMessages extends CyclicBehaviour{

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology("RESTOCK"));
			ACLMessage message = myAgent.receive(template);
			if (message != null) {
				//System.out.println(myAgent.getLocalName() + ": Received external message!!");
				int amount = Integer.valueOf(message.getContent());
				restock(amount);
			}
			block();
		}
		
	}


	


}
