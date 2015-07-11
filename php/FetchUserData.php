<?php
    include 'db_connect.php';
	
	error_reporting(-1);
    ini_set('display_errors', 'On');
      
	//---------------------------------------------------------------------------------INPUTS---------------------------------------------------------------------------------  
    $email = strtoupper($_REQUEST["username"]);

	$email = $link->real_escape_string($email);
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	$user = array(); // array that will be returned as JSON string
	
	//Query to check if user exist
	$query =  "SELECT * FROM users WHERE email = ?";
	if($stmt = $link->prepare($query))
	{
		mysqli_stmt_bind_param($stmt, "s", $email);
		mysqli_stmt_execute($stmt);
		$user_results = mysqli_stmt_get_result($stmt);
		if($user_results_rows = mysqli_fetch_assoc($user_results))
		{
			$user["password"] = $user_results_rows["password"];	
			$user["firstName"] = $user_results_rows["firstName"];	
			$user["lastName"] = $user_results_rows["lastName"];	
		}
	}
    
    
    echo json_encode($user);
    mysqli_close($link);
?>
