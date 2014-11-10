/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol‡s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 1.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package robots;

import jade.core.Agent;


public class RobotAgent extends Agent {

	protected void setup(){
		System.out.println("Hello, I am "+getLocalName());
	}
}
