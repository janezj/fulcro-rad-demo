FROM theasp/clojurescript-nodejs:shadow-cljs-alpine as frontend
ARG SHADOW_COMMAND=compile  ## or "release"

WORKDIR /work
RUN apk add git 
COPY package.json shadow-cljs.edn deps.edn ./
COPY src /work/src/
RUN shadow-cljs npm-deps && npm install --legacy-peer-deps --save-dev shadow-cljs
RUN echo shadow-cljs command ${SHADOW_COMMAND} 
RUN shadow-cljs ${SHADOW_COMMAND} main

# ---- backend
FROM clojure:openjdk-17-tools-deps-buster as backend
WORKDIR /work
COPY --from=frontend /work/resources/public/ ./resources/public/
RUN echo "--------------------------------------------------------------------------------------------"
RUN ls -lh ./resources/public/js/main
RUN echo "--------------------------------------------------------------------------------------------"
ADD deps.edn ./
ADD src ./src
ADD build ./build/
RUN rm -rf ./src/dev # remove dev folder, user.clj is cursed !!!
RUN ls -l /work
RUN ls -l /work/src
# ENV TZ=Europe/Ljubljana
RUN clj -T:build uber :db-alias :datomic

#  ---- main java image
FROM openjdk:17-oracle
WORKDIR /app/
# ENV TZ=Europe/Ljubljana
COPY --from=backend /work/target/app.jar /app/
EXPOSE 3000 9000
CMD ["java", "-jar", "app.jar"]


