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
package org.umbrella.sftp.dynamic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.springframework.integration.MessageChannel;
import org.umbrella.ftp.DynamicFtpChannelResolver;
import org.umbrella.ftp.support.FtpInfo;

/**
 * @author Gary Russell
 * @since 2.1
 *
 */
public class DynamicFtpChannelResolverTests {

	/**
	 * Test method for {@link org.springframework.integration.samples.ftp.DynamicFtpChannelResolver#resolve(java.lang.String)}.
	 */
	@Test
	public void testResolve() {
		DynamicFtpChannelResolver dynamicFtpChannelResolver = new DynamicFtpChannelResolver();
		FtpInfo ftpInfo = new FtpInfo();
		ftpInfo.setPassword("password");
		ftpInfo.setHostAddress("host");
		ftpInfo.setDirectory("directory");
		
		ftpInfo.setUserName("customer1");
		MessageChannel channel1 = dynamicFtpChannelResolver.resolve(ftpInfo);
		assertNotNull(channel1);
		ftpInfo.setUserName("customer2");
		MessageChannel channel2 = dynamicFtpChannelResolver.resolve(ftpInfo);
		assertNotNull(channel2);
		assertNotSame(channel1, channel2);
		ftpInfo.setUserName("customer1");
		MessageChannel channel1a = dynamicFtpChannelResolver.resolve(ftpInfo);
		assertSame(channel1, channel1a);
	}

}
