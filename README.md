# Websocket-example-spring-boot-vuejs
An working example of Spring boot backend and Vuejs frontend on different URL.

I created this small project to tackle few issues I encouterd on other tutorials.
The main one was that all project ran FE on the same URL than BE. It makes the stuff easier for allowed origins and url management.

# Requirements

To run both projects you will need:
* java 17
* node 18

# Run

You will need to run both project separatly. Then you can go to http://localhost:7000 and test this small webapp.

Spring project will run on port `7100`.

Vuejs project uses port `7000`.


## Websocket-spring-boot

At the root of the `websocket-example-spring-boot-vuejs`:
```bash
cd websocket-spring-boot
./gradlew bootRun
```

## Websocket-vuejs
At the root of the `websocket-example-spring-boot-vuejs`:

```bash
cd websocket-vuejs
npm install
npm run dev
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
```

1. Create an in-memory message brook with some destinations for message exchanging.
2. Set a prefix for Spring to determine which message should be rooted to `@MessageMapping`.
3. The stomp endpoint clients will connect to
4. As we have different url/port, we need to allow some origin to avoid CORS issue.

Here is a diagram of the architecture: ![architecture diagram](https://assets.toptal.io/images?url=https%3A%2F%2Fuploads.toptal.io%2Fblog%2Fimage%2F129598%2Ftoptal-blog-image-1555593632876-e8be5fa57853689bab282bb8be341130.png)
(_source: https://www.toptal.com/java/stomp-spring-boot-websocket_)

```java
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate; // 1)

    @MessageMapping("/hello") // 2)
    public void greeting(String message) {
        messagingTemplate.convertAndSend("/topic/greetings", "Hello, " + message + "!"); // 3)
        messagingTemplate.convertAndSend("/alert/trigger", ""); // 3)
    }
}
```

1. This component injection can replace the `@SendTo`. It is usefull when you want to send data to different channels.
2. Such as `@Get(/hello)` for HTTP call, this annotation will listen the message with prefix + path (here: `/app/hello`) and run the approriate controller function.
3. Use the injected component to send message to two channels.


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
  stompClient.publish({destination: '/app/hello', body: name.value}) // 5)
}

stompClient.activate(); // 6)
```
1. Instanciate a stomp client to communicate with the BE server.
2. Set the broker url. It needs to be prefixed with `ws` to use websocket protocole.
3. Define a callback when the connection is established.
4. During the connection callback, subscribe to some channel and set some callback when message will arrive.
5. Send body to a determined destination. It is prefixed by `/app` for the Spring controller to handle it.
6. Activate the client we created (rephrased: fire the connection to the STOMP server)


## Usefull links:
* https://www.toptal.com/java/stomp-spring-boot-websocket
* https://spring.io/guides/gs/messaging-stomp-websocket/
