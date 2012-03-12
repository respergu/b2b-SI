package org.umbrella.sftp;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.umbrella.ftp.FtpOutboundChannelAdapterTest;

public class SftpOutboundChannelAdapterTest {

	@Test
	public void testUploadFile() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
				"/META-INF/spring/sftp/SftpOutboundChannelTransferTest-context.xml",
				SftpOutboundChannelAdapterTest.class);
		ac.start();
		final InputStream inputStreamA = FtpOutboundChannelAdapterTest.class
				.getResourceAsStream("/test-files/upload.txt");

		byte[] fileABytes = IOUtils.toByteArray(inputStreamA);

		Message<byte[]> message = MessageBuilder
				.withPayload(fileABytes)
				.setHeader("remoteName", "upload-remote.txt")
				.setHeader("remoteDir", "/STS-2.7.1/workspace/b2b-int/remote-target-dir")
				.build();
		MessageChannel inputChannel = ac.getBean("inputChannel",
				MessageChannel.class);
		inputChannel.send(message);
		Thread.sleep(2000);

		assertTrue(new File("remote-target-dir/upload-remote.txt").exists());
		ac.stop();
	}

}
