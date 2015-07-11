<?php

error_reporting(-1);
ini_set('display_errors', 'On');

    include 'db_connect.php';
	
	
	
    $fname = strtoupper($_REQUEST["firstName"]);
    $lname = strtoupper($_REQUEST["lastName"]);
    $email = strtoupper($_REQUEST["username"]);
    $password = $_REQUEST["password"];
	

    
	//---------------------------------------------------------------------------------INPUTS---------------------------------------------------------------------------------
	//get user signup details, coming from ServerRequest.java (protected Void doInBackground(Void... params))
	
	
    
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	

	$email = $link->real_escape_string($email);
	
	
	$message  = array(); // array that will be returned as JSON string
	$userExist = false;	
		
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Validate e-mail	
	if (!filter_var($email, FILTER_VALIDATE_EMAIL) === false){
	//Query to check if user exist
		$query = "SELECT firstName FROM users u
						WHERE u.email = ? ";
						
		if($stmt = $link->prepare($query)){
			mysqli_stmt_bind_param($stmt, 's', $email);
			mysqli_stmt_execute($stmt);
			$firstName_results = mysqli_stmt_get_result($stmt);
			
			if($firstName_results_rows = mysqli_fetch_assoc($firstName_results)){
				$userExist = true;	
			}
		}
	} // end of if (!filter_var($email, FILTER_VALIDATE_EMAIL) === false) 
	else{
		$message["response_message"] = "Email is not a valid email address";
	}

	
	//----------------------------------------------------------------------------------------------------------------------------------------------------------
	
		//Query to get the userTypeId of a GENERAL user
	$query = "SELECT userTypeId FROM userType WHERE UPPER(userTypeName) =  'GENERAL'";
	if($stmt = $link->prepare($query))
	{
		mysqli_stmt_execute($stmt);
		$userType_results = mysqli_stmt_get_result($stmt);
		if($userType_results_rows = mysqli_fetch_assoc($userType_results))
			$userType = $userType_results_rows["userTypeId"];	
	}
	
	$status =1; //Active user, change when status table is built
	

		
	if(!$userExist){ //if a user with the supplied email does not exist create the user, Email has already been validated is this is true
		
		$query = "INSERT INTO users (firstName, lastName, email, password, status, userTypeId) VALUES (?, ?, ?, ?, ?, ?)";
		if($stmt = $link->prepare($query)){
			$fname = $link->real_escape_string($fname);
			$lname = $link->real_escape_string($lname);
			mysqli_stmt_bind_param($stmt, "ssssii", $fname, $lname, $email, $password, $status, $userType);
			mysqli_stmt_execute($stmt);
		}
	}
	else{
		$message["response_message"] = "The user already exist";
	} // End of if ($userExist)
	

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	echo json_encode($message);
	
		
	//mysqli_stmt_close($query);
	mysqli_close($link);
	
	

?>
