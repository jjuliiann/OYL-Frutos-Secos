package cl.oyl.users;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import cl.oyl.security.TokenService;
import java.util.Optional;
import cl.oyl.users.UserDtos.Credentials;
import cl.oyl.users.UserDtos.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
 @Mock UserRepository repo; UserController controller;
 @BeforeEach void setUp(){controller=new UserController(repo,new TokenService("0123456789abcdef0123456789abcdef",3600));}
 @Test void registerStoresHashedPasswordAndClientRole(){when(repo.findByAlias("nuevo")).thenReturn(Optional.empty());when(repo.save(any())).thenAnswer(call->{UserAccount a=call.getArgument(0);a.id=7L;return a;});var result=controller.register(new Register(" nuevo ","ClaveSegura123","Nuevo Cliente","n@oyl.cl","+56912345678","Santiago"));assertEquals("CLIENT",result.role());assertEquals(7L,result.id());verify(repo).save(argThat(a->!"ClaveSegura123".equals(a.passwordHash)&&new BCryptPasswordEncoder().matches("ClaveSegura123",a.passwordHash)));}
 @Test void rejectsShortPassword(){assertThrows(ResponseStatusException.class,()->controller.register(new Register("x","123","Nombre","","","")));verify(repo,never()).save(any());}
 @Test void loginChecksPasswordHash(){UserAccount a=new UserAccount();a.id=3L;a.alias="cliente";a.name="Cliente";a.role="CLIENT";a.passwordHash=new BCryptPasswordEncoder().encode("Cliente1234!");when(repo.findByAlias("cliente")).thenReturn(Optional.of(a));assertEquals(3L,controller.login(new Credentials(" cliente ","Cliente1234!")).id());assertNotNull(controller.login(new Credentials("cliente","Cliente1234!")).token());assertThrows(ResponseStatusException.class,()->controller.login(new Credentials("cliente","incorrecta")));}

 @Test void perfilSoloDelTitularYRespuestaSinToken(){
  var jwt=new TokenService("0123456789abcdef0123456789abcdef",3600);
  var a=new UserAccount();a.id=7L;a.alias="ana";a.role="CLIENT";when(repo.findById(7L)).thenReturn(Optional.of(a));when(repo.save(a)).thenReturn(a);
  var update=new UserDtos.ProfileUpdate(" Ana Perez ","a@oyl.cl","912345678","Centro");
  var result=controller.update(7L,"Bearer "+jwt.issue(7,"CLIENT"),update);
  assertEquals("Ana Perez",result.name());assertNull(result.token());assertEquals("Centro",controller.internal(7L).address());
  assertThrows(ResponseStatusException.class,()->controller.update(7L,"Bearer "+jwt.issue(8,"CLIENT"),update));
  assertThrows(ResponseStatusException.class,()->controller.update(7L,"Bearer roto",update));
 }
 @Test void noRecreaCuentasExistentesAlReiniciar() throws Exception {
  when(repo.findByAlias("admin")).thenReturn(Optional.empty(),Optional.of(new UserAccount()));
  when(repo.findByAlias("cliente")).thenReturn(Optional.empty(),Optional.of(new UserAccount()));
  var seed=new UserSeedData().seedUsers(repo);seed.run();seed.run();verify(repo,times(2)).save(any());
 }
}
