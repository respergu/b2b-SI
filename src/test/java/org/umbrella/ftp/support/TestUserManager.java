package org.umbrella.ftp.support;


import java.util.Arrays;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class TestUserManager extends AbstractUserManager {
	 private BaseUser testUser;
	    private BaseUser anonUser;

	    private static final String TEST_USERNAME = "demo";
	    private static final String TEST_PASSWORD = "demo";

		public TestUserManager(String homeDirectory) {
	        super("admin", new ClearTextPasswordEncryptor());

	        testUser = new BaseUser();
	        testUser.setAuthorities(Arrays.asList(new Authority[] {new ConcurrentLoginPermission(1, 1), new WritePermission()}));
	        testUser.setEnabled(true);
	        testUser.setHomeDirectory(homeDirectory);
	        testUser.setMaxIdleTime(10000);
	        testUser.setName(TEST_USERNAME);
	        testUser.setPassword(TEST_PASSWORD);

	        anonUser = new BaseUser(testUser);
	        anonUser.setName("anonymous");
	    }

	    public User getUserByName(String username) throws FtpException {
	        if(TEST_USERNAME.equals(username)) {
	            return testUser;
	        } else if(anonUser.getName().equals(username)) {
	            return anonUser;
	        }

	        return null;
	    }

	    public String[] getAllUserNames() throws FtpException {
	        return new String[] {TEST_USERNAME, anonUser.getName()};
	    }

	    public void delete(String username) throws FtpException {
	    	throw new UnsupportedOperationException("Deleting of FTP Users is not supported.");
	    }

	    public void save(User user) throws FtpException {
	        throw new UnsupportedOperationException("Saving of FTP Users is not supported.");
	    }

	    public boolean doesExist(String username) throws FtpException {
	        return (TEST_USERNAME.equals(username) || anonUser.getName().equals(username)) ? true : false;
	    }

	    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
	        if(UsernamePasswordAuthentication.class.isAssignableFrom(authentication.getClass())) {
	            UsernamePasswordAuthentication upAuth = (UsernamePasswordAuthentication) authentication;

	            if(TEST_USERNAME.equals(upAuth.getUsername()) && TEST_PASSWORD.equals(upAuth.getPassword())) {
	                return testUser;
	            }

	            if(anonUser.getName().equals(upAuth.getUsername())) {
	                return anonUser;
	            }
	        } else if(AnonymousAuthentication.class.isAssignableFrom(authentication.getClass())) {
	            return anonUser;
	        }

	        return null;
	    }
	}