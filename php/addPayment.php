<?php
include 'db_connect.php';
include 'functions.php';

sec_session_start(); //custom way of starting session, code inside functions.php

// Login Check to guarantee user is logged in
// --------------------------------------------------------------------------------------------------------------------------------------
if (!login_check($link))
{
	//Destroy session
	session_destroy();
	header('Location: index.php');
	
}
// --------------------------------------------------------------------------------------------------------------------------------------

//Query to get usertypename
$query = "SELECT UPPER(ut.userType) AS userType FROM users u, usertype ut
				WHERE u.id = ?
				AND u.userTypeId = ut.userTypeId ";
if($stmt = $link->prepare($query))
{
	mysqli_stmt_bind_param($stmt, 'i', $_SESSION['user_id']);
	mysqli_stmt_execute($stmt);
	$usertype_results = mysqli_stmt_get_result($stmt);
	if($usertype_results_rows = mysqli_fetch_assoc($usertype_results))
		$usertype = $usertype_results_rows["userType"];
}

if(!($usertype == 'ADMINISTRATOR' || $usertype == 'OPERATOR'))
{
	
	// Destroy session 
	session_destroy();
	header('Location: index.php');
}
// --------------------------------------------------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------------------------------------------------

//unset session variables
if(isset($_SESSION["booking_total"]))
	unset($_SESSION["booking_total"]);



if(isset($_SESSION["payments_array"]))
	unset($_SESSION["payments_array"]);

// --------------------------------------------------------------------------------------------------------------------------------------

$booking_id = $_GET["p"];	//booking id sent as GET from nyromodal window on dashboard.php 


//query to get existing payment information for this booking
$query = "SELECT CONCAT_WS(' ', c.FirstName, c.LastName) as Name, bookingTotal 
				FROM booking b, customer c 
				WHERE b.bookingId = ? 
				AND b.mainCustomerId = c.CustomerId";
if($stmt = $link->prepare($query))
{
	mysqli_stmt_bind_param($stmt, 'i', $booking_id );
	mysqli_stmt_execute($stmt);
	$booking_results = mysqli_stmt_get_result($stmt);
	if($booking_results_rows = mysqli_fetch_assoc($booking_results))
	{
		$customer_name = $booking_results_rows["Name"];
		$booking_total = $booking_results_rows["bookingTotal"];
	}
}


//Query to get the acculumated payments total for this booking
$accumulated_total = 0;
$query = "SELECT SUM(amount) AS amount 
				FROM payment p, bookingpayment bp 
				WHERE bp.bookingId = ? 
				AND bp.paymentId = p.paymentId";
if($stmt = $link->prepare($query))
{
	mysqli_stmt_bind_param($stmt, 'i', $booking_id);
	mysqli_stmt_execute($stmt);
	$accumulated_total_results = mysqli_stmt_get_result($stmt);
	if($accumulated_total_results_rows = mysqli_fetch_assoc($accumulated_total_results))
		$accumulated_total = $accumulated_total_results_rows["amount"];
}			


//Query to get payment types to populate dropdown menu 
$query = "SELECT typeName FROM paymenttype ORDER BY typeName ASC";
if($stmt = $link->prepare($query))
{
	mysqli_stmt_execute($stmt);
	$payment_types_results = mysqli_stmt_get_result($stmt);

}		


//Query to get currency to populate currency dropdown 
$query = "SELECT currencyId FROM currency";
if($stmt = $link->prepare($query))
{
	mysqli_stmt_execute($stmt);
	$currency_results = mysqli_stmt_get_result($stmt);

}	


// create $_SESSION["booking_total"] to use in addPayment_add_new_payment.php 
$_SESSION["booking_total"] = $booking_total - $accumulated_total;	


?>




<!DOCTYPE html>
<html><head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="description" content="" />
<meta name="copyright" content="" />
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<!-- CUSTOM STYLES -->

<link rel="stylesheet" type="text/css" href="style.css" media="all" />  
<link rel="stylesheet" type="text/css" href="css/component.css" />
                        
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="js/kickstart.js"></script>      
<script src="js/modernizr.custom.js"></script>

