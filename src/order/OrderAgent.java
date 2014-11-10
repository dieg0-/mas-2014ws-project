/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol‡s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 1.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/



package order;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class OrderAgent extends Agent {
	protected void setup(){
		System.out.println("Agent " + getLocalName() + " started.");
		addBehaviour(new CompletedOrder());
		
	}
	
	// Put agent clean-up operations here
		protected void takeDown() {
			// Printout a dismissal message
			System.out.println("Order " + getAID().getName()
					+ " finished.");
		}

	private class CompletedOrder extends CyclicBehaviour{
		public void action(){
			
		}
	}
	
}
