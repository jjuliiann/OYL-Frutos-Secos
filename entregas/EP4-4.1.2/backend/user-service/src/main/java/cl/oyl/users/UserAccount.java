package cl.oyl.users;
import jakarta.persistence.*;
@Entity @Table(name="user_accounts", uniqueConstraints=@UniqueConstraint(columnNames="alias"))
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=60) public String alias;
 @Column(nullable=false,length=100) public String name;
 @Column(length=160) public String email;
 @Column(length=30) public String phone;
 @Column(length=240) public String address;
 @Column(nullable=false,length=12) public String role="CLIENT";
 @Column(nullable=false,length=100) public String passwordHash;
 protected UserAccount() {}
}
