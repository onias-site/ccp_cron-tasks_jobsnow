package com.ccp.jn.cron.controller;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;

/**
 * Entry point for scheduled tasks (cron jobs). Receives a topic name and JSON parameters,
 * resolves the process responsible for that topic via CcpMensageriaReceiver and runs it.
 */
public class CcpCronTasksController {

	/**
	 * Runs the process of a topic: the parameters (a JSON text naming the receiver in {@code mensageriaReceiver}) select
	 * the {@code CcpMensageriaReceiver}, which resolves and executes the process of the topic.
	 * @param topic the topic (the class name of the process)
	 * @param parameters the JSON parameters of the process
	 * @throws Exception when the process fails
	 */
	public static void main(String topic, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = CcpMensageriaReceiver.getInstance(json);
		CcpBusiness process = receiver.getProcess(topic, json);
		process.execute(json);

	}
	
	
}
