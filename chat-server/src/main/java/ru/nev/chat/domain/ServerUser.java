package ru.nev.chat.domain;

public class ServerUser {

  private final String address;
  private String name;

  public ServerUser(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasName() {
    return name != null;
  }

  @Override
  public String toString() {
    return name + "@" + address;
  }
}
