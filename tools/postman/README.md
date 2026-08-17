# Lifecare handläggarlabb (HYDRAN-2629)

Postman-kollektion för att avgöra om Lifecares svar räcker för att visa vem som fattat ett beslut,
eller om det krävs enrichment mot Employee-tjänsten.

## Vad labben ska svara på

**Den enda öppna tekniska frågan:** ligger EC:s `Caseworker.Id` i samma id-rymd som `User.Id`?

- **Ja** → kedjan `Caseworker.Id` → `GET /api/v1/users/{id}` → `NetworkUserId` →
  Employee `GET /api/v1/employee/portalpersondata/{domain}/{loginName}` finns, och enrichment är
  tekniskt möjlig för EC-beslut.
- **Nej** → enrichment är omöjlig även för EC.

**För FC är frågan redan avgjord av specen:** `PersonBasedDecisionDTO.DecisionMaker` är en bar
sträng utan id. Employee slår upp på personnummer, `domain`+`loginName`, e-post eller `personId`
(UUID) — inget av det går att härleda ur ett visningsnamn. Mapp 3 finns för att bekräfta det mot
verkligt data, inte för att leta efter en väg runt.

Notera att ett "ja" på den tekniska frågan inte gör enrichment till rätt beslut. Employee ger
*nuvarande* namn och anställning; ett myndighetsbeslut ska attribueras till den som fattade det med
det namn som gällde vid beslutstillfället. Lifecares lagrade namn är protokollet.

## Setup

1. Importera `lifecare-caseworker-lab.postman_collection.json`.
2. Kopiera `lifecare-utb.postman_environment.example.json` → egen miljö, fyll i `domain`, `key` och
   `testPersonId`. **Committa aldrig den ifyllda miljön.**
3. `ecBaseUrl`/`fcBaseUrl` pekar på test-URL:erna från `template/.../config/test/config.yaml`. Byt
   till utb-värden om utb ligger på annan host.
4. **mTLS:** `lifecare-ext.sundsvall.se` kräver klientcertifikat för riktiga API-anrop (bara
   swagger-docs gick utan). Lägg in certet under Postman → Settings → Certificates för den hosten,
   annars faller anropen på TLS-nivå och inte på något som har med labben att göra.

## Körordning

1. Mapp 1 — hämta EC-beslut. Testskriptet plockar första `Caseworker.Id` till `{{ecCaseworkerId}}`.
2. Mapp 2 — **kärntestet**. Slår upp id:t som User. 200 + `NetworkUserId` = ja, 404 = nej.
   Ger by-id 404 kan du jämföra id-formaten via "Lista users" innan du drar slutsatser.
3. Mapp 3 — bekräfta FC-bilden (namn, inget id).
4. Mapp 4 — FC users, id-format. Skriptet kollar också om `FullName` är unikt; är det inte det
   faller idén om att namnmatcha `DecisionMaker` mot en user.

Svaren skrivs till Postman-konsolen (View → Show Postman Console).

## Säkerhet

Personnummer och API-nyckeln reser i **query-strängen** på båda API:erna — därför tvingas Feign-loggen
till `NONE` i tjänsten. Samma försiktighet gäller här: dela inte körhistorik, exportera inte miljön
med nyckeln i, klistra inte svar i ärenden eller chattar. Använd testpersoner i utb, inte skarpa
personnummer.
