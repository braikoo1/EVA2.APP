#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <Firebase_ESP_Client.h>
#include <EEPROM.h>

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

long duration;
int  distanceCm;

bool cfgAlarma = true;
int  cfgDistancia = 15;
bool cfgSilencio = false;
int  cfgSonido = 1;
String cfgHorario = "09:15 - 09:16"; 

bool alarmaAnterior = false;     
bool alarmaSonando = false;       
bool puertaAnterior = false;      

#define EEPROM_SIZE 512
#define MAX_NOTIFS 10

struct NotificacionEEPROM {
  char mensaje[50];
  char hora[6];
  bool abierta;
};

int notifCount = 0;
unsigned long ultimoReintento = 0;
unsigned long ultimoEstado = 0;
unsigned long ultimoFirebaseCheck = 0;

// ================== FUNCIONES =================
String horaActual() {
  char buf[6];
  sprintf(buf, "%02d:%02d", timeClient.getHours(), timeClient.getMinutes());
  return String(buf);
}

void guardarNotificacionLocal(String mensaje, bool abierta) {
  if (notifCount >= MAX_NOTIFS) return;

  NotificacionEEPROM n;
  mensaje.toCharArray(n.mensaje, 50);
  horaActual().toCharArray(n.hora, 6);
  n.abierta = abierta;

  EEPROM.put(notifCount * sizeof(NotificacionEEPROM), n);
  notifCount++;
  EEPROM.put(400, notifCount);
  EEPROM.commit();

  Serial.println("📥 Notificación guardada en EEPROM");
}

void cargarNotificacionesLocales() {
  EEPROM.get(400, notifCount);
  if (notifCount < 0 || notifCount > MAX_NOTIFS) notifCount = 0;
}

void enviarNotificacionesPendientes() {
  if (!Firebase.ready() || notifCount == 0) return;

  for (int i = 0; i < notifCount; i++) {
    NotificacionEEPROM n;
    EEPROM.get(i * sizeof(NotificacionEEPROM), n);

    String id = String(millis()) + "_" + String(i);
    String payload = String(n.mensaje) + "|" + String(n.hora) + "|" + (n.abierta ? "true" : "false");

    if (!Firebase.RTDB.setString(&fbdo, "/configuracion/notificaciones/" + id, payload)) {
      Serial.println(" Error reenviando, se reintentará");
      return;
    }

    Serial.println("Reenviado: " + payload);
  }

  notifCount = 0;
  EEPROM.put(400, notifCount);
  EEPROM.commit();
}

void manejarAlarma(bool activar) {
  if (activar && !alarmaAnterior) {
    alarmaSonando = true; 
    alarmaAnterior = true;

    Firebase.RTDB.setBool(&fbdo, "/configuracion/puertaPrincipal", true);

    String mensaje = "Puerta Principal abierta - alarma activada";
    String payload = mensaje + "|" + horaActual() + "|true";
    String id = String(millis());

    if (Firebase.ready()) {
      if (!Firebase.RTDB.setString(&fbdo, "/configuracion/notificaciones/" + id, payload)) {
        guardarNotificacionLocal(mensaje, true);
      }
    } else {
      guardarNotificacionLocal(mensaje, true);
    }

    Serial.println("🚨" + mensaje);
  }

  if (!activar && alarmaAnterior) {
    alarmaSonando = false;  
    alarmaAnterior = false;

    Firebase.RTDB.setBool(&fbdo, "/configuracion/puertaPrincipal", false);
    Serial.println("Alarma desactivada");
  }
}

void mostrarEstado() {
  Serial.println("----- CONFIG CARGADA -----");
  Serial.print("alarma: "); Serial.println(cfgAlarma ? 1 : 0);
  Serial.print("distancia: "); Serial.println(cfgDistancia);
  Serial.print("horario: "); Serial.println(cfgHorario);
  Serial.print("silencio: "); Serial.println(cfgSilencio ? 1 : 0);
  Serial.print("sonido: "); Serial.println(cfgSonido);
  Serial.println("--------------------------");
  Serial.print("Hora actual Chile UTC-3: "); Serial.println(horaActual());
  Serial.print("puertaPrincipal -> "); Serial.println(puertaAnterior ? "true" : "false");
  Serial.println();
}

// ================== SETUP =================
void setup() {
  Serial.begin(115200);

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(buzzerPin, OUTPUT);

  EEPROM.begin(EEPROM_SIZE);
  cargarNotificacionesLocales();

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) delay(300);

  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  timeClient.begin();
  timeClient.update();

  Serial.println("Sistema iniciado - Alarma controlada por activación");
}

// ================== LOOP =================
void loop() {
  timeClient.update();

  // Cada 5 segundos leer sonido y silencio de Firebase
  if (millis() - ultimoFirebaseCheck > 5000) {
    ultimoFirebaseCheck = millis();
    if (Firebase.RTDB.getInt(&fbdo, "/configuracion/sonido")) cfgSonido = fbdo.intData();
    if (Firebase.RTDB.getBool(&fbdo, "/configuracion/silencio")) cfgSilencio = fbdo.boolData();
  }

  // Medición distancia
  digitalWrite(trigPin, LOW); delayMicroseconds(2);
  digitalWrite(trigPin, HIGH); delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH, 30000);
  if (duration > 0) distanceCm = duration / 58;
  else distanceCm = 999;

  bool activarAlarma = (distanceCm <= cfgDistancia) && cfgAlarma;

  manejarAlarma(activarAlarma);

  if (alarmaSonando && !cfgSilencio) {
    switch(cfgSonido){
      case 1: tone(buzzerPin,1200); delay(150); noTone(buzzerPin); delay(120);
              tone(buzzerPin,1000); delay(150); noTone(buzzerPin); delay(120);
              break;
      case 2: tone(buzzerPin,800); delay(100); noTone(buzzerPin); delay(80);
              tone(buzzerPin,1500); delay(180); noTone(buzzerPin); delay(80);
              break;
      case 3: for(int i=600;i<=1500;i+=120){ tone(buzzerPin,i); delay(60); }
              noTone(buzzerPin);
              break;
    }
  }

  if (millis() - ultimoEstado > 5000) {
    ultimoEstado = millis();
    mostrarEstado();
  }

  puertaAnterior = (distanceCm <= cfgDistancia);

  if (millis() - ultimoReintento > 5000) {
    ultimoReintento = millis();
    enviarNotificacionesPendientes();
  }

  delay(200);
}
