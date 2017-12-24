package ru.nev.chat.client.cli;

import org.apache.commons.cli.*;
import ru.nev.chat.client.ChatClient;
import ru.nev.chat.client.ChatResponseHandler;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.MessageConverterFactory;
import ru.nev.chat.messages.Message;
import ru.nev.chat.messages.TextMessage;
import ru.nev.chat.transport.MessageTransportFactory;
import ru.nev.chat.transport.SocketClientTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

  public static void main(String args[]) {
    Options options = new Options();

    Option hostOption = new Option("h", "host", true, "server ip/url");
    hostOption.setRequired(true);
    options.addOption(hostOption);

    Option portOption = new Option("p", "port", true, "server port");
    portOption.setRequired(true);
    portOption.setType(Number.class);
    options.addOption(portOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("client", options);

      System.exit(1);
      return;
    }

    String host = cmd.getOptionValue("host");
    Long port;
    try {
      port = (Long) cmd.getParsedOptionValue("port");
    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      start(host, port.intValue());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void start(String host, int port) throws IOException, InterruptedException {
    MessageConverter<Message> converter = MessageConverterFactory.INSTANCE.make();
    SocketClientTransport<Message> transport = MessageTransportFactory.socketClient(host, port, converter);

    ChatResponseHandler<Message> handler = message -> System.out.printf("@%s: %s%n", message.getUser().getName(), message.toChat());

    ChatClient chatClient = new ChatClient<>(transport, handler);
    chatClient.start();

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      if (br.ready()) {
        chatClient.sendMessage(new TextMessage(br.readLine()));
      }
      Thread.sleep(10);
    }
  }
}
