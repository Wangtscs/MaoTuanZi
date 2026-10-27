package com.maotuanzi.ops;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.controller.ops.OpsAuthController;
import com.maotuanzi.entity.AdminPermission;
import com.maotuanzi.entity.AdminUser;
import com.maotuanzi.interceptor.AdminAuthInterceptor;
import com.maotuanzi.mapper.AdminMapper;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.security.AdminPermissions;
import com.maotuanzi.security.jwt.JwtUtil;
import com.maotuanzi.service.AdminService;
import com.maotuanzi.service.impl.AdminServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class OpsAuthenticationContractTest {
 private JwtUtil jwt;
 @BeforeEach void setup(){jwt=new JwtUtil();ReflectionTestUtils.setField(jwt,"secret","test-only-ops-signing-key-with-32-bytes-minimum");}
 @AfterEach void cleanup(){AdminContext.clear();}
 static class Endpoint {@RequireAdmin(permission="ops.metrics") public void metrics(){}}
 @Test void permissionsNormalizeArraysAndDoNotGrantSiblingModules(){
  assertEquals(Set.of("ops.view","ops.*"),AdminPermissions.normalize(Arrays.asList(" ops.view ","ops.*",null,17)));
  assertTrue(AdminPermissions.allows(Set.of("ops.*"),"ops.metrics"));assertFalse(AdminPermissions.allows(Set.of("ops.view"),"ops.config"));assertFalse(AdminPermissions.allows(Set.of("ops.*"),"admin.users"));assertTrue(AdminPermissions.normalize("ops.*").isEmpty());
 }
 @Test void adminSigningUsesSameConfiguredKeyAsParser(){Claims c=jwt.parse(jwt.generateAdmin(41L,List.of("ops.view"),300));assertEquals(41,((Number)c.get("adminId")).intValue());assertEquals("admin",c.get("type"));assertEquals(Set.of("ops.view"),AdminPermissions.normalize(c.get("perms")));}
 @Test void validBearerAndJsonPermissionCollectionAuthenticate()throws Exception{
  AdminAuthInterceptor auth=new AdminAuthInterceptor(jwt,mock(StringRedisTemplate.class),new ObjectMapper());MockHttpServletRequest r=new MockHttpServletRequest();r.addHeader("X-Admin-Token","Bearer "+jwt.generateAdmin(41L,List.of("ops.*"),300));assertTrue(auth.preHandle(r,new MockHttpServletResponse(),new HandlerMethod(new Endpoint(),"metrics")));assertEquals(41L,AdminContext.getAdminId());
 }
 @Test void missingBearerAndInsufficientPermissionAreRejected()throws Exception{
  AdminAuthInterceptor auth=new AdminAuthInterceptor(jwt,mock(StringRedisTemplate.class),new ObjectMapper());MockHttpServletRequest r=new MockHttpServletRequest();r.addHeader("X-Admin-Token",jwt.generateAdmin(41L,List.of("ops.*"),300));MockHttpServletResponse response=new MockHttpServletResponse();assertFalse(auth.preHandle(r,response,new HandlerMethod(new Endpoint(),"metrics")));assertEquals(401,response.getStatus());
  r=new MockHttpServletRequest();r.addHeader("X-Admin-Token","Bearer "+jwt.generateAdmin(41L,List.of("ops.view"),300));response=new MockHttpServletResponse();assertFalse(auth.preHandle(r,response,new HandlerMethod(new Endpoint(),"metrics")));assertEquals(403,response.getStatus());assertNull(AdminContext.getAdminId());
 }
 @Test void opsLoginFlattensLegacyEnvelopeWithoutChangingIt(){
  AdminService service=mock(AdminService.class);Map<String,Object> nested=new LinkedHashMap<>();nested.put("id",41L);nested.put("username","operator");nested.put("nickname","值班员");nested.put("permissions",List.of("ops.view"));nested.put("roles",List.of("viewer"));Map<String,Object> original=new LinkedHashMap<>();original.put("token","test-token");original.put("admin",nested);when(service.login(eq("operator"),eq("password"),anyString())).thenReturn(original);
  Map<String,Object> result=new OpsAuthController(service,mock(AdminMapper.class)).login(Map.of("username","operator","password","password"),new MockHttpServletRequest()).getData();assertSame(nested,result.get("admin"));assertEquals("operator",result.get("username"));assertEquals(Set.of("ops.view"),result.get("perms"));assertFalse(original.containsKey("perms"));
 }
 @Test void meReturnsCurrentIdentityAndDatabasePermissions(){
  AdminMapper mapper=mock(AdminMapper.class);AdminUser user=new AdminUser();user.setId(41L);user.setUsername("operator");user.setNickname("值班员");user.setStatus(0);AdminPermission p=new AdminPermission();p.setCode("ops.view");when(mapper.selectById(41L)).thenReturn(user);when(mapper.selectPermsByUserId(41L)).thenReturn(List.of(p));when(mapper.selectRolesByUserId(41L)).thenReturn(List.of());AdminContext.set(41L,Set.of("ops.view"));Map<String,Object> result=new OpsAuthController(mock(AdminService.class),mapper).me().getData();assertEquals("operator",result.get("username"));assertEquals("值班员",result.get("nickname"));assertEquals(Set.of("ops.view"),result.get("perms"));
 }
 @Test void redisRevocationFailureMustNotReportRemoteLogoutSuccess(){
  JwtUtil util=mock(JwtUtil.class);StringRedisTemplate redis=mock(StringRedisTemplate.class);Claims c=Jwts.claims();c.setExpiration(new Date(System.currentTimeMillis()+60000));when(util.parse("token")).thenReturn(c);@SuppressWarnings("unchecked") ValueOperations<String,String> values=mock(ValueOperations.class);when(redis.opsForValue()).thenReturn(values);doThrow(new IllegalStateException("redis unavailable")).when(values).set(anyString(),eq("1"),any(Duration.class));AdminServiceImpl s=new AdminServiceImpl(null,null,null,null,null,null,null,null,util,redis);assertThrows(IllegalStateException.class,()->s.logout("token"));
 }
}
