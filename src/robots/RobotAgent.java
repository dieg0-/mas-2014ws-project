/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.1.n.
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package robots;

import java.util.Random;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class RobotAgent extends Agent {
	// Attributes.
	private String id;
	private double[] position;
	private boolean busy;
	
	protected void setup() {
		System.out.println("Robot Agent: " + getLocalName());
		
		this.position = new double[2];
		this.position[0] = (new Random()).nextDouble();
		this.position[1] = (new Random()).nextDouble();
		
		this.busy = false;
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("JADE-robot-agents");
		dfd.addServices(sd);
		
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.id = (String) args[0];
			System.out.println("ID: " + this.id);
			System.out.println("Waiting...");
			this.addBehaviour(new OfferRequestsServer());
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		else {
			System.out.println("No ID given. Agent won't be register.");
			doDelete();
		}
	}
	
	private class LocalizationBehaviour extends OneShotBehaviour {
		public void action() {
			System.out.println("Location: (" + position[0] + "," + position[1] + ").\n");			
		}
		
	}
	
	private	class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = myAgent.receive();
				if (msg != null) {
					System.out.println("Agent " + myAgent.getLocalName());
					String msg_content = msg.getContent();
					if (msg_content.matches("Status")) {
						System.out.println("Busy: " + busy);
					}
					else {
						System.out.println(myAgent.getLocalName() + " here.");
						System.out.println("Message: " + msg_content);
						myAgent.addBehaviour(new LocalizationBehaviour());
						System.out.println(myAgent.getLocalName() + " out.");
					}
				}
				else {
					block();
				}
		}
	}
	// End of inner class OfferRequests Server
	
	protected void takeDown() {
		System.out.println("Agent out of service.");
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

}