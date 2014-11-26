/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.0.n.
@since 26.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package station;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import utilities.Pose;

@SuppressWarnings("serial")
public class RobotAgent extends Agent {
	// Attributes.
	private String id;
	private Pose position;
	private boolean busy;
	protected DFAgentDescription dfd;
	
	protected void setup() {
		System.out.println("\n--ROBOT-------------");
		System.out.println("Agent: " + getLocalName());
		
		this.position.randomInit();
		this.busy = false;
		
		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("JADE-robot-agents");
		this.dfd.addServices(sd);
		
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.id = (String) args[0];
			System.out.println("ID: " + this.id);
			System.out.println("  > Waiting.");
			this.addBehaviour(new OfferRequestsServer());
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		else {
			System.err.println("  > No ID given. Agent won't be register.");
			doDelete();
		}
		System.out.println("--------------------\n");
	}
	
	
	private class LocalizationBehaviour extends SimpleBehaviour {
		
		public LocalizationBehaviour(Agent a) {
			super(a);			
		}
		
		public void action() {
			String current_pos = String.format("(%.2d, %.2d)", position.getX(), position.getY());
			System.out.println("  > Location: " + current_pos + ").\n");
			block(250);
		}
		
		public boolean done() {
			return true;
		}
	}
	
	
	private class FetchBehaviour extends SimpleBehaviour {
		//Simulated the fetching time, in seconds.
		private long timeout;
		
		public FetchBehaviour(Agent a, long time_sleep) {
			super(a);
			this.timeout = time_sleep;
		}
		
		public void action() {
			try {
				DFService.deregister(myAgent);
				Thread.sleep(this.timeout*1000);
			}
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName() + ": couldn't unregister.");
			}
			catch (Exception e) {
				System.err.println(myAgent.getLocalName() + ": terminated abruptly.");
			}
			//System.out.println(myAgent.getState());	
		}
		
		public boolean done() {
			System.out.println(myAgent.getLocalName() + ": task accomplish.");
			ServiceDescription sd = new ServiceDescription();
			sd.setType("fetch");
			sd.setName("JADE-robot-agents");
			dfd.addServices(sd);
			try {
				DFService.register(myAgent, dfd);
			}
			catch (Exception e) {
				
			}
			return true;
		}
	}
	
	
	private	class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				System.out.println("--------------------\n");
				String msg_content = msg.getContent();
				System.out.print(myAgent.getLocalName() + ": ");
				System.out.println("[message recieved].");
				System.out.println("  > Message: " + msg_content);
				
				if (msg_content.matches("status")) {
					System.out.println("  > Busy: " + busy);
				}
				else if (msg_content.matches("localization")) {
					myAgent.addBehaviour(new LocalizationBehaviour(myAgent));
				}
				else if (msg_content.matches("fetch")) {
					myAgent.addBehaviour(new FetchBehaviour(myAgent, 20));
				}
				else {
					System.out.println("  > No message recognized.");
					System.out.println("--------------------\n");
				}
			}
			block();
		}
	}
	// End of inner class OfferRequests Server
	
	protected void takeDown() {
		System.out.println(this.getLocalName() + " out of service.");
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

}