package com.ccp.jn.cron.controller;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
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
import com.ccp.implementations.http.request.log.gcp.logging.CcpGcpHttpRequestLog;
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
				CcpApacheMimeHttp.INSTANCE,
				CcpGcpHttpRequestLog.INSTANCE
		);
		String topic = args[0];
		String parameters = args[1];
		main(topic, parameters);
		System.exit(0);
	}

	/** The texts of this class. */
	static enum Constants implements CcpJsonFieldName {
		/** Separates the topics of a job that runs several processes, one after the other. */
		TOPICS_SEPARATOR { public String getValue() { return ","; } },
	}

	/**
	 * Runs the process of each topic, in order: the parameters (a JSON text naming the receiver in
	 * {@code mensageriaReceiver}) select the {@code CcpMensageriaReceiver}, which resolves and executes the process of the
	 * topic. Since 2026-11-24 a job may name several topics separated by commas (the backup and then the daily resumes of
	 * the positions, for example): a failing process does not stop the next ones, and the first failure is thrown at the end.
	 * @param topics the topics (the class names of the processes), separated by commas
	 * @param parameters the JSON parameters of the processes
	 * @throws Exception when a process fails
	 */
	public static void main(String topics, String parameters) throws Exception {
		CcpJsonRepresentation json = new CcpJsonRepresentation(parameters);
		CcpMensageriaReceiver receiver = CcpMensageriaReceiver.getInstance(json);
		String separator = Constants.TOPICS_SEPARATOR.getValue();
		String[] topicList = topics.split(separator);
		List<Exception> failures = new ArrayList<>();
		for (String topic : topicList) {
			String trimmedTopic = topic.trim();
			try {
				CcpBusiness process = receiver.getProcess(trimmedTopic, json);
				process.execute(json);
			} catch (Exception failure) {
				failure.printStackTrace();
				failures.add(failure);
			}
		}
		boolean everyProcessRan = failures.isEmpty();
		if(everyProcessRan) {
			return;
		}
		Exception firstFailure = failures.get(0);
		throw firstFailure;
	}
	
	
}
