# Post Mortem Blatt 03: LockSnake

## Zusammenfassung

Ich habe bei LockSnake die fehlende Spielzustandslogik, die GameEngine-Anbindung, das Observer-Pattern und die JUnit-Tests umgesetzt. Der Spielzustand verwaltet jetzt Level, Snake, Pins, Status und die aktuelle Blickrichtung. Pro Tick wird geprueft, ob die Schlange laufen darf, ob sie gegen eine Wand oder einen Pin stoesst, ob sie sich selbst trifft oder ob sie das Spielfeld verlaesst. Die GUI wird ueber Observer aktualisiert, und Tastatureingaben werden ebenfalls ueber Observer als `Direction` an die `GameEngine` weitergegeben.

## Details

Besonders interessant war die Pin-Mechanik, weil ein Pin zwar beruehrt, aber nicht betreten wird. Dadurch musste ich Bewegung und Interaktion sauber trennen: Erst wird die naechste Kopfposition berechnet, dann werden Wand, Selbstkollision und Pin-Regeln ausgewertet. Wichtig war auch, dass `Position` echte Wertgleichheit bekommt, weil sonst Vergleiche wie Selbstkollision oder Pin-Suche mit neu erzeugten Positionen nicht funktionieren.

## Reflexion

Der schwierigste Teil war, die Reihenfolge der Regeln eindeutig festzulegen. Wenn man zum Beispiel zuerst die Schlange bewegt und danach den Pin prueft, ist das Verhalten falsch. Geloest habe ich das durch kleine Testlevel, die jeweils nur einen Fall pruefen. Dadurch wurden Off-by-One-Fehler und falsche Erwartungen schnell sichtbar. Gelernt habe ich vor allem, wie gut sich ein unveraenderlicher `GameState` fuer solche Logik eignet: Jeder Tick liefert einen neuen Zustand, und die Observer muessen nur noch diesen Zustand weitergeben.

## Link

https://github.com/Ph4ntomic/prog2_ybel_locksnake/tree/solution-locksnake
