<?php
/*
 * @author tifan
 */
include('include/simple_html_dom.php'); 
/* Credit: */
$oAuthEntryPage = isset($_POST['g']) ? $_POST['g'] : urldecode($_GET['g']);
$twitterAccount = isset($_POST['u']) ? $_POST['u'] : base64_decode($_GET['u']);
$twitterPassword = isset($_POST['p']) ? $_POST['p'] : base64_decode($_GET['p']);

/* form: https://twitter.com/oauth/authenticate
  authenticity_token -> 抓
	oauth_token -> 抓
	session[username_or_email] -> twitterAccount
	session[password] -> twitterPassword
*/
/* After this page, we should validate the returning page.
	Statud 403 -> No longer valid / wrong password.
	Status 200 ->
		if (contain_Allow) {
			post_allow;
			get_oauth_strings;
			post_oauth_strings_to_oauth.php;
		} else {
			get_oauth_strings;
			...
		}
*/
$page_auth = file_get_html($oAuthEntryPage);
if($page_auth === FALSE){
    echo "Cannot load http resource using file_get_contents";
    exit();
}
$oauth_token = $page_auth->find('input[name=oauth_token]', 0)->attr['value'];
$authenticity_token = $page_auth->find('input[name=authenticity_token]', 0)->attr['value'];
$login_fields = Array(
    'oauth_token' => urlencode($oauth_token),
    'authenticity_token' => urlencode($authenticity_token),
    'session[username_or_email]' => urlencode($twitterAccount),
    'session[password]' => urlencode($twitterPassword)
);
foreach($login_fields as $key=>$value) {
  $login_string .= $key.'='.$value.'&';
}
$ckfile = tempnam ("/tmp", "CURLCOOKIE");
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://api.twitter.com/oauth/authorize');
curl_setopt($ch, CURLOPT_COOKIEJAR, $ckfile);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_POST, count($login_fields));
curl_setopt($ch, CURLOPT_POSTFIELDS, $login_string);
$login_result = curl_exec($ch);
curl_close($ch);
$login_obj = str_get_html($login_result);
$login_error = $login_obj->find('div[class=error notice] p', 0)->innertext;
if(strlen($login_error) > 8) {
  /* This is a workaround coz oauth_errors can be "&nbsp;" */
  echo "There must be something wrong with your user account and password combination.<br/>";
  echo "Twitter said: <b>$login_error</b>\n";
  die(-1);
}
/*
// Now, see if we have to manually approve this request.
$oauth_approve_form = $login_obj->find('form[id=login_form]', 0);
if($oauth_approve_form) {
  echo "We have to manually approve this request.\r";
  $newAToken = $login_obj->find('input[name=authenticity_token]', 0)->attr['value'];
  $newOToken = $login_obj->find('input[name=oauth_token]', 0)->attr['value'];
  echo ">>>New Request<<<\r";
  echo "oauth_token->$oauth_token\rauthenticity_token->$authenticity_token\r";
  $oauth_fields = Array(
    'oauth_token' => urlencode($newOToken),
    'authenticity_token' => urlencode($newAToken),
    'submit' => 'Allow'
  );
  foreach($oauth_fields as $key=>$value) {
    $oauth_strings .= $key.'='.$value.'&';
  }
  //echo $login_result;
  
  $ch = curl_init();
  curl_setopt($ch, CURLOPT_URL, 'https://twitter.com/oauth/authorize');
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($ch, CURLOPT_COOKIEJAR, $ckfile);
  curl_setopt($ch, CURLOPT_POST, count($oauth_fields));
  curl_setopt($ch, CURLOPT_POSTFIELDS, $oauth_strings);
  $oauthResult = curl_exec($ch);
  curl_close($ch);
  echo $oauthResult;
  $oauthObject = str_get_html($oauthResult);
  $targetURL = $oauthObject->find('div[class=message-content] a', 0)->href;
  echo "Click <a href='$targetURL'>here</a> to continue.";

} else {
  $targetURL = $login_obj->find('div[class=message-content] a', 0)->href;
  echo "Click <a href='$targetURL'>here</a> to continue.";
}
*/
  $targetURL = $login_obj->find('div[class=happy notice callback] a', 0)->href;
  header('HTTP/1.1 302 Found');
  header('Status: 302 Found');
  header("Location: $targetURL");
  echo "Please click <a href='$targetURL'>here</a> to continue.";
?>
