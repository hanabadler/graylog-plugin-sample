package org.graylog.plugins;


import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendToMailUsingSendGridAlarmCallback implements AlarmCallback{

    private static final Logger LOG = LoggerFactory.getLogger(SendToMailUsingSendGridAlarmCallback.class);

	private final OkHttpClient httpClient;
    private Configuration configuration;
    
	private static final String SENDGRID_AUTHORIZATION = "sendgrid_authorization";
	private static final String SENDGRID_URL = "sendgrid_url";
	private static final String SENDGRID_ADMINMAIL= "sendgrid_adminmail";
	private static final String SENDGRID_FROMADMINMAIL= "sendgrid_fromadminmail";
	
	
    @Inject
    public SendToMailUsingSendGridAlarmCallback(final OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    
    
    private String buildMessage(final AlertCondition.CheckResult result) {
        final String msg = "[Graylog] " + result.getMatchingMessages().get(0).getMessage();

        return msg;
    }
    
    
	@Override
	public void call(Stream arg0, AlertCondition.CheckResult result)
			throws AlarmCallbackException {
	
		String autorizationBearer = configuration.getString(SENDGRID_AUTHORIZATION);
		//https://api.sendgrid.com/v3/mail/send
		String sendgridUrl		   = configuration.getString(SENDGRID_URL);
		String sendgridToMail	   = configuration.getString(SENDGRID_ADMINMAIL);
		String sendgridfromMail	   = configuration.getString(SENDGRID_FROMADMINMAIL);
		
		
		String message = "GL Message ";
		
		if (result.getMatchingMessages() == null){
			message  = message  + " result.getMatchingMessages() == null";
			
		}
		if (result.getMatchingMessages() != null){
				if (result.getMatchingMessages().size() == 0){
					
					message  = message  + " result.getMatchingMessages().size == 0";
				}else{
					
					message  = message  + " result.getMatchingMessages().size == "  +result.getMatchingMessages().size();
					message  = message  + buildMessage(result);
				}
		}
		    
		try{


				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, "{\"personalizations\": [{\"to\": [{\"email\": \""+sendgridToMail+"\"}]}],\"from\": {\"email\": \""+sendgridfromMail+"\"},\"subject\": \"graylog alert\",\"content\": [{\"type\": \"text/plain\", \"value\": \""+message+"\"}]}");
				Request request = new Request.Builder()
				  .url(sendgridUrl)
				  .post(body)
				  .addHeader("authorization", autorizationBearer )
				  .addHeader("content-type", "application/json")
				  .addHeader("cache-control", "no-cache")
				  
				  .build();

				Response response = httpClient.newCall(request).execute();
			
		}catch(Exception e){
					e.printStackTrace();
			}
		
		
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// TODO Auto-generated method stub
		if (!configuration.stringIsSet(SENDGRID_URL )) {
			throw new ConfigurationException(SENDGRID_URL+ " is mandatory and must be not be null or empty.");
		}
		if (!configuration.getString(SENDGRID_URL).startsWith("https://")) {
			throw new ConfigurationException(SENDGRID_URL+ " should start with https://.");
		}
		if (!configuration.stringIsSet(SENDGRID_AUTHORIZATION)) {
			throw new ConfigurationException(SENDGRID_AUTHORIZATION+ " is mandatory and must be not be null or empty.");
		}
		if (!configuration.stringIsSet(SENDGRID_ADMINMAIL)) {
			throw new ConfigurationException(SENDGRID_ADMINMAIL+ " is mandatory and must be not be null or empty.");
		}
		if (!configuration.stringIsSet(SENDGRID_FROMADMINMAIL)) {
			throw new ConfigurationException(SENDGRID_FROMADMINMAIL+ " is mandatory and must be not be null or empty.");
		}
	}

	@Override
	public Map<String, Object> getAttributes() {
		// TODO Auto-generated method stub
		return configuration.getSource();

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SendGrid alarm callback";
	}

	@Override
	public ConfigurationRequest getRequestedConfiguration() {
		// TODO Auto-generated method stub
		final ConfigurationRequest configurationRequest = new ConfigurationRequest();
		
		configurationRequest.addField(new TextField(SENDGRID_URL, "SendGrid Base URL", "https://api.sendgrid.com/v3/mail/send",
				"The base url of the SendGrid .", ConfigurationField.Optional.NOT_OPTIONAL));
		
		
		configurationRequest.addField(new TextField(SENDGRID_AUTHORIZATION, "SendGrid Bearer Token", "",
				"", ConfigurationField.Optional.NOT_OPTIONAL));
		
		
		configurationRequest.addField(new TextField(SENDGRID_ADMINMAIL, "SendGRid Destination Mail", "touser@example.com",
				"", ConfigurationField.Optional.NOT_OPTIONAL));
		
		configurationRequest.addField(new TextField(SENDGRID_FROMADMINMAIL, "SendGRid from Mail", "fromuser@example.com",
				"", ConfigurationField.Optional.NOT_OPTIONAL));
		
		
	
		
		return configurationRequest;
	}

	@Override
	public void initialize(Configuration config)
			throws AlarmCallbackConfigurationException {
		// TODO Auto-generated method stub
		  this.configuration = config;
		
	}

	
}
