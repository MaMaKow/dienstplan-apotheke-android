# TODO - Design & UX Verbesserungen

## 1. Visuelles Feedback (Ladezustand)

- [x] **ProgressBar hinzufügen**: Ein kreisender Ladebalken während `refreshData` aktiv ist.
- [x] **Swipe-to-Refresh**: Implementierung von `SwipeRefreshLayout` für manuelles Aktualisieren der
  Liste.

## 2. Leere Zustände (Empty States)

- [x] **Platzhalter-Ansicht**: Anzeige eines Texts (z.B. "Keine Dienste gefunden") oder einer
  Illustration, wenn die Liste leer ist.

## 3. Fehlerkommunikation

- [x] **Snackbars/Toasts**: Einblendung von Fehlermeldungen bei Netzwerkproblemen oder Token-Fehlern
  statt nur im Logcat zu protokollieren.

## 4. Modernisierung der Auswahl (Material Design)

- [x] **Material Button Toggle Group**: Ersetzen der RadioButtons durch moderne Toggle-Buttons für
  die Sicht-Umschaltung.

## 5. Datumsnavigation

- [x] **Pfeil-Buttons**: `<` und `>` Buttons neben dem Datum, um schnell einen Tag vor oder zurück
  zu springen.

## 6. Listendesign (RosterAdapter)

- [x] **Material Cards**: Jeden Dienst in einer `CardView` darstellen.
- [x] **Farbkodierung**: Akzentfarben je nach Filiale oder Status des Dienstes (Dark Mode
  optimiert).
- [x] **Icons**: Icons für Berufe (Apotheker, PTA, etc.) und Pausenzeiten zur besseren Scanbarkeit.
- [x] **Kontrastoptimierung der Kartenfarben für Dark Mode**: Dynamische Anpassung von Sättigung und
  Textkontrast.
- [x] **Dark Mode Polishing**: Drawer Header Integration und optimierte Farben für Negativ-Werte.

## 7. Filter für Wochenansicht

- [x] **Mitarbeiter-Auswahl**: Spinner zur Auswahl des Mitarbeiters in der Wochenansicht (aktuell
  fest auf ID 7).

## 8. Testabdeckung & Softwarequalität

### Prio 1: Kritische Logik & Datenintegrität

- [x] **RosterDatabaseTest**: DAO-Logik für Dienstpläne (CRUD & Filter).
- [x] **ConvertersTest**: Unit-Tests für `Converters` (Mapping von LocalDate/LocalDateTime).
- [ ] **RosterTest (Erweiterung)**: Logik für Arbeitszeitberechnungen (Pause vs. Netto-Arbeitszeit).

### Prio 2: Datenbank & Business Logik

- [ ] **EmployeeDatabaseTest**: DAO-Logik für Mitarbeiterverwaltung.
- [ ] **AbsenceDatabaseTest**: DAO-Logik für Abwesenheiten/Urlaub.
- [ ] **BranchDatabaseTest**: DAO-Logik für Filialverwaltung.
- [ ] **RosterRepositoryTest**: Synchronisation zwischen Netzwerk (Retrofit) und Datenbank (Room).

### Prio 3: UI-Logik & Stabilität

- [ ] **RosterViewModelTest**: Validierung der Datenaufbereitung für die View.
- [ ] **EmployeeViewModelTest**: Validierung der Mitarbeiter-Filterlogik.
- [ ] **MainActivityTest (Espresso)**: Grundlegende UI-Flows (Navigation, Listenanzeige).

## 9. App-Einstellungen & Konfiguration

### Technische Basis

- [x] **Preference Library Integration**: Implementierung des Einstellungs-Fragments mittels
  `androidx.preference:preference-ktx`.
- [x] **Settings-Fragment**: Verknüpfung des Menüpunkts `nav_settings` mit einem
  `PreferenceFragmentCompat`.

### Funktionale Einstellungen

- [ ] **Benachrichtigungs-Management**: Toggle-Schalter für Push-Infos bei Dienstplanänderungen und
  Notdienst-Erinnerungen.
- [x] **Anzeige-Optionen**: Auswahl Dark/Light Mode.
- [ ] **Synchronisation**: Einstellung der Intervalle für den Datenabgleich und "Nur über WLAN"
  -Option.
- [ ] **Account-Info**: Anzeige/Änderung des angemeldeten Benutzers, Passwort und der zugewiesenen
  Stammfiliale.
- [ ] **Rechtliches**: Integration von Links zur Lizenzdatei.
- [x] **Support & Info** App-Version: (z.B. v1.0.4)
- [x] **Feedback/Support** Button, für eine E-Mail an den Administrator

## 10. Fehlerbehebungen

- [ ] **Mein Plan** Beim initialen Laden werden Dienstplandaten ab Jahresanfang angezeigt.
  Der Filter für die aktuelle Woche funktioniert offensichtlich nicht.

## 11. API Planung

- [ ] **Fehlende API Endpunkte im PHP Code** Im PHP in der API fehlen:
    - [ ] **Emailadresse** des Administrators als Kontaktadresse für die App
    - [ ] **Grundpläne**
    - [ ] **Notdienste**
    - [ ] **Passwort-Reset**
    - [ ] **Personalverwaltung** um selbstständig die eigene Haupt-Filiale zu wählen 