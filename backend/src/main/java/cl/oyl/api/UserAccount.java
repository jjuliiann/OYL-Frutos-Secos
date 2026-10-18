package cl.oyl.api;
import jakarta.persistence.*;
@Entity public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(unique=true,nullable=false) public String alias;
 public String name,email,phone,address,passwordHash,role="CLIENT";
 public UserAccount(){}
}
