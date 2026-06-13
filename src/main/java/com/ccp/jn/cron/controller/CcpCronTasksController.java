package com.ccp.jn.cron.controller;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;

/**
 * Ponto de entrada para execução de tarefas agendadas (cron jobs). Recebe o nome de um tópico e parâmetros
 * em formato JSON, resolve o processo responsável por esse tópico via CcpMensageriaReceiver e o executa.
 */
public class CcpCronTasksController {

	public static void main(CcpBusiness jnAsyncBusinessNotifyError, String topic, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = CcpMensageriaReceiver.getInstance(json);
		CcpBusiness process = receiver.getProcess(topic, json);
		process.apply(json);

	}
	
	
}
