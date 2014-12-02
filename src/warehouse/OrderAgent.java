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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class OrderAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap <String,Integer> partList;
	boolean completed;
	boolean assigned;
	String orderNum;
	protected DFAgentDescription dfd;
	
	@SuppressWarnings("unchecked")
	protected void setup(){
		Object [] args = getArguments();
		partList = (HashMap<String,Integer>)args[0];
		orderNum = (String) args[1];
		completed = false;
		assigned = false;
		
		//printPartList(partList);
		
		this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID()); 
        
        ServiceDescription sd = new ServiceDescription();
		sd.setType("order");
		sd.setName("order-agents");
		this.dfd.addServices(sd);
		
		try {  
            DFService.register(this,dfd); 
            //System.out.println("Subscribed");
        }catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
		
		System.out.println(getLocalName() + ": Started.");
		//partList = new Hashtable<String, Integer>();
		//System.out.println("Order "+getLocalName() + ": Requesting the following parts:");
		//printPartList(partList);
		
		//Behaviours
			//addBehaviour(new requestParts());
			addBehaviour(new CompletedOrder());
			addBehaviour(new MissingPieces());
			addBehaviour(new orderStatus());
			
	}
	
	void printPartList(HashMap<String,Integer> mp){
		Set<Entry<String, Integer>> set = mp.entrySet();
		Iterator<Entry<String, Integer>> i = set.iterator();
		System.out.println("___________________");
		while(i.hasNext()) {
	         Entry<String, Integer> me = i.next();
	         System.out.print(me.getKey() + ": ");
	         System.out.println(me.getValue());
	      }
		System.out.println("___________________");		
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message		
		System.out.println(getAID().getLocalName()+ ": Order finished.");
		doDelete();
	}

	private class CompletedOrder extends CyclicBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			if (completed == true){
			System.out.println(myAgent.getLocalName()+": Order completed...");
			  ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
			  compMsg.setOntology("Completed Order");
			  compMsg.setContent("Completed");
			  compMsg.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(compMsg);
			  doDelete();
			}else{
				block();
			}
			
		}
	}
	
	private class MissingPieces extends CyclicBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			//TODO Check hashtable qty vs parts
			block();
		}
	}
	
	private class requestParts extends OneShotBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		AID picker;
		public requestParts(AID a){
			picker = a;
		}
		  public void action() {
			  System.out.println(getAID().getLocalName()+ ": Requesting parts...");
			  ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
			  order.setOntology("requestParts");
			  //order.setContent(Integer.toString(randomOrder));
			  try{
			  order.setContentObject(partList);
			  }catch(IOException e){}
			  
			  order.addReceiver(picker);
			  send(order);
			  //doDelete();
			} 
		  }
	
	private class orderStatus extends CyclicBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("assignment"));

			ACLMessage msg = myAgent.receive(mt);
			if (msg != null){
				ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
				reply.setOntology("assignment");
				
				System.out.println(getLocalName()+" assigned to "+msg.getSender().getLocalName()+".");
				assigned=true;
				addBehaviour(new requestParts(msg.getSender()));
				try { 
					DFService.deregister(myAgent); 
					}catch (Exception e) {}
			}else{
				block();
			}
			
		}
	}
}
