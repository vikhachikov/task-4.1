package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;


    @MockBean
    private Keycloak keycloak;

    @MockBean
    private RealmResource realmResource;

    @MockBean
    private UsersResource usersResource;

    @MockBean
    private List<RoleRepresentation> roleRepresentations;

    @MockBean
    private List<GroupRepresentation> groupRepresentations;

    @MockBean
    private RoleMappingResource roleMappingResource;

    @MockBean
    private MappingsRepresentation mappingsRepresentation;

    @BeforeEach
    public void setup() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    public void createUserTest() throws Exception {
        UserRequest userRequest = new UserRequest("user", "asdfgh@mail.com", "12345", "John", "Doe");
        Response response = Response.status(Response.Status.CREATED).location(new URI("user_id")).build();
        when(usersResource.create(any())).thenReturn(response);
        userService.createUser(userRequest);
        verify(usersResource, times(1)).create(any());
    }

    @Test
    public void testGetUserById() {

        UserRepresentation userRepresentation = new UserRepresentation();
        UUID userId = UUID.randomUUID();
        userRepresentation.setId(String.valueOf(userId));
        userRepresentation.setFirstName("John");

        when(usersResource.get(anyString())).thenReturn(mock(UserResource.class));
        when(keycloak.realm(anyString()).users().get(anyString()).toRepresentation()).thenReturn(userRepresentation);
        when(keycloak.realm(anyString()).users().get(anyString()).roles()).thenReturn(roleMappingResource);
        when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll()).thenReturn(mappingsRepresentation);
        when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll().getRealmMappings()).thenReturn(roleRepresentations);
        when(keycloak.realm(anyString()).users().get(anyString()).groups()).thenReturn(groupRepresentations);

        UserResponse response = userService.getUserById(userId);
        assertEquals("John", response.getFirstName());
    }
}