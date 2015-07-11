<?php 

	

	$dbhost = "localhost";
	$dbuser = "lookoutt_appUser";
	$dbpass = "}atkrWP3vx@W";
	$dbname = "lookoutt_lookout";

	
	//$dbuser2 = "krystalt_dbuser2";
	//$dbpass2 = "n0d@t@l0g1n";
	
	
	$link = mysqli_connect($dbhost, $dbuser, $dbpass,$dbname);
	
	//$link2 = mysqli_connect($dbhost, $dbuser2, $dbpass2,$dbname);
	
	/* check connection */
	if (mysqli_connect_errno()) 
	{
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	/* check if server is alive */
	if (!mysqli_ping($link)) 
	{
		printf ("Error: %s\n", mysqli_error($link));
	}

?>