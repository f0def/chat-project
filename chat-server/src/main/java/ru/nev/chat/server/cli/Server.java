package ru.nev.chat.server.cli;

import org.apache.commons.cli.*;
import ru.nev.chat.converter.MessageConverterFactory;
import ru.nev.chat.repository.MessageRepository;
import ru.nev.chat.repository.UserRepository;
import ru.nev.chat.server.ChatServer;

import java.io.IOException;

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

    ChatServer chatServer = new ChatServer(port.intValue(), MessageConverterFactory.INSTANCE.make(),
      UserRepository.make(), MessageRepository.make());
    chatServer.start();
  }
}
