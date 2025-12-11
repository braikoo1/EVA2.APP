#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <Firebase_ESP_Client.h>  

const int trigPin   = D5;
const int echoPin   = D6;
const int buzzerPin = D7;

const char* ssid     = "iPhone";
const char* password = "martin12";

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

const char* DATABASE_URL    = "https://eva2-82148-default-rtdb.firebaseio.com";
const char* DATABASE_SECRET = "9JAXJ9M1hFHCqMf2IRSBGqDy18Xf5Z6OY7y7jiK1";

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", -3 * 3600, 60000);
unsigned long lastTimeSync = 0;
const unsigned long timeSyncInterval = 60000; 

long duration;
int  distanceCm;

bool   cfgAlarma    = true;
int    cfgDistancia = 15;
bool   cfgSilencio  = false;
int    cfgSonido    = 1;
String cfgHorario   = "00:00 - 23:59";

int horarioInicioMin = 0;
int horarioFinMin    = 23*60 + 59;

bool puertaPrincipalAnterior = false;

unsigned long ultimoUpdateConfig = 0;
const unsigned long intervaloConfigMs = 5000;


void parseHorario(const String &h) {
  int guion = h.indexOf('-');
  if (guion < 0) return;

  String inicio = h.substring(0, guion);
  String fin    = h.substring(guion + 1);

  inicio.trim();
  fin.trim();

  int p = inicio.indexOf(':');
  int q = fin.indexOf(':');
  if (p < 0 || q < 0) return;

  int h1 = inicio.substring(0, p).toInt();
  int m1 = inicio.substring(p + 1).toInt();
  int h2 = fin.substring(0, q).toInt();
  int m2 = fin.substring(q + 1).toInt();

  horarioInicioMin = h1 * 60 + m1;
  horarioFinMin    = h2 * 60 + m2;
}


void aplicarHorarioAutomatico() {
  static int ultimaActivacionMin  = -1;
  static int ultimaDesactivacionMin = -1;

  int h = timeClient.getHours();
  int m = timeClient.getMinutes();
  int actual = h * 60 + m;

  if (actual == horarioInicioMin && actual != ultimaActivacionMin) {
    ultimaActivacionMin = actual;
    cfgAlarma = true;
    Firebase.RTDB.setBool(&fbdo, "/configuracion/alarma", true);
    Serial.println(">> Alarma ACTIVADA por horario");
  }

  if (actual == horarioFinMin && actual != ultimaDesactivacionMin) {
    ultimaDesactivacionMin = actual;
    cfgAlarma = false;
    Firebase.RTDB.setBool(&fbdo, "/configuracion/alarma", false);
    Serial.println(">> Alarma DESACTIVADA por horario");
  }
}


void leerConfiguracionFirebase() {

  Serial.println("=== LEYENDO CONFIGURACIÓN FIREBASE ===");

  if (Firebase.RTDB.getBool(&fbdo, "/configuracion/alarma"))
    cfgAlarma = fbdo.boolData();

  if (Firebase.RTDB.getInt(&fbdo, "/configuracion/distancia"))
    cfgDistancia = fbdo.intData();
  else if (Firebase.RTDB.getString(&fbdo, "/configuracion/distancia"))
    cfgDistancia = fbdo.stringData().toInt();

  if (Firebase.RTDB.getString(&fbdo, "/configuracion/horario")) {
    cfgHorario = fbdo.stringData();
    parseHorario(cfgHorario);
  }

  if (Firebase.RTDB.getBool(&fbdo, "/configuracion/silencio"))
    cfgSilencio = fbdo.boolData();

  if (Firebase.RTDB.getInt(&fbdo, "/configuracion/sonido")) {
    cfgSonido = fbdo.intData();
    if (cfgSonido < 1 || cfgSonido > 3) cfgSonido = 1;
  }

  Serial.println("----- CONFIG CARGADA -----");
  Serial.print("alarma: ");    Serial.println(cfgAlarma);
  Serial.print("distancia: "); Serial.println(cfgDistancia);
  Serial.print("horario: ");   Serial.println(cfgHorario);
  Serial.print("silencio: ");  Serial.println(cfgSilencio);
  Serial.print("sonido: ");    Serial.println(cfgSonido);
  Serial.println("--------------------------");
}


void actualizarPuertaEnFirebase(bool abierta) {
  if (abierta != puertaPrincipalAnterior) {
    puertaPrincipalAnterior = abierta;
    Firebase.RTDB.setBool(&fbdo, "/configuracion/puertaPrincipal", abierta);
    Serial.print("puertaPrincipal -> ");
    Serial.println(abierta ? "true" : "false");
  }
}


void sonarPatron() {
  if (cfgSilencio) {
    noTone(buzzerPin);
    delay(200);
    return;
  }

  switch (cfgSonido) {
    case 1:
      tone(buzzerPin, 1200); delay(150);
      noTone(buzzerPin);     delay(120);
      tone(buzzerPin, 1000); delay(150);
      noTone(buzzerPin);     delay(120);
      break;

    case 2:
      tone(buzzerPin, 800);  delay(100);
      noTone(buzzerPin);     delay(80);
      tone(buzzerPin, 1500); delay(180);
      noTone(buzzerPin);     delay(80);
      break;

    case 3:
      for (int i = 600; i <= 1500; i += 120) {
        tone(buzzerPin, i);
        delay(60);
      }
      noTone(buzzerPin);
      delay(100);
      break;
  }
}


void setup() {
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(buzzerPin, OUTPUT);

  Serial.begin(115200);
  Serial.println("\nConectando WiFi...");

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
  }
  Serial.println("\nWiFi OK");
  Serial.println(WiFi.localIP());

  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  timeClient.begin();
  timeClient.update();

  Serial.println("Sistema listo.");
}


void loop() {

  if (millis() - lastTimeSync > timeSyncInterval) {
    lastTimeSync = millis();
    timeClient.update();
    Serial.print("Hora actual Chile UTC-3: ");
    Serial.print(timeClient.getHours());
    Serial.print(":");
    Serial.println(timeClient.getMinutes());
  }
  if (Firebase.ready() &&
      WiFi.status() == WL_CONNECTED &&
      millis() - ultimoUpdateConfig > intervaloConfigMs) {

    leerConfiguracionFirebase();
    ultimoUpdateConfig = millis();
  }

  aplicarHorarioAutomatico();

  digitalWrite(trigPin, LOW);  
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH); 
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH, 30000);
  distanceCm = duration / 58;

  if (distanceCm <= 0 || distanceCm > 400) {
    actualizarPuertaEnFirebase(false);
    noTone(buzzerPin);
    delay(200);
    return;
  }

  bool puertaAbierta = (distanceCm <= cfgDistancia);
  actualizarPuertaEnFirebase(puertaAbierta);

  bool puedeSonar = (cfgAlarma && puertaAbierta);

  if (puedeSonar) sonarPatron();
  else {
    noTone(buzzerPin);
    delay(200);
  }
}
