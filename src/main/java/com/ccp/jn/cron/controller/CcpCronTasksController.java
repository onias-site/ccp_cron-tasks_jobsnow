package com.ccp.jn.cron.controller;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;

/**
 * Entry point for scheduled tasks (cron jobs). Receives a topic name and JSON parameters,
 * resolves the process responsible for that topic via CcpMensageriaReceiver and runs it.
 */
public class CcpCronTasksController {

	public static void main(CcpBusiness jnAsyncBusinessNotifyError, String topic, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = CcpMensageriaReceiver.getInstance(json);
		CcpBusiness process = receiver.getProcess(topic, json);
		process.execute(json);

	}
	
	
}
