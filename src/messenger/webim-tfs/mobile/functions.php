<?php

require_once('../libs/common.php');
require_once('../libs/operator.php');
require_once('../libs/chat.php');
require_once('../libs/userinfo.php');
require_once('../libs/groups.php');

/* Mobile client error codes
	0 = success
	1 = login failed
	2 = invalid operator token
	3 = invalid thread
	4 = cannot take over
	5 = confirm take over
	6 = cannot view thread
	7 = wrong thread
	8 = invalid chat token
	9 = invalid command
   10 = unknown error
*/


$url = "http://nsoesie.dyndns-home.com:5242/transmawfoods/webim";
$logoURL = "http://nsoesie.dyndns-home.com:5242/transmawfoods/includes/templates/genesis/images/TFS-95.jpg";
$mibewMobVersion = "0.1";
$serverName = "Scalior Test Server";

// Todo: These two arrays are from operator/upate.php.
// They need to be put in a common place to be shared rather than duplicated
$threadstate_to_string = array(
	$state_queue => "wait",
	$state_waiting => "prio",
	$state_chatting => "chat",
	$state_closed => "closed",
	$state_loading => "wait",
	$state_left => "closed"
);

$threadstate_key = array(
	$state_queue => "chat.thread.state_wait",
	$state_waiting => "chat.thread.state_wait_for_another_agent",
	$state_chatting => "chat.thread.state_chatting_with_agent",
	$state_closed => "chat.thread.state_closed",
	$state_loading => "chat.thread.state_loading"
);


function mobile_login($username, $password) {
	if (isset($username) && isset($password)) {
		// Note: Blank passwords not currently allowed.
		$op = operator_by_login($username);
	
		if (isset($op) && md5($password) == $op['vcpassword']) {
			$oprtoken = create_operator_session($op);
			$out = array('oprtoken' => $oprtoken,
						 'operatorid' => $op['operatorid'],
						 'localename' => $op['vclocalename'],
						 'commonname' => $op['vccommonname'],
						 'permissions' => $op['iperm'],
						 'username' => $op['vclogin'],
						 'email' => $op['vcemail'],
						 'status' => $op['istatus'],
						 'lastvisited' => $op['dtmlastvisited'],
						 'errorMsg' => 'success',
						 'errorCode' => 0);
			return $out;
		}
	}
	
	$out = array('errorMsg' => 'login failed',
				 'errorCode' => 1);
	
	return $out;
}

function chat_server_status() {
	global $version, $url, $logoURL, $mibewMobVersion, $serverName;
	$out = array('name' => $serverName,
				 'URL' => $url,
				 'version' => $version,
				 'logoURL' => $logoURL,
				 'mibewMobVersion' => $mibewMobVersion,
				 'server_status' => 'on',
				 'errorCode' => 0,
				 'errorMsg' => 'success');
				 
	return $out;
}

