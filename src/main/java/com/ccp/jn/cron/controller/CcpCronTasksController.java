package com.ccp.jn.cron.controller;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.implementations.cache.gcp.memorystore.CcpGcpMemoryStore;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.email.sendgrid.CcpSendGridEmailProvider;
import com.ccp.implementations.file.bucket.gcp.CcpGcpFileBucket;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.main.authentication.gcp.oauth.CcpGcpMainAuthentication;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;

/**
 * Entry point for scheduled tasks (cron jobs). Receives a topic name and JSON parameters,
 * resolves the process responsible for that topic via CcpMensageriaReceiver and runs it.
 */
public class CcpCronTasksController {

	/**
	 * The standalone process: wires the production dependencies, runs the process and ends. The business classes come at
	 * runtime, named by the topic and by {@code mensageriaReceiver}. What the process sends to the messaging runs here, in
	 * the calling thread: this jar lives on the machine of the consumers, so a queue would only bring the work back to it.
	 * @param args the topic and the JSON parameters
	 * @throws Exception when the process fails
	 */
	public static void main(String[] args) throws Exception {
		CcpGsonJsonHandler jsonHandler = CcpGsonJsonHandler.INSTANCE;
		CcpDependencyInjection.loadAllDependencies(jsonHandler);
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpGcpMemoryStore.INSTANCE,
				CcpGcpFileBucket.INSTANCE,
				CcpSendGridEmailProvider.INSTANCE,
				CcpTelegramInstantMessenger.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpGcpMainAuthentication.INSTANCE,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
		String topic = args[0];
		String parameters = args[1];
		main(topic, parameters);
		System.exit(0);
	}

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
