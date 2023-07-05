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
```
cd websocket-spring-boot
./gradlew bootRun
```

## Websocket-vuejs
At the root of the `websocket-example-spring-boot-vuejs`:

```
cd websocket-vuejs
npm install
npm run dev
```