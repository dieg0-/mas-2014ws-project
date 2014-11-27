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

@SuppressWarnings("serial")
public class OrderAgent extends Agent {
	
	HashMap <String,Integer> partList;
	boolean completed;
	int orderNum;
	protected DFAgentDescription dfd;
	
	@SuppressWarnings("unchecked")
	protected void setup(){
		Object [] args = getArguments();
		partList = (HashMap<String,Integer>)args[0];
		orderNum = (Integer) args[1];
		completed = false;
		
		this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID()); 
        
        ServiceDescription sd = new ServiceDescription();
		sd.setType("order");
		sd.setName("order-agents");
		this.dfd.addServices(sd);
		
		try {  
            DFService.register(this,dfd); 
            System.out.println("Subscribed");
        }catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
		
		System.out.println("Order "+getLocalName() + ": Started.");
		//partList = new Hashtable<String, Integer>();
		//System.out.println("Order "+getLocalName() + ": Requesting the following parts:");
		//printPartList(partList);
		
		//Behaviours
			addBehaviour(new requestParts());
			addBehaviour(new CompletedOrder());
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
		try { 
			DFService.deregister(this); 
			}catch (Exception e) {}
		System.out.println("Order "+getAID().getLocalName()+ ": Order finished.");
		doDelete();
	}

	private class CompletedOrder extends CyclicBehaviour{
		public void action(){
			if (completed == true){
			System.out.println("Order "+myAgent.getLocalName()+": Order completed...");
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
		public void action(){
			//TODO Check hashtable qty vs parts
			block();
		}
	}
	
	private class requestParts extends OneShotBehaviour {
		  public void action() {
			  System.out.println("Order "+getAID().getLocalName()+ ": Requesting parts...");
			  ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
			  order.setOntology("requestParts");
			  //order.setContent(Integer.toString(randomOrder));
			  try{
			  order.setContentObject(partList);
			  }catch(IOException e){}
			  
			  order.addReceiver(new AID("Picky",AID.ISLOCALNAME));
			  send(order);
			  //doDelete();
			} 
		  }
	
}
