# alex-silva-music

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

OR

If you're developing with Cursive:
 - Select Edit Configurations
 - Create a new local Clojure REPL Run/Debug Configuration
 - Select the "clojure.main in normal JVM process" radio button option
 - Enter "script/repl.clj" in parameters field
 - Save and run the configuration

### Run tests:

```
lein clean
lein doo phantom test once
```

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn). 

## Production Build

```
lein clean
lein cljsbuild once min
```