<!-- Script to add new payment  -->
<script src="js/addPayment_add_new_payment_ajax.js"></script>

<!--- Script to validate decimal values entered-->
<script src="js/decimal_validation.js"></script>


<!-- Script to convert booking total to currency selected  -->
<script src="js/dashboard_addNewPayment_convert_currency_ajax.js"></script>

<!-- Script to convert booking total to currency selected  -->
<script src="js/addPayment_remove_payment_ajax.js"></script>


</head><body class="white">


<form action="dashboard_addNewPayment.php" method="post" name="addPaymentInfo" target="_parent">
<div class="col_12"><h6>Add Payment</h6></div>
	<div class="col_4">
		<input type="text" name="fname" id="fname" placeholder="First Name" required/> 
	</div>
	<div class="col_4">
		<input type="text" name="lname" id="lname" placeholder="Last Name" required/>
	</div>
	<div class="col_4">
		<input type="text" name="phone" id= "phone"placeholder="Phone" />
	</div>

	<div id="booking_info">
		<h6> Booking Info </h6>
		</br>
		&nbsp; &nbsp;  Booking # : <?php echo $booking_id;?> </br>
		&nbsp; &nbsp;Customer Name: <?php echo $customer_name; ?> </br>
		&nbsp; &nbsp;Booking Total:  <?php echo $booking_total; ?></br><br>
		&nbsp; &nbsp;<label for="payment_currency"> Currency</label>
		<select id="payment_currency" name="payment_currency" onchange="convert_total();">
					<option value="0" > Select Currency </option>
					<?php 
						while($currency_results_rows = mysqli_fetch_assoc($currency_results))
						{
							echo'<option value="'.$currency_results_rows["currencyId"].'" >'.$currency_results_rows["currencyId"].'</br>';
						}
					?>
		</select>
	</div>

<div class="col_12">
	<div  id="payment_info" name="payment_info" >
	
		
		<table class=" tight sortable">
		<thead><tr>
			<th>Current Balance</th>
			<th>Payment Amount</th>
			<th>Payment Method</th>
			<th> Action</th>
		</tr></thead>
		<tbody>
		<tr>
			<td id="current_balance" name="current_balance"><?php echo $booking_total - $accumulated_total; ?></td>
			<td><input type="text" name="new_payment_amount" id="new_payment_amount" onblur="validate_decimal(this.id);" readonly /></td>
			<td><select id="payment_types"  name="payment_types" disabled>
				<option VALUE="0" >Select Payment Type</option>
				<?php
				while($payment_types_results_rows =  mysqli_fetch_assoc($payment_types_results))
				{
					echo '<option VALUE='.$payment_types_results_rows["typeName"].' >'.$payment_types_results_rows["typeName"].'</option>';
				}		

				?>
			</select></td>
			<td><input type="button"  name="add_payment_amount" id= "add_payment_amount" value="Add Payment" class="action-button" onclick="add_new_payment();" disabled /></td>
		</tr>
		</tbody>
		</table>
	<!-- Empty table row where added payments will show up via ajax-->
		<?php
			echo'<input type="hidden" name="payment_amount" id= "payment_amount" value ="'.$accumulated_total.'"/>'; 
			
		?>


	</div>
	<?php
	echo'<input type="hidden" name="hidden_booking_id" id= "hidden_booking_id" value ="'.$booking_id.'"/>'; 
	echo'<input type="hidden" name="hidden_booking_total" id= "hidden_booking_total" value ="'.$booking_total.'"/>';  
	$initial_current_balance = $booking_total- $accumulated_total;
	echo'<input type="hidden" name="hidden_initial_current_balance" id="hidden_initial_current_balance"  value="'.$initial_current_balance.'" />';
	echo'<input type="hidden" name="hidden_booking_id" id="hidden_booking_id" value="'.$booking_id.'" />';
	echo'<input type="hidden" name="hidden_customer_name" id="hidden_customer_name" value="'.$customer_name.'" />';
	?>
<div class="col_3"><input name="submit" id="submit_button" type="submit" class=" form-button" value="Update" onclick="check_form();" disabled/></div>
</div>
<br/>
</form>

</body>
</html>