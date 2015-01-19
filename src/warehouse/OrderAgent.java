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
import java.util.Map;
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
import jade.lang.acl.UnreadableException;


public class OrderAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap <String,Integer> partList;
	HashMap <String,Integer> missingParts;
	boolean completed;
	boolean assigned;
	String orderNum;
	String assignedPicker;
	protected DFAgentDescription dfd;
	
	@SuppressWarnings("unchecked")
	protected void setup(){
		Object [] args = getArguments();
		partList = (HashMap<String,Integer>)args[0];
		orderNum = (String) args[1];
		completed = false;
		assigned = false;
		
		missingParts = (HashMap<String, Integer>) partList.clone();
		
		this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID()); 
        
        ServiceDescription sd = new ServiceDescription();
		sd.setType("order");
		sd.setName("order-agents");
		this.dfd.addServices(sd);
		
		try {  
            DFService.register(this,dfd); 
        }catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
		
		System.out.println(getLocalName() + ": Started.");
		//System.out.println("Order "+getLocalName() + ": Requesting the following parts:");
		//printPartList(partList);
		
		//Behaviours
			addBehaviour(new orderStatus());
			addBehaviour(new MissingPieces());
			
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

	private class CompletedOrder extends OneShotBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			//if (completed == true){
			System.out.println(myAgent.getLocalName()+": Order completed...");
			  ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
			  compMsg.setOntology("Completed Order");
			  compMsg.setContent("Completed");
			  compMsg.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(compMsg);
			  doDelete();
			//}else{
				//block();
			//}
			
		}
	}
	
	private class MissingPieces extends CyclicBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action(){
			
			MessageTemplate partsMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("Check Part List"));
			ACLMessage partsMsg = myAgent.receive(partsMT);
			
			MessageTemplate checkMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("Check Part List"));
			ACLMessage temp = myAgent.receive(checkMT);
			
			if (temp !=null){
				//
				System.out.println(myAgent.getLocalName()+": Updating missing pieces...");
				  ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
				  compMsg.setOntology("Final shelf");
				  compMsg.addReceiver(new AID(assignedPicker,AID.ISLOCALNAME));
				  send(compMsg);
			}
			if (partsMsg !=null){
				try {
					@SuppressWarnings("unchecked")
					HashMap <String,Integer> available = (HashMap<String,Integer>) partsMsg.getContentObject();
					for (Map.Entry<String, Integer> entry : missingParts.entrySet()) { 
						String part = entry.getKey();
						if(available.containsKey(part)){
							if(entry.getValue()>available.get(part)){
								entry.setValue(entry.getValue()-available.get(part));								
							}else if(entry.getValue()<available.get(part)){
								missingParts.remove(part);
							}else if (entry.getValue()==available.get(part)){
								missingParts.remove(part);
							}
						}	
					}
					
					if (missingParts.isEmpty()){
						//System.out.println(myAgent.getLocalName()+": Order completed...");
						  ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
						  compMsg.setOntology("Final shelf");
						  compMsg.addReceiver(new AID(assignedPicker,AID.ISLOCALNAME));
						  send(compMsg);
					}else{
						System.out.println("Need a new shelf");
						ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
						order.setOntology("requestParts");
						try{
							order.setContentObject(missingParts);
						}catch(IOException e){}
  
						order.addReceiver(new AID(assignedPicker,AID.ISLOCALNAME));
						send(order);
					}					
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}else{
				block();
			}
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
			  try{
			  order.setContentObject(partList);
			  }catch(IOException e){}
			  
			  order.addReceiver(picker);
			  order.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(order);
			} 
		  }
	
	private class orderStatus extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;

		public void action(){
			MessageTemplate assignMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("assignment"));
			ACLMessage assignMsg = myAgent.receive(assignMT);

			MessageTemplate completeMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchOntology("Completed Order"));
			ACLMessage completeMsg = myAgent.receive(completeMT);
			
			
			if (assignMsg != null){
				ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
				reply.setOntology("assignment");
				assignedPicker = assignMsg.getSender().getLocalName();
				System.out.println(getLocalName()+" assigned to "+assignMsg.getSender().getLocalName()+".");
				assigned=true;
				addBehaviour(new requestParts(assignMsg.getSender()));
				try { 
					DFService.deregister(myAgent); 
				}catch (Exception e) {}
			}else if (completeMsg != null){
				addBehaviour(new CompletedOrder());
			}else{
				block();
			}
			
		}
	}
}
