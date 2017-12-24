package ru.nev.chat.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nev.chat.commands.*;
import ru.nev.chat.messages.*;
import ru.nev.chat.repository.MessageRepository;
import ru.nev.chat.repository.SessionRepository;

import java.util.List;

import static ru.nev.chat.messages.User.SERVER;

public class MessageProcessQueueHandler<P> extends AbstractProcessQueueHandler<P, Message> {

  private final Logger logger = LoggerFactory.getLogger(MessageProcessQueueHandler.class);

  private final Transport<P, Message> transport;
  private final CommandParser<Cmd<List<Message>>> parser;
  private final MessageRepository<Message> messageRepository;
  private final SessionRepository sessionRepository;

  public MessageProcessQueueHandler(int threadPoolSize, Transport<P, Message> transport,
                                    MessageRepository<Message> messageRepository,
                                    SessionRepository sessionRepository) {
    super("msg-prc", threadPoolSize);

    this.transport = transport;
    this.messageRepository = messageRepository;
    this.sessionRepository = sessionRepository;

    this.parser = new CommandParser<>();
    this.parser
      .add(new ChatCommand<>("help", "show help", new HelpFn(this.parser)))
      .add(new ChatCommand<>("users", "number of connected users", new UsersFn(sessionRepository)));
  }

  @Override
  public void processMessage(Session session, P sender, Message message) {
    logger.trace("{} sent {}", session, message);

    if (message instanceof TextMessage) {
      TextMessage textMessage = (TextMessage) message;

      if (textMessage.getUser() == null) {
        User user = new User(session.getName());
        textMessage.setUser(user);
      }

      String text = textMessage.getText();
      if (session.hasName()) {
        if (text.startsWith("/")) {
          Cmd<List<Message>> cmd = parser.parse(text);
          if (cmd != null) {
            List<Message> messages = cmd.execute();
            for (Message msg : messages) {
              msg.setUser(SERVER);

              transport.write(sender, msg);
            }
          } else {
            transport.write(sender, new TextMessage(SERVER, "Unknown command " + text));
          }
        } else {
          User user = new User(session.getName());
          transport.writeAllExcept(new TextMessage(user, text), sender);
          messageRepository.add(textMessage);
        }
      } else {
        authenticate(session, sender, text);
      }
    }
  }

  private void authenticate(Session session, P sender, String name) {
    name = sessionRepository.prepareName(name);

    if (!sessionRepository.isCorrectUserName(name)) {
      String msg = String.format("Name \"%s\" is incorrect, select a different name", name);
      transport.write(sender, new NotAuthenticatedMessage(msg));
    } else if (sessionRepository.isUserNameExists(name)) {
      String msg = String.format("Name \"%s\" already exists, select a different name", name);
      transport.write(sender, new NotAuthenticatedMessage(msg));
    } else {
      sessionRepository.putName(session, name);

      transport.write(sender, new NameChangedMessage(name));
      transport.write(sender, new TextMessage(SERVER, "Type /help for the help"));
      transport.writeAllExcept(new UserJoinedMessage(name), sender);

      for (Message message : messageRepository.getLastMessages()) {
        transport.write(sender, message);
      }
    }
  }
}
