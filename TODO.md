# TODO - Design & UX Verbesserungen

## 1. Visuelles Feedback (Ladezustand)

- [ ] **ProgressBar hinzufügen**: Ein kreisender Ladebalken während `refreshData` aktiv ist.
- [ ] **Swipe-to-Refresh**: Implementierung von `SwipeRefreshLayout` für manuelles Aktualisieren der
  Liste.

## 2. Leere Zustände (Empty States)

- [ ] **Platzhalter-Ansicht**: Anzeige eines Texts (z.B. "Keine Dienste gefunden") oder einer
  Illustration, wenn die Liste leer ist.

## 3. Fehlerkommunikation

- [ ] **Snackbars/Toasts**: Einblendung von Fehlermeldungen bei Netzwerkproblemen oder Token-Fehlern
  statt nur im Logcat zu protokollieren.

## 4. Modernisierung der Auswahl (Material Design)

- [ ] **Material Button Toggle Group**: Ersetzen der RadioButtons durch moderne Toggle-Buttons für
  die Sicht-Umschaltung.

## 5. Datumsnavigation

- [x] **Pfeil-Buttons**: `<` und `>` Buttons neben dem Datum, um schnell einen Tag vor oder zurück
  zu springen.

## 6. Listendesign (RosterAdapter)

- [x] **Material Cards**: Jeden Dienst in einer `CardView` darstellen.
- [ ] **Farbkodierung**: Akzentfarben je nach Filiale oder Status des Dienstes.
- [x] **Icons**: Icons für Berufe (Apotheker, PTA, etc.) und Pausenzeiten zur besseren Scanbarkeit.

## 7. Filter für Wochenansicht

- [ ] **Mitarbeiter-Auswahl**: Spinner zur Auswahl des Mitarbeiters in der Wochenansicht (aktuell
  fest auf ID 7).
