# LockSnake Vorgang

## Umgesetzt

- `GameState` speichert Level, Snake, Pins, Status und Blickrichtung als neuen unveraenderlichen Spielzustand.
- `tick()` behandelt Bewegung, Wachstum, Waende, Out-of-Bounds, Selbstkollisionen und Pins.
- LOW-Pins werden nur aus ihrer Aktivierungsrichtung gesetzt. Das Pin-Feld wird dabei nicht betreten.
- HIGH-Pins, falsche Pin-Richtung und Waende blockieren die Bewegung und setzen die Blickrichtung auf `NONE`.
- Das Spiel wird gewonnen, sobald alle Pins `HIGH` sind.
- `Position` vergleicht jetzt Koordinaten als Werte, damit Pin- und Kollisionspruefungen funktionieren.
- `GameObserver<T>` bildet das Observer-Pattern ab.
- `GameEngine` beobachtet Richtungen und benachrichtigt registrierte `GameState`-Observer.
- `GamePanel` beobachtet den `GameState` und benachrichtigt registrierte Richtungs-Observer bei Tastatureingaben.

## Tests

Die Tests in `GameStateTest` verwenden kleine ASCII-Level als Fixtures. Abgedeckt sind Initialzustand, Bewegung und Wachstum, Wandblockade, Out-of-Bounds, Selbstkollision, falsche Pin-Richtung, korrektes Pin-Setzen, HIGH-Pin-Blockade, Gewinnbedingung und bereits beendete Spiele.

## Lambda-Ausdruecke und Methodenreferenzen

Lambda-Ausdruecke stehen unter anderem in Swing- und Stream-Aufrufen. Methodenreferenzen werden unter anderem fuer das Registrieren der Key-Bindings und fuer die Pin-Status-Auswertung genutzt.
