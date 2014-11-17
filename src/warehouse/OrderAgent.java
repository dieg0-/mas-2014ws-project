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

import java.util.Hashtable;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class OrderAgent extends Agent {
	
	Hashtable <String,Integer> partList;
	
	protected void setup(){
		System.out.println("Order "+getLocalName() + ": Started.");
		partList = new Hashtable<String, Integer>();
		
		//Behaviours
		addBehaviour(new CompletedOrder());
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Order "+getAID().getLocalName()+ ": Order finished.");
		doDelete();
	}

	private class CompletedOrder extends CyclicBehaviour{
		public void action(){
			System.out.println("Order "+myAgent.getLocalName()+": Order completed...");
			  ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
			  compMsg.setOntology("Completed Order");
			  compMsg.setContent("Completed");
			  compMsg.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(compMsg);
			  doDelete();
		}
	}
	
	private class MissingPieces extends CyclicBehaviour{
		public void action(){
			//TODO Check hashtable qty vs parts
		}
	}
	
}
