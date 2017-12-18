package ru.nev.chat.commands;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class CommandParserTest {

  @Spy
  private CommandParser<Object> parser;

  @Test
  public void testAddAndCommands() {
    ChatCommand<Object> command = new ChatCommand<>("foo", "FOO", mock(Fn.class));
    parser.add(command);

    assertThat(parser.getCommands(), contains(command));
  }

  @Test
  public void testGetNameNull() {
    assertThat(parser.getName(""), is(nullValue()));
    assertThat(parser.getName(""), is(nullValue()));
    assertThat(parser.getName("foo"), is(nullValue()));
  }

  @Test
  public void testGetName() {
    assertThat(parser.getName("/foo"), is("foo"));
  }

  @Test
  public void testGetArgsNull() {
    assertThat(parser.getArgs("/foo"), is(nullValue()));
  }

  @Test
  public void testGetArgs() {
    assertThat(parser.getArgs("/foo a b c "), is("a b c "));
  }

  @Test
  public void testParseUnknown() {
    assertThat(parser.parse("/unknown"), is(nullValue()));
  }
}
