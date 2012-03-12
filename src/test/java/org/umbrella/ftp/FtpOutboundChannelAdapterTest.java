package org.umbrella.ftp;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

public class FtpOutboundChannelAdapterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpOutboundChannelAdapterTest.class);
	
	@Test
	public void runDemo() throws Exception{
		
		ConfigurableApplicationContext ctx = 
			new ClassPathXmlApplicationContext("META-INF/spring/ftp/FtpOutboundChannelAdapterTest-context.xml");

		MessageChannel ftpChannel = ctx.getBean("ftpChannel", MessageChannel.class);

		final InputStream inputStreamA = FtpOutboundChannelAdapterTest.class.getResourceAsStream("/test-files/a.txt");
		final InputStream inputStreamB = FtpOutboundChannelAdapterTest.class.getResourceAsStream("/test-files/b.txt");
		
		byte[] fileABytes = IOUtils.toByteArray(inputStreamA);
		byte[] fileBBytes = IOUtils.toByteArray(inputStreamB);
		
		assertNotNull(fileABytes);
		assertNotNull(fileBBytes);
		
		final Message<byte[]> messageA = MessageBuilder.
				withPayload(fileABytes).
				setHeader("remoteDir", "remote").
				setHeader("file_name", "a.txt").
				build();
		final Message<byte[]> messageB = MessageBuilder.
				withPayload(fileBBytes).
				setHeader("remoteDir", "/").
				setHeader("file_name", "b.txt").
				build();
		
		ftpChannel.send(messageA);
		ftpChannel.send(messageB);
		
		Thread.sleep(2000);

		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "remote" + File.separator +"a.txt").exists());
		assertTrue(new File(TestSuite.FTP_ROOT_DIR + File.separator + "b.txt").exists());
		
		LOGGER.info("Successfully transfered file 'a.txt' and 'b.txt' to a remote FTP location.");
			
	}
   
}
