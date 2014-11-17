/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.1.n.
@since 17.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package station;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class PickerAgent extends Agent {
	
	// Private String name;
	private AID[] activeAgent;
	// Setup for the PickStation Agent.
	protected void setup() {
		// Initialization Messages
		System.out.println("--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		System.out.println("--------------------------\n\n");
		// Behavior for searching robots subscribed to the yellow pages.
		this.addBehaviour(new getRobotAgents(this, 15000) );
	}
	
	private class getRobotAgents extends TickerBehaviour {

		public getRobotAgents(Agent a, long period) {
			super(a, period);
		}
		
		protected void onTick() {
			// Update the list of robot agents.
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			// Search for agents who offer a fetch service.
			sd.setType("fetch");
			template.addServices(sd);
			try {
				// Searching process.
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				System.out.println("\n\n-SEARCHING FOR AGENTS---------------");
				System.out.println("Found the following active agents:");
				activeAgent = new AID[result.length];
				// Found Agents.
				for (int i = 0; i < result.length; ++i) {
					// Listing the agents ID's found.
					activeAgent[i] = result[i].getName();
					System.out.println(activeAgent[i].getName());
				}
				System.out.println("------------------------------------\n");
				/* Sending Messages to the found agents. */
				ACLMessage query = new ACLMessage(ACLMessage.QUERY_IF);
				for (int i = 0; i < result.length; ++i) {
					query.addReceiver(result[i].getName());
				}
				query.setContent("fetch");
				myAgent.send(query);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	}
	
	// Killing the agent.
	protected void takeDown() {
		System.out.println("PickerAgent Killed!!!!!!!!.");
	}
	
	/**
	 * Reservado para Argen
	 * 
	 * 
	 private class freePicker extends CyclicBehaviour {
		public void action() {			
			ACLMessage freed = new ACLMessage(ACLMessage.CONFIRM);
			finish.setOntology("Free Picker");
			  finish.setContent("Yes");
			  finish.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(finish);
			  doDelete();
			
		}
	}
	 */

}