package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WithMockUser(username = "user", password = "user_password", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

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

    private ObjectMapper mapper = new ObjectMapper();

    private UserRequest userRequest;

    @BeforeEach
    public void setup() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        userRequest = new UserRequest("user", "asdfgh@mail.com", "12345", "John", "Doe");
    }

    @Test
    public void testCreateUser() {
        try {
            Response response = Response.status(Response.Status.CREATED).location(new URI("user_id")).build();
            when(keycloak.realm(anyString()).users().create(any())).thenReturn(response);
            mvc.perform(requestWithContent(post("/api/users"), userRequest));
            verify(usersResource, times(1)).create(any());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHello() {
        try {
            MvcResult result = mvc.perform(get("/api/users/hello"))
                    .andExpect(status().isOk())
                    .andReturn();
            String responseContent = result.getResponse().getContentAsString();
            Assertions.assertEquals("user", responseContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetUserById() {
        try {
            UserRepresentation userRepresentation = new UserRepresentation();
            UUID userId = UUID.randomUUID();
            userRepresentation.setId(String.valueOf(userId));
            userRepresentation.setFirstName("John");

            when(keycloak.realm(anyString()).users().get(anyString())).thenReturn(mock(UserResource.class));
            when(keycloak.realm(anyString()).users().get(anyString()).toRepresentation()).thenReturn(userRepresentation);
            when(keycloak.realm(anyString()).users().get(anyString()).roles()).thenReturn(mock(RoleMappingResource.class));
            when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll()).thenReturn(mock(MappingsRepresentation.class));
            when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll().getRealmMappings()).thenReturn(roleRepresentations);
            when(keycloak.realm(anyString()).users().get(anyString()).groups()).thenReturn(groupRepresentations);

            MvcResult result = mvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andReturn();

            UserResponse userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
            Assertions.assertEquals("John", userResponse.getFirstName());
            verify(keycloak.realm(anyString()).users(), times(1)).get(any());
        }
        catch (Exception e) {
        }
    }
}