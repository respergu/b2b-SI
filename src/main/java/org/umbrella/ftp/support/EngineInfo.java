package org.umbrella.ftp.support;


public class EngineInfo {
	
    protected String engineId;
    protected String providerId;
    protected Integer providerType;
    protected String providerName;
    protected String sendType;
    protected String reportName;
    protected String reportDirectory;
    protected FtpInfo ftpInfo;
    
	public String getEngineId() {
		return engineId;
	}
	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public Integer getProviderType() {
		return providerType;
	}
	public void setProviderType(Integer providerType) {
		this.providerType = providerType;
	}
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	public String getSendType() {
		return sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public String getReportDirectory() {
		return reportDirectory;
	}
	public void setReportDirectory(String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}
	public FtpInfo getFtpInfo() {
		return ftpInfo;
	}
	public void setFtpInfo(FtpInfo ftpInfo) {
		this.ftpInfo = ftpInfo;
	}
}
