
class drive:

	def __init__(self, forward_pin,reverse_pin,right_pin, left_pin)
		forward_obj = mraa.Gpio(forward_pin)
		forward_obj.dir(mraa.DIR_OUT)

		reverse_obj = mraa.Gpio(reverse_pin)
		reverse_obj.dir(mraa.DIR_OUT)
		
		right_obj = mraa.Gpio(right_pin)
		right_obj.dir(mraa.DIR_OUT)

		left_obj = mraa.Gpio(left_pin)
		left_obj.dir(mraa.DIR_OUT)
	

	def fullStop()
		stop()
		center()

		
	//Movment
	def forward()
	    //digitalWrite(REVERSE_PIN, LOW);
		//digitalWrite(FORWARD_PIN, HIGH); 
		forward_obj.write(1)
		reverse_obj.write(0)

 
	def stop()
		//digitalWrite(FORWARD_PIN, LOW); 
		//digitalWrite(REVERSE_PIN, LOW);
		forward_obj.write(0)
		reverse_obj.write(0)

  
	def reverse()
		//digitalWrite(FORWARD_PIN, LOW); 
		//digitalWrite(REVERSE_PIN, HIGH);
		forward_obj.write(0)
		reverse_obj.write(1)

	
	//Steering
	def left()
		//digitalWrite(RIGHT_PIN, LOW);
		//digitalWrite(LEFT_PIN, HIGH); 

  
	def center()
    //digitalWrite(LEFT_PIN, LOW); 
    //digitalWrite(RIGHT_PIN, LOW);
 
  
	def right()
    //digitalWrite(LEFT_PIN, LOW); 
    //digitalWrite(RIGHT_PIN, HIGH);
 