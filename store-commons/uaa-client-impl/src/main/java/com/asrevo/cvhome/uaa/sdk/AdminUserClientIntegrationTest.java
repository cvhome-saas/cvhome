/*
 * package com.asrevo.cvhome.uaa.sdk;
 *
 * import com.asrevo.cvhome.uaa.TestcontainersConfiguration; import
 * com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest; import
 * com.asrevo.cvhome.uaa.sdk.dto.PageResponse; import
 * com.asrevo.cvhome.uaa.sdk.dto.UserDto; import org.junit.jupiter.api.BeforeEach; import
 * org.junit.jupiter.api.Test; import
 * org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.boot.test.web.server.LocalServerPort; import
 * org.springframework.context.annotation.Import;
 *
 * import java.util.Map;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * @Import(TestcontainersConfiguration.class)
 *
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) public
 * class AdminUserClientIntegrationTest {
 *
 * @LocalServerPort private int port;
 *
 * private AdminUserClient client;
 *
 * @BeforeEach void setUp() { // admin-sdk credentials from data.sql // The bcrypt hash
 * $2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa in // V2 corresponds to
 * "secret" client = new AdminUserClient("http://localhost:" + port, "admin-sdk", "revo");
 * System.out.println("[DEBUG_LOG] Setup finished"); }
 *
 * @Test void testCreateAndListUsersWithMetadata() { String username = "sdk-user-" +
 * System.currentTimeMillis(); CreateUserRequest req = new CreateUserRequest(username,
 * username + "@example.com", Map.of("tenant", "sdk-tenant", "org", "sdk-org"));
 *
 * UserDto created = client.createUser(req);
 * assertThat(created.username()).isEqualTo(username);
 * assertThat(created.metadata()).containsEntry("tenant", "sdk-tenant");
 *
 * // List all PageResponse<UserDto> page = client.listUsers(null);
 * assertThat(page.content()).extracting(UserDto::username).contains(username);
 * assertThat(page.totalElements()).isGreaterThanOrEqualTo(1);
 *
 * // Filter by metadata PageResponse<UserDto> filtered =
 * client.listUsers(Map.of("tenant", "sdk-tenant"));
 * assertThat(filtered.content()).extracting(UserDto::username).contains(username);
 *
 * // Filter by non-existent metadata PageResponse<UserDto> empty =
 * client.listUsers(Map.of("tenant", "non-existent"));
 * assertThat(empty.content()).extracting(UserDto::username).doesNotContain(username); }
 *
 * }
 */
