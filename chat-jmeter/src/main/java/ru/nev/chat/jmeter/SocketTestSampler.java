package ru.nev.chat.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import ru.nev.chat.client.ChatClient;
import ru.nev.chat.client.ChatResponseHandler;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.MessageConverterFactory;
import ru.nev.chat.messages.Message;
import ru.nev.chat.messages.NameChangedMessage;
import ru.nev.chat.messages.NotAuthenticatedMessage;
import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.transport.MessageTransportFactory;
import ru.nev.chat.transport.SocketClientTransport;

import java.io.IOException;
import java.io.Serializable;

public class SocketTestSampler extends AbstractJavaSamplerClient implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String HOST = "HOST";
  private static final String PORT = "PORT";
  private static final String MIN_MSG_COUNT = "MIN_MSG_COUNT";
  private static final String MAX_MSG_COUNT = "MAX_MSG_COUNT";
  private static final String MIN_MSG_LENGTH = "MIN_MSG_LENGTH";
  private static final String MAX_MSG_LENGTH = "MAX_MSG_LENGTH";

  @Override
  public Arguments getDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument(HOST, "localhost");
    defaultParameters.addArgument(PORT, "3128");
    defaultParameters.addArgument(MIN_MSG_COUNT, "5");
    defaultParameters.addArgument(MAX_MSG_COUNT, "15");
    defaultParameters.addArgument(MIN_MSG_LENGTH, "40");
    defaultParameters.addArgument(MAX_MSG_LENGTH, "80");
    return defaultParameters;
  }

  @Override
  public SampleResult runTest(JavaSamplerContext context) {
    String host = context.getParameter(HOST);
    int port = context.getIntParameter(PORT);
    int minMsgCount = context.getIntParameter(MIN_MSG_COUNT);
    int maxMsgCount = context.getIntParameter(MAX_MSG_COUNT);
    int minMsgLength = context.getIntParameter(MIN_MSG_LENGTH);
    int maxMsgLength = context.getIntParameter(MAX_MSG_LENGTH);

    SampleResult result = new SampleResult();
    result.sampleStart();

    try {
      process(host, port, minMsgCount, maxMsgCount, minMsgLength, maxMsgLength);
      success(result);
    } catch (Exception e) {
      error(result, e);
    }

    result.sampleEnd();

    return result;
  }

  public void process(String host, int port, int minMsgCount, int maxMsgCount,
                      int minMsgLength, int maxMsgLength) throws IOException, InterruptedException {
    final int[] uniqueNameAttempt = {0};
    int uniqueNameAttempts = 20;

    final ChatClient[] chatClient = {null};
    ChatResponseHandler handler = message -> {
      getNewLogger().debug("Received: {}", message);

      if (message instanceof NotAuthenticatedMessage) {
        authenticate(chatClient[0]);
        uniqueNameAttempt[0]++;
        if (uniqueNameAttempt[0] > uniqueNameAttempts) {
          throw new RuntimeException("I'm tired of coming up with a name, sorry");
        }
      } else if (message instanceof NameChangedMessage) {
        spam(minMsgCount, maxMsgCount, minMsgLength, maxMsgLength, chatClient[0]);

        //omg
        new Thread(() -> {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          chatClient[0].stop();
        }).start();
      }
    };

    MessageConverter<Message> converter = MessageConverterFactory.INSTANCE.make();
    SocketClientTransport<Message> transport = MessageTransportFactory.socketClient(host, port, converter);

    chatClient[0] = new ChatClient<>(transport, handler);
    chatClient[0].start();

//    while (chatClient[0].isRunning()) {
//      Thread.sleep(10);
//    }
  }

  private void authenticate(ChatClient chatClient) {
    sendText(chatClient, Utils.generateName(10, 10000));
  }

  private void spam(int minMsgCount, int maxMsgCount, int minMsgLength, int maxMsgLength, ChatClient chatClient) {
    for (int i = 0; i < Utils.randomBetween(minMsgCount, maxMsgCount); i++) {
      //Utils.generateText(minMsgLength, maxMsgLength)
      sendText(chatClient, "My_text_" + i);
    }
  }

  private void sendText(ChatClient chatClient, String text) {
    getNewLogger().debug("Send: {}", text);
    chatClient.sendMessage(new TextMessage(text));
  }

  private void success(SampleResult result) {
    result.setSuccessful(true);
    result.setResponseMessage("Successfully performed action");
    result.setResponseCodeOK();
  }

  private void error(SampleResult result, Exception e) {
    result.setSuccessful(false);
    result.setResponseMessage("Exception: " + e);
    getNewLogger().error("", e);
    java.io.StringWriter stringWriter = new java.io.StringWriter();
    e.printStackTrace(new java.io.PrintWriter(stringWriter));
    result.setResponseData(stringWriter.toString().getBytes());
    result.setDataType(SampleResult.TEXT);
    result.setResponseCode("500");
  }

}
