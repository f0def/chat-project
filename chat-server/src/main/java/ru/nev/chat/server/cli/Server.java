package ru.nev.chat.server.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import ru.nev.chat.converter.MessageConverter;
import ru.nev.chat.converter.MessageConverterFactory;
import ru.nev.chat.messages.Message;
import ru.nev.chat.repository.MessageRepository;
import ru.nev.chat.repository.SessionRepository;
import ru.nev.chat.server.ChatServer;
import ru.nev.chat.transport.MessageProcessQueueHandler;
import ru.nev.chat.transport.MessageTransportFactory;
import ru.nev.chat.transport.SocketServerTransport;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class Server {

  public static void main(String[] args) throws IOException {
    Options options = new Options();

    Option portOption = new Option("p", "port", true, "port to listen");
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
      formatter.printHelp("server", options);

      System.exit(1);
      return;
    }

    Long port;
    try {
      port = (Long) cmd.getParsedOptionValue("port");
    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(1);
      return;
    }

    start(port.intValue());
  }

  private static void start(int port) {
    MessageConverter<Message> converter = MessageConverterFactory.INSTANCE.make();
    SocketServerTransport<Message> transport = MessageTransportFactory.socketServer(port, converter);

    SessionRepository sessionRepository = SessionRepository.make();
    MessageProcessQueueHandler<SelectionKey> readQueueHandler = new MessageProcessQueueHandler<>(8,
      transport, MessageRepository.make(), sessionRepository);
    readQueueHandler.start();

    ChatServer chatServer = new ChatServer<>(transport, readQueueHandler, sessionRepository);
    chatServer.start();
  }
}
