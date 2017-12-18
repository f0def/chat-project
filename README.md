# Building

```bash
$ mvn install
```

# Server startup

```bash
$ java -jar chat-server/target/chat-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar --port=3128
```

# Client startup

```bash
$ java -jar chat-client/target/chat-client-0.0.1-SNAPSHOT-jar-with-dependencies.jar --host=localhost --port=3128
```

# Stress testing

- Download [Jmeter](http://jmeter.apache.org/)
- Export `$JMETER_HOME`
- Copy (or make links)

```bash
$ cp ~/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar $JMETER_HOME/lib/ext/
$ cp ~/.m2/repository/ru/nev/chat-jmeter/0.0.1-SNAPSHOT/chat-jmeter-0.0.1-SNAPSHOT-jar-with-dependencies.jar $JMETER_HOME/lib/ext/
```

- Run `stress-test.jmx`
