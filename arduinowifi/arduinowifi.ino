
/*
Arduino WiFi Script Server

Created October 20, 2013
Mikael Kindborg, Evothings AB

TCP socket server that accept commands for basic scripting
of the Arduino board.

This example is written for a network using WPA encryption.
For WEP or WPA, change the Wifi.begin() call accordingly.

The API consists of the requests listed below.

Requests and responses end with a new line.

The input parameter n is a pin number ranging from 2 to 9.

The response is always a 4-character string with a
hex encoded number ranging from 0 to FFFF.

Possible response string values:

H (result from digital read)
L (result from digital read)
0 to 1023 - Analog value (result from analog read)

Set pin mode to OUTPUT for pin n: On
Response: None
Example: O5
Note: O is upper case letter o, not digit zero (0).

Set pin mode to INPUT for pin n: In
Response: None
Example: I5

Write LOW to pin n: Ln
Response: None
Example: L5

Write HIGH to pin n: Hn
Response: None
Example: H5

READ pin n: Rn
Response: "H" (HIGH) or "L" (LOW)
Example: R5 -> H

ANALOG read pin n: An
Response: int value as string (range "0" to "1023")
Example: A5 -> 42
*/

// Include files.
//#include <SPI.h>


//#include <Dhcp.h>
//#include <Dns.h>
//#include <WiFi.h>
//#include <WiFiClient.h>
#include <WiFiServer.h>
//#include <WiFiUdp.h>

//#define LCD_ENABLED 1 

#ifdef LCD_ENABLED
// include the library code:
#include <LiquidCrystal.h>

// initialize the library with the numbers of the interface pins
//LiquidCrystal(rs, enable, d4, d5, d6, d7) 
LiquidCrystal lcd(11, 10, 9, 8, 7, 6);
#endif

// Remove this line once you've entered WiFi SSID and password below.
//#error "WiFi SSID and password required!"

// Your network SSID (network name).
// TODO: Enter the name of your wifi network here.
char ssid[] = "tesla";

// Your network password.
// TODO: Enter the password of your wifi network here.
char pass[] = "FIXME";

// Your network key Index number (needed only for WEP).
int keyIndex = 0;

// Server status flag.
int status = WL_IDLE_STATUS;

// Create WiFi server listening on the given port.
WiFiServer server(3300);



// LED ports
//#define GREEN_LED 0
//#define RED_LED 1

#define FORWARD_PIN 12
#define REVERSE_PIN 11

#define LEFT_PIN 9
#define RIGHT_PIN 10

#define WIFI_PIN 13

enum STATUS_LED {
  NO_WIFI, //LED off
  NO_CLIENT, //blink
  ALL_CONNECTED //LED on
  } status_enum;


void setup()
{
	// Start serial communication with the given baud rate.
	// NOTE: Remember to set the baud rate in the Serial
	// monitor to the same value.
	Serial.begin(9600);

	// Wait for serial port to connect. Needed for Leonardo only
	while (!Serial) { ; }

  Serial.println("Serial debug output up...\n");

	// Check for the presence of the WiFi shield.
	if (WiFi.status() == WL_NO_SHIELD)
	{
		// If no shield, print message and exit setup.
		Serial.println("WiFi shield not present");
		status = WL_NO_SHIELD;
		return;
	}

//  pinMode(LED_BUILTIN, OUTPUT);  //rtpike

  pinMode(WIFI_PIN, OUTPUT);

/*
  pinMode(GREEN_LED, OUTPUT);
  pinMode(RED_LED, OUTPUT);
  digitalWrite(GREEN_LED, HIGH);
  digitalWrite(RED_LED, HIGH);
*/


  pinMode(FORWARD_PIN, OUTPUT);
  pinMode(REVERSE_PIN, OUTPUT);
  pinMode(LEFT_PIN, OUTPUT);
  pinMode(RIGHT_PIN, OUTPUT);

  digitalWrite(FORWARD_PIN, LOW);
  digitalWrite(REVERSE_PIN, LOW);
  digitalWrite(LEFT_PIN, LOW);
  digitalWrite(RIGHT_PIN, LOW);
  

  

/*
	String version = WiFi.firmwareVersion();
	if (version != "1.1.0")
	{
		Serial.println("Please upgrade the firmware");
	}

// wifi should already be on (rtpike)


	// Connect to Wifi network.
	while (status != WL_CONNECTED)
	{
		Serial.print("Connecting to Network named: ");
		Serial.println(ssid);

		// Connect to WPA/WPA2 network. Update this line if
		// using open or WEP network.
    status = WiFi.begin(); //rtpike
		//status = WiFi.begin(ssid, pass);

		// Wait for connection.
		delay(1000);
	}
*/

#ifdef LCD_ENABLED
 //LCD
  lcd.begin(16,2);
  lcd.clear();
  lcd.print("Running...");
  //lcd.setCursor(1,0); //col,row
#endif

	// Start the server.
	server.begin();



	// Print WiFi status.
	//printWifiStatus();
}


