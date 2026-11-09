# Ronova Cosmetics (Fabric, Minecraft 26.3, nur Client)

Rot-schwarze Cosmetics im Stil des Schattens des Todes plus ein eigenes Bild als Himmel.
Alles wird mit **/ronona** ein- und ausgeschaltet. Inoffizielle Fan-Mod, nicht mit HoYoverse verbunden.

## Funktionen

**Cosmetics** (siehst du am besten in der 3rd-Person-Ansicht mit F5 – nur du selbst siehst sie):

| Name | Effekt |
|---|---|
| Blutroter Heiligenschein | Rotierender Ring aus roter Glut über dem Kopf |
| Seelenaura | Aufsteigende Seelen um deine Füße |
| Rabenschwarm | Drei dunkle Raben kreisen um deine Schultern |
| Todesspur | Seelenfeuer, wo du entlangläufst |

**Himmel:** Dein eigenes Bild wird rund um den Horizont aufgespannt (nur Overworld).
Einstellbar: Wiederholungen (1–4×), Helligkeit (40/70/100 %), langsame Drehung, Sonne/Mond/Sterne ausblenden.

## Himmelsbild einrichten

1. Minecraft einmal mit der Mod starten – dabei entsteht der Ordner `.minecraft/config/ronova/`.
2. Dein Bild als **`sky.png`** dort hineinlegen (nur PNG; breite Bilder wie 2048×1024 sehen am besten aus).
3. Im Spiel `/ronona` → **Bild neu laden**.

## Bauen

Repo hochladen → *Actions* → *Build Mod* → Artefakt **ronova-cosmetics** herunterladen.
Lokal: Java 25 + Gradle, dann `gradle build` (Jar in `build/libs/`).

## Installieren

Fabric Loader ≥ 0.19 + Fabric API 0.165.2+26.3 für Minecraft 26.3, Jar in den `mods`-Ordner. Muss nur auf dem Client sein.
