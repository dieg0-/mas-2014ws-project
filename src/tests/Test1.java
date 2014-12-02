package tests;

/*****************************************************************

 Purchaser1:    Program which sends QUERY_REF to agents "a1,a2 & a3"

 Author:  Jean Vaucher
 Date:    Sept 2 2003 

 Test:  % java jade.Boot main:Template a1:Responder a2:Responder

 *****************************************************************/

import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.Random;

@SuppressWarnings("serial")
public class Test1 extends Agent {
	long t0;
	Random rnd = new Random(hashCode());
	MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

	protected void setup() {
		t0 = System.currentTimeMillis();

		addBehaviour(new TickerBehaviour(this, 2000) {

			int n = 0;

			protected void onTick() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(myAgent.getAID());
				msg.setContent("#" + n);
				myAgent.send(msg);
				if (n++ > 3)
					stop();
			}
		});

		addBehaviour(new myReceiver(this, 1000, mt) {
			public void handle(ACLMessage msg) {
				System.out.println("R1:");
				if (msg == null) {
					System.out.println("Timeout:" + time());
					reset(500);
				} else
					dumpMessage(msg);
			}
		});
		addBehaviour(new myReceiver(this, 3000, mt) {
			public void handle(ACLMessage msg) {
				System.out.println("R2:");
				if (msg == null)
					System.out.println("Timeout at 3000");
				else
					dumpMessage(msg);
			}
		});
		addBehaviour(new myReceiver(this, 4000, mt) {
			public void handle(ACLMessage msg) {
				System.out.println("R3:");
				if (msg == null)
					System.out.println("Timeout at 4000");
				else
					dumpMessage(msg);
			}
		});
	}

	void dumpMessage(ACLMessage msg) {
		System.out.println(time() + ": " + getLocalName() + " gets "
				+ ACLMessage.getPerformative(msg.getPerformative()) + " from "
				+ msg.getSender().getLocalName() + ", content: "
				+ msg.getContent() + ", cid=" + msg.getConversationId());
	}

	int time() {
		return (int) (System.currentTimeMillis() - t0);
	}

}
