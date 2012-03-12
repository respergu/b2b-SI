/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.umbrella.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.support.MessageBuilder;
import org.umbrella.ftp.support.FtpInfo;
import org.umbrella.ftp.support.TestUserManager;

/**
 * @author Gary Russell
 * 
 */
public class DynamicFtpOutboundChannelAdapterTest {

	public static final String FTP_ROOT_DIR = "target" + File.separator
			+ "ftproot";
	public static final String LOCAL_FTP_TEMP_DIR = "target" + File.separator
			+ "local-ftp-temp";
	public static FtpServer server;

	@Before
	public void setupFtpServer() throws FtpException, SocketException,
			IOException {

		File ftpRoot = new File(FTP_ROOT_DIR);
		ftpRoot.mkdirs();
		TestUserManager userManager = new TestUserManager(
				ftpRoot.getAbsolutePath());
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager(userManager);
		ListenerFactory factory = new ListenerFactory();
		factory.setPort(3333);
		serverFactory.addListener("default", factory.createListener());
		server = serverFactory.createServer();
		server.start();
	}

	@Test
	public void runDemo() throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"META-INF/spring/ftp/DynamicFtpOutboundChannelAdapterTest-context.xml");
		MessageChannel channel = ctx.getBean("toFtpDynRouter",
				MessageChannel.class);
		
		FtpInfo ftpInfo = new FtpInfo();
		String userName = "demo";
		String password = "demo";
		String host = "localhost";
		String directory = "remote";
		ftpInfo.setUserName(userName);
		ftpInfo.setPassword(password);
		ftpInfo.setHostAddress(host);
		ftpInfo.setDirectory(directory);

		final InputStream inputStreamA = FtpOutboundChannelAdapterTest.class
				.getResourceAsStream("/test-files/a.txt");
		byte[] fileABytes = IOUtils.toByteArray(inputStreamA);
		assertNotNull(fileABytes);

		Message<byte[]> message = MessageBuilder
				.withPayload(fileABytes)
				.setHeader("file_name", getReportFileName("a.txt"))
				.setHeader("ftpInfo", ftpInfo)
				.build();
		
		channel.send(message);
		Thread.sleep(2000);
		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "remote" + File.separator + getReportFileName("a.txt")).exists());
		
		
		final InputStream inputStreamB = FtpOutboundChannelAdapterTest.class.getResourceAsStream("/test-files/b.txt");
		byte[] fileBBytes = IOUtils.toByteArray(inputStreamB);
		assertNotNull(fileBBytes);
		Message<byte[]> sameProviderMessage = MessageBuilder
				.withPayload(fileBBytes)
				.setHeader("file_name", getReportFileName("b.txt"))
				.setHeader("ftpInfo", ftpInfo)
				.build();
		channel.send(sameProviderMessage);
		Thread.sleep(2000);
		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "remote" + File.separator + getReportFileName("b.txt")).exists());
		
		
		FtpInfo anotherProviderFtpInfo = new FtpInfo();
		String anotherProviderUserName = "anotherProviderUserName";
		String anotherProviderPassword = "anotherProviderPassword";
		String anotherProviderHost = "anotherProviderHost";
		String anotherProviderDirectory = "anotherProviderDirectory";
		
		anotherProviderFtpInfo.setUserName(anotherProviderUserName);
		anotherProviderFtpInfo.setPassword(anotherProviderPassword);
		anotherProviderFtpInfo.setHostAddress(anotherProviderHost);
		anotherProviderFtpInfo.setDirectory(anotherProviderDirectory);
		
		Message<byte[]> anotherProviderMessage = MessageBuilder
				.withPayload(fileBBytes)
				.setHeader("file_name", getReportFileName("anotherProvider.txt"))
				.setHeader("ftpInfo", anotherProviderFtpInfo)
				.build();
		// send to a different customer; again, check the log to see a new
		try {
			channel.send(anotherProviderMessage);
		} catch (MessageHandlingException e) {
			assertTrue(e.getCause().getCause() instanceof UnknownHostException);
			assertEquals(anotherProviderFtpInfo.getHostAddress(), e.getCause().getCause().getMessage());
		}
	}
	
	private String getReportFileName(String name) {
		return name;
	}
	
	@After
	public void shutDown() {
		server.stop();
		FileUtils.deleteQuietly(new File(FTP_ROOT_DIR));
	}


}
