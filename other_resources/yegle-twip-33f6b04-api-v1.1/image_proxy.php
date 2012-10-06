<?php

function imageUpload($oauth_key, $oauth_secret, $token) {
    if(empty($_FILES['media'])) header('HTTP/1.0 400 Bad Request');
    $image = $_FILES['media']['tmp_name'];
    $postdata = array
    (
        'message' => empty($_POST['message']) ? '' : $_POST['message'],
        'media' => "@$image", 
    );
    $signingurl = 'https://api.twitter.com/1.1/account/verify_credentials.json';
    $consumer = new OAuthConsumer($oauth_key, $oauth_secret);
    $token = new OAuthConsumer($token['oauth_token'], $token['oauth_token_secret']);
    $sha1_method = new OAuthSignatureMethod_HMAC_SHA1();
    $request = OAuthRequest::from_consumer_and_token($consumer, $token, 'GET', $signingurl, array());
    $request->sign_request($sha1_method, $consumer, $token);
    // header
    $header = $request->to_header("http://api.twitter.com/");

    /**** request method ****/ 
    $url = 'http://img.ly/api/2/upload.json';
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postdata);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('X-Auth-Service-Provider: '.$signingurl,'X-Verify-Credentials-'.$header)); 
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_TIMEOUT, 60);

    $response = curl_exec($ch);
    $response_info=curl_getinfo($ch);
    curl_close($ch);

    if ($response_info['http_code'] == 200) {
        if(preg_match('/^Twitter\/[^ ]+ CFNetwork\/[^ ]+ Darwin\/[^ ]+$/',$_SERVER['HTTP_USER_AGENT'])){
            $data = json_decode($response);
            return empty($data) ? '' : '<mediaurl>'.$data->{'url'}.'</mediaurl>';
        }else{
            header('Content-Type: application/json');
            return $response;
        }
    } else {
        return 'error '.$response_info['http_code'];
    }
}

?>
