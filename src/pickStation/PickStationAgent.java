/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.0.n.
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package pickStation;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class PickStationAgent extends Agent {
	
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
		this.addBehaviour(new TickerBehaviour(this, 30000) {
			protected void onTick() {
				// Update the list of robot agents.
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				// Search for agents who offer a fetch service.
				sd.setType("fetch");
				// 
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
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < result.length; ++i) {
						cfp.addReceiver(result[i].getName());
					}
					cfp.setContent("Status");
					myAgent.send(cfp);
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
		
	}
	// Killing the agent.
	protected void takeDown() {
		System.out.println("Cordinator Killed!!!!!!!!.");
	}

}