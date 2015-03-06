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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * A simple tool for consuming messages
 * 
 * 
 */
public class ConsumerTool implements MessageListener, ExceptionListener {

	private Destination destination;
	private Session session;

	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url = ActiveMQConnection.DEFAULT_BROKER_URL;

	private String queueName = "TOOL.DEFAULT";	
	private boolean transacted = false;

	public static void main(String[] args) {

		ConsumerTool consumerTool = new ConsumerTool();

		String[] unknown = CommandLineSupport.setOptions(consumerTool, args);
		
		if (unknown.length > 0) {
			System.out.println("Unknown options: " + Arrays.toString(unknown));
			System.exit(-1);
		}

		consumerTool.showParameters();

		consumerTool.run();
	}


	public void run() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			Connection connection = connectionFactory.createConnection();

			connection.setExceptionListener(this);
			connection.start();

			session = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(queueName);

			MessageConsumer consumer = session.createConsumer(destination);

			consumer.setMessageListener(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onMessage(Message message) {

		try {

			if (message instanceof TextMessage) {
				TextMessage txtMsg = (TextMessage) message;

				String msg = txtMsg.getText();
				int length = msg.length();
				if (length > 50) {
					msg = msg.substring(0, 50) + "...";
				}
				System.out.println("Received: '" + msg + "'");
			} else {
				System.out.println("Received: '" + message + "'");
			}

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		ex.printStackTrace();
		System.exit(1);
	}

	public void showParameters() {
		System.out
				.println("\n\n-------------------------------------------------------\n");
		System.out.println("Connecting to URL: " + url + " (" + user + ":"
				+ password + ")");
		System.out.println("Consuming messages from queue: " + queueName);
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