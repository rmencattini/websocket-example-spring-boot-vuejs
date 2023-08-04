# Websocket-example-spring-boot-vuejs
An working example of Spring boot backend and Vuejs frontend on different URL.

I created this small project to tackle few issues I encouterd on other tutorials.
The main one was that all project ran FE on the same URL than BE. It makes the stuff easier for allowed origins and url management.

## Requirements

To run both projects you will either:
* java 17
* node 18

or `docker-compose`

## Run

There is two way to run it. As dev and as docker.

### Dev run

You will need to run all projects separatly. Then you can go to http://localhost:7000 and test this small webapp.

Spring project will run on port `7100`.

Vuejs project uses port `7000`.


#### Websocket-spring-boot

At the root of the `websocket-example-spring-boot-vuejs`:
```bash
cd websocket-spring-boot
./gradlew bootRun
```
:warning: The keycloak docker should be running before starting the BE.

#### Websocket-vuejs
At the root of the `websocket-example-spring-boot-vuejs`:

```bash
cd websocket-vuejs
npm install
npm run dev
```

#### Keycloak

```
docker run --name keycloak -p 8080:8080 \
     -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
     -v ./oauth2-server:/opt/keycloak/data/import \
     quay.io/keycloak/keycloak:latest \
     start-dev \
     --http-port 8080 \
     --http-relative-path /auth \
     --import-realm
```

### Docker-compose run

Simply go to the root of project and run:
```
docker-compose up
```

## Code breakdown

### Spring

```java
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/alert"); // 1)
        config.setApplicationDestinationPrefixes("/app"); // 2)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/websocket") // 3)
            .setAllowedOriginPatterns("*"); // 4)
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new HttpHandshakeInterceptor()); // 5)
    }

    public class HttpHandshakeInterceptor implements ChannelInterceptor {

        @Override
        public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, // 6)
                MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (StompCommand.SEND == accessor.getCommand()) { // 7)
                JwtDecoder jwtDecoder = jwtDecoder(); // 8)
                String authorizationToken = accessor.getFirstNativeHeader("Authorization");
                if (authorizationToken != null) {
                    Jwt jwt = jwtDecoder.decode(token);
                    Principal principal = User.builder().build(); // 9)
                    accessor.setUser(principal);
                } else {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("invalid_token", "Missing access token", null));
                }
            }
            return message;
        }
    }


```

1. Create an in-memory message brook with some destinations for message exchanging.
2. Set a prefix for Spring to determine which message should be rooted to `@MessageMapping`.
3. The stomp endpoint clients will connect to
4. As we have different url/port, we need to allow some origin to avoid CORS issue.
5. Add an interceptor to channel message.
6. The interceptor will work on incoming channel message before dispatch it to controllers.
7. Check which type of message (could be `CONNECT`, `HEARTBEAT`,...)
8. Init a jwt decoder (implementation depends on your authentication server)
9. Once you will have decode the jwt, you can build a `User` object with all the property you want. The `User` class should implement the `Principal` interface.
10. Set for the context *of the current message*, the authenticated user.

Here is a diagram of the architecture: ![architecture diagram](https://assets.toptal.io/images?url=https%3A%2F%2Fuploads.toptal.io%2Fblog%2Fimage%2F129598%2Ftoptal-blog-image-1555593632876-e8be5fa57853689bab282bb8be341130.png)
(_source: https://www.toptal.com/java/stomp-spring-boot-websocket_)

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate; // 1)

    @MessageMapping("/hello") // 2)
    public void greeting(@Payload String message, Principal principal){ // 3)
        messagingTemplate.convertAndSend("/topic/greetings", "Hello, " + message + "!"); // 4)
        messagingTemplate.convertAndSend("/alert/trigger", ""); // 4)
        log.warn("\n\nUsername: " + principal.getName());
    }
}
```

1. This component injection can replace the `@SendTo`. It is usefull when you want to send data to different channels.
2. Such as `@Get(/hello)` for HTTP call, this annotation will listen the message with prefix + path (here: `/app/hello`) and run the approriate controller function.
3. `Principal` is the authenticated user passed. It is set during a `preSend` hook configured during websocket configuration.
4. Use the injected component to send message to two channels.


### Vuejs

```typescript
const stompClient = new Stomp.Client({ // 1)
  brokerURL: 'ws://localhost:7100/websocket', // 2)
  onConnect() { // 3)
      connected.value = true;
      stompClient.subscribe('/topic/greetings', (message: Stomp.IMessage) => { // 4)
        result.value = message.body;
      });
      stompClient.subscribe('/alert/trigger', () => { // 4)
        shake.value = true;
        setTimeout(() => {
          shake.value = false;
        }, 2000)
      })
  }
});

function sendGrettings(): void {
  stompClient.publish({destination: '/app/hello', body: name.value, headers: { "Authorization": "Bearer eyz..."}}) // 5)
}

stompClient.activate(); // 6)
```
1. Instanciate a stomp client to communicate with the BE server.
2. Set the broker url. It needs to be prefixed with `ws` to use websocket protocole.
3. Define a callback when the connection is established.
4. During the connection callback, subscribe to some channel and set some callback when message will arrive.
5. Send body to a determined destination. It is prefixed by `/app` for the Spring controller to handle it. It requires the `Authorization` header to be set with valid token.
6. Activate the client we created (rephrased: fire the connection to the STOMP server)

### Keycloak

The config in the `oaut2-server` folder contains one realm: `dummy` and one couple user/password: `foo` / `bar`.


## Notes

Configure `CORS` in two places:
```java
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
```
```java
@EnableWebSecurity
class SecurityConfig {
    //...
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
```
can create `CORS not allowed` on the FE.


## Usefull links:
* https://www.toptal.com/java/stomp-spring-boot-websocket
* https://spring.io/guides/gs/messaging-stomp-websocket/
* https://howtodoinjava.com/devops/keycloak-export-import-realm/
* https://rob-ferguson.me/getting-started-with-keycloak/
