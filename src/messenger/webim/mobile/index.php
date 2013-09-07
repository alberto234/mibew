<?php
/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:	Eyong Nsoesie (eyong.ns@gmail.com)
 * Date: 	September 3, 2013
 */


/*
 * This is the entry point of the mobile client API. 
 * It is a JSON-based REST API. All the supporting methods
 * will return an array where appropriate and will be JSON
 * encoded here. This allows for easy migration to other standards
 */
 
 
require_once('../libs/common.php');
require_once('functions.php');

// For testing
// C74BEDBF52


header("Content-Type: application/json");

// Mobile client command processor
if ($_GET['cmd'] == 'isalive') {
	$out = chat_server_status();
	$jsonOut = json_encode($out);
	echo $jsonOut;
}
else if($_GET['cmd'] == 'login') {
	$username = $_GET['username'];
	$password = $_GET['password'];
	
	$out = mobile_login($username, $password);
	$jsonOut = json_encode($out);
	echo $jsonOut;
}
else if ($_GET['cmd'] == 'visitorlist') {
	$oprtoken = $_GET['oprtoken'];
	
	$out = get_active_visitors($oprtoken);

	$jsonOut = json_encode($out);
	echo $jsonOut;
}
else if ($_GET['cmd'] == 'startchat') {
	$oprtoken = $_GET['oprtoken'];
	$threadid = $_GET['threadid'];
	
	$out = start_chat($oprtoken, $threadid);
	$jsonOut = json_encode($out);
	echo $jsonOut;
}
else if ($_GET['cmd'] == 'newmessages') {
	$oprtoken = $_GET['oprtoken'];
	$threadid = $_GET['threadid'];
	$chattoken = $_GET['token'];
	
	$out = get_new_messages($oprtoken, $threadid, $chattoken);
	$jsonOut = json_encode($out);
	echo $jsonOut;
}
else if ($_GET['cmd'] == 'postmessage') {
	$oprtoken = $_GET['oprtoken'];
	$opMsg = $_GET['message'];
	$threadid = $_GET['threadid'];
	$chattoken = $_GET['token'];
	
	$out = msg_from_mobile_op($oprtoken, $threadid, $chattoken, $opMsg);
	$jsonOut = json_encode($out);
	echo $jsonOut;
}
?>
