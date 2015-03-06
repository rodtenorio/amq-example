/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.gpe.training.osedev.amq;

import java.util.Arrays;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * A simple tool for publishing messages
 * 
 * 
 */
public class ProducerTool {

	private Destination destination;
	private int messageCount = 10;
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private String queueName = "TOOL.DEFAULT";
	private boolean transacted = false;

	public static void main(String[] args) {
		ProducerTool producerTool = new ProducerTool();
		
		String[] unknown = CommandLineSupport.setOptions(producerTool, args);

		if (unknown.length > 0) {
			System.out.println("Unknown options: " + Arrays.toString(unknown));
			System.exit(-1);
		}

		producerTool.showParameters();

		producerTool.run();		
	}
	
	public void run() {
		Connection connection = null;

		try {
			// Create the connection.
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();

			// Create the session
			Session session = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(queueName);

			// Create the producer.
			MessageProducer producer = session.createProducer(destination);

			// Start sending messages
			sendLoop(session, producer);

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Throwable ignore) {
			}
		}
	}

	protected void sendLoop(Session session, MessageProducer producer)
			throws Exception {

		for (int i = 0; i < messageCount; i++) {

			TextMessage message = session
					.createTextMessage(createMessageText(i));

			String msg = message.getText();
			if (msg.length() > 50) {
				msg = msg.substring(0, 50) + "...";
			}
			
			System.out.println("Sending message: '"
					+ msg + "'");

			producer.send(message);
		}
	}
	
	private String createMessageText(int index) {
		return ("Message: " + index + " sent at: " + new Date());
	}

	public void showParameters() {
		System.out
				.println("\n\n-------------------------------------------------------\n");
		System.out.println("Connecting to URL: " + url + " (" + user + ":"
				+ password + ")");
		System.out.println("Sending messages to queue: " + queueName);
		System.out
				.println("\n-------------------------------------------------------\n");
	}
	
	public void setPassword(String pwd) {
		this.password = pwd;
	}

	public void setQueueName(String theQueueName) {
		this.queueName = theQueueName;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

}