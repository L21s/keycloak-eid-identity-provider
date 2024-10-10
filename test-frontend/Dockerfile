FROM node:alpine
EXPOSE 4200
WORKDIR /usr/src/app
COPY . /usr/src/app

RUN npm install -g @angular/cli
RUN npm install

CMD ["ng", "serve", "--host", "0.0.0.0"]