package org.umbrella.sftp;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.integration.MessageChannel;
import org.umbrella.ftp.support.FtpInfo;

public class DynamicSftpChannelResolver {


	private final Map<String, MessageChannel> channels = new HashMap<String, MessageChannel>();

	/**
	 * Resolve a customer to a channel, where each customer gets a private
	 * application context and the channel is the inbound channel to that
	 * application context.
	 *
	 * @param customer
	 * @return a channel
	 */
	public MessageChannel resolve(FtpInfo ftpInfo) {
		String customer = ftpInfo.getUserName();
		MessageChannel channel = this.channels.get(customer);
		if (channel == null) {
			channel = createNewCustomerChannel(ftpInfo);
		}
		return channel;
	}

	private synchronized MessageChannel createNewCustomerChannel(FtpInfo ftpInfo) {
		MessageChannel channel = this.channels.get(ftpInfo.getUserName());
		if (channel == null) {
			ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
					new String[] { "/META-INF/spring/sftp/dynamic-sftp-outbound-adapter-context.xml" },
					false);
			this.setEnvironmentForCustomer(ctx, ftpInfo);
			ctx.refresh();
			channel = ctx.getBean("toSftpChannel", MessageChannel.class);
			this.channels.put(ftpInfo.getUserName(), channel);
		}
		return channel;
	}

	/**
	 * Use Spring 3.1. environment support to set properties for the
	 * customer-specific application context.
	 *
	 * @param ctx
	 * @param customer
	 */
	private void setEnvironmentForCustomer(ConfigurableApplicationContext ctx,
			FtpInfo ftpInfo) {
		StandardEnvironment env = new StandardEnvironment();
		Properties props = new Properties();
		// populate properties for customer
		props.setProperty("host", ftpInfo.getHostAddress());
		props.setProperty("user", ftpInfo.getUserName());
		props.setProperty("password", ftpInfo.getPassword());
		props.setProperty("remote.directory", ftpInfo.getDirectory());
		PropertiesPropertySource pps = new PropertiesPropertySource("ftpprops", props);
		env.getPropertySources().addLast(pps);
		ctx.setEnvironment(env);
	}

}