void blink_led(int ledPin) {

  // Variables will change :
  static int ledState = LOW;             // ledState used to set the LED

  // Generally, you should use "unsigned long" for variables that hold time
  // The value will quickly become too large for an int to store
  static unsigned long previousMillis = 0;        // will store last time LED was updated

  // constants won't change :
  static const long interval = 700;           // interval at which to blink (milliseconds)

  // check to see if it's time to blink the LED; that is, if the
  // difference between the current time and last time you blinked
  // the LED is bigger than the interval at which you want to
  // blink the LED.
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= interval) {
    // save the last time you blinked the LED
    previousMillis = currentMillis;

    // if the LED is off turn it on and vice-versa:
    if (ledState == LOW) {
      ledState = HIGH;
    } else {
      ledState = LOW;
    }
    // set the LED with the ledState of the variable:
    digitalWrite(ledPin, ledState);
  }
}
  


void loop()
{

  char abBuf[256];
  FILE *fpipe; 
  static int oldWifiStatus = -1; //0-down, 1-up, -1 NA
  static int statusLED = NO_WIFI;
  static int clientStatus = 0; 

  strcpy(abBuf,"/sbin/ifconfig wlan0 | grep inet\\ addr | wc -l");
  //Serial.println(abBuf); 
  if ( !(fpipe = (FILE*)popen(abBuf,"r")) ) {  
    Serial.println("Problems with popen pipe");  
    return;  
  }  

  while ( fgets( abBuf, sizeof(abBuf), fpipe)) {  
    //Serial.print(abBuf);  //debug
  }  
  pclose(fpipe);  

  char wifiStatus = abBuf[0];
  //Serial.print("wifiStatus: ");
  //Serial.println(wifiStatus);
  if (wifiStatus == '1') {
    if (oldWifiStatus  != 1) {
      digitalWrite(WIFI_PIN, HIGH); //Wifi UP
      Serial.println("Wifi Up ");
#ifdef LCD_ENABLED      
      lcd.clear();
      lcd.print("Wifi Up");
#endif      
      statusLED = NO_CLIENT;
      oldWifiStatus = 1;

    }
  } else {
    blink_led(WIFI_PIN);
    if (oldWifiStatus != 0) {
      Serial.println("Wifi Down");
#ifdef LCD_ENABLED      
      lcd.clear();
      lcd.print("Wifi Down");
#endif      
      oldWifiStatus = 0;
      statusLED = NO_WIFI;
    }
  }

/*
  // when characters arrive over the serial port...
  if (Serial.available()) {
    // wait a bit for the entire message to arrive
    delay(100);
    // clear the screen
    lcd.clear();
    // read all the available characters
    while (Serial.available() > 0) {
      // display each character to the LCD
      lcd.write(Serial.read());
    }
  }
*/
  
  /*
	// Check that we are connected.
	if (status != (WL_CONNECTED))
	{
    //Serial.println("WiFi NOT connected");
		return;
	}
*/
	// Listen for incoming client requests.
	WiFiClient client = server.available();

  if (client)
  {
    statusLED = ALL_CONNECTED;
  }

/*
  switch (statusLED) {
  
    case NO_WIFI: //LED off
      digitalWrite(WIFI_PIN, LOW);  //Wifi Down
      //Serial.println("Wifi Down");
      oldWifiStatus = 0;
      break;

    case NO_CLIENT: //blink
      //blink_led(WIFI_PIN); 
      oldWifiStatus = 1;
      //Serial.println("NO_CLIENT");
      break;
          
    case ALL_CONNECTED: //LED on
      digitalWrite(WIFI_PIN, HIGH); //Wifi UP
      Serial.println("All connected");
      //oldWifiStatus = 3;
      break;
  }
*/

 	if (!client)
	{
    //Serial.println("Client NOT connected");
    fullStop();
    if (clientStatus ^ client) {
#ifdef LCD_ENABLED      
      //lcd.setCursor(0,1); //col,row
      lcd.clear();
      lcd.print("Disconnected");
#endif      
      Serial.println("Client disonnected");
      clientStatus=0;
    }
		return;
	}


  if (clientStatus ^ client) {
#ifdef LCD_ENABLED    
    lcd.clear();
    lcd.print("Client connected");
    //lcd.setCursor(0,1); //col,row
#endif    
    Serial.println("Client connected");
    clientStatus = 1;
  }

	String request = readRequest(&client);
	executeRequest(&client, &request);

	// Close the connection.
	//client.stop();

  //lcd.print("Client disonnected");
	//Serial.println("Client disonnected");
}

