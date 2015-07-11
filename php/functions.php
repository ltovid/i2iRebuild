<?php
ob_start();
function sec_session_start()
{
	$session_name = 'sec_session_id'; //Set a custom session name
	$secure = false; //set to true if using https
	$httponly = true; //stops javascript from being able to access session id
	
	ini_set('session.use_only_cookies', 1);//Forces the session to only use cookies
	$cookieParams = session_get_cookie_params();//Gets current cookies params
	session_set_cookie_params($cookieParams["lifetime"],$cookieParams["path"], $cookieParams["domain"], $secure, $httponly);
	session_name($session_name);//Sets the session name to sec_session_id
	session_start();//Start the php session
	session_regenerate_id();//regenerate the session, delete the old one.
}

function login($username,$password,$link)
{
	if($stmt = $link->prepare("SELECT id, password, salt, status FROM users WHERE username = LOWER(?) LIMIT 1"))
	{
		$stmt->bind_param('s', $username);
		$stmt->execute();
		$stmt->store_result();
		$stmt->bind_result($user_id, $db_password, $salt, $status);//get variables from result
		$stmt->fetch();
		$password = hash('sha512',$password.$salt);
		
		if($stmt->num_rows == 1) //If user exist
		//check to see if account is locked
		{
			if($status == 1) // 2 status code means locked account, 1 is active
			{
				if(checkbrute($user_id, $link) == true)//too many login attempts
				{
					//Account is locked
					//Send an email to user and Administrators
					//Change status to locked
					return 3; //return 3 if account has just been locked 
				}
				else
					if($db_password == $password)
						return 5; //return 5 if login successful
					else
						//Insert into login_attempts table
						return 4;//return 4 if password is incorrect
			}
			else
				return 2; //return 2 is account is not active
		}
		return 1; // return 1 if username does not exist
	}
}//end of login

function checkbrute($user_id, $link)
{
	//Get timestamp of current time
	$now = time();
	//All login attempts are counted from the past 30 minutes
	$valid_attempts = $now - (30 * 60);
	
	if($stmt = $link->prepare("SELECT time FROM login_attempts WHERE user_id = ? AND time > '$valid_attempts'"))
	{
		$stmt->bind_param('i',$user_id);
		$stmt->execute();
		$stmt->store_result();
		// If there are more than 5 attempted logins from room
		if($stmt->num_rows > 5)
			return true;
		else
			return false;
	
	}
}

function login_check($link)
{

	$now = new DateTime();
	$now->setTimezone(new DateTimeZone('America/Port_of_Spain'));
	$time = $now->format('Y-m-d H:i:s'); // same format as NOW()
	
	//Check to see if our "last action" session
	//variable has been set.
	if(isset($_SESSION['last_action']))
	{
	    session_regenerate_id(true);
	    //Figure out how many seconds have passed
	    //since the user was last active.
	    $secondsInactive = time() - $_SESSION['last_action'];
	    
	    //Convert our minutes into seconds.
	    $expireAfterSeconds = $_SESSION['expireAfter'] * 60;
	    
	    //Check to see if they have been inactive for too long.
	    if($secondsInactive >= $expireAfterSeconds)
	    {
	        //User has been inactive for too long.
	        //Kill their session.
	        session_unset();
	        session_destroy();
	        header('Location: index.php');
	    }
	    else
	        $_SESSION['last_action'] = time(); //reset the counter
	}



	if(isset($_SESSION['user_id'], $_SESSION['username'], $_SESSION['login_string']))
	{
		$user_id = $_SESSION['user_id'];
		$username = $_SESSION['username'];
		$login_string = $_SESSION['login_string'];
		
		
		$user_browser = $_SERVER['HTTP_USER_AGENT']; //get user-agent string of the user
		
		if($stmt = $link->prepare("SELECT password FROM users WHERE id = ? LIMIT 1"))
		{
			$stmt->bind_param('i', $user_id);
			$stmt->execute();
			$stmt->store_result();
			
			if($stmt->num_rows == 1) //if user exist
			{
				$stmt->bind_result($password); //get variable from result
				$stmt->fetch();
				$login_check = hash('sha512', $password.$user_browser);
				
				if($login_check == $login_string)
				{
					//Logged In!!!
					return true;
				} // end of if($login_check == $login_string)
				else
					return false;
				
			}//end of if($stmt->num_rows == 1)
			
		}//end of if($stmt = $link->prepare("SELECT password FROM members WHERE id = ? LIMIT 1"))
		
	}//end of if(isset($_SESSION['user_id'], $_SESSION['username'], $_SESSION['login_string']))

} // end of login_check

ob_flush();
?>