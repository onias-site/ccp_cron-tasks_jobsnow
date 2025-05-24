package com.ccp.jn.cron.controller;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.receiver.CcpTopic;

public class CcpCronTasksController {

	public static void main(Function<CcpJsonRepresentation, CcpJsonRepresentation> jnAsyncBusinessNotifyError, String topic, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = new CcpMensageriaReceiver(topic, "operation");
		CcpTopic process = receiver.getProcess(topic, json);
		process.apply(json);

	}
	
	
}
