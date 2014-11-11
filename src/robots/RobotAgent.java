/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.0.n.
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
	private String area;
	private double[] position;
	
	protected void setup() {
		System.out.println("Robot Agent: " + getLocalName());
		
		this.position = new double[2];
		this.position[0] = (new Random()).nextDouble();
		this.position[1] = (new Random()).nextDouble();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("JADE-robot-agents");
		dfd.addServices(sd);
		
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.area = (String) args[0];
			System.out.println("Area " + this.area + " will be covered.");
			System.out.println("Waiting for commands...");
			this.addBehaviour(new OfferRequestsServer());
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		else {
			System.out.println("No arguments are given. Agent didn't spawn");
			doDelete();
		}
	}
	
	public class LocalizationBehaviour extends OneShotBehaviour {
		public void action() {
			System.out.println("Location: (" + position[0] + "," + position[1] + ").\n");			
		}
		
	}
	
	private	class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = myAgent.receive();
				if (msg != null) {
					String mensaje = msg.getContent();
					System.out.println(myAgent.getLocalName() + " here.");
					System.out.println("Message: " + mensaje);
					myAgent.addBehaviour(new LocalizationBehaviour());
					System.out.println(myAgent.getLocalName() + " out.");
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