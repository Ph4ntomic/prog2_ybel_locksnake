# Vorgangsbeschreibung LockSnake

## 1. Ausgangspunkt

Zuerst wurde das vorhandene Projektgeruest analysiert. Dabei waren vor allem die Klassen `Main`, `GamePanel`, `Java2DRenderer`, `Level`, `LevelLoader`, `Snake`, `Pin`, `Direction` und die beiden TODO-Klassen `GameState` und `GameEngine` wichtig. Die vorhandene Level-Datei zeigt, wie Waende, leere Felder, Startposition und Pins codiert werden. Daraus ergab sich, dass die Spielregeln im Modell umgesetzt werden muessen, waehrend die GUI nur anzeigen und Eingaben weitergeben soll.

## 2. Spielzustand

Der zentrale Spielzustand wurde in `GameState` umgesetzt. Ein `GameState` enthaelt das Level, die Schlange, die Pins, den aktuellen Status und die vorgemerkte Bewegungsrichtung. Die Methode `tick()` berechnet daraus den naechsten Zustand. Wenn das Spiel nicht mehr laeuft oder keine Richtung gesetzt ist, bleibt der Zustand unveraendert.

Bei jedem Tick wird zuerst die naechste Kopfposition berechnet. Danach werden die Regeln in fester Reihenfolge geprueft:

- Verlassen des Spielfelds fuehrt zu `LOST_OUT_OF_BOUNDS`.
- Eine Wand blockiert die Bewegung und setzt die Richtung auf `NONE`.
- Eine Kollision mit dem eigenen Koerper fuehrt zu `LOST_SELF_COLLISION`.
- Ein Pin blockiert grundsaetzlich das Betreten des Feldes.
- Ein LOW-Pin wird nur gesetzt, wenn die Schlange aus der richtigen Aktivierungsrichtung kommt.
- Sind danach alle Pins gesetzt, wird der Status `WON`.
- Sonst waechst die Schlange um einen Schritt in die aktuelle Richtung.

Damit Positionsvergleiche korrekt funktionieren, wurde `Position` um `equals`, `hashCode` und `toString` ergaenzt. Ohne diese Wertgleichheit wuerden Pin-Suche und Selbstkollision mit neu erzeugten Positionsobjekten nicht zuverlaessig funktionieren.

## 3. GameEngine

Die `GameEngine` verwaltet den aktuellen `GameState`. Beim Erzeugen der Engine wird aus dem geladenen Level ein Startzustand mit einer einteiligen Schlange an der Startposition erstellt. `tick()` laesst den Zustand einen Schritt weiterlaufen. `update(Direction)` nimmt Tastatureingaben entgegen und aktualisiert die vorgemerkte Richtung.

Die Engine benachrichtigt nach einer Zustandsaenderung alle registrierten Observer. Dadurch muss die GUI nicht aktiv nachfragen, sondern bekommt den neuen Zustand automatisch.

## 4. Observer-Pattern

Fuer das Observer-Pattern wurde eine kleine generische Schnittstelle `GameObserver<T>` ergaenzt. `GamePanel` implementiert `GameObserver<GameState>` und kann dadurch von der `GameEngine` aktualisiert werden. Gleichzeitig verwaltet `GamePanel` eigene Observer fuer `Direction`, sodass Tastatureingaben an die `GameEngine` gemeldet werden.

Die Verkabelung passiert in `Main`:

- `engine.addObserver(panel)` fuer Spielzustand zu GUI.
- `panel.addObserver(engine)` fuer Tastatureingabe zu Engine.

Damit sind die beiden geforderten Beobachtungsrichtungen sichtbar im Code umgesetzt.

## 5. Lambda-Ausdruecke und Methodenreferenzen

Lambda-Ausdruecke werden unter anderem bei Swing-Actions, Timer-Aufrufen, Stream-Operationen und Assertions eingesetzt. Methodenreferenzen werden zum Beispiel bei der Registrierung der Key-Bindings und bei der Pin-Status-Auswertung genutzt. Dadurch werden die Anforderungen zu Lambda-Ausdruecken und Methodenreferenzen erfuellt, ohne kuenstliche Beispiele einzubauen.

## 6. Tests

Die Unit-Tests fuer `GameState` liegen in `GameStateTest`. Die Tests bauen kleine ASCII-Level direkt im Test auf, damit jeder Fall reproduzierbar und gut lesbar ist. Insgesamt wurden 12 Tests erstellt.

Abgedeckt werden:

- Initialzustand
- Tick ohne Richtung
- Bewegung auf ein freies Feld
- Wandblockade
- Verlassen des Spielfelds
- Selbstkollision
- Pin aus falscher Richtung
- Pin aus richtiger Richtung
- bereits gesetzter Pin
- Gewinn durch letzten Pin
- Gewinn bei bereits gesetzten Pins
- unveraenderter Zustand nach Spielende

## 7. Pruefung

Zum Schluss wurde das Projekt mit Gradle geprueft. `spotlessApply` formatiert die Java-Dateien passend zur Projektkonfiguration. Danach wurde `check` ausgefuehrt, wodurch Formatierung und Tests gemeinsam geprueft wurden.

Ergebnis:

- `gradlew.bat test` erfolgreich
- `gradlew.bat spotlessApply check` erfolgreich
- 12 Tests erfolgreich
- 0 Fehler
- 0 fehlgeschlagene Tests
