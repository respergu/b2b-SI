package org.umbrella.ftp;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.umbrella.ftp.support.TestUserManager;
import org.umbrella.sftp.MyPasswordAuthenticator;
import org.umbrella.sftp.MyPublickeyAuthenticator;
import org.umbrella.sftp.SftpOutboundChannelAdapterTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	FtpOutboundChannelAdapterTest.class,
	SftpOutboundChannelAdapterTest.class})
public class TestSuite {

	public static final String FTP_ROOT_DIR = "target" + File.separator + "ftproot";
	public static final String LOCAL_FTP_TEMP_DIR = "target" + File.separator + "local-ftp-temp";
	private static SshServer sshd;
	public static FtpServer server;

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@BeforeClass
	public static void setupFtpServer() throws FtpException, SocketException,
			IOException {

		File ftpRoot = new File(FTP_ROOT_DIR);
		ftpRoot.mkdirs();
		TestUserManager userManager = new TestUserManager(ftpRoot.getAbsolutePath());
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager(userManager);
		ListenerFactory factory = new ListenerFactory();
		factory.setPort(3333);
		serverFactory.addListener("default", factory.createListener());
		server = serverFactory.createServer();
		server.start();
	}

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setupSFtpServer() throws Exception {
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

	@AfterClass
	public static void shutDown() {
		server.stop();
		FileUtils.deleteQuietly(new File(FTP_ROOT_DIR));
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		sshd.stop();
		cleanFiles();
	}

	private static void cleanFiles() {
		File uploaded = new File("remote-target-dir/upload-remote.txt");
		if (uploaded.exists()) {
			uploaded.delete();
		}
	}

}
