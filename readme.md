## Implementation of [7guis](https://eugenkiss.github.io/7guis/)
- Code is in src/frontend/app/frontend
- Based on [aum-start-app](https://github.com/Michieljoris/aum-starter-app)
- Cells non gui part is copied from https://github.com/eugenkiss/7guis-Clojure-Seesaw

Install node version manager first, then in the repo:

    nvm install
    nvm use

 Run production version:

    bin/prod-install-js
    SERVER_PORT=8090 java -jar dist/app.jar

App is at [localhost:8090/app]()

 Run in dev mode

    bin/dev-install-js
    bin/dev-backend

And in another terminal:

    bin/dev-frontend

App is at [localhost:8080/app]()

### Good to know
The css of semantic ui downloads a google font:

@import url(https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic&subset=latin);

 When working offline this blocks refreshing the page. A quick work around is to
 comment out this import in node_modules/semantic-ui-css/semantic.css