function create_operator_session($op) {
	global $mysqlprefix;
	$link = connect();
	
	// Return the current unexpired token if the user is already logged in.
	$loggedInOp = select_one_row("select * from ${mysqlprefix}chatoperatorsession
								 where operatorid = ". $op['operatorid'], $link);
	
	if ($loggedInOp != null) {
		mysql_close($link);
		return $loggedInOp['oprtoken'];
	}

	// Token is the first 10 characters of the md5 of the current time
	$oprtoken = strtoupper(substr(md5(time()), 0, 10));
	
	$query = sprintf("insert into ${mysqlprefix}chatoperatorsession (operatorid, oprtoken) values (%d, '%s')",
					 $op['operatorid'],
					 $oprtoken);
	perform_query($query, $link);

	mysql_close($link);
	
	return $oprtoken;
}


/***********
 * Method:	
 *		get_active_visitors
 * Description:
 *	  	Returns a list of active visitors.
 *	  	i.e, those that are waiting for an operator as well as
 *	    those who already have a chat in session
 * Author:
 * 		ENsoesie 	9/4/2013	Creation
 ***********/
function get_active_visitors($oprtoken) {
	$operatorId = operator_from_token($oprtoken);
	
	if ($operatorId != NULL) {
		$out = get_pending_threads();
		$out['errorCode'] = 0;
		$out['errorMsg'] = 'success';
	}
	else {
		$out = array('errorCode' => 2,
					 'errorMsg' => 'invalid operator token');
	}
	
	return $out;
}


/***********
 * Method:	
 *		operator_from_token
 * Description:
 *	  	Checks whether the token is valid, i.e, exists and (todo) not expired.
 *		Returns the associated operator id if it is valid
 * Author:
 * 		ENsoesie 	9/4/2013	Creation
 ***********/
function operator_from_token($oprtoken) {
	global $mysqlprefix;
	$link = connect();
	
	$query = "select operatorid from ${mysqlprefix}chatoperatorsession where oprtoken = '$oprtoken'";
	$row = select_one_row($query, $link);
	
	mysql_close($link);
	
	return $row['operatorid'];
}

/***********
 * Method:	
 *		get_pending_threads
 * Description:
 *	  	This is just like print_pending_threads from operator/update.php, 
 * 		except that the output is an array, with mobile friendly data
 * Author:
 * 		ENsoesie 	9/4/2013	Creation
 ***********/
function get_pending_threads()
{
	global $webim_encoding, $settings, $state_closed, $state_left, $mysqlprefix;
	$link = connect();

	$output = array();
	$query = "select threadid, userName, agentName, unix_timestamp(dtmcreated), userTyping, " .
			 "unix_timestamp(dtmmodified), lrevision, istate, remote, nextagent, agentId, userid, shownmessageid, userAgent, (select vclocalname from ${mysqlprefix}chatgroup where ${mysqlprefix}chatgroup.groupid = ${mysqlprefix}chatthread.groupid) as groupname " .
			 "from ${mysqlprefix}chatthread where istate <> $state_closed AND istate <> $state_left " . 
			 "ORDER BY threadid";
	$rows = select_multi_assoc($query, $link);
	
	$output['threadCount'] = count($rows);
	if (count($rows) > 0) {
		$threadList = array();
		foreach ($rows as $row) {
			$thread = thread_to_array($row, $link);
			$threadList[] = $thread;
		}
		$output['threadList'] = $threadList;
	}
	mysql_close($link);

	//foreach ($output as $thr) {
		//print myiconv($webim_encoding, "utf-8", $thr);
	//}
	
	return $output;
}

function thread_to_array($thread, $link)
{
	global $state_chatting, $threadstate_to_string, $threadstate_key,
$webim_encoding, $operator, $settings,
$can_viewthreads, $can_takeover, $mysqlprefix;
	$state = $threadstate_to_string[$thread['istate']];

	
	$result = array();
	$result['threadid'] = $thread['threadid'];
	
	if ($state == "closed") {
		$result['state'] = $thread['istate'];
		return $result;
	}

	$state = getstring($threadstate_key[$thread['istate']]);
	$nextagent = $thread['nextagent'] != 0 ? operator_by_id_($thread['nextagent'], $link) : null;
	$threadoperator = $nextagent ? get_operator_name($nextagent)
			: ($thread['agentName'] ? $thread['agentName'] : "-");

	if ($threadoperator == "-" && $thread['groupname']) {
		$threadoperator = "- " . $thread['groupname'] . " -";
	}

	if (!($thread['istate'] == $state_chatting && $thread['agentId'] != $operator['operatorid'] && !is_capable($can_takeover, $operator))) {
		$result['canopen'] = "true";
	}
	if ($thread['agentId'] != $operator['operatorid'] && $thread['nextagent'] != $operator['operatorid']
		&& is_capable($can_viewthreads, $operator)) {
		$result['canview'] = "true";
	}
	if ($settings['enableban'] == "1") {
		$result['canban'] = "true";
	}

	$banForThread = $settings['enableban'] == "1" ? ban_for_addr_($thread['remote'], $link) : false;
	if ($banForThread) {
		$result['ban'] = "blocked";
		$result['banid'] = $banForThread['banid'];
	}

	$result['state'] = $thread['istate'];
	$result['typing'] = $thread['userTyping'];
	
	if ($banForThread) {
		$name = htmlspecialchars(getstring('chat.client.spam.prefix'));
	}
	$name .= htmlspecialchars(htmlspecialchars(get_user_name($thread['userName'], $thread['remote'], $thread['userid'])));
	
	$result['name'] = $name;
	
//	$result['addr'] = htmlspecialchars(get_user_addr($thread['remote']));
	$result['agent'] = htmlspecialchars(htmlspecialchars($threadoperator));
	$result['time'] = $thread['unix_timestamp(dtmcreated)'] . "000";
	$result['modified'] = $thread['unix_timestamp(dtmmodified)'] . "000";

	if ($banForThread) {
		$result['reason'] = $banForThread['comment'];
	}

	$userAgent = get_useragent_version($thread['userAgent']);
	$result['useragent'] = $userAgent;
	if ($thread["shownmessageid"] != 0) {
		$query = "select tmessage from ${mysqlprefix}chatmessage where messageid = " . $thread["shownmessageid"];
		
		// I can't explain why using $link causes select_one_row to fail
		$newlink = connect();
		$line = select_one_row($query, $newlink);
		mysql_close($newlink);

		if ($line) {
			$message = preg_replace("/[\r\n\t]+/", " ", $line["tmessage"]);
			$result['message'] = htmlspecialchars(htmlspecialchars($message));
		}
	}

	return $result;
}


/***********
 * Method:	
 *		start_chat
 * Description:
 *	  	Start a chat with a visitor. This constitutes a new chat, continuing the chat, 
 * 		or taking over the chat.
 * Author:
 * 		ENsoesie 	9/4/2013	Creation
 ***********/
function start_chat($oprtoken, $threadid) {
	$operatorId = operator_from_token($oprtoken);
	if ($operatorId == NULL) {
		return array('errorCode' => 2,
					 'errorMsg' => 'invalid operator token');
	}

	$operator = operator_by_id($operatorId);
	$chattoken = $_GET['token'];

	$thread = thread_by_id($threadid);
	if (!$thread || !isset($thread['ltoken'])) {
		return array('errorCode' => 3,
					 'errorMsg' => 'invalid thread');
	}

	// If token is not set, this is a new chat session for this operator
	if (!isset($chattoken)) {
		$viewonly = filter_var($_GET['viewonly'], FILTER_VALIDATE_BOOLEAN);
		$forcetake = filter_var($_GET['force'], FILTER_VALIDATE_BOOLEAN);

		if (!$viewonly && $thread['istate'] == $state_chatting && 
			$operator['operatorid'] != $thread['agentId']) {
			
			if (!is_capable($can_takeover, $operator)) {
				return array('errorCode' => 4,
							 'errorMsg' => 'cannot take over');
			}
			
			if ($forcetake == false) {
				// Todo. Confirm that you want to force the takeover of the conversation
				// 1 month later and I'm not sure what this should do. This is a potential
				// bug that needs to be reviewed.
				return array('errorCode' => 5,
							 'errorMsg' => 'confirm take over');
			}
		}

		if (!$viewonly) {
			take_thread($thread, $operator);
		} else if (!is_capable($can_viewthreads, $operator)) {
			return array('errorCode' => 6,
						 'errorMsg' => 'cannot view thread');
		}
		
		$chattoken = $thread['ltoken'];
	}

	// Chat token may be different if token was supplied from the http request
	if ($chattoken != $thread['ltoken']) {
		return array('errorCode' => 7,
					 'errorMsg' => 'wrong thread');
	}
	
	if ($thread['agentId'] != $operator['operatorid'] && 
		!is_capable($can_viewthreads, $operator)) {
		return array('errorCode' => 6,
					 'errorMsg' => 'cannot view thread');
	}
	
	$out = array('errorCode' => 0,
				 'errorMsg' => 'success',
				 'threadid' => $threadid,
				 'chattoken' => $chattoken);
	return $out;
}

/***********
 * Method:	
 *		get_new_messages
 * Description:
 *	  	Get messages that have not yet been sync'ed for the current chat session
 * Author:
 * 		ENsoesie 	9/7/2013	Creation
 ***********/
function get_new_messages($oprtoken, $threadid, $chattoken) {
	$operatorId = operator_from_token($oprtoken);
	if ($operatorId == NULL) {
		return array('errorCode' => 2,
					 'errorMsg' => 'invalid operator token');
	}

	$operator = operator_by_id($operatorId);
	$thread = thread_by_id($threadid);
	if (!$thread || !isset($thread['ltoken'])) {
		return array('errorCode' => 3,
					 'errorMsg' => 'invalid thread');
	}
	
	if ($chattoken != $thread['ltoken']) {
		return array('errorCode' => 8,
					 'errorMsg' => 'invalid chat token');
	}
	
	return get_unsynced_messages($threadid);
}

/***********
 * Method:	
 *		get_unsynced_messages
 * Description:
 *	  	Helper method to get messages that have not 
 *		yet been sync'ed for the current chat session
 * Author:
 * 		ENsoesie 	9/7/2013	Creation
 ***********/
function get_unsynced_messages($threadid) {
	$link = connect();

	$query = "select messageid, tmessage, dtmcreated, threadid, agentId, tname, ikind 
			  from ${mysqlprefix}chatmessage 
			  where threadid = $threadid";
	
	$rows = select_multi_assoc($query, $link);

	mysql_close($link);

	$out = array('errorCode' => 0,
				 'errorMsg' => 'success');
				 
	$out['messageCount'] = count($rows);

	// Make sure there is at least one result
	if (count($rows) > 0) {
		$out['messageList'] = $rows;
	}
	
	return $out;
}


/***********
 * Method:	
 *		msg_from_mobile_op
 * Description:
 *	  	Post a message from the mobile operator
 * Author:
 * 		ENsoesie 	9/7/2013	Creation
 ***********/
function msg_from_mobile_op($oprtoken, $threadid, $chattoken, $opMsg) {
	$operatorId = operator_from_token($oprtoken);
	if ($operatorId == NULL) {
		return array('errorCode' => 2,
					 'errorMsg' => 'invalid operator token');
	}

	$operator = operator_by_id($operatorId);
	$thread = thread_by_id($threadid);
	if (!$thread || !isset($thread['ltoken'])) {
		return array('errorCode' => 3,
					 'errorMsg' => 'invalid thread');
	}
	
	if ($chattoken != $thread['ltoken']) {
		return array('errorCode' => 8,
					 'errorMsg' => 'invalid chat token');
	}


	global $kind_agent;
	$from = $thread['agentName'];

	$link = connect();
	$postedid = post_message_($threadid, $kind_agent, $opMsg, $link, $from, null, $operatorId);
	mysql_close($link);
	
	return array('errorCode' => 0,
			     'errorMsg' => 'success');
}

/***********
 * Method:	
 *		invalid_command
 * Description:
 *	  	Returns an invalid command error message
 * Author:
 * 		ENsoesie 	10/19/2013	Creation
 ***********/
 function invalid_command() {
	 return array('errorCode' => 9,
	 			  'errorMsg' => 'Invalid command');
 }
	 
?>
