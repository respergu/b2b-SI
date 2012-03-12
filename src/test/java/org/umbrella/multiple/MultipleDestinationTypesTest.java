package org.umbrella.multiple;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.umbrella.ftp.FtpOutboundChannelAdapterTest;
import org.umbrella.ftp.TestSuite;
import org.umbrella.ftp.support.EngineInfo;
import org.umbrella.ftp.support.FtpInfo;
import org.umbrella.ftp.support.TestUserManager;
import org.umbrella.sftp.MyPasswordAuthenticator;
import org.umbrella.sftp.MyPublickeyAuthenticator;

public class MultipleDestinationTypesTest {


	public static final String FTP_ROOT_DIR = "target" + File.separator
			+ "ftproot";
	public static final String LOCAL_FTP_TEMP_DIR = "target" + File.separator
			+ "local-ftp-temp";
	private FtpServer server;
	private SshServer sshd;

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
	
	@Before
	public  void setupSFtpServer() throws Exception {
		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(22);
		sshd.setPasswordAuthenticator(new MyPasswordAuthenticator());
		sshd.setPublickeyAuthenticator(new MyPublickeyAuthenticator());
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		sshd.setSubsystemFactories(Arrays
				.<NamedFactory<Command>> asList(new SftpSubsystem.Factory()));
		sshd.setCommandFactory(new ScpCommandFactory());
		sshd.start();
		cleanFiles();
	}

	@Test
	public void runDemo() throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"META-INF/spring/multiple/MultipleDynamicOutboundChannelAdaptersTest-context.xml");
		MessageChannel channel = ctx.getBean("reports", MessageChannel.class);
		
		//FTP
		EngineInfo ftpEngineInfo = new EngineInfo();
		ftpEngineInfo.setSendType("FTP");
		
		FtpInfo ftpInfo = new FtpInfo();
		String userName = "demo";
		String password = "demo";
		String host = "localhost";
		String directory = "remote";
		ftpInfo.setUserName(userName);
		ftpInfo.setPassword(password);
		ftpInfo.setHostAddress(host);
		ftpInfo.setDirectory(directory);

		final InputStream inputStreamA = MultipleDestinationTypesTest.class.getResourceAsStream("/test-files/a.txt");
		byte[] fileABytes = IOUtils.toByteArray(inputStreamA);
		assertNotNull(fileABytes);

		Message<byte[]> message = MessageBuilder
				.withPayload(fileABytes)
				.setHeader("file_name", getReportFileName("a.txt"))
				.setHeader("ftpInfo", ftpInfo)
				.setHeader("engineInfo", ftpEngineInfo)
				.build();
		
		channel.send(message);
		Thread.sleep(2000);
		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "remote" + File.separator + getReportFileName("a.txt")).exists());
		
		final InputStream inputStreamB = MultipleDestinationTypesTest.class.getResourceAsStream("/test-files/b.txt");
		byte[] fileBBytes = IOUtils.toByteArray(inputStreamB);
		assertNotNull(fileBBytes);
		Message<byte[]> sameProviderMessage = MessageBuilder
				.withPayload(fileBBytes)
				.setHeader("file_name", getReportFileName("b.txt"))
				.setHeader("ftpInfo", ftpInfo)
				.setHeader("engineInfo", ftpEngineInfo)
				.build();
		channel.send(sameProviderMessage);
		Thread.sleep(2000);
		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "remote" + File.separator + getReportFileName("b.txt")).exists());
		
		//SFTP
		EngineInfo sftpEngineInfo = new EngineInfo();
		sftpEngineInfo.setSendType("SFTP");
		FtpInfo sftpInfo = new FtpInfo();
		sftpInfo.setDirectory("/STS-2.7.1/workspace/b2b-int/remote-target-dir");
		sftpInfo.setHostAddress("localhost");
		sftpInfo.setUserName("login");
		sftpInfo.setPassword("testPassword");
		
		final InputStream sftpInputStream = MultipleDestinationTypesTest.class.getResourceAsStream("/test-files/upload.txt");
		byte[] sftpBytes = IOUtils.toByteArray(sftpInputStream);
        assertNotNull(sftpBytes);
        
		Message<byte[]> sftMessage = MessageBuilder
				.withPayload(sftpBytes)
				.setHeader("file_name", getReportFileName("upload-remote.txt"))
				.setHeader("ftpInfo", sftpInfo)
				.setHeader("engineInfo", sftpEngineInfo)
				.build();
		channel.send(sftMessage);
		Thread.sleep(2000);

		assertTrue(new File("remote-target-dir/upload-remote.txt").exists());
		
	}
	
	private String getReportFileName(String name) {
		return name;
	}
	
	@After
	public void shutDown() {
		server.stop();
		FileUtils.deleteQuietly(new File(FTP_ROOT_DIR));
	}
	
	@After
	public void tearDown() throws InterruptedException {
		sshd.stop();
		cleanFiles();
	}

	private void cleanFiles() {
		File uploaded = new File("remote-target-dir/upload-remote.txt");
		if (uploaded.exists()) {
			uploaded.delete();
		}
	}




}
