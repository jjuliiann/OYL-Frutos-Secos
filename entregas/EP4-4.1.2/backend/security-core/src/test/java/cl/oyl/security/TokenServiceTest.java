package cl.oyl.security;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class TokenServiceTest {
 private final TokenService tokens=new TokenService("0123456789abcdef0123456789abcdef",3600);
 @Test void verifiesIssuedIdentity(){String token=tokens.issue(9,"ADMIN");var identity=tokens.verify("Bearer "+token);assertEquals(9,identity.userId());assertEquals("ADMIN",identity.role());}
 @Test void rejectsMissingAndTamperedTokens(){assertThrows(IllegalArgumentException.class,()->tokens.verify(null));String token=tokens.issue(9,"ADMIN");int dot=token.indexOf('.');String altered=token.substring(0,dot+1)+"AAAAAAAA";assertThrows(IllegalArgumentException.class,()->tokens.verify("Bearer "+altered));}

 @Test void issuerRoleAndExpiryAreVerified(){
  assertThrows(IllegalArgumentException.class,()->new TokenService("short",5));
  assertThrows(IllegalArgumentException.class,()->tokens.issue(8,"ANON"));
  String expired=new TokenService("0123456789abcdef0123456789abcdef",-5).issue(1,"ADMIN");
  assertThrows(IllegalArgumentException.class,()->tokens.verify("Bearer "+expired));
  String foreign=new TokenService("otra-clave-muy-diferente-para-oyl-local",900).issue(1,"ADMIN");
  assertThrows(IllegalArgumentException.class,()->tokens.verify("Bearer "+foreign));
 }
}
