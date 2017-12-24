package ru.nev.chat.repository;

import org.junit.Test;
import ru.nev.chat.messages.Message;
import ru.nev.chat.repository.impl.InMemoryMessageRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

public class MessageRepositoryTest {

  @Test
  public void testGetLast2Messages() {
    MessageRepository<Message> messageRepository = new InMemoryMessageRepository<>(2);

    Message message1 = mock(Message.class);
    Message message2 = mock(Message.class);
    Message message3 = mock(Message.class);
    messageRepository.add(message1);
    messageRepository.add(message2);
    messageRepository.add(message3);

    assertThat(messageRepository.getLastMessages(), contains(message2, message3));
  }
}