// Read the request line. The string from the JavaScript client ends with a newline.
String readRequest(WiFiClient* client)
{
	String request = "";

	// Loop while the client is connected.
	while (client->connected())
	{
		// Read available bytes.
		while (client->available())
		{
			// Read a byte.
			char c = client->read();

			// Print the value (for debugging).
			Serial.write(c);
      //lcd.write(c); //LCD, debug

			// Exit loop if end of line.
			if ('\n' == c)
			{
				return request;
			}

			// Add byte to request line.
			request += c;
		}
	}
	return request;
}

/* Signals are active low  
 *  
 * forward: F
 * stop: S (pins high)
 * reverse: V
 * 
 * left: E
 * center: C (pins high)
 * right:T
 * 
 */
void executeRequest(WiFiClient* client, String* request)
{
	char command = readCommand(request);
	int n = readParam(request);
	if ('O' == command)
	{
		pinMode(n, OUTPUT);
	}
	else if ('I' == command)
	{
		pinMode(n, INPUT);
	}
/*	else if ('L' == command)
	{
		digitalWrite(n, LOW);
    digitalWrite(LED_BUILTIN, HIGH);  //rtpike
    digitalWrite(GREEN_LED, LOW);    
    digitalWrite(RED_LED, HIGH);
	}
	else if ('H' == command)
	{
		digitalWrite(n, HIGH);
    digitalWrite(LED_BUILTIN, HIGH); //rtpike
    digitalWrite(GREEN_LED, HIGH);
    digitalWrite(RED_LED, LOW);
	}
 */
	else if ('R' == command)
	{
		sendResponse(client, String(digitalRead(n)));
	}
	else if ('A' == command)
	{
		sendResponse(client, String(analogRead(n)));
	}

  //Movment
  else if ('F' == command) //forward
  {
    digitalWrite(REVERSE_PIN, LOW);
    digitalWrite(FORWARD_PIN, HIGH); 

  }
  else if ('S' == command) //stop
  {
    digitalWrite(FORWARD_PIN, LOW); 
    digitalWrite(REVERSE_PIN, LOW);
  }
  else if ('V' == command) //resverse
  {
    digitalWrite(FORWARD_PIN, LOW); 
    digitalWrite(REVERSE_PIN, HIGH);
  }  

  //Steering
  else if ('E' == command) //left
  {
    digitalWrite(RIGHT_PIN, LOW);
    digitalWrite(LEFT_PIN, HIGH); 
  }
  else if ('C' == command) //center
  {
    digitalWrite(LEFT_PIN, LOW); 
    digitalWrite(RIGHT_PIN, LOW);
  }
  else if ('T' == command) //right
  {
    digitalWrite(LEFT_PIN, LOW); 
    digitalWrite(RIGHT_PIN, HIGH);
  }  


}

// Read the command from the request string.
char readCommand(String* request)
{
	String commandString = request->substring(0, 1);
	return commandString.charAt(0);
}

// Read the parameter from the request string.
int readParam(String* request)
{
	// This handles a hex digit 0 to F (0 to 15).
	char buffer[2];
	buffer[0] = request->charAt(1);
	buffer[1] = 0;
	return (int) strtol(buffer, NULL, 16);
}

void sendResponse(WiFiClient* client, String response)
{
	// Send response to client.
	client->println(response);

	// Debug print.
	Serial.println("sendResponse:");
	Serial.println(response);
}

void printWifiStatus()
{
	Serial.println("WiFi status");

	// Print network name.
	Serial.print("  SSID: ");
	Serial.println(WiFi.SSID());

	// Print WiFi shield IP address.
	IPAddress ip = WiFi.localIP();
	Serial.print("  IP Address: ");
	Serial.println(ip);

	// Print the signal strength.
	long rssi = WiFi.RSSI();
	Serial.print("  Signal strength (RSSI):");
	Serial.print(rssi);
	Serial.println(" dBm");
}

void fullStop() {
  //stop
  digitalWrite(FORWARD_PIN, LOW); 
  digitalWrite(REVERSE_PIN, LOW);

  //Steering, /center

  digitalWrite(LEFT_PIN, LOW); 
  digitalWrite(RIGHT_PIN, LOW);
}
