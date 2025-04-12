package za.co.sindi.email.aws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import za.co.sindi.email.AbstractJavaMailSender;
import za.co.sindi.email.exception.MailSendException;

/**
 * @author Buhake Sindi
 * @since 02 March 2025
 */
public class SESJavaMailSender extends AbstractJavaMailSender {
	
	private SesClient sesClient;
	
	@Override
	public void send(Message... messages) throws MailSendException {
		// TODO Auto-generated method stub
		if (messages == null || messages.length == 0) {
			throw new IllegalArgumentException("No mail messages were provided.");
		}
		
		try {
			 AwsRequestOverrideConfiguration myConf = AwsRequestOverrideConfiguration.builder()
	                    .build();
			
			for (Message message : messages) {
				//Just a fix, in case someone forgets to set the date....				
				if (message.getSentDate() == null) {
					message.setSentDate(new Date());
				}
				
				//Save first
				message.saveChanges();
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				message.writeTo(outputStream);
				
				SdkBytes data = SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()));
	            RawMessage rawMessage = RawMessage.builder()
	                    .data(data)
	                    .build();
	            
	            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
	                    .rawMessage(rawMessage)
	                    .overrideConfiguration(myConf)
	                    .build();

	            sesClient.sendRawEmail(rawEmailRequest);
			}
		} catch (MessagingException | IOException e) {
			// TODO Auto-generated catch block
			throw new MailSendException(e);
		} 
	}

	/* (non-Javadoc)
	 * @see za.co.sindi.email.AbstractJavaMailSender#ensureSession()
	 */
	@Override
	protected void ensureSession() {
		// TODO Auto-generated method stub
		if (session == null) {
			session = Session.getDefaultInstance(new Properties());
		}
		super.ensureSession();
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		sesClient.close();
	}
}
