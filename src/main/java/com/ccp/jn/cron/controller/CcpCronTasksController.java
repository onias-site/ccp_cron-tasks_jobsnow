package com.ccp.jn.cron.controller;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;

public class CcpCronTasksController {

	public static void main(Function<CcpJsonRepresentation, CcpJsonRepresentation> jnAsyncBusinessNotifyError, String topic, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = CcpMensageriaReceiver.getInstance(json);
		Function<CcpJsonRepresentation, CcpJsonRepresentation> process = receiver.getProcess(topic, json);
		process.apply(json);

	}
	
	
}
