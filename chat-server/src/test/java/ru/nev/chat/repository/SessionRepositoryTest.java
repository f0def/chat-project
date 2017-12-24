package ru.nev.chat.repository;

import org.junit.Test;
import ru.nev.chat.repository.impl.InMemorySessionRepository;
import ru.nev.chat.transport.Session;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class SessionRepositoryTest {

  private SessionRepository sessionRepository = new InMemorySessionRepository();

  @Test
  public void testPrepareName() {
    assertThat(sessionRepository.prepareName(null), is(nullValue()));
    assertThat(sessionRepository.prepareName("FOO "), is("foo"));
  }

  @Test
  public void testIsCorrectName() {
    assertThat(sessionRepository.isCorrectUserName("foo"), is(true));
    assertThat(sessionRepository.isCorrectUserName("/foo"), is(false));
    assertThat(sessionRepository.isCorrectUserName("server"), is(false));
  }

  @Test
  public void testIsUserNameExists() {
    sessionRepository.putName(mock(Session.class), "foo");
    assertThat(sessionRepository.isUserNameExists("foo"), is(true));
    assertThat(sessionRepository.isUserNameExists("bar"), is(false));
  }

  @Test
  public void testPutName() {
    Session mock = new Session(null);
    sessionRepository.putName(mock, "foo");
    assertThat(mock.getName(), is("foo"));
  }

  @Test
  public void testRemoveAndSessionCount() {
    sessionRepository.put("foo", mock(Session.class));
    assertThat(sessionRepository.getSessionCount(), is(1));
    sessionRepository.remove("foo");
    assertThat(sessionRepository.getSessionCount(), is(0));
  }
}
