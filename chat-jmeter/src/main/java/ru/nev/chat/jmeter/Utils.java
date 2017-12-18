package ru.nev.chat.jmeter;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public class Utils {

  public static int randomBetween(int min, int max) {
    Random r = new Random();
    return r.nextInt(max - min) + min;
  }

  public static String generateName(int min, int max) {
    int i = randomBetween(min, max);
    return "jmeter" + i;
  }

  public static String generateText(int min, int max) {
    return RandomStringUtils.randomAlphabetic(randomBetween(min, max));
  }
}
