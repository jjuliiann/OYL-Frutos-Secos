package cl.oyl.users;
public final class UserDtos {
 private UserDtos() {}
 public record Credentials(String alias,String password) {}
 public record Register(String alias,String password,String name,String email,String phone,String address) {}
 public record ProfileUpdate(String name,String email,String phone,String address) {}
 public record Account(Long id,String alias,String name,String email,String phone,String address,String role,String token) {}
}
