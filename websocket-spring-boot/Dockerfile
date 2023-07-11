FROM gradle:8-jdk17

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
 
ENTRYPOINT [ "gradle", "bootRun" ]
