package org.umbrella.ftp.support;

public class FtpInfo {
	
    private String hostAddress;
    private String userName;
    private String password;
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setDirectory(String newDirectory) {
        directory = newDirectory;
    }

    public void setHostAddress(String newHostAddress) {
        hostAddress = newHostAddress;
    }

    public void setPassword(String newPassword) {
        password = newPassword;
    }

    public void setUserName(String newUserName) {
        userName = newUserName;
    }
}
