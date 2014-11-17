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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class OrderCreator extends Agent {
	Random randomGenerator = new Random();
	int randomOrder;
	
	protected void setup() {
		randomOrder = randomGenerator.nextInt(1000);
		System.out.println("Created random order. Agent " + getLocalName() + " started.");
		
		addBehaviour(new sendOrder());
	}

	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agent " + getAID().getName() + " terminating.");
	}
	
	private class sendOrder extends OneShotBehaviour {
		  public void action() {
			  System.out.println("Sending order...");
			  ACLMessage order = new ACLMessage(ACLMessage.INFORM);
			  order.setContent(Integer.toString(randomOrder));
			  order.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(order);
			  doDelete();
			} 
		  }
	
}
